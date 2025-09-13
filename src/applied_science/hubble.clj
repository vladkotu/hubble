(ns applied-science.hubble
  "
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
  :mark \"line\"
  :encoding {:x {:field :a :type \"ordinal\" :axis {\"labelAngle\" 0}},
             :y {:field :b :type \"quantitative\"}}})
             
;; Verify svg string of vega lite lib before drawing
(vega-lite-spec->svg
 {:data {:values test-data}
  :mark \"line\"
  :encoding {:x {:field :a :type \"ordinal\" :axis {\"labelAngle\" 0}},
             :y {:field :b :type \"quantitative\"}}})

;; Plot multiple charts at the same time
(defonce plot1 (make-svg-window {:title \"Stacked\"}))
(plot-vega! plot1 (slurp (io/resource \"stacked.bar.vg.json\")))

(def plot2 (make-svg-window {:title \"Bar\"}))
(plot-vega! plot2 (slurp (io/resource \"bar.vg.json\")))
  "

  (:require [seesaw.core :as see]
            [jsonista.core :as json]
            [applied-science.darkstar :as ds]
            [clojure.java.io :as io]))

(set! *warn-on-reflection* true)

(defn make-canvas [] (org.apache.batik.swing.JSVGCanvas.))

(defprotocol ISvgWindow
  (id [_])
  (frame [_])
  (canvas [_]))

(defrecord SvgWindow [frame canvas]
  ISvgWindow
  (frame [_] frame)
  (canvas [_] canvas)
  (id [_] id))

(def frame-defaults {:title "svg" :id (str (java.util.UUID/randomUUID))})
(defn make-svg-window
  ([] (make-svg-window frame-defaults))
  ([{:as opts}]
   (let [opts (merge frame-defaults opts)
         canvas (make-canvas)
         frame (->> (assoc opts :content (see/scrollable canvas))
                    (apply concat)
                    (apply see/frame)
                    (see/pack!))]
     (map->SvgWindow {:id (:id opts)
                      :frame frame
                      :canvas canvas}))))

(def default-svg-window-inst nil)

(defn default-inst
  []
  (when-not default-svg-window-inst
    (let [inst (make-svg-window {:id :default :title "SVG"})]
      (alter-var-root #'default-svg-window-inst (constantly inst))))
  default-svg-window-inst)

(defn show-svg!
  ([svg-str] (show-svg! (default-inst) svg-str))
  ([inst svg-str]
   (let [svg-canvas ^org.apache.batik.swing.JSVGCanvas (canvas inst)
         parser-class (org.apache.batik.util.XMLResourceDescriptor/getXMLParserClassName)
         doc-factory (org.apache.batik.anim.dom.SAXSVGDocumentFactory. parser-class)
         string-reader (java.io.StringReader. svg-str)
         svg-document (.createDocument doc-factory "" string-reader)]
     (.setSVGDocument svg-canvas svg-document)
     (see/show! (frame inst)))))

(defn keep-on-top!
  ([] (keep-on-top! (default-inst)))
  ([inst]
   (doto ^javax.swing.JFrame (frame inst)
     (.setVisible true)
     (.toFront)
     (.repaint)
     (.setAlwaysOnTop true))))

(defn vega-spec->svg
  "Converts a Vega specification (Clojure map) to an SVG string.
   
   Args:
     spec - A Clojure map representing a Vega visualization specification
   
   Returns:
     SVG string representation of the visualization
   
   Example:
     (vega-spec->svg {:$schema \"https://vega.github.io/schema/vega/v5.json\" 
                      :width 400 :height 200 ...})"
  [spec]
  (ds/vega-spec->svg (json/write-value-as-string spec)))
(defn vega-lite-spec->svg
  "Converts a Vega-Lite specification (Clojure map) to an SVG string.
   
   Args:
     spec - A Clojure map representing a Vega-Lite visualization specification
   
   Returns:
     SVG string representation of the visualization
   
   Example:
     (vega-lite-spec->svg {:$schema \"https://vega.github.io/schema/vega-lite/v5.json\" 
                           :mark \"bar\" ...})"
  [spec]
  (ds/vega-lite-spec->svg (json/write-value-as-string spec)))

(defn plot-vega!
  "Displays a Vega visualization in a Swing window.
   Creates or reuses a default window instance and keeps it on top.
   
   Args:
     spec - Vega specification map
     inst - (optional) window instance to use, defaults to (default-inst)
   
   Returns:
     The window instance displaying the visualization
   
   Side-effects:
     Opens/updates a Swing window and keeps it on top
   
   Example:
     (plot-vega! {:$schema \"https://vega.github.io/schema/vega/v5.json\" 
                  :width 400 :height 200 
                  :data [{:name \"table\" :values [...]}]
                  :marks [...]})"
  ([spec] (plot-vega! (default-inst) spec))
  ([inst spec]
   (show-svg! inst (vega-spec->svg spec))
   (keep-on-top! inst)))

(defn plot-vega-lite!
  "Displays a Vega-Lite visualization in a Swing window.
   Creates or reuses a default window instance and keeps it on top.
   
   Args:
     spec - Vega-Lite specification map  
     inst - (optional) window instance to use, defaults to (default-inst)
   
   Returns:
     The window instance displaying the visualization
   
   Side-effects:
     Opens/updates a Swing window and keeps it on top
   
   Example:
     (plot-vega-lite! {:$schema \"https://vega.github.io/schema/vega-lite/v5.json\" 
                       :mark \"bar\"
                       :data {:values [{:a \"A\" :b 28} {:a \"B\" :b 55}]}
                       :encoding {:x {:field \"a\" :type \"ordinal\"}
                                  :y {:field \"b\" :type \"quantitative\"}}})"
  ([spec] (plot-vega-lite! (default-inst) spec))
  ([inst spec]
   (show-svg! inst (vega-lite-spec->svg spec))
   (keep-on-top! inst)))

(defn spit-vega!
  "Saves a Vega visualization as an SVG file to disk.
   
   Args:
     fpath - File path where to save the SVG
     spec - Vega specification map
   
   Returns:
     nil
   
   Side-effects:
     Writes an SVG file to the specified path
   
   Example:
     (spit-vega! \"charts/my-chart.svg\" 
                 {:$schema \"https://vega.github.io/schema/vega/v5.json\" 
                  :width 400 :height 200 ...})"
  [fpath spec]
  (spit fpath (vega-spec->svg spec)))

(defn spit-vega-lite!
  "Saves a Vega-Lite visualization as an SVG file to disk.
   
   Args:
     fpath - File path where to save the SVG
     spec - Vega-Lite specification map
   
   Returns:
     nil
   
   Side-effects:
     Writes an SVG file to the specified path
   
   Example:
     (spit-vega-lite! \"charts/my-chart.svg\" 
                      {:$schema \"https://vega.github.io/schema/vega-lite/v5.json\" 
                       :mark \"bar\"
                       :data {:values [{:a \"A\" :b 28}]}
                       :encoding {:x {:field \"a\" :type \"ordinal\"}
                                  :y {:field \"b\" :type \"quantitative\"}}})"
  [fpath spec]
  (spit fpath (vega-lite-spec->svg spec)))

(comment

  (show-svg! (ds/vega-spec->svg (slurp (io/resource "bar.vg.json"))))

  (def plot1 (make-svg-window {:title "Stacked"}))
  (show-svg! plot1 (ds/vega-spec->svg (slurp (io/resource "stacked.bar.vg.json"))))
  (keep-on-top! plot1)

  (def plot2 (make-svg-window {:title "Stacked"}))
  (vega-lite-spec->svg
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

  :end)

;;(.requestToggleFullScreen (com.apple.eawt.Application/getApplication) @the-frame)

;; TODO add PNG decoder option
;;org.apache.batik.transcoder.image.PNGTranscoder
;; TranscoderInput input = new TranscoderInput(document);
;;        OutputStream ostream = new FileOutputStream("out.jpg");
;;        TranscoderOutput output = new TranscoderOutput(ostream);

;;        // Perform the transcoding.
;;        t.transcode(input, output);
;;        ostream.flush();
;;        ostream.close();

