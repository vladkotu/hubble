(ns applied-science.hubble
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
   (let [svg-canvas   ^org.apache.batik.swing.JSVGCanvas (canvas inst)
         parser-class  (org.apache.batik.util.XMLResourceDescriptor/getXMLParserClassName)
         doc-factory   (org.apache.batik.anim.dom.SAXSVGDocumentFactory. parser-class)
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

(defn plot-vega!
  ([spec] (plot-vega! (default-inst) spec))
  ([inst spec]
   (show-svg! inst (ds/vega-spec->svg (json/write-value-as-string spec)))
   (keep-on-top! inst)))

(defn plot-vega-lite!
  ([spec] (plot-vega-lite! (default-inst) spec))
  ([inst spec]
   (show-svg! inst (ds/vega-lite-spec->svg (json/write-value-as-string spec)))
   (keep-on-top! inst)))

(defn spit-vega! [fpath spec]
  (spit fpath (ds/vega-spec->svg (json/write-value-as-string spec))))

(defn spit-vega-lite! [fpath spec]
  (spit fpath (ds/vega-lite-spec->svg (json/write-value-as-string spec))))

(comment

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
