;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Clojure Metaphysics: Atoms, Refs, Vars, and Cuddle Zombies
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns code
  (:require [clojure.string]
            [clojure.java.io]))

;; ;;;;;
;; Atoms
;; ;;;;;

(def fred (atom {:cuddle-hunger-level  0
                 :percent-deteriorated 0}))

@fred ; {:cuddle-hunger-level 0, :percent-deteriorated 0}

(let [zombie-state @fred]
  (when (<= (:percent-deteriorated zombie-state) 50)
    (future (println (:percent-deteriorated zombie-state)))))
; (out) 0

(swap! fred
       (fn [current-state]
         (merge-with + current-state {:cuddle-hunger-level 1})))
; {:cuddle-hunger-level 1, :percent-deteriorated 0}

@fred
; {:cuddle-hunger-level 1, :percent-deteriorated 0}

(swap! fred
       (fn [current-state]
         (merge-with + current-state {:cuddle-hunger-level 1
                                      :percent-deteriorated 1})))
; {:cuddle-hunger-level 2, :percent-deteriorated 1}

(defn increase-cuddle-hunger-level
  [zombie-state increase-by]
  (merge-with + zombie-state {:cuddle-hunger-level increase-by}))

(increase-cuddle-hunger-level @fred 10)
; {:cuddle-hunger-level 12, :percent-deteriorated 1}

@fred ; {:cuddle-hunger-level 2, :percent-deteriorated 1}

(swap! fred increase-cuddle-hunger-level 10)
; {:cuddle-hunger-level 12, :percent-deteriorated 1}

@fred
; {:cuddle-hunger-level 12, :percent-deteriorated 1}

(update-in {:a {:b 3}} [:a :b] inc)  ; {:a {:b 4}}
(update-in {:a {:b 3}} [:a :b] + 10) ; {:a {:b 13}}

(swap! fred update-in [:cuddle-hunger-level] + 10)
; {:cuddle-hunger-level 22, :percent-deteriorated 1}

@fred ; {:cuddle-hunger-level 22, :percent-deteriorated 1}

(let [num (atom 1)
      s1 @num]
  (swap! num inc)
  (println "Old state:    " s1)
  (println "Current state:" @num))
; (out) Old state:     1
; (out) Current state: 2

(reset! fred {:cuddle-hunger-level 0
              :percent-deteriorated 0})
; {:cuddle-hunger-level 0, :percent-deteriorated 0}

@fred
; {:cuddle-hunger-level 0, :percent-deteriorated 0}

;; ;;;;;;;;;;;;;;;;;;;;;;
;; Watches and Validators
;; ;;;;;;;;;;;;;;;;;;;;;;

;; ;;;;;;;
;; Watches
;; ;;;;;;;

(defn shuffle-speed
  [zombie]
  (* (:cuddle-hunger-level zombie)
     (- 100 (:percent-deteriorated zombie))))

;; watch function (see arguments)
(defn shuffle-alert
  [key _watched _old-state new-state]
  (let [sph (shuffle-speed new-state)]
    (if (> sph 5000)
      (do
        (println "Run, you fool!")
        (println "The zombie's SPH is now" sph)
        (println "This message brought to your courtesy of" key))
      (do
        (println "All's well with" key)
        (println "Cuddle hunger:" (:cuddle-hunger-level new-state))
        (println "Percent deteriorated:" (:percent-deteriorated new-state))
        (println "SPH:" sph)))))

(reset! fred {:cuddle-hunger-level 22
              :percent-deteriorated 2})
; {:cuddle-hunger-level 22, :percent-deteriorated 2}

(add-watch fred :fred-shuffle-alert shuffle-alert)

(swap! fred update-in [:percent-deteriorated] + 1)
; (out) All's well with :fred-shuffle-alert
; (out) Cuddle hunger: 22
; (out) Percent deteriorated: 3
; (out) SPH: 2134

@fred ; {:cuddle-hunger-level 22, :percent-deteriorated 3}

(swap! fred update-in [:cuddle-hunger-level] + 30)
; {:cuddle-hunger-level 52, :percent-deteriorated 3}
; (out) Run, you fool!
; (out) The zombie's SPH is now 5044
; (out) This message brought to your courtesy of :fred-shuffle-alert

;; ;;;;;;;;;;
;; Validators
;; ;;;;;;;;;;

(def my-atom (atom 0 :validator (fn [new-number] (println new-number) (even? new-number))))

@my-atom

(swap! my-atom inc)
; (err) java.lang.IllegalStateException

(swap! my-atom (partial + 2)) ; 2
; (out) 2

