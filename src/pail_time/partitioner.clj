(ns pail-time.partitioner
  "A vertical partitioner for use with the Joda time library."
  (:require [clj-pail.partitioner :as p]
            [clj-time.format :as f]
            [clojure.string :as string]))


(defrecord ^{:doc "A Pail vertical partitioner for the Joda time library. It partitions data
                  according to a list of time format strings."}
  TimePartitioner
  [formats]

  p/VerticalPartitioner
  (p/make-partition [this date-time]
    (map #(-> %
            (f/formatter)
            (f/unparse date-time))
         formats))

  (p/validate [this dirs]
    (let [time-dirs (take (count formats) dirs)
          rest-dirs (nthnext dirs (count formats))]
      (try
        (f/parse (f/formatter (string/join "/" formats))
                 (string/join "/" time-dirs))
        [true rest-dirs]
        (catch IllegalArgumentException e
          [false rest-dirs])))))


(defn time-partitioner
  "Returns a `TimePartitioner` for the provided format strings. The format strings should be
  compatible with the `org.joda.time.format.DateTimeFormat` class.

    ; defines a partitioner that partitions by day
    (time-partitioner [\"yyyy-MM\" \"dd\"])"
  [formats]
  (TimePartitioner. formats))
