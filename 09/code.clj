
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Sacred Art of Concurrent and Parallel Programming
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Futures, Delays, and Promises
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ;;;;;;;
;; Futures
;; ;;;;;;;

(future (Thread/sleep 4000)
        (println "I'll print after 4 seconds")) ; #<Future@16856796: :pending>
(println "I'll print immediately") ; nil

(let [result (future (println "this prints once")
                     (+ 1 1))]
  (println "deref: " (deref result))
  (println "@: " @result)) ; nil
; "this prints once"
; deref: 2
; @: 2

(let [result (future (Thread/sleep 3000)
                     (+ 1 1))]
  (println "The result is: " @result)
  (println "It will be at least 3 seconds before I print")) ; nil
; The result is: 2
; It will be at least 3 seconds before I print

(deref (future (Thread/sleep 1000) 0) 10 5)  ; 5

(realized? (future (Thread/sleep 1000))) ; false

(let [f (future)]
  @f
  (realized? f)) ; true

;; ;;;;;;
;; Delays
;; ;;;;;;

(def jackson-5-delay
  (delay (let [message "Just call my name and I'll be there"]
           (println "First deref:" message)
           message)))

(force jackson-5-delay) ; "Just call my name and I'll be there"

@jackson-5-delay ; "Just call my name and I'll be there"

(def gimli-headshots ["serious.jpg" "fun.jpg" "playful.jpg"]) ; #'user/gimli-headshots

(defn email-user
  [email-address]
  (println "Sending headshot notification to" email-address))

(defn upload-document
  "Needs to be implemented"
  [_headshot]
  true)

(let [notify (delay (email-user "and-my-axe@gmail.com"))]
  (doseq [headshot gimli-headshots]
    (future (upload-document headshot)
            (force notify))))
; (out) Sending headshot notification to and-my-axe@gmail.com

;; ;;;;;;;;
;; Promises
;; ;;;;;;;;

(def my-promise (promise))

(deliver my-promise (+ 1 2))

@my-promise ; 3

(def yak-butter-international
  {:store "Yak Butter International"
   :price 90
   :smoothness 90})

(def butter-than-nothing
  {:store "Butter Than Nothing"
   :price 150
   :smoothness 83})

;; This is the butter that meets our requirements
(def baby-got-yak
  {:store "Baby Got Yak"
   :price 94
   :smoothness 99})

(defn mock-api-call
  [result]
  (Thread/sleep 1000)
  result)

(defn satisfactory?
  "If the butter meets our criteria, return the butter, else return false"
  [butter]
  (and (<= (:price butter) 100)
       (>= (:smoothness butter) 97)
       butter))

(time (some (comp satisfactory? mock-api-call)
            [yak-butter-international butter-than-nothing baby-got-yak]))
; (out) "Elapsed time: 3000.870112 msecs"
; {:store "Baby Got Yak", :price 94, :smoothness 99}

(time
 (let [butter-promise (promise)]
   (doseq [butter [yak-butter-international butter-than-nothing baby-got-yak]]
     (future (when-let [satisfactory-butter (satisfactory? (mock-api-call butter))]
               (deliver butter-promise satisfactory-butter))))
   (println "And the winner is:" @butter-promise)))
; (out) And the winner is: {:store Baby Got Yak, :price 94, :smoothness 99}
; (out) "Elapsed time: 1006.92151 msecs"

(let [p (promise)]
  (deref p 100 "timed out"))
; "timed out"

(let [ferengi-wisdom-promise (promise)]
  (future (println "Here's some Ferengi wisdom:" @ferengi-wisdom-promise))
  (Thread/sleep 100)
  (deliver ferengi-wisdom-promise "Whisper your way to success."))
; (out) Here's some Ferengi wisdom: Whisper your way to success.

;; ;;;;;;;;;;;;;;;;;;;;;;
;; Rolling Your Own Queue
;; ;;;;;;;;;;;;;;;;;;;;;;

(defmacro wait
  "Sleep `timeout` seconds before evaluating body"
  [timeout & body]
  `(do (Thread/sleep ~timeout) ~@body))

;; what we want -> output in the correct order
(let [saying3 (promise)]
  (future (deliver saying3 (wait 100 "Cheerio!")))
  @(let [saying2 (promise)] ; blocks till saying2 is done
     (future (deliver saying2 (wait 400 "Pip pip!")))
     @(let [saying1 (promise)] ; blocks till saying1 is done
        (future (deliver saying1 (wait 200 "'Ello, gov'na!")))
        (println @saying1)
        saying1)
     (println @saying2)
     saying2)
  (println @saying3)
  saying3)
; (out) 'Ello, gov'na!
; (out) Pip pip!
; (out) Cheerio!

;; this is how the `enqueue` macro should work
;; (-> (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
;;     (enqueue saying (wait 400 "Pip pip!") (println @saying))
;;     (enqueue saying (wait 100 "Cheerio!") (println @saying)))

(defmacro enqueue
  ([q concurrent-promise-name concurrent serialized]
   `(let [~concurrent-promise-name (promise)]
      (future (deliver ~concurrent-promise-name ~concurrent))
      (deref ~q)
      ~serialized
      ~concurrent-promise-name))
  ([concurrent-promise-name concurrent serialized]
   `(enqueue (future) ~concurrent-promise-name ~concurrent ~serialized)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(time (-> (enqueue saying (wait 2000 "'Ello, gov'na!") (println @saying))
          (enqueue saying (wait 200  "Pip pip!")       (println @saying))
          (enqueue saying (wait 100  "Cheerio!")       (println @saying))))
; (out) 'Ello, gov'na!
; (out) Pip pip!
; (out) Cheerio!
; (out) "Elapsed time: 2001.139787 msecs"

(macroexpand
 '(-> (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
      (enqueue saying (wait 400 "Pip pip!") (println @saying))
      (enqueue saying (wait 100 "Cheerio!") (println @saying))))
;; (let*
;;  [saying (clojure.core/promise)]
;;  (clojure.core/future
;;   (clojure.core/deliver saying (wait 100 "Cheerio!")))
;;  @(enqueue
;;    (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
;;    saying
;;    (wait 400 "Pip pip!")
;;    (println @saying))
;;  (println @saying)
;;  saying)

(time (-> (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
          (enqueue saying (wait 400 "Pip pip!") (println @saying))
          (enqueue saying (wait 100 "Cheerio!") (println @saying))))
; => 'Ello, gov'na!
; => Pip pip!
; => Cheerio!
; => "Elapsed time: 401.635 msecs"