;; validator returning a boolean
(defn percent-deteriorated-validator
  [{:keys [percent-deteriorated]}]
  (and (>= percent-deteriorated 0)
       (<= percent-deteriorated 100)))

(def bobby
  (atom
   {:cuddle-hunger-level  0
    :percent-deteriorated 0}
   :validator percent-deteriorated-validator))

@bobby

(swap! bobby update-in [:percent-deteriorated] + 20)
; {:cuddle-hunger-level 0, :percent-deteriorated 20}

(swap! bobby update-in [:percent-deteriorated] + 200)
; (err) java.lang.IllegalStateException

@bobby ; {:cuddle-hunger-level 0, :percent-deteriorated 20}

;; validator throwing an exception in error case
(defn percent-deteriorated-validator1
  [{:keys [percent-deteriorated]}]
  (or (and (>= percent-deteriorated 0)
           (<= percent-deteriorated 100))
      (throw (IllegalStateException. "That's not mathy!"))))

(def bobby1
  (atom
   {:cuddle-hunger-level  0
    :percent-deteriorated 0}
   :validator percent-deteriorated-validator1))

(swap! bobby1 update-in [:percent-deteriorated] + 20)
; {:cuddle-hunger-level 0, :percent-deteriorated 20}

(swap! bobby1 update-in [:percent-deteriorated] + 200)
; (err) java.lang.IllegalStateException: That's not mathy! 

;; ;;;;;;;;;;;;;;;;;;;;;;;
;; Modeling Sock Transfers
;; ;;;;;;;;;;;;;;;;;;;;;;;

(def sock-varieties
  #{"darned" "argyle" "wool" "horsehair" "mulleted"
    "passive-aggressive" "striped" "polka-dotted"
    "athletic" "business" "power" "invisible" "gollumed"})

(defn sock-count
  [sock-variety count]
  {:variety sock-variety
   :count count})

