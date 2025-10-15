# hubble

quick merge of [applied-sciense/darkstar](https://github.com/applied-science/darkstar) and [applied-sciense/hubble](https://github.com/applied-science/hubble)
projects, allows to render vega and vega-lite charts
both libs updated to 6.x versions

```clojure
(applied-science.darkstar/versions) ;; => {"vega" "6.2.0", "vegaLite" "6.4.1"}
```

## Installation


``` clojure
applied-science/hubble {:git/url "https://github.com/vladkotu/hubble"
                        :sha "f9e03db389c46b594635f1c857bc9ed65405ed1e"}
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


## License

Copyright © 2020 Jack

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
