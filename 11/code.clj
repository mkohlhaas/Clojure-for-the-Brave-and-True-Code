(ns code
  (:require [clojure.string :as str]
            [clojure.core.async
             :as a
             :refer [>! <! >!! go chan thread close! alts!! timeout <!!]]))

;; blocks indefinitely
(>!! (chan) "mustard")

(def echo-buffer (chan 2))

(>!! echo-buffer "ketchup")
; => true

(>!! echo-buffer "ketchup")
; => true

(>!! echo-buffer "ketchup")
; This blocks because the channel buffer is full

(def hi-chan (chan))
(doseq [n (range 1000)]
  (go (>! hi-chan (str "hi " n))))

(def echo-chan (chan))

(thread (println (<!! echo-chan)))

(>!! echo-chan "mustard")
; => true
; => mustard

(let [t (thread "chili")]
  (<!! t))
; => "chili"

;; Hot dog machine stuff

(defn hot-dog-machine
  []
  (let [in (chan)
        out (chan)]
    (go (<! in)
        (>! out "hot dog"))
    [in out]))

(let [[in out] (hot-dog-machine)]
  (>!! in "pocket lint")
  (<!! out))
; => "hot dog"

;; Hot dog machine v2

(defn hot-dog-machine-v2
  [hot-dog-count]
  (let [in (chan)
        out (chan)]
    (go (loop [hc hot-dog-count]
          (if (> hc 0)
            (let [input (<! in)]
              (if (= 3 input)
                (do (>! out "hot dog")
                    (recur (dec hc)))
                (do (>! out "wilted lettuce")
                    (recur hc))))
            (do (close! in)
                (close! out)))))
    [in out]))

(let [[in out] (hot-dog-machine-v2 2)]
  (>!! in "pocket lint")
  (println (<!! out))

  (>!! in 3)
  (println (<!! out))

  (>!! in 3)
  (println (<!! out))

  (>!! in 3)
  (<!! out))
; => wilted lettuce
; => hotdog
; => hotdog
; => nil

;; Pipeline
(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (go (>! c2 (clojure.string/upper-case (<! c1))))
  (go (>! c3 (clojure.string/reverse (<! c2))))
  (go (println (<! c3)))
  (>!! c1 "redrum"))
; => MURDER

(defn upload
  [headshot c]
  (go (Thread/sleep (rand 100))
      (>! c headshot)))

;; alts!!
(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (upload "serious.jpg" c1)
  (upload "fun.jpg" c2)
  (upload "sassy.jpg" c3)
  (let [[headshot _channel] (alts!! [c1 c2 c3])]
    (println "Sending headshot notification for" headshot)))
; => Sending headshot notification for sassy.jpg

(let [c1 (chan)]
  (upload "serious.jpg" c1)
  (let [[headshot _channel] (alts!! [c1 (timeout 20)])]
    (if headshot
      (println "Sending headshot notification for" headshot)
      (println "Timed out!"))))
; => Timed out!

(let [c1 (chan)
      c2 (chan)]
  (go (<! c2))
  (let [[value channel] (alts!! [c1 [c2 "put!"]])]
    (println value)
    (= channel c2)))
; => true
; => true

;; reverser

(defn upper-caser
  [in]
  (let [out (chan)]
    (go (while true (>! out (clojure.string/upper-case (<! in)))))
    out))

(defn reverser
  [in]
  (let [out (chan)]
    (go (while true (>! out (clojure.string/reverse (<! in)))))
    out))

(defn printer
  [in]
  (go (while true (println (<! in)))))

(def in-chan (chan))
(def upper-caser-out (upper-caser in-chan))
(def reverser-out (reverser upper-caser-out))

(printer reverser-out)

(>!! in-chan "redrum")
; => MURDER

(>!! in-chan "repaid")
; => DIAPER
