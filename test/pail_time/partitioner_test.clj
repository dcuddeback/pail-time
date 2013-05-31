(ns pail-time.partitioner-test
  (:require [pail-time.partitioner :as p]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-pail.partitioner :as pail])
  (:use midje.sweet))


(facts "TimePartitioner"

  (facts "make-partition"
    (tabular "partitions with clj-time format strings"
      (let [formats ?formats
            the-time (apply t/date-time ?time)]
        (pail/make-partition (p/time-partitioner formats) the-time) => ?expected)

      ?formats            ?time                 ?expected
      ["yyyy-MM-dd"]      [2013 5 31]           ["2013-05-31"]
      ["yyyy" "MM" "dd"]  [2013 5 31]           ["2013" "05" "31"]
      ["HH" "mm-ss"]      [2013 5 31 12 39 58]  ["12" "39-58"]))


  (facts "validate"
    (tabular "checks the validity of a date"
      (pail/validate (p/time-partitioner ?formats) ?dirs) => (just ?result anything)

      ?formats ?dirs ?result
      ; exact matches
      ["yyyy-MM-dd"]      ["2013-04-01"]            true
      ["yyyy-MM-dd"]      ["2013-04-30"]            true
      ["yyyy-MM-dd"]      ["2013-05-31"]            true
      ["yyyy" "MM" "dd"]  ["2013" "05" "31"]        true

      ; with incomplete format strings
      ["yyyy"]            ["2013"]                  true
      ["MM"]              ["05"]                    true
      ["dd"]              ["31"]                    true

      ; with extra dirs
      ["yyyy-MM-dd"]      ["2013-05-31" "extra"]    true
      ["yyyy" "MM" "dd"]  ["2013" "05" "31" "foo"]  true

      ; leap days
      ["yyyy-MM-dd"]      ["2012-02-29"]            true
      ["yyyy-MM-dd"]      ["2013-02-29"]            false
      ["yyyy-MM-dd"]      ["2000-02-29"]            true
      ["yyyy-MM-dd"]      ["2100-02-29"]            false

      ; invalid dates
      ["yyyy-MM-dd"]      ["2013-00-01"]            false
      ["yyyy-MM-dd"]      ["2013-13-01"]            false
      ["yyyy-MM-dd"]      ["2013-05-00"]            false
      ["yyyy-MM-dd"]      ["2013-05-32"]            false
      ["yyyy-MM-dd"]      ["2013-04-31"]            false
      ["yyyy-MM-dd"]      ["2013-00-00"]            false
      ["yyyy-MM-dd"]      ["XXXX-01-01"]            false
      ["yyyy-MM-dd"]      ["2013-XX-01"]            false
      ["yyyy-MM-dd"]      ["2013-01-XX"]            false

      ; format mismatch
      ["yyyy-MM-dd"]      ["2013.05.31"]            false
      ["yyyy.MM.dd"]      ["2013-05-31"]            false
      ["yyyy-MM-dd"]      ["2013" "05" "31"]        false
      ["yyyy" "MM" "dd"]  ["2013-05-31"]            false

      ; too few dirs
      ["yyyy-MM-dd"]      []                        false
      ["yyyy" "MM" "dd"]  []                        false
      ["yyyy" "MM" "dd"]  ["2013" "05"]             false)

    (tabular "returns dirs not used to match format strings"
      (pail/validate (p/time-partitioner ?formats) ?dirs) => (just anything ?extra)

      ?formats        ?dirs                             ?extra
      ["yyyy-MM-dd"]  ["2013-05-31"]                    empty?
      ["yyyy-MM-dd"]  ["2013-05-31" "extra"]            (just "extra")
      ["yyyy-MM-dd"]  ["XXXX-YY-ZZ" "extra"]            (just "extra")
      ["yyyy-MM-dd"]  ["X" "extra" "dirs"]              (just "extra" "dirs")

      ["yyyy" "MM" "dd"]  []                            empty?
      ["yyyy" "MM" "dd"]  ["X"]                         empty?
      ["yyyy" "MM" "dd"]  ["X" "Y"]                     empty?
      ["yyyy" "MM" "dd"]  ["X" "Y" "Z"]                 empty?
      ["yyyy" "MM" "dd"]  ["X" "Y" "Z" "extra"]         (just "extra")
      ["yyyy" "MM" "dd"]  ["X" "Y" "Z" "extra" "dirs"]  (just "extra" "dirs"))))
