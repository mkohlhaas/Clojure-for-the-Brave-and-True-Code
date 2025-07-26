;; #_{:clj-kondo/ignore [:refer-all]}

(ns pegthing.core-test
  (:require [clojure.test  :refer [deftest is testing]]
            [pegthing.core :refer [triangular?]]))

;; https://clojuredocs.org/clojure.core/apply#example-542692cdc026201cdc326d50
(defmacro make-fn [m]
  `(fn [& args#]
     (eval
      (cons '~m args#))))

(deftest triangular-numbers
  (testing "Triangular numbers"
    (let [tri-numbers '(1 3 6 10 15 21 28 36 45 55 66 78 91 105 120 136 153 171 190 210 231 253 276 300 325 351 378 406 435 465 496 528 561 595 630 666 703 741 780 820 861 903 946 990 1035 1081 1128 1176 1225 1275 1326 1378 1431 1485)]
      (is (apply (make-fn and) (map triangular? tri-numbers))))))
