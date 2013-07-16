# pail-time

[![Build Status](https://travis-ci.org/dcuddeback/pail-time.png?branch=master)](https://travis-ci.org/dcuddeback/pail-time)

Partitioning strategies for using [`clj-time`](https://github.com/clj-time/clj-time) (or [Joda
Time](http://joda-time.sourceforge.net/))  with
[`clj-pail`](https://github.com/dcuddeback/clj-pail).

## Usage

Add `pail-time` to your project's dependencies. If you're using Leiningen, your `project.clj` should
look something like this:

~~~clojure
(defproject ...
  :dependencies [[pail-time VERSION]])
~~~

Where `VERSION` is the latest version on [Clojars](https://clojars.org/pail-time).

### Partitioning by Time

`pail-time` defines a vertical partitioner that can be used to partition data in a `PailStructure`
by the value of a Joda `DateTime` object. The partitioner is defined by providing a list of format
strings:

~~~clojure
(require '[pail-time.partitioner :as p])
(require '[clj-time.core :as time])
(require '[clj-pail.partitioner :as pail])

(def partitioner (p/time-partitioner ["yyyy-MM" "dd"]))

; partitioning an `org.joda.time.DateTime` object:
(pail/vertical-partitioner partitioner (time/now)) ; => ("2013-07" "16")

; valid date
(pail/valid-partition? partitioner ["2013-07" "16"]) ; => true

; valid leap day
(pail/valid-partition? partitioner ["2012-02" "29"]) ; => true

; invalid leap day
(pail/valid-partition? partitioner ["2013-02" "29"]) ; => false
~~~

This example will partition data into a two-level hierarchy. The first level is partitioned by the
year and month. The second level is partitioned by the day of the month. So for data with a
timestamp of July 16, 2013, the data will be partitioned into the directory `2013-07/16`.

Any number of format strings can be specified, and the format strings can be any string that is
considered valid by the `clj-time` library (which also happens to be the format strings that are
valid by the Joda Time library). See the documentation for Joda Time's
[DateTimeFormat](http://joda-time.sourceforge.net/api-release/org/joda/time/format/DateTimeFormat.html)
class for details on specifying format strings.

#### Using the Partitioner

`pail-time` provides generic partitioners that can handle partitioning based on time objects.
However, timestamps are usually just a single field in an application's data schema. You will
usually want to define an application-specific partitioner that composes a time partitioner for
handling the time-based part of your application's partitioning strategy.

## License

Copyright © 2013 David Cuddeback

Distributed under the [MIT License](LICENSE).
