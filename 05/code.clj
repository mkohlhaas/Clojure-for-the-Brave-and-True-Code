(ns code
  (:require [clojure.string :as str]))

(+ 1 2) ; 3

(defn wisdom
  [words]
  (str words ", Daniel-san"))

(wisdom "Always bathe on Fridays") ; "Always bathe on Fridays, Daniel-san"

(defn year-end-evaluation
  []
  (if (> (rand) 0.5)
    "You get a raise!"
    "Better luck next year!"))

(year-end-evaluation) ; "You get a raise!"

(defn analysis
  [text]
  (str "Character count: " (count text)))

(analysis "This is just a dummy text.") ; "Character count: 26"

(defn analyze-file
  [filename]
  (analysis (slurp filename)))

(analyze-file "code.clj")  ; "Character count: 2713"

(def great-baby-name "Rosanthony")

great-baby-name ; "Rosanthony"

(let [great-baby-name "Bloodthunder"]
  great-baby-name)  ; "Bloodthunder"

great-baby-name ; "Rosanthony"

(defn sum
  ([vals] (sum vals 0))
  ([vals accumulating-total]
   (if (empty? vals)
     accumulating-total
     (sum (rest vals) (+ (first vals) accumulating-total)))))

;; how it runs
(sum [39 5 1])   ; 45
(sum [39 5 1] 0) ; 45
(sum [5 1] 39)   ; 45
(sum [1] 44)     ; 45
(sum [] 45)      ; 45

;; using recur (s/sum/recur/)
(defn sum1
  ([vals] (sum1 vals 0))
  ([vals accumulating-total]
   (if (empty? vals)
     accumulating-total
     (recur (rest vals) (+ (first vals) accumulating-total)))))

(sum1 [39 5 1]) ; 45

(defn clean
  [text]
  (str/replace (str/trim text) #"lol" "LOL"))

(clean "My boa constrictor is so sassy lol!  ") ; "My boa constrictor is so sassy LOL!"

;; composing functions (right to left)
((comp inc *) 2 3) ; 7
;; 2 * 3 = 6
;; 6 + 1 = 7

(def character
  {:name "Smooches McCutes"
   :attributes {:intelligence 10
                :strength     4
                :dexterity    5}})

(def c-intelligence (comp :intelligence :attributes))
(def c-strength     (comp :strength     :attributes))
(def c-dexterity    (comp :dexterity    :attributes))

(c-intelligence character) ; 10
(c-strength     character) ; 4
(c-dexterity    character) ; 5

;; the same
(fn [c] (:strength (:attributes c)))

(defn spell-slots
  [char]
  (int (inc (/ (c-intelligence char) 2))))

(spell-slots character) ; 6

;; the same (with `comp`)
(def spell-slots-comp (comp int inc #(/ % 2) c-intelligence))

;; composition of two functions
(defn two-comp
  [f g]
  (fn [& args]
    (f (apply g args))))

;; (+ 3 (+ 5 8))
;; (+ 3 13)
;; 16

(defn slow-identity
  "Returns the given value after 1 second"
  [x]
  (Thread/sleep 1000)
  x)

(slow-identity "Mr. Fantastico") ; "Mr. Fantastico"
(slow-identity "Mr. Fantastico") ; "Mr. Fantastico"

;; remember calculated values
(def memo-sleepy-identity (memoize slow-identity))

(memo-sleepy-identity "Mr. Fantastico") ; "Mr. Fantastico"
(memo-sleepy-identity "Mr. Fantastico") ; "Mr. Fantastico"
