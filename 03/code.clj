(require 'clojure.string)

;; valid forms
1                             ; 1
"a string"                    ; "a string"
["a" "vector" "of" "strings"] ; ["a" "vector" "of" "strings"]

;; general form of operations. This won't actually run because
;; operator, operand1, etc aren't bound
;; (operator operand1 operand2 ... operandn)

;; Example operations
(+ 1 2 3) ; 6

(str "It was the panda " "in the library " "with a dust buster")
; => "It was the panda in the library with a dust buster"

;; This is an invalid form. I've commented it out so it doesn't mess
;; things up if you try to evaluate this file.
;; (+

;; General structure of `if`
;; (if boolean-form
;;   then-form
;;   optional-else-form)

;; if examples
(if true
  "By Zeus's hammer!"
  "By Aquaman's trident!") ; "By Zeus's hammer!"

(if false
  "By Zeus's hammer!"
  "By Aquaman's trident!") ; "By Aquaman's trident!"

(when false
  "By Odin's Elbow!") ; nil

(if true
  (do (println "Success!")
      "By Zeus's hammer!")
  (do (println "Failure!")
      "By Aquaman's trident!")) ; "By Zeus's hammer!"

;; More boolean operators
(when true
  (println "Success!")
  "abra cadabra") ; "abra cadabra"

(nil? 1) ; false

(nil? nil) ; true

(when "bears eat beets"
  "bears beets Battlestar Galactica") ; "bears beets Battlestar Galactica"

(if nil
  "This won't be the result because nil is falsey"
  "nil is falsey") ; "nil is falsey"

(= 1 1) ; true

(= nil nil) ; true

(= 1 2) ; false

(or false nil :large_I_mean_venti :why_cant_I_just_say_large) ; :large_I_mean_venti

(or (= 0 1) (= "yes" "no")) ; false

(or nil) ; nil

(and :free_wifi :hot_coffee) ; :hot_coffee

(and :feelin_super_cool nil false) ; nil

;; Naming Values with def
(def failed-protagonist-names
  ["Larry Potter" "Doreen the Explorer" "The Incredible Bulk"])

failed-protagonist-names ; ["Larry Potter" "Doreen the Explorer" "The Incredible Bulk"]

;; This is bad, don't do this
(def severity :mild)
(def error-message1 "OH GOD! IT'S A DISASTER! WE'RE ")
(if (= severity :mild)
  (def error-message1 (str error-message1 "MILDLY INCONVENIENCED!"))
  (def error-message1 (str error-message1 "DOOOOOOOMED!")))

error-message1 ; "OH GOD! IT'S A DISASTER! WE'RE "

;; This is better
(defn error-message2
  [severity]
  (str "OH GOD! IT'S A DISASTER! WE'RE "
       (if (= severity :mild)
         "MILDLY INCONVENIENCED!"
         "DOOOOOOOMED!")))

(error-message2 :mild)  ; "OH GOD! IT'S A DISASTER! WE'RE MILDLY INCONVENIENCED!"

;; Oh look, some numbers
93
1.2
1/5

;; Oooh and some strings
"Lord Voldemort"
"\"He who must not be named\""
"\"Great cow of Moscow!\" - Hermes Conrad"

(def name1 "Chewbacca")
(str "\"Uggllglglglglglglglll\" - " name1) ; "\"Uggllglglglglglglglll\" - Chewbacca"

;; Maps now
{}

{:first-name "Charlie"
 :last-name "McFishwich"} ; {:first-name "Charlie", :last-name "McFishwich"}

{"string-key" +}
; {"string-key"
;  #object[clojure.core$_PLUS_ 0x495c49b "clojure.core$_PLUS_@495c49b"]}

{:name {:first "John" :middle "Jacob" :last "Jingleheimerschmidt"}} ; {:name {:first "John", :middle "Jacob", :last "Jingleheimerschmidt"}}

(hash-map :a 1 :b 2) ; {:b 2, :a 1}

(get {:a 0 :b 1} :b) ; 1

(get {:a 0 :b {:c "ho hum"}} :b) ; {:c "ho hum"}

(get {:a 0 :b 1} :c) ; nil

(get {:a 0 :b 1} :c "unicorns?") ; "unicorns?"

(get-in {:a 0 :b {:c "ho hum"}} [:b :c]) ; "ho hum"

({:name "The Human Coffeepot"} :name) ; {:name "The Human Coffeepot"}

;; goes both ways
({:a 42 :b 18} :a)    ; 42
({:a 42 :b 18} :c 21) ; 21
(:a {:a 42 :b 18})    ; 42
(:c {:a 42 :b 18} 21) ; 21

;; Here we're looking at keywords
:a
:rumplestiltsken
:34
:_?

(:a {:a 1 :b 2 :c 3}) ; 1

(get {:a 1 :b 2 :c 3} :a) ; 1

(:d {:a 1 :b 2 :c 3} "No gnome knows homes like Noah knows") ; "No gnome knows homes like Noah knows"

;; Vector time!
[3 2 1]

(get [3 2 1] 0) ; 3

(get ["a" {:name "Pugsley Winterbottom"} "c"] 1) ; {:name "Pugsley Winterbottom"}

(vector "creepy" "full" "moon") ; ["creepy" "full" "moon"]

(conj [1 2 3] 4) ; [1 2 3 4]

;; List time!
'(1 2 3 4) ; (1 2 3 4)

(nth '(:a :b :c) 0) ; :a

(nth '(:a :b :c) 2) ; :c

;; (nth '(:a :b :c) 3) ; (err) java.lang.IndexOutOfBoundsException 

(list 1 "two" {3 4}) ; (1 "two" {3 4})

;; compare to vector
(conj [1 2 3] 4) ; [1 2 3 4]

(conj '(1 2 3) 4) ; (4 1 2 3)

;; Sets are super cool. Here's some set usage
#{"kurt vonnegut" 20 :icicle}

(hash-set 1 1 2 2)  ; #{1 2}

(conj #{:a :b} :c)  ; #{:c :b :a}

(conj #{:a :b} :b)  ; #{:b :a}

(set [3 3 3 4 4])   ; #{4 3}

(contains? #{:a :b} :a) ; true
(contains? [:a :b]  :a) ; false
(contains? [:a :b]   1) ; true

(contains? #{:a :b} 3) ; false

(contains? #{nil} nil) ; true

(:a #{:a :b})       ; :a
(#{:a :b} :a)       ; :a
(:c #{:a :b} 42)    ; 42
;; (#{:a :b} :c 42) ; no defaults

(get #{:a :b} :a) ; :a

(get #{:a nil} nil) ; nil

(get #{:a :b} "kurt vonnegut") ; nil

;; Just some more example function calls
(+ 1 2 3 4)       ; 10
(* 1 2 3 4)       ; 24
(first [1 2 3 4]) ; 1

;; You can return functions as values
(or + -)  ; #object[clojure.core$_PLUS_ 0x495c49b "clojure.core$_PLUS_@495c49b"]
(and + -) ; #object[clojure.core$_ 0x9cdc4a0 "clojure.core$_@9cdc4a0"]

;; Some neat tricks
((or + -) 1 2 3) ; 6

((and (= 1 1) +) 1 2 3) ; 6

((first [+ 0]) 1 2 3) ; 6

;; These won't work
;; (1 2 3 4)
;; ("test" 1 2 3)

;; Higher-order function examples
(inc 1.1) ; 2.1

(map inc [0 1 2 3]) ; (1 2 3 4)

;; Demonstrating recursive evaluation
(+ (inc 199) (/ 100 (- 7 2))) ; 220

;; Special forms
;; (if boolean-form
;;   then-form
;;   optional-else-form)

;; (if good-mood
;;   (tweet walking-on-sunshine-lyrics)
;;   (tweet mopey-country-song-lyrics))

;; Defining your own function
(defn too-enthusiastic
  "Return a cheer that might be a bit too enthusiastic"
  [name]
  (str "OH. MY. GOD! " name " YOU ARE MOST DEFINITELY LIKE THE BEST "
       "MAN SLASH WOMAN EVER I LOVE YOU AND WE SHOULD RUN AWAY SOMEWHERE"))

(too-enthusiastic "Zelda") ; "OH. MY. GOD! Zelda YOU ARE MOST DEFINITELY LIKE THE BEST MAN SLASH WOMAN EVER I LOVE YOU AND WE SHOULD RUN AWAY SOMEWHERE"

;; Arity examples
(defn no-params
  []
  "I take no parameters!")
(defn one-param
  [x]
  (str "I take one parameter: " x))
(defn two-params
  [x y]
  (str "Two parameters! That's nothing! Pah! I will smoosh them "
       "together to spite you! " x y))

;; Using arity to provide a default value for an argument
(defn x-chop
  "Describe the kind of chop you're inflicting on someone"
  ([name chop-type]
   (str "I " chop-type " chop " name "! Take that!"))
  ([name]
   (x-chop name "karate")))

(x-chop "Kanye West" "slap") ; "I slap chop Kanye West! Take that!"

(x-chop "Kanye East")        ; "I karate chop Kanye East! Take that!"

(defn weird-arity
  ([]
   "Destiny dressed you this morning, my friend, and now Fear is
           trying to pull off your pants. If you give up, if you give in,
           you're gonna end up naked with Fear just standing there laughing
           at your dangling unmentionables! - the Tick")
  ([number]
   (inc number)))

(weird-arity)      ; "Destiny dressed you this morning, my friend, and now Fear is\n           trying to pull off your pants. If you give up, if you give in,\n           you're gonna end up naked with Fear just standing there laughing\n           at your dangling unmentionables! - the Tick"
(weird-arity 1)    ; 2
;; (weird-arity "1")  ; (err) java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Number 

;; Rest parameters
(defn codger-communication
  [whippersnapper]
  (str "Get off my lawn, " whippersnapper "!!!"))

(defn codger
  [& whippersnappers]
  (map codger-communication whippersnappers))

(codger "Billy" "Anne-Marie" "The Incredible Bulk")
; ("Get off my lawn, Billy!!!"
;  "Get off my lawn, Anne-Marie!!!"
;  "Get off my lawn, The Incredible Bulk!!!")

(defn favorite-things
  [name & things]
  (str "Hi, " name ", here are my favorite things: "
       (clojure.string/join ", " things)))

(favorite-things "Doreen" "gum" "shoes" "karate") ; "Hi, Doreen, here are my favorite things: gum, shoes, karate"

;; Destructuring

;; Return the first element of a collection
(defn my-first
  [[first-thing]] ; first-thing is within a vector
  first-thing)

(my-first ["oven" "bike" "war-axe"])   ; "oven"
(my-first '("oven" "bike" "war-axe"))  ; "oven"

(defn chooser
  [[first-choice second-choice & unimportant-choices]]
  (println (str "Your first  choice is: " first-choice))
  (println (str "Your second choice is: " second-choice))
  (println (str "We're ignoring the rest of your choices. "
                "Here they are in case you need to cry over them: "
                (clojure.string/join ", " unimportant-choices))))

(chooser ["Marmalade", "Handsome Jack", "Pigpen", "Aquaman"]) ; nil
; (out) Your first  choice is: Marmalade
; (out) Your second choice is: Handsome Jack
; (out) We're ignoring the rest of your choices. Here they are in case you need to cry over them: Pigpen, Aquaman

;; Destructuring maps
(defn announce-treasure-location1
  [{lat :lat lng :lng}] ; looks reversed
  (println (str "Treasure lat: " lat))
  (println (str "Treasure lng: " lng)))

(announce-treasure-location1 {:lat 28.22 :lng 81.33})  ; nil
; (out) Treasure lat: 28.22
; (out) Treasure lng: 81.33

(defn announce-treasure-location2
  [{:keys [lat lng]}]
  (println (str "Treasure lat: " lat))
  (println (str "Treasure lng: " lng)))

(announce-treasure-location2 {:lat 28.22 :lng 81.33}) ; nil
; (out) Treasure lat: 28.22
; (out) Treasure lng: 81.33

;; (defn receive-treasure-location
;;   [{:keys [lat lng] :as treasure-location}]
;;   (println (str "Treasure lat: " lat))
;;   (println (str "Treasure lng: " lng))

  ;; One would assume that this would put in new coordinates for your ship
  ;; (steer-ship! treasure-location))

;; Function bodies return the last value
(defn illustrative-function
  []
  (+ 1 304)
  30
  "joe")

(illustrative-function) ; "joe"

(defn number-comment
  [x]
  (if (> x 6)
    "Oh my gosh! What a big number!"
    "That number's OK, I guess"))

(number-comment 5) ; "That number's OK, I guess"

(number-comment 7) ; "Oh my gosh! What a big number!"

;; Anonymous functions
;; (fn [param-list]
;;   function body)

(map (fn [name] (str "Hi, " name))
     ["Darth Vader" "Mr. Magoo"]) ; ("Hi, Darth Vader" "Hi, Mr. Magoo")

((fn [x] (* x 3)) 8) ; 24

(def my-special-multiplier (fn [x] (* x 3)))

(my-special-multiplier 12) ; 36

;; Compact anonymous function
#(* % 3)

(#(* % 3) 8) ; 24
; => 24

(map #(str "Hi, " %)
     ["Darth Vader" "Mr. Magoo"]) ; ("Hi, Darth Vader" "Hi, Mr. Magoo")

;; Function call
(* 8 3) ; 24

;; Anonymous function
#(* % 3)

(#(str %1 " and " %2) "cornbread" "butter beans") ; "cornbread and butter beans"

;; NOTE: special syntax
(#(identity %&) 1 "blarg" :yip) ; (1 "blarg" :yip)

;; Returning functions
(defn inc-maker
  "Create a custom incrementor"
  [inc-by]
  #(+ % inc-by))

(def inc3 (inc-maker 3))

(inc3 7) ; 10

;; Hobbit violence!
(def asym-hobbit-body-parts [{:name "head"           :size 3}
                             {:name "left-eye"       :size 1}
                             {:name "left-ear"       :size 1}
                             {:name "mouth"          :size 1}
                             {:name "nose"           :size 1}
                             {:name "neck"           :size 2}
                             {:name "left-shoulder"  :size 3}
                             {:name "left-upper-arm" :size 3}
                             {:name "chest"          :size 10}
                             {:name "back"           :size 10}
                             {:name "left-forearm"   :size 3}
                             {:name "abdomen"        :size 6}
                             {:name "left-kidney"    :size 1}
                             {:name "left-hand"      :size 2}
                             {:name "left-knee"      :size 2}
                             {:name "left-thigh"     :size 4}
                             {:name "left-lower-leg" :size 3}
                             {:name "left-achilles"  :size 1}
                             {:name "left-foot"      :size 2}])

(defn matching-part
  [part]
  {:name (clojure.string/replace (:name part) #"^left-" "right-")
   :size (:size part)})

(defn symmetrize-body-parts
  "Expects a seq of maps that have a :name and :size"
  [asym-body-parts]
  (loop [remaining-asym-parts asym-body-parts
         final-body-parts     []]
    (if (empty? remaining-asym-parts)
      final-body-parts
      (let [[part & remaining] remaining-asym-parts]
        (recur remaining
               (into final-body-parts
                     (set [part (matching-part part)])))))))

(symmetrize-body-parts asym-hobbit-body-parts)
; [{:name "head", :size 3}
;  {:name "left-eye", :size 1}
;  {:name "right-eye", :size 1}
;  {:name "left-ear", :size 1}
;  {:name "right-ear", :size 1}
;  {:name "mouth", :size 1}
;  {:name "nose", :size 1}
;  {:name "neck", :size 2}
;  {:name "left-shoulder", :size 3}
;  {:name "right-shoulder", :size 3}
;  {:name "right-upper-arm", :size 3}
;  {:name "left-upper-arm", :size 3}
;  {:name "chest", :size 10}
;  {:name "back", :size 10}
;  {:name "left-forearm", :size 3}
;  {:name "right-forearm", :size 3}
;  {:name "abdomen", :size 6}
;  {:name "left-kidney", :size 1}
;  {:name "right-kidney", :size 1}
;  {:name "left-hand", :size 2}
;  {:name "right-hand", :size 2}
;  {:name "right-knee", :size 2}
;  {:name "left-knee", :size 2}
;  {:name "right-thigh", :size 4}
;  {:name "left-thigh", :size 4}
;  {:name "right-lower-leg", :size 3}
;  {:name "left-lower-leg", :size 3}
;  {:name "right-achilles", :size 1}
;  {:name "left-achilles", :size 1}
;  {:name "right-foot", :size 2}
;  {:name "left-foot", :size 2}]

;; Let expressions
(let [x 3]
  x) ; 3

(def dalmatian-list ["Pongo" "Perdita" "Puppy 1" "Puppy 2"])

(let [dalmatians (take 2 dalmatian-list)]
  dalmatians) ; ("Pongo" "Perdita")

(def x 0)

(let [x 1] x) ; 1

(let [x (inc x)] x) ; 1

(let [[pongo & dalmatians] dalmatian-list]
  [pongo dalmatians]) ; ["Pongo" ("Perdita" "Puppy 1" "Puppy 2")]

;; (let [[part & remaining] remaining-asym-parts]
;;   (recur remaining
;;          (into final-body-parts
;;                (set [part (matching-part part)]))))

;; Into
;; (into final-body-parts
;;       (set [part (matching-part part)]))

(into [] (set [:a :a])) ; [:a]

;; example ugly code
;; (recur (rest remaining-asym-parts)
;;        (into final-body-parts
;;              (set [(first remaining-asym-parts) (matching-part (first remaining-asym-parts))])))

;; Loop
(loop [iteration 0]
  (println (str "Iteration " iteration))
  (if (> iteration 3)
    (println "Goodbye!")
    (recur (inc iteration)))) ; nil
; (out) Iteration 0
; (out) Iteration 1
; (out) Iteration 2
; (out) Iteration 3
; (out) Iteration 4
; (out) Goodbye!

(defn recursive-printer
  ([]
   (recursive-printer 0))
  ([iteration]
   (println iteration)
   (if (> iteration 3)
     (println "Goodbye!")
     (recursive-printer (inc iteration)))))

(recursive-printer)
; (out) 0
; (out) 1
; (out) 2
; (out) 3
; (out) 4
; (out) Goodbye!

;; Regular expressions
#"regular-expression"

(re-find #"^left-" "left-eye") ; "left-"

(re-find #"^left-" "cleft-chin") ; nil

(re-find #"^left-" "wongleblart") ; nil

;; More hobbit violence
;; (defn matching-part
;;   [part]
;;   {:name (clojure.string/replace (:name part) #"^left-" "right-")
;;    :size (:size part)})

(matching-part {:name "left-eye" :size 1}) ; {:name "right-eye", :size 1}

(matching-part {:name "head" :size 3}) ; {:name "head", :size 3}

;; sum with reduce
(reduce + [1 2 3 4]) ; 10

;; (+ (+ (+ 1 2) 3) 4)

(reduce + 15 [1 2 3 4]) ; 25

;; build your own reduce function
(defn my-reduce
  ([f initial coll]
   (loop [result initial
          remaining coll]
     (if (empty? remaining)
       result
       (recur (f result (first remaining)) (rest remaining)))))
  ([f [head & tail]]
   (my-reduce f head tail)))

(defn better-symmetrize-body-parts
  "Expects a seq of maps that have a :name and :size"
  [asym-body-parts]
  (reduce (fn [final-body-parts part]
            (into final-body-parts (set [part (matching-part part)])))
          []
          asym-body-parts))

(defn hit
  [asym-body-parts]
  (let [sym-parts (better-symmetrize-body-parts asym-body-parts)
        body-part-size-sum (reduce + (map :size sym-parts))
        target (rand body-part-size-sum)]
    (loop [[part & remaining] sym-parts
           accumulated-size (:size part)]
      (if (> accumulated-size target)
        part
        (recur remaining (+ accumulated-size (:size (first remaining))))))))

(hit asym-hobbit-body-parts) ; {:name "left-shoulder", :size 3}

(hit asym-hobbit-body-parts) ; {:name "chest", :size 10}

(hit asym-hobbit-body-parts) ; {:name "back", :size 10}

;; (def dec9 (dec-maker 9))

;; (dec9 10)
; => 1

;; (mapset inc [1 1 2 2])
; => #{2 3}
