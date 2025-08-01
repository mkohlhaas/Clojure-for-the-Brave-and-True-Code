;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Creating and Extending Abstractions with Multimethods, Protocols, and Records
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns code)

;; ;;;;;;;;;;;;
;; Polymorphism
;; ;;;;;;;;;;;;

;; ;;;;;;;;;;;;
;; Multimethods
;; ;;;;;;;;;;;;

(defmulti full-moon-behavior (fn [were-creature] (:were-type were-creature)))

(defmethod full-moon-behavior :wolf
  [were-creature]
  (str (:name were-creature) " will howl and murder"))

(defmethod full-moon-behavior :simmons
  [were-creature]
  (str (:name were-creature) " will encourage people and sweat to the oldies"))

(full-moon-behavior {:were-type :wolf
                     :name "Rachel from next door"})
; "Rachel from next door will howl and murder"

(full-moon-behavior {:name "Andy the baker"
                     :were-type :simmons})
; "Andy the baker will encourage people and sweat to the oldies"

(defmethod full-moon-behavior nil
  [were-creature]
  (str (:name were-creature) " will stay at home and eat ice cream"))

(full-moon-behavior {:were-type nil
                     :name "Martin the nurse"})
; "Martin the nurse will stay at home and eat ice cream"

(defmethod full-moon-behavior :default
  [were-creature]
  (str (:name were-creature) " will stay up all night fantasy footballing"))

(full-moon-behavior {:were-type :office-worker
                     :name "Jimmy from sales"})
; "Jimmy from sales will stay up all night fantasy footballing"

(comment
  ; "extending defmulti"
  (ns random-namespace
    (:require [code]))

  (defmethod code/full-moon-behavior :bill-murray
    [were-creature]
    (str (:name were-creature) " will be the most likeable celebrity"))

  ; "Laura the intern will be the most likeable celebrity"
  (code/full-moon-behavior {:name "Laura the intern"}
                           :were-type :bill-murray))

; multiple parameters
(defmulti  types (fn [x y] [(class x) (class y)]))
(defmethod types [java.lang.String java.lang.String]
  [_x _y]
  "Two strings!")

(types "String 1" "String 2")
; "Two strings!"

;; ;;;;;;;;;
;; Protocols
;; ;;;;;;;;;

(defprotocol Psychodynamics
  "Plumb the inner depths of your data types"
  (thoughts [x] "The data type's innermost thoughts")
  (feelings-about [x] [x y] "Feelings about self or other"))

(extend-type java.lang.String
  Psychodynamics
  (thoughts [x] (str x " thinks, 'Truly, the character defines the data type'"))
  (feelings-about
    ([x] (str x " is longing for a simpler way of life"))
    ([x y] (str x " is envious of " y "'s simpler way of life"))))

(thoughts "blorb")
; "blorb thinks, 'Truly, the character defines the data type'"

(feelings-about "schmorb")
; "schmorb is longing for a simpler way of life"

(feelings-about "schmorb" 2)
; "schmorb is envious of 2's simpler way of life"

; adding default type (= java.lang.Object)
(extend-type java.lang.Object
  Psychodynamics
  (thoughts [_x] "Maybe the Internet is just a vector for toxoplasmosis")
  (feelings-about
    ([_x] "meh")
    ([_x y] (str "meh about " y))))

(thoughts 3)
; "Maybe the Internet is just a vector for toxoplasmosis"

(feelings-about 3)
; "meh"

(feelings-about 3 "blorb")
; "meh about blorb"

(extend-protocol Psychodynamics
  java.lang.String
  (thoughts [_x] "Truly, the character defines the data type")
  (feelings-about
    ([_x] "longing for a simpler way of life")
    ([_x y] (str "envious of " y "'s simpler way of life")))

  java.lang.Object
  (thoughts [_x] "Maybe the Internet is just a vector for toxoplasmosis")
  (feelings-about
    ([_x] "meh")
    ([_x y] (str "meh about " y))))

(ns were-records)
(defrecord WereWolf [name title])

(WereWolf. "David" "London Tourist")
; {:name "David", :title "London Tourist"}

(->WereWolf "Jacob" "Lead Shirt Discarder")
; {:name "Jacob", :title "Lead Shirt Discarder"}

(map->WereWolf {:name "Lucian" :title "CEO of Melodrama"})
; {:name "Lucian", :title "CEO of Melodrama"}

(ns monster-mash
  (:import [were_records WereWolf]))

(WereWolf. "David" "London Tourist")
; {:name "David", :title "London Tourist"}

(def jacob (->WereWolf "Jacob" "Lead Shirt Discarder"))
(.name jacob)
; "Jacob"

(:name jacob)
; "Jacob"

(get jacob :name)
; "Jacob"

(= jacob (->WereWolf "Jacob" "Lead Shirt Discarder"))
; true

(= jacob (WereWolf. "David" "London Tourist"))
; false

(= jacob {:name "Jacob" :title "Lead Shirt Discarder"})
; false

(assoc jacob :title "Lead Third Wheel")
; {:name "Jacob", :title "Lead Third Wheel"}

(dissoc jacob :title)
; {:name "Jacob"} <- that's not a were_records.WereWolf

; implement protocol via record
(comment
  (defprotocol WereCreature
    (full-moon-behavior [x]))

  (defrecord WereWolf [name title]
    WereCreature
    (full-moon-behavior [_x] (str name " will howl and murder")))

  ; "Lucian will howl and murder"
  (full-moon-behavior (map->WereWolf {:name "Lucian" :title "CEO of Melodrama"})))