(defn generate-sock-gnome
  "Create an initial sock gnome state with no socks"
  [name]
  {:name name
   :socks #{}})

(def sock-gnome (ref (generate-sock-gnome "Barumpharumph")))

(def dryer (ref {:name "LG 1337"
                 :socks (set (map #(sock-count % 2) sock-varieties))}))

@dryer
; {:name "LG 1337",
;  :socks
;  #{{:variety "gollumed", :count 2}
;    {:variety "striped", :count 2}
;    {:variety "wool", :count 2}
;    {:variety "passive-aggressive", :count 2}
;    {:variety "argyle", :count 2}
;    {:variety "business", :count 2}
;    {:variety "darned", :count 2}
;    {:variety "polka-dotted", :count 2}
;    {:variety "horsehair", :count 2}
;    {:variety "power", :count 2}
;    {:variety "athletic", :count 2}
;    {:variety "mulleted", :count 2}
;    {:variety "invisible", :count 2}}}

(:socks @dryer)
; #{{:variety "gollumed", :count 2}
;   {:variety "striped", :count 2}
;   {:variety "wool", :count 2}
;   {:variety "passive-aggressive", :count 2}
;   {:variety "argyle", :count 2}
;   {:variety "business", :count 2}
;   {:variety "darned", :count 2}
;   {:variety "polka-dotted", :count 2}
;   {:variety "horsehair", :count 2}
;   {:variety "power", :count 2}
;   {:variety "athletic", :count 2}
;   {:variety "mulleted", :count 2}
;   {:variety "invisible", :count 2}}

(defn steal-sock
  [gnome dryer]
  (dosync
   (when-let [pair (some #(when (= (:count %) 2) %) (:socks @dryer))]
     (let [updated-count (sock-count (:variety pair) 1)]
       (alter gnome update-in [:socks] conj updated-count)
       (alter dryer update-in [:socks] disj pair)
       (alter dryer update-in [:socks] conj updated-count)))))

(steal-sock sock-gnome dryer)
; {:name "LG 1337",
;  :socks
;  #{{:variety "striped", :count 2}
;    {:variety "wool", :count 2}
;    {:variety "passive-aggressive", :count 2}
;    {:variety "argyle", :count 2}
;    {:variety "business", :count 2}
;    {:variety "darned", :count 2}
;    {:variety "polka-dotted", :count 2}
;    {:variety "horsehair", :count 2}
;    {:variety "power", :count 2}
;    {:variety "athletic", :count 2}
;    {:variety "gollumed", :count 1}    ; one sock removed
;    {:variety "mulleted", :count 2}
;    {:variety "invisible", :count 2}}}

(:socks @sock-gnome) ; #{{:variety "gollumed", :count 1}} ; sock added

(defn similar-socks
  [target-sock sock-set]
  (filter #(= (:variety %) (:variety target-sock)) sock-set))

(similar-socks (first (:socks @sock-gnome)) (:socks @dryer))
; ({:variety "gollumed", :count 2} {:variety "gollumed", :count 1})

;; in-transaction state
(def counter (ref 0))
(future
  (dosync
   (alter counter inc)
   (println @counter)
   (Thread/sleep 500)
   (alter counter inc)
   (println @counter)))
(Thread/sleep 250)
(println @counter)

;; ;;;;;;;
;; commute
;; ;;;;;;;

(defn sleep-print-update
  [sleep-time thread-name update-fn]
  (fn [state]
    (Thread/sleep sleep-time)
    (println (str thread-name ": " state))
    (update-fn state)))

(def counter (ref 0))

(future (dosync (commute counter (sleep-print-update 100 "Thread A" inc))))
(future (dosync (commute counter (sleep-print-update 150 "Thread B" inc))))

;; example of unsafe commuting
(def receiver-a (ref #{}))
(def receiver-b (ref #{}))
(def giver (ref #{1}))
(do (future (dosync (let [gift (first @giver)]
                      (Thread/sleep 10)
                      (alter receiver-a conj gift)
                      (alter giver disj gift))))
    (future (dosync (let [gift (first @giver)]
                      (Thread/sleep 50)
                      (alter receiver-b conj gift)
                      (alter giver disj gift)))))

(do (future (dosync (let [gift (first @giver)]
                      (Thread/sleep 10)
                      (commute receiver-a conj gift)
                      (commute giver disj gift))))
    (future (dosync (let [gift (first @giver)]
                      (Thread/sleep 50)
                      (commute receiver-b conj gift)
                      (commute giver disj gift)))))

@receiver-a ; #{1}
@receiver-b ; #{1}
@giver      ; #{}

;; ;;;;
;; Vars
;; ;;;;

;; ;;;;;;;;;;;;;;;
;; Dynamic Binding
;; ;;;;;;;;;;;;;;;

(def ^:dynamic *notification-address* "dobby@elf.org")

*notification-address* ; "dobby@elf.org"

; change temporarily
(binding [*notification-address* "test@elf.org"]
  *notification-address*)
; "test@elf.org"

*notification-address* ; "dobby@elf.org"

; stack bindings
(binding [*notification-address* "tester-1@elf.org"]
  (println *notification-address*)
  (binding [*notification-address* "tester-2@elf.org"]
    (println *notification-address*))
  (println *notification-address*)) ; nil
; (out) tester-1@elf.org
; (out) tester-2@elf.org
; (out) tester-1@elf.org

;; ;;;;;;;;;;;;;;;;
;; Dynamic Var Uses
;; ;;;;;;;;;;;;;;;;

(defn notify
  [message]
  (str "TO: " *notification-address* "\n"
       "MESSAGE: " message))

(notify "I fell.")
; "TO: dobby@elf.org\nMESSAGE: I fell."

(binding [*notification-address* "test@elf.org"]
  (notify "test!")) ; "TO: test@elf.org\nMESSAGE: test!"
; "TO: test@elf.org\nMESSAGE: test!"

; Clojure comes with a ton of built-in dynamic vars, e.g. *out*
(binding [*out* (clojure.java.io/writer "print-output.txt")]
  (println "A man who carries a cat by the tail learns")
  (println "something he can learn in no other way.")
  (println "-- Mark Twain"))

(slurp "print-output.txt")
; A man who carries a cat by the tail learns
; something he can learn in no other way.
; -- Mark Twain

; another build-in dynamic var, *print-length*.

(println ["Print" "all" "the" "things!"])
; (out) [Print all the things!]

; another build-in dynamic var
(binding [*print-length* 1]
  (println ["Print" "just" "one!"]))
; (out) [Print ...]

(def ^:dynamic *troll-thought* nil)

(defn troll-riddle
  [your-answer]
  (let [number "man meat"]
    ;; thread-bound? takes the var itself as an argument, not the value it refers to
    (when (thread-bound? #'*troll-thought*) ; thread-local binding ?
      (println "setting *troll-thought*")
      (set! *troll-thought* number)) ; set! will succeed if thread-local binding
    (if (= number your-answer)
      "TROLL: You can cross the bridge!"
      "TROLL: Time to eat you, succulent human!")))

(binding [*troll-thought* nil]
  (println (troll-riddle 2))
  (println "SUCCULENT HUMAN: Oooooh! The answer was" *troll-thought*))
; (out) setting *troll-thought*
; (out) TROLL: Time to eat you, succulent human!
; (out) SUCCULENT HUMAN: Oooooh! The answer was man meat

;; var returns to its original value outside of binding
*troll-thought* ; nil

;; ;;;;;;;;;;;;;;;;;;
;; Per-Thread Binding
;; ;;;;;;;;;;;;;;;;;;

;; If you access a dynamically bound var from within a manually created thread,
;; the var will evaluate to the original value.

(.write *out* "prints to repl")
; (out) prints to repl

(.start (Thread. #(.write *out* "prints to standard out")))

(let [out *out*]
  (.start
   (Thread. #(binding [*out* out]
               (.write *out* "prints to repl from thread")))))

(.start (Thread. (bound-fn [] (.write *out* "prints to repl from thread"))))

;; ;;;;;;;;;;;;;;;;;;;;;
;; Altering the Var Root
;; ;;;;;;;;;;;;;;;;;;;;;

; the initial value that you supply is its root
(def power-source "hair")

(alter-var-root #'power-source (fn [_previous_value] "7-eleven parking lot"))

power-source
; "7-eleven parking lot"

; temporarily alter a var’s root, seen in all threads
(with-redefs [*out* *out*]
  (doto (Thread. #(println "with redefs allows me to show up in the REPL"))
    .start
    .join))
; (out) with redefs allows me to show up in the REPL

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Stateless Concurrency and Parallelism with pmap
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn always-1
  []
  1)

(take 5 (repeatedly always-1))
; (1 1 1 1 1)

(take 5 (repeatedly rand))
; (0.3637898392804828
;  0.2762018646831279
;  0.02931888915454972
;  0.17067728009184058
;  0.7784136009137743)

(take 5 (repeatedly (partial rand-int 10)))
; (1 5 0 3 4)

(def alphabet-length 26)

(def letters (mapv (comp str char (partial + 65)) (range alphabet-length)))
; ["A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N" "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z"]

(defn random-string
  "Returns a random string of specified length"
  [length]
  (apply str (take length (repeatedly #(rand-nth letters)))))

(random-string 10) ; "DVGMVNOLWY"

(defn random-string-list
  [list-length string-length]
  (doall (take list-length (repeatedly (partial random-string string-length)))))

(random-string-list 3 10) ; ("OZKRBJWMJM" "XLJLMUVRBX" "CZLDLWEGMM")

(def orc-names (random-string-list 3000 7000)) ; #'code/orc-names

; pmap faster
(time (dorun (map clojure.string/lower-case orc-names))) ; nil
; (out) "Elapsed time: 50.645672 msecs"
; "Elapsed time: 270.182 msecs"

(time (dorun (pmap clojure.string/lower-case orc-names)))
; (out) "Elapsed time: 41.374177 msecs"
; "Elapsed time: 147.562 msecs"

(def orc-name-abbrevs (random-string-list 20000 300))

; pmap slower
(time (dorun (map clojure.string/lower-case orc-name-abbrevs)))
; (out) "Elapsed time: 18.429014 msecs"
; "Elapsed time: 78.23 msecs"

(time (dorun (pmap clojure.string/lower-case orc-name-abbrevs)))
; (out) "Elapsed time: 73.985266 msecs"
; "Elapsed time: 124.727 msecs"

(def numbers [1 2 3 4 5 6 7 8 9 10])

(partition-all 3 numbers)
; ((1 2 3) (4 5 6) (7 8 9) (10))

(pmap inc numbers) ; (2 3 4 5 6 7 8 9 10 11)

(pmap (fn [number-group] (doall (map inc number-group)))
      (partition-all 3 numbers))
; ((2 3 4) (5 6 7) (8 9 10) (11))

(apply concat
       (pmap (fn [number-group] (doall (map inc number-group)))
             (partition-all 3 numbers)))
; (2 3 4 5 6 7 8 9 10 11)

(time
 (dorun
  (apply concat
         (pmap (fn [name] (doall (map clojure.string/lower-case name)))
               (partition-all 1000 orc-name-abbrevs)))))
; (out) "Elapsed time: 42.478763 msecs"
; "Elapsed time: 44.677 msecs"

(defn ppmap
  "Partitioned pmap, for grouping map ops together to make parallel overhead worthwhile"
  [grain-size f & colls]
  (apply concat
         (apply pmap
                (fn [& pgroups] (doall (apply map f pgroups)))
                (map (partial partition-all grain-size) colls))))

(time (dorun (ppmap 1000 clojure.string/lower-case orc-name-abbrevs)))
; (out) "Elapsed time: 86.06846 msecs"
; => "Elapsed time: 44.902 msecs"
