# hubble

A thin wrapper around Darkstar and Batik to allow easy viewing of Vega
and Vega-lite graphs from the Clojure REPL.

## Installation

We have not yet released to Clojars, so we recommended you use deps.edn:

``` clojure
applied-science/hubble {:git/url "https://github.com/vladkotu/hubble"
                        :sha "a541bda15464cc2b2f77e56863a172828b0d7808"}
```

## Usage

```clojure
(ns test
  (:require [applied-science.hubble :as ah]))

(def test-data (map hash-map
                      (repeat :a)
                      (range 1 1000)
                      (repeat :b)
                      (repeatedly #(+ 25 (* 50 (Math/random))))))
;; Plot vega lite spec
;; will create default frame and redraw every new chart in it
(plot-vega-lite!
 {:data {:values test-data}
  :mark "line"
  :encoding {:x {:field :a :type "ordinal" :axis {"labelAngle" 0}},
             :y {:field :b :type "quantitative"}}})
             
;; Verify svg string of vega lite lib before drawing
(vega-lite-spec->svg
 {:data {:values test-data}
  :mark "line"
  :encoding {:x {:field :a :type "ordinal" :axis {"labelAngle" 0}},
             :y {:field :b :type "quantitative"}}})

;; Plot multiple charts at the same time
(defonce plot1 (make-svg-window {:title "Stacked"}))
(plot-vega! plot1 (slurp (io/resource "stacked.bar.vg.json")))

(def plot2 (make-svg-window {:title "Bar"}))
(plot-vega! plot2 (slurp (io/resource "bar.vg.json")))


```

## TODO

* Add Batik's PNG transcoder support to export PNGs of drawings.

## Development 

Build a deployable jar of this library:

    $ clojure -A:jar

Install it locally:

    $ clojure -A:install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables:

    $ clojure -A:deploy

## License

Copyright © 2020 Jack

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
