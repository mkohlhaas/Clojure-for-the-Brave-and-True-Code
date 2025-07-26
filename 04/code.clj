(require 'clojure.string)
;; (require 'fwpd.core :as fwpd)

(defn titleize
  [topic]
  (str topic " for the Brave and True"))

(map titleize   ["Hamsters" "Ragnarok"]) ; ("Hamsters for the Brave and True" "Ragnarok for the Brave and True")
(map titleize  '("Hamsters" "Ragnarok")) ; ("Hamsters for the Brave and True" "Ragnarok for the Brave and True")
(map titleize  #{"Hamsters" "Ragnarok"}) ; ("Hamsters for the Brave and True" "Ragnarok for the Brave and True")

(map #(titleize (second %)) {:uncomfortable-thing "Winking"}) ; ("Winking for the Brave and True")

(seq '(1 2 3)) ; (1 2 3)
(seq  [1 2 3]) ; (1 2 3)
(seq #{1 2 3}) ; (1 3 2)

(seq {:name "Bill Compton" :occupation "Dead mopey guy"}) ; ([:name "Bill Compton"] [:occupation "Dead mopey guy"])

;; seq creates a list of vectors from a map
(seq {:a 1 :b 2 :c 3})           ; ([:a 1] [:b 2] [:c 3])
(into {} (seq {:a 1 :b 2 :c 3})) ; {:a 1, :b 2, :c 3}

;; Seq Function Examples

;; map

(map inc [1 2 3]) ; (2 3 4)

(map str ["a" "b" "c"] ["A" "B" "C"])            ; ("aA" "bB" "cC")

;; the same
(list (str "a" "A") (str "b" "B") (str "c" "C")) ; ("aA" "bB" "cC")

(def human-consumption   [8.1 7.3 6.6 5.0])
(def critter-consumption [0.0 0.2 0.3 1.1])

(defn unify-diet-data
  [human critter]
  {:human human
   :critter critter})

(map unify-diet-data human-consumption critter-consumption)
; ({:human 8.1, :critter 0.0}
;  {:human 7.3, :critter 0.2}
;  {:human 6.6, :critter 0.3}
;  {:human 5.0, :critter 1.1})

(def sum #(reduce + %))
(def avg #(/ (sum %) (count %)))

(defn stats
  [numbers]
  (map #(% numbers) [sum count avg]))

(stats [3 4 10])       ; (17 3 17/3)
(stats [80 1 44 13 6]) ; (144 5 144/5)

(def identities
  [{:alias "Batman"       :real "Bruce Wayne"}
   {:alias "Spider-Man"   :real "Peter Parker"}
   {:alias "Santa"        :real "Your mom"}
   {:alias "Easter Bunny" :real "Your dad"}])

(map :real identities) ; ("Bruce Wayne" "Peter Parker" "Your mom" "Your dad")

;; reduce

;; iterating through a map creates a list of 2-element vectors (-> [key value]). 
;; see above the `seq` function
;; In other words: `reduce` treats the argument {:max 30 :min 10} as a sequence of vectors, i.e. ([:max 30] [:min 10]).
(reduce (fn [new-map [key val]]
          (assoc new-map key (inc val)))
        {}
        {:max 30 :min 10}) ; {:max 31, :min 11}

;; assoc adds key value to a map
(assoc {:a 1} :b 2) ; {:a 1, :b 2}

(reduce (fn [new-map [key val]]
          (if (> val 4)
            (assoc new-map key val)
            new-map))
        {}
        {:human 4.1
         :critter 3.9}) ; {:human 4.1}

;; take, drop, take-while, and drop-while

(take 3 [1 2 3 4 5 6 7 8 9 10]) ; (1 2 3)

(drop 3 [1 2 3 4 5 6 7 8 9 10]) ; (4 5 6 7 8 9 10)

(def food-journal
  [{:month 1 :day 1 :human 5.3 :critter 2.3}
   {:month 1 :day 2 :human 5.1 :critter 2.0}
   {:month 2 :day 1 :human 4.9 :critter 2.1}
   {:month 2 :day 2 :human 5.0 :critter 2.5}
   {:month 3 :day 1 :human 4.2 :critter 3.3}
   {:month 3 :day 2 :human 4.0 :critter 3.8}
   {:month 4 :day 1 :human 3.7 :critter 3.9}
   {:month 4 :day 2 :human 3.7 :critter 3.6}])

(take-while #(< (:month %) 3) food-journal)
; ({:month 1, :day 1, :human 5.3, :critter 2.3}
;  {:month 1, :day 2, :human 5.1, :critter 2.0}
;  {:month 2, :day 1, :human 4.9, :critter 2.1}
;  {:month 2, :day 2, :human 5.0, :critter 2.5})

(drop-while #(< (:month %) 3) food-journal)
; ({:month 3, :day 1, :human 4.2, :critter 3.3}
;  {:month 3, :day 2, :human 4.0, :critter 3.8}
;  {:month 4, :day 1, :human 3.7, :critter 3.9}
;  {:month 4, :day 2, :human 3.7, :critter 3.6})

(take-while #(< (:month %) 4)
            (drop-while #(< (:month %) 2) food-journal))
; ({:month 2, :day 1, :human 4.9, :critter 2.1}
;  {:month 2, :day 2, :human 5.0, :critter 2.5}
;  {:month 3, :day 1, :human 4.2, :critter 3.3}
;  {:month 3, :day 2, :human 4.0, :critter 3.8})

;; filter and some

;; filter-in
(filter #(< (:human %) 5) food-journal)
; ({:month 2, :day 1, :human 4.9, :critter 2.1}
;  {:month 3, :day 1, :human 4.2, :critter 3.3}
;  {:month 3, :day 2, :human 4.0, :critter 3.8}
;  {:month 4, :day 1, :human 3.7, :critter 3.9}
;  {:month 4, :day 2, :human 3.7, :critter 3.6})

(filter #(< (:month %) 3) food-journal)
; ({:month 1, :day 1, :human 5.3, :critter 2.3}
;  {:month 1, :day 2, :human 5.1, :critter 2.0}
;  {:month 2, :day 1, :human 4.9, :critter 2.1}
;  {:month 2, :day 2, :human 5.0, :critter 2.5})

(some #(> (:critter %) 5) food-journal) ; nil

(some #(> (:critter %) 3) food-journal) ; true

;; nice trick using `and` to get the actual value
(some      #(> (:critter %) 3)    food-journal) ; true
(some #(and (> (:critter %) 3) %) food-journal) ; {:month 3, :day 1, :human 4.2, :critter 3.3}

;; sort and sort-by

(sort [3 1 2]) ; (1 2 3)

(sort          ["aaa" "c" "bb"]) ; ("aaa" "bb" "c")
(sort-by count ["aaa" "c" "bb"]) ; ("c" "bb" "aaa")

;; concat

(concat [1 2] [3 4]) ; (1 2 3 4)

;; Lazy Seqs

;; Demonstrating Lazy Seq Efficiency

(def vampire-database
  {0 {:makes-blood-puns? false, :has-pulse? true  :name "McFishwich"}
   1 {:makes-blood-puns? false, :has-pulse? true  :name "McMackson"}
   2 {:makes-blood-puns? true,  :has-pulse? false :name "Damon Salvatore"}
   3 {:makes-blood-puns? true,  :has-pulse? true  :name "Mickey Mouse"}})

(defn vampire-related-details
  [social-security-number]
  (Thread/sleep 1000) ; lookup takes 1 sec !!!
  (get vampire-database social-security-number))

(defn vampire?
  [record]
  (and (:makes-blood-puns? record)
       (not (:has-pulse? record))
       record))

(defn identify-vampire
  [social-security-numbers]
  (first (filter vampire?
                 (map vampire-related-details social-security-numbers))))

;; lookup takes about 1 sec
(time (vampire-related-details 0)) ; {:makes-blood-puns? false, :has-pulse? true, :name "McFishwich"}
; (out) "Elapsed time: 1000.498089 msecs"

;; map is lazy; map returns instantly 
(time (def mapped-details (map vampire-related-details (range 0 1000000)))) ; #'user/mapped-details
; (out) "Elapsed time: 0.012373 msecs"

;; Clojure goes ahead and prepares the next 31 - not only the first one
(time (first mapped-details)) ; {:makes-blood-puns? false, :has-pulse? true, :name "McFishwich"}
; (out) "Elapsed time: 32014.146038 msecs"

;; mapping has been done - no time penalty the next time
(time (first mapped-details)) ; {:name "McFishwich", :makes-blood-puns? false, :has-pulse? true}
; (out) "Elapsed time: 0.003045 msecs"

;; we don't have to wait for all 1000000 records to be mapped
(time (identify-vampire (range 0 1000000))) ; {:makes-blood-puns? true, :has-pulse? false, :name "Damon Salvatore"}
; (out) "Elapsed time: 32014.757931 msecs"

;; Infinite Sequences

(concat (take 8 (repeat "na")) ["Batman!"]) ; ("na" "na" "na" "na" "na" "na" "na" "na" "Batman!")

(take 3 (repeatedly (fn [] (rand-int 10)))) ; (2 7 9)

(defn even-numbers
  ([] (even-numbers 0))
  ([n] (cons n (lazy-seq (even-numbers (+ n 2))))))

(take 10 (even-numbers)) ; (0 2 4 6 8 10 12 14 16 18)

(cons 0 '(2 4 6)) ; (0 2 4 6)

;; The Collection Abstraction

(empty? []) ; true

(empty? ["no!"]) ; false

;; into

(map identity {:sunlight-reaction "Glitter!"}) ; ([:sunlight-reaction "Glitter!"])

(into {} (map identity {:sunlight-reaction "Glitter!"})) ; {:sunlight-reaction "Glitter!"}

(map identity [:garlic :sesame-oil :fried-eggs]) ; (:garlic :sesame-oil :fried-eggs)

(into [] (map identity [:garlic :sesame-oil :fried-eggs])) ; [:garlic :sesame-oil :fried-eggs]

(map identity [:garlic-clove :garlic-clove]) ; (:garlic-clove :garlic-clove)

(into #{} (map identity [:garlic-clove :garlic-clove])) ; #{:garlic-clove}

;; the first argument of into doesn’t have to be empty
(into {:favorite-emotion "gloomy"} [[:sunlight-reaction "Glitter!"]]) ; {:favorite-emotion "gloomy", :sunlight-reaction "Glitter!"}

(into ["cherry"] '("pine" "spruce")) ; ["cherry" "pine" "spruce"]

(into {:favorite-animal "kitty"} {:least-favorite-smell       "dog"
                                  :relationship-with-teenager "creepy"})
; {:favorite-animal "kitty",
;  :least-favorite-smell "dog",
;  :relationship-with-teenager "creepy"}

;; conj

;; compare `into` and `conj`
(into [0] [1]) ; [0 1]
(conj [0] [1]) ; [0 [1]]

(conj [0] 1)       ; [0 1]
(conj [0] 1 2 3 4) ; [0 1 2 3 4]

;; we need key-value pair (as vector) to add to maps
(conj {:time "midnight"} [:place "ye olde cemetarium"]) ; {:time "midnight", :place "ye olde cemetarium"}

;; `conj` in terms of `into`
(defn my-conj
  [target & additions]
  (into target additions))

(my-conj [0] 1 2 3) ; [0 1 2 3]

;; Function Functions

;; apply

;; `apply` explodes a seqable data structure so it can be passed to a function that expects a rest parameter.

(max 0 1 2) ; 2

#_{:clj-kondo/ignore [:type-mismatch]}
(max [0 1 2]) ; [0 1 2]

(apply max [0 1 2]) ; 2

;; `into` in terms of `conj`
(defn my-into
  [target additions]
  (apply conj target additions))

(my-into [0] [1 2 3]) ; [0 1 2 3]

;; partial

;; `partial` allows currying

(def add10 (partial + 10))

(add10 3) ; 13
(add10 5) ; 15

(def add11 #(+ 11 %))

(add11 2) ; 13
(add11 4) ; 15

(def add-missing-elements
  (partial conj ["water" "earth" "air"]))

(add-missing-elements "unobtainium" "adamantium") ; ["water" "earth" "air" "unobtainium" "adamantium"]

(defn my-partial
  [partialized-fn & args]
  (fn [& more-args]
    (apply partialized-fn (into args more-args))))

(def add20 (my-partial + 20))

(add20 3) ; 23

(defn lousy-logger
  [log-level message]
  (condp = log-level
    :warn      (clojure.string/lower-case message)
    :emergency (clojure.string/upper-case message)))

;; `warn` and `emergency` take a single argument - the `message`
(def warn      (partial lousy-logger :warn))
(def emergency (partial lousy-logger :emergency))

(warn      "Red light ahead")  ; "red light ahead"
(emergency "Red light ahead")  ; "RED LIGHT AHEAD"

;; complement

(defn identify-humans1
  [social-security-numbers]
  (filter #(not (vampire? %))
          (map vampire-related-details social-security-numbers)))

(def not-vampire? (complement vampire?))

(defn identify-humans2
  [social-security-numbers]
  (filter not-vampire?
          (map vampire-related-details social-security-numbers)))

(defn my-complement
  [pred]
  (fn [& args]
    (not (apply pred args))))

(def my-pos? (complement neg?))

(my-pos?  1) ; true
(my-pos? -1) ; false
