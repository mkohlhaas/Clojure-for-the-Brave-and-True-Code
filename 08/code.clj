;; ;;;;;;;;;;;;;;
;; Writing Macros
;; ;;;;;;;;;;;;;;

(ns code
  (:refer-clojure :exclude [and when]))

;; ;;;;;;;;;;;;;;;;;;;;
;; Macros Are Essential
;; ;;;;;;;;;;;;;;;;;;;;

(macroexpand '(when boolean-expression
                expression-1
                expression-2
                expression-3))
; (if boolean-expression (do expression-1 expression-2 expression-3))

;; ;;;;;;;;;;;;;;;;;;
;; Anatomy of a Macro
;; ;;;;;;;;;;;;;;;;;;

(defmacro infix
  "Use this macro when you pine for the notation of your childhood"
  [infixed]
  (list (second infixed) (first infixed) (last infixed)))

(infix (1 + 1)) ; 2

(macroexpand '(infix (1 + 1))) ; (+ 1 1)

;; argument destructuring
(defmacro infix-2
  [[operand1 op operand2]]
  (list op operand1 operand2))

;; multiple-arity macros,
(defmacro and
  "Evaluates exprs one at a time, from left to right. If a form
	  returns logical false (nil or false), and returns that value and
	  doesn't evaluate any of the other expressions, otherwise it returns
	  the value of the last expr. (and) returns true."
  {:added "1.0"}
  ([] true)
  ([x] x)
  ([x & next]
   `(let [and# ~x]
      (if and# (and ~@next) and#))))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Building Lists for Evaluation
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Distinguishing Symbols and Values
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; this should be the result of our macro:
;; (let [result expression]
;;   (println result)
;;   result)

;; (defmacro my-print-whoopsie
;;   [expression]
;;   (list let [result expression]
;;         (list println result)
;;         result))
; (err) java.lang.IllegalStateException: Can't take value of a macro: #'clojure.core/let

;; NOTE: distinguish between a symbol and its value
(defmacro my-print
  [expression]
  (list 'let ['result expression]
        (list 'println 'result)
        'result))

(my-print (+ 1 2)) ; 3
; (out) 3

;; ;;;;;;;;;;;;;;
;; Simple Quoting
;; ;;;;;;;;;;;;;;

(+ 1 2) ; 3

;; quote returns an unevaluated data structure
(quote (+ 1 2)) ; (+ 1 2)

+ ; #object[clojure.core$_PLUS_ 0x495c49b "clojure.core$_PLUS_@495c49b"]

(quote +) ; +

;; evaluating an unbound symbol raises an exception
sweating-to-the-oldies ; Unable to resolve symbol: sweating-to-the-oldies in this context

;; quoting the symbol returns a symbol 
;; regardless of whether the symbol has a value associated with it
(quote sweating-to-the-oldies) ; sweating-to-the-oldies

;; single quote character is a reader macro for `(quote x)`
'sweating-to-the-oldies

'(+ 1 2) ; (+ 1 2)

'dr-jekyll-and-richard-simmons ; dr-jekyll-and-richard-simmons

(defmacro when
  "Evaluates test. If logical true, evaluates body in an implicit do."
  {:added "1.0"}
  [test & body]
  (list 'if test (cons 'do body)))

(macroexpand '(when (the-cows-come :home)
                (call me :pappy)
                (slap me :silly)))
; (if (the-cows-come :home) (do (call me :pappy) (slap me :silly)))

(defmacro unless
  "Inverted 'if'"
  [test & branches]
  (conj (reverse branches) test 'if))

(macroexpand '(unless (done-been slapped? me)
                      (slap me :silly)
                      (say "I reckon that'll learn me")))
; (if
;  (done-been slapped? me)
;  (say "I reckon that'll learn me")
;  (slap me :silly))

;; ;;;;;;;;;;;;;;
;; Syntax Quoting
;; ;;;;;;;;;;;;;;

'+ ; +

'clojure.core/+ ; clojure.core/+

'(+ 1 2)  ; (+ 1 2)

;; Syntax quoting returns unevaluated data structures.
;; Syntax quoting returns the fully qualified symbols avoiding name collisions.

`+ ; clojure.core/+

;; quoting
(list '+ 1 (inc 1)) ; (+ 1 2)
;; syntax quoting
`(+ 1 2) ; (clojure.core/+ 1 2)
;; syntax quoting
`(+ 1 (inc 1))      ; (clojure.core/+ 1 (clojure.core/inc 1))
;; syntax quoting with unquoting using `~`
`(+ 1 ~(inc 1))     ; (clojure.core/+ 1 2)

;; ` -> do not evaluate
;; ~ -> do     evaluate

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Using Syntax Quoting in a Macro
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; quoting
(defmacro code-critic
  "Phrases are courtesy Hermes Conrad from Futurama"
  [bad good]
  (list 'do
        (list 'println
              "Great squid of Madrid, this is bad code:"
              (list 'quote bad))
        (list 'println
              "Sweet gorilla of Manila, this is good code:"
              (list 'quote good))))

(macroexpand
 '(code-critic (1 + 1) (+ 1 1))) ; nil
; (do
;  (println "Great squid of Madrid, this is bad code:" '(1 + 1))
;  (println "Sweet gorilla of Manila, this is good code:" '(+ 1 1)))

(code-critic (1 + 1) (+ 1 1)) ; nil
; (out) Great squid of Madrid, this is bad code: (1 + 1)
; (out) Sweet gorilla of Manila, this is good code: (+ 1 1)

;; syntax quoting
(defmacro code-critic
  "Phrases are courtesy Hermes Conrad from Futurama"
  [bad good]
  `(do (println "Great squid of Madrid, this is bad code:"
                (quote ~bad))
       (println "Sweet gorilla of Manila, this is good code:"
                (quote ~good))))

(macroexpand
 '(code-critic (1 + 1) (+ 1 1))) ; nil
; (do
;  (clojure.core/println
;   "Great squid of Madrid, this is bad code:"
;   '(1 + 1))
;  (clojure.core/println
;   "Sweet gorilla of Manila, this is good code:"
;   '(+ 1 1)))

(code-critic (1 + 1) (+ 1 1)) ; nil
; (out) Great squid of Madrid, this is bad code: (1 + 1)
; (out) Sweet gorilla of Manila, this is good code: (+ 1 1)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Refactoring a Macro and Unquote Splicing
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; syntax quoting not only in defmacro's
;; NOTE: returns a syntax quoted list
(defn criticize-code
  [criticism code]
  `(println ~criticism (quote ~code)))

(defmacro code-critic
  [bad good]
  `(do ~(criticize-code "Cursed bacteria of Liberia, this is bad code:" bad)
       ~(criticize-code "Sweet sacred boa of Western and Eastern Samoa, this is good code:" good)))

(code-critic (1 + 1) (+ 1 1)) ; nil
; (out) Cursed bacteria of Liberia, this is bad code: (1 + 1)
; (out) Sweet sacred boa of Western and Eastern Samoa, this is good code: (+ 1 1)

;; even better version
(defmacro code-critic
  [bad good]
  `(do ~@(map #(apply criticize-code %)
              [["Great squid of Madrid, this is bad code:" bad]
               ["Sweet gorilla of Manila, this is good code:" good]])))

(macroexpand
 '(code-critic (+ 1 1) (1 + 1))) ; nil
; (do
;  (clojure.core/println
;   "Great squid of Madrid, this is bad code:"
;   '(+ 1 1))
;  (clojure.core/println
;   "Sweet gorilla of Manila, this is good code:"
;   '(1 + 1)))

;; NOTE: no NullPointerException as in the book
(code-critic (1 + 1) (+ 1 1))
; (out) Great squid of Madrid, this is bad code: (1 + 1)
; (out) Sweet gorilla of Manila, this is good code: (+ 1 1)

`(+ ~(list 1 2 3)) ; (clojure.core/+ (1 2 3))

;; unquote splicing
`(+ ~@(list 1 2 3)) ; (clojure.core/+ 1 2 3)

(defmacro code-critic
  [{:keys [good bad]}]
  `(do ~@(map #(apply criticize-code %)
              [["Sweet lion of Zion, this is bad code:" bad]
               ["Great cow of Moscow, this is good code:" good]])))

(macroexpand
 '(code-critic {:good (+ 1 1) :bad (1 + 1)})) ; nil
; (do
;  (clojure.core/println
;   "Sweet lion of Zion, this is bad code:"
;   '(1 + 1))
;  (clojure.core/println
;   "Great cow of Moscow, this is good code:"
;   '(+ 1 1)))

(code-critic {:good (+ 1 1) :bad (1 + 1)})
; (out) Sweet lion of Zion, this is bad code: (1 + 1)
; (out) Great cow of Moscow, this is good code: (+ 1 1)

;; ;;;;;;;;;;;;;;;;;;;;;;;
;; Things to Watch Out For
;; ;;;;;;;;;;;;;;;;;;;;;;;

;; ;;;;;;;;;;;;;;;;
;; Variable Capture
;; ;;;;;;;;;;;;;;;;

(def message "Good job!")

(defmacro with-mischief
  [& stuff-to-do]
  (concat (list 'let ['message "Oh, big deal!"])
          stuff-to-do))

(macroexpand
 '(with-mischief
    (println "Here's how I feel about that thing you did: " message)))
; (let*
;  [message "Oh, big deal!"]
;  (println "Here's how I feel about that thing you did: " message))

;; print "wrong" message
(with-mischief
  (println "Here's how I feel about that thing you did: " message))
; => Here's how I feel about that thing you did: Oh, big deal!

(def message "Good job!")

(defmacro with-mischief
  [& stuff-to-do]
  `(let [message "Oh, big deal!"]
     ~@stuff-to-do))

;; No Exception as in book: Can't let qualified name: user/message
(with-mischief
  (println "Here's how I feel about that thing you did: " message)) ; nil
; (out) Here's how I feel about that thing you did:  Good job!

;; gensym produces unique symbols on each successive call
(gensym) ; G__292
(gensym) ; G__355

;; with prefix
(gensym 'message_) ; message_370
(gensym 'message_) ; message_375

(defmacro without-mischief
  [& stuff-to-do]
  (let [macro-message (gensym 'message)]
    `(let [~macro-message "Oh, big deal!"]
       ~@stuff-to-do
       (println "I still need to say: " ~macro-message))))

(without-mischief
 (println "Here's how I feel about that thing you did: " message))
; (out) Here's how I feel about that thing you did:  Good job!
; (out) I still need to say:  Oh, big deal!

;; NOTE: auto-gensym (same symbol)
`(blarg# blarg#) ; (blarg__975__auto__ blarg__975__auto__)

;; same symbol makes this possible
`(let [name# "Larry Potter"] name#) ; (clojure.core/let [name__980__auto__ "Larry Potter"] name__980__auto__)

;; ;;;;;;;;;;;;;;;;;
;; Double Evaluation
;; ;;;;;;;;;;;;;;;;;

(defmacro report
  [to-try]
  `(if ~to-try
     (println (quote ~to-try) "was successful:" ~to-try)
     (println (quote ~to-try) "was not successful:" ~to-try)))

(macroexpand
 '(report (do (Thread/sleep 1000) (+ 1 1)))) ; nil
; (if
;  (do (Thread/sleep 1000) (+ 1 1))    ; sleeps for one second
;  (clojure.core/println
;   '(do (Thread/sleep 1000) (+ 1 1))  
;   "was successful:"
;   (do (Thread/sleep 1000) (+ 1 1)))  ; sleeps for another second
;  (clojure.core/println
;   '(do (Thread/sleep 1000) (+ 1 1))
;   "was not successful:"
;   (do (Thread/sleep 1000) (+ 1 1)))) ; sleeps for another second

;; sleeps for two seconds instead of one
(report (do (Thread/sleep 1000) (+ 1 1))) ; nil
; (out) (do (Thread/sleep 1000) (+ 1 1)) was successful: 2

;; using auto-gensym
(defmacro report
  [to-try]
  `(let [result# ~to-try]
     (if result#
       (println (quote ~to-try) "was successful:" result#)
       (println (quote ~to-try) "was not successful:" result#))))

(macroexpand
 '(report (do (Thread/sleep 1000) (+ 1 1)))) ; nil
; (let*
;  [result__419__auto__ (do (Thread/sleep 1000) (+ 1 1))] ; sleeps only for one second
;  (if
;   result__419__auto__
;   (clojure.core/println
;    '(do (Thread/sleep 1000) (+ 1 1))
;    "was successful:"
;    result__419__auto__)
;   (clojure.core/println
;    '(do (Thread/sleep 1000) (+ 1 1))
;    "was not successful:"
;    result__419__auto__)))

;; ;;;;;;;;;;;;;;;;;;;;;;;
;; Macros All the Way Down
;; ;;;;;;;;;;;;;;;;;;;;;;;

(report (= 1 1)) ; nil
; (out) (= 1 1) was successful: true

(report (= 1 2)) ; nil
; (out) (= 1 2) was not successful: false

;; always succesful - not what we anticpated
(doseq [code ['(= 1 1) '(= 1 2)]]
  (report code)) ; nil
; (out) code was successful: (= 1 1)
; (out) code was successful: (= 1 2)

;; (if
;;  code
;;   (clojure.core/println 'code "was successful:" code)
;;   (clojure.core/println 'code "was not successful:" code))

;; going down the macro rabbit hole
(defmacro doseq-macro
  [macroname & args]
  `(do
     ~@(map (fn [arg] (list macroname arg)) args)))

;; now it works
(doseq-macro report (= 1 1) (= 1 2)) ; nil
; (out) (= 1 1) was successful: true
; (out) (= 1 2) was not successful: false

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Brews for the Brave and True
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ;;;;;;;;;;;;;;;;;;;;
;; Validation Functions
;; ;;;;;;;;;;;;;;;;;;;;

(def order-details
  {:name "Mitchard Blimmons"
   :email "mitchard.gmail.com"})

;; map of field-name error_message-validators-pairs
(def order-details-validations
  {:name  ["Please enter a name" not-empty]
   :email ["Please enter an email address" not-empty
           "Your email address doesn't look like an email address" #(or (empty? %) (re-seq #"@" %))]})

(defn error-messages-for
  "Return a seq of error messages"
  [to-validate message-validator-pairs]
  (map first (filter #(not ((second %) to-validate))
                     (partition 2 message-validator-pairs))))

(error-messages-for ""                ["Please enter a name" not-empty])                           ; ("Please enter a name")
(error-messages-for ""                ["Please enter a name" not-empty, "invalid name" not-empty]) ; ("Please enter a name" "invalid name")
(error-messages-for "John"            ["Please enter a name" not-empty])                           ; ()
(error-messages-for "email.gmail.com" ["invalid email" #(or (not-empty %) (re-seq #"@" %))])       ; ("invalid email")
(error-messages-for ""                ["invalid email" #(or (not-empty %) (re-seq #"@" %))])       ; ("invalid email")
(error-messages-for "email@gmail.com" ["invalid email" #(or (not-empty %) (re-seq #"@" %))])       ; ()
(error-messages-for "email.gmail.com" (:email order-details-validations))                          ; ("Your email address doesn't look like an email address")
(error-messages-for ""                (:email order-details-validations))                          ; ("Please enter an email address")

(defn validate
  "Returns a map with a vector of errors for each key"
  [to-validate validations]
  (reduce (fn [errors validation]
            ;; (println validation) 
            ;; [:name [Please enter a name #not_empty]] 
            ;; [:email ...]
            (let [[fieldname validation-check-groups] validation
                  value (fieldname to-validate)
                  error-messages (error-messages-for value validation-check-groups)]
              (if (empty? error-messages)
                errors
                (assoc errors fieldname error-messages))))
          {}
          validations)) ; #'code/validate

(validate order-details order-details-validations) ; {:email ("Your email address doesn't look like an email address")}

(let [errors (validate order-details order-details-validations)]
  (if (empty? errors)
    (println :success)
    (println :failure errors)))
; (out) :failure {:email (Your email address doesn't look like an email address)}

(defn if-valid
  [record validations success-code failure-code]
  (let [errors (validate record validations)]
    (if (empty? errors)
      success-code
      failure-code)))

;; (if-valid order-details order-details-validations errors
;;           (render :success)
;;           (render :failure errors))

;; NOTE: why not using auto-gensym instead of `errors-name`
(defmacro if-valid
  "Handle validation more concisely"
  [to-validate validations errors-name & then-else]
  `(let [~errors-name (validate ~to-validate ~validations)]
     (if (empty? ~errors-name)
       ~@then-else)))

(macroexpand
 '(if-valid order-details order-details-validations my-error-name
            (println :success)
            (println :failure my-error-name)))
; (let*
;  [my-error-name
;   (code/validate order-details order-details-validations)]
;  (if
;   (clojure.core/empty? my-error-name)
;   (println :success)
;   (println :failure my-error-name)))

(let*
 [my-error-name (validate order-details order-details-validations)]
 (if (clojure.core/empty? my-error-name)
   (println :success)
   (println :failure my-error-name))) ; nil
; (out) :failure {:email (Your email address doesn't look like an email address)}
