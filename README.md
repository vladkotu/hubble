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

;; pops up a window containing this visualization
(show-svg! (ds/vega-spec->svg (slurp (io/resource "bar.vg.json"))))

  (def plot1 (make-svg-window {:title "Stacked"}))
  (show-svg! plot1 (ds/vega-spec->svg (slurp (io/resource "stacked.bar.vg.json"))))

  (keep-on-top! plot1)

  (def plot2 (make-svg-window {:title "Stacked"}))
  (plot-vega-lite!
   plot2
   {:data {:values (map hash-map
                        (repeat :a)
                        (range 1 1000)
                        (repeat :b)
                        (repeatedly #(+ 25 (* 50 (Math/random)))))}
    :mark "line",
    :width 800
    :height 600
    :encoding {:x {:field :a, :type "ordinal", :axis {"labelAngle" 0}},
               :y {:field :b, :type "quantitative"}}})
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
