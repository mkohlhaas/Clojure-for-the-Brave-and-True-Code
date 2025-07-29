(ns the-divine-cheese-code.visualization.svg
  (:require [clojure.string :as str]
            [clojure.core   :as core])
  (:refer-clojure :exclude [min max]))

(comment
  "comparator-over-locations"
  (def locations [{:lat 1 :lng 3} {:lat 5 :lng 0}])
  (def key :lat)
  (def comparison-fn core/max)
  (def keys [:lat :lng])

  (zipmap keys
          (map (fn [key] (apply comparison-fn (map key locations)))
               keys))) ; {:lat 5, :lng 3}

(defn comparator-over-locations
  [comparison-fn keys]
  (fn [locations]
    (zipmap keys
            (map (fn [k] (apply comparison-fn (map k locations)))
                 keys)))) ; => {:lat 5, :lng 3}

(def min (comparator-over-locations core/min [:lat :lng]))
(def max (comparator-over-locations core/max [:lat :lng]))

(comment
  "min/max"
  (def locations1 [{:lat 1 :lng 3} {:lat 5 :lng 0}])
  (def locations2 [{:location "Cologne, Germany"
                    :cheese-name "Archbishop Hildebold's Cheese Pretzel"
                    :lat 50.95
                    :lng 6.97}
                   {:location "Zurich, Switzerland"
                    :cheese-name "The Standard Emmental"
                    :lat 47.37
                    :lng 8.55}
                   {:location "Marseille, France"
                    :cheese-name "Le Fromage de Cosquer"
                    :lat 43.30
                    :lng 5.37}
                   {:location "Zurich, Switzerland"
                    :cheese-name "The Lesser Emmental"
                    :lat 47.37
                    :lng 8.55}
                   {:location "Vatican City"
                    :cheese-name "The Cheese of Turin"
                    :lat 41.90
                    :lng 12.45}])

  (min locations1) ; {:lat 1, :lng 0}
  (min locations2) ; {:lat 41.9, :lng 5.37}

  (max locations1)  ; {:lat 5, :lng 3}
  (max locations2)) ; {:lat 50.95, :lng 12.45}

(comment
  "translate-to-00"
  (def locations [{:lat 1 :lng 3} {:lat 5 :lng 2}]) ; [{:lat 1, :lng 3} {:lat 5, :lng 2}]
  (def mincoords (min locations))                   ;  {:lat 1, :lng 2}

  (map #(merge-with - % mincoords) locations))      ; ({:lat 0, :lng 1} {:lat 4, :lng 0}))

(defn translate-to-00
  [locations]
  (let [mincoords (min locations)]
    (map #(merge-with - % mincoords) locations)))

(comment
  "scale"
  (def locations [{:lat 1 :lng 3} {:lat 5 :lng 2}])
  (def width 5)
  (def height 10)
  (def maxcoords (max locations))                ; {:lat 5, :lng 3}
  (def ratio {:lat (/ height (:lat maxcoords))   ; {:lat 2, :lng 5/3}
              :lng (/ width  (:lng maxcoords))})
  (map #(merge-with * % ratio) locations))       ; ({:lat 2, :lng 5N} {:lat 10, :lng 10/3})

(defn scale
  [width height locations]
  (let [maxcoords (max locations)
        ratio {:lat (/ height (:lat maxcoords))
               :lng (/ width (:lng maxcoords))}]
    (map #(merge-with * % ratio) locations)))

(comment
  "latlng->point"
  (def latlng {:lat 1 :lng 3})
  (str (:lat latlng) "," (:lng latlng))) ; "1,3"

(defn latlng->point
  "Convert lat/lng map to comma-separated string"
  [latlng]
  (str (:lat latlng) "," (:lng latlng)))

(comment
  "points"
  (def locations [{:lat 1 :lng 3} {:lat 5 :lng 2}])
  (str/join " " (map latlng->point locations))) ; "1,3 5,2"

(defn points
  [locations]
  (str/join " " (map latlng->point locations)))

(comment
  "line"
  (def points "1,3 5,2")
  (str "<polyline points=\"" points "\" />")) ; "<polyline points=\"1,3 5,2\" />"

(defn line
  [points]
  (str "<polyline points=\"" points "\" />"))

(comment
  "transform"
  (def locations [{:lat 1 :lng 3} {:lat 5 :lng 2}])
  (def width 5)
  (def height 10)

  (->> locations
       translate-to-00
       (scale width height))) ; ({:lat 0N, :lng 5} {:lat 10N, :lng 0})

(defn transform
  "Just chains other functions"
  [width height locations]
  (->> locations
       translate-to-00
       (scale width height)))

(comment
  "xml"
  (def locations [{:lat 1 :lng 3} {:lat 5 :lng 2}])
  (def width 5)
  (def height 10)

  (str "<svg height=\"" height "\" width=\"" width "\">"
        ;; These two <g> tags change the coordinate system so that
        ;; 0,0 is in the lower-left corner, instead of SVG's default
        ;; upper-left corner
       "<g transform=\"translate(0," height ")\">"
       "<g transform=\"rotate(-90)\">"
       (-> (transform width height locations)
           points
           line)
       "</g></g>"
       "</svg>")) ; "<svg height=\"10\" width=\"5\"><g transform=\"translate(0,10)\"><g transform=\"rotate(-90)\"><polyline points=\"0,5 10,0\" /></g></g></svg>"

(defn xml
  "svg 'template', which also flips the coordinate system"
  [width height locations]
  (str "<svg height=\"" height "\" width=\"" width "\">"
       ;; These two <g> tags change the coordinate system so that
       ;; 0,0 is in the lower-left corner, instead of SVG's default
       ;; upper-left corner
       "<g transform=\"translate(0," height ")\">"
       "<g transform=\"rotate(-90)\">"
       (->> (transform width height locations)
            points
            line)
       "</g></g>"
       "</svg>"))
