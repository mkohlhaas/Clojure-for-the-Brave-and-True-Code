(ns fwpd.core
  (:require [clojure.string :as str]))

;; (def filename "suspects.csv")
(def filename "04/fwpd/suspects.csv")

(def vamp-keys [:name :glitter-index])

(def conversions {:name          identity
                  :glitter-index Integer/parseInt})

(defn convert
  [vamp-key value]
  ((get conversions vamp-key) value))

(convert :name "Edward Cullen") ; "Edward Cullen"
(convert :glitter-index "42")   ; 42

(defn parse
  "Convert a CSV into rows of columns"
  [str]
  (map #(str/split % #",")
       (str/split str #"\n")))

(parse "Edward Cullen,10") ; (["Edward Cullen" "10"])

(parse "Edward Cullen,10
Bella Swan,0
Charlie Swan,0
Jacob Black,3
Carlisle Cullen,6")
; (["Edward Cullen" "10"]
;  ["Bella Swan" "0"]
;  ["Charlie Swan" "0"]
;  ["Jacob Black" "3"]
;  ["Carlisle Cullen" "6"])

(defn mapify
  "Return a seq of maps like {:name \"Edward Cullen\" :glitter-index 10}"
  [rows]
  (map (fn [row]
         (reduce (fn [res-map [vamp-key value]]
                   (assoc res-map vamp-key (convert vamp-key value)))
                 {}
                 (map vector vamp-keys row))); [:name "Edward Cullern", :glitter-index 10]
       rows))

(defn glitter-filter
  [minimum-glitter records]
  (filter #(>= (:glitter-index %) minimum-glitter) records))

;; Examples from FWPD

(slurp filename) ; "Edward Cullen,10\nBella Swan,0\nCharlie Swan,0\nJacob Black,3\nCarlisle Cullen,6"

(parse (slurp filename))
; (["Edward Cullen" "10"]
;  ["Bella Swan" "0"]
;  ["Charlie Swan" "0"]
;  ["Jacob Black" "3"]
;  ["Carlisle Cullen" "6"])

(mapify (parse (slurp filename)))
; ({:name "Edward Cullen", :glitter-index 10}
;  {:name "Bella Swan", :glitter-index 0}
;  {:name "Charlie Swan", :glitter-index 0}
;  {:name "Jacob Black", :glitter-index 3}
;  {:name "Carlisle Cullen", :glitter-index 6})

(glitter-filter 3 (mapify (parse (slurp filename))))
; ({:name "Edward Cullen", :glitter-index 10}
;  {:name "Jacob Black", :glitter-index 3}
;  {:name "Carlisle Cullen", :glitter-index 6})
