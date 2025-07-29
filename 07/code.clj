;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Clojure Alchemy: Reading, Evaluation, and Macros
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro backwards
  [form]
  (reverse form))

(backwards (" backwards" " am" "I" str)) ; "I am backwards"

;; An Overview of Clojure’s Evaluation Model

(def addition-list (list + 1 2))

(eval addition-list) ; 3

(eval (concat addition-list [10])) ; 13

(eval (list 'def 'lucky-number (concat addition-list [10]))) ; #'user/lucky-number

lucky-number ; 13

;; ;;;;;;;;;;
;; The Reader
;; ;;;;;;;;;;

;; ;;;;;;;
;; Reading
;; ;;;;;;;

(str "To understand what recursion is,"
     " you must first understand recursion."
     "To understand what recursion is, you must first understand recursion.")
; "To understand what recursion is, you must first understand recursion.To understand what recursion is, you must first understand recursion."

;; ;;;;;;;;;;;;;
;; Reader Macros
;; ;;;;;;;;;;;;;

(read-string "(+ 1 2)") ; (+ 1 2)

(list? (read-string "(+ 1 2)")) ; true

(conj (read-string "(+ 1 2)") :zagglewag) ; (:zagglewag + 1 2)

;; read, eval, print (similar to the REPL)
;; reads string and converts it into a Clojure data structure; in this case a list
;; then evaluates this list -> function call
;; "(+ 1 2)" represents a reader form, in this case a list reader form
(eval (read-string "(+ 1 2)")) ; 3

(#(+ 1 %) 3) ; 4

;; Reader macro and does some conversion in the reader.
;; Reader macros are sets of rules for transforming text into data structures.        
;; A reader macro typically converts an abbreviated reader form and expands it into a full form.
;; They’re designated by macro characters, like ', #, and @.
;; See 'Special Characters' in Clojure Cheatsheet.

;; # reader macro
(read-string "#(+ 1 %)") ; (fn* [%1] (+ 1 %1))

;; ' reader macro
(read-string "'(a b c)") ; (quote a b c)

;; @ reader macro
(read-string "@var") ; (deref var)

;; ; is the single line comment reader macro
(read-string "; ignore!\n(+ 1 2)") ; (+ 1 2)

;; ;;;;;;;;;;;;;
;; The Evaluator
;; ;;;;;;;;;;;;;

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; These Things Evaluate to Themselves
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

true     ; true
false    ; false
{}       ; {}
:huzzah  ; :huzzah
()       ; ()

;; ;;;;;;;
;; Symbols
;; ;;;;;;;

;; if is a special form
;; Special forms are always used in the context of an operation; they’re always the first element in a list.
(if true :a :b) ; :a

;; Referring to a special form outside of this context results in an exception.
if
; => CompilerException java.lang.RuntimeException: Unable to resolve symbol: if in this context, compiling:(NO_SOURCE_PATH:0:0) 

;; local bindings

(comment
  (let [x 5]
    (+ x 3))) ; 8

(comment
  (def x 15) ; #'user/x
  (+ x 3)) ; 18

(comment
  (def x 15) ; #'user/x
  (let [x 5]
    ((+ x 3)))) ; 8

(comment
  (let [x 5]
    (let [x 6]
      (+ x 3)))) ; 9

(defn exclaim
  [exclamation] ; also creates a local binding
  (str exclamation "!"))

(exclaim "Hadoken") ; "Hadoken!"

;; The symbol map refers to the map function,
;; but it shouldn’t be confused with the function itself.
(map inc [1 2 3]) ; (2 3 4)

;; + symbol
(read-string "+") ; +

(type (read-string "+")) ; clojure.lang.Symbol

(list (read-string "+") 1 2) ; (+ 1 2)
(eval (list + 1 2))          ; 3

(list + 1 2)
; (#object[clojure.core$_PLUS_]
;  1
;  2)
(eval (list (read-string "+") 1 2)) ; 3

;; ;;;;;
;; Lists
;; ;;;;;

(read-string "()")        ; ()
(eval (read-string "()")) ; ()

;; ;;;;;;;;;;;;;;
;; Function Calls
;; ;;;;;;;;;;;;;;

;; look up the + symbol and evaluate each argument
(+ 1 2) ; 3

;; nested function call
(+ 1 (- 3 2)) ; 2

;; ;;;;;;;;;;;;;
;; Special Forms
;; ;;;;;;;;;;;;;

;; Special forms don’t follow the same evaluation rules as normal functions.
;; For example, when you call a function, each operand gets evaluated.
;; However, with `if` you don’t want each operand to be evaluated.
(if true 1 2) ; 1

;; special form `quote`
'(a b c) ; (quote a b c)

;; The quote special form tells the evaluator,
;; “Instead of evaluating my next data structure like normal,
;;  just return the data structure itself.”
(quote (a b c)) ; (a b c)

;; def, let, loop, fn, do, and recur are all special forms as well.
;; You can see why: they don’t get evaluated the same way as functions.

;; ;;;;;;
;; Macros
;; ;;;;;;

;; returns a Clojure list
(read-string "(1 + 1)")        ; (1 + 1) 

(eval (read-string "(1 + 1)")) ; ClassCastException java.lang.Long cannot be cast to clojure.lang.IFn

;; can manipulate Clojure list
(let [infix (read-string "(1 + 1)")]
  (list (second infix) (first infix) (last infix))) ; (+ 1 1)

;; can evaluate manipulated Clojure script
(eval
 (let [infix (read-string "(1 + 1)")]
   (list (second infix) (first infix) (last infix)))) ; 2

;; Macros give you a convenient way to manipulate lists before Clojure evaluates them. 
;; NOTE: They are executed in between the reader and the evaluator.
;; They can manipulate the data structures that the reader spits out 
;; and transform with those data structures before passing them to the evaluator.
;; The best way to think about this whole process is to picture a phase between 
;; reading and evaluation: the macro expansion phase.
(defmacro ignore-last-operand
  [function-call]
  (butlast function-call))

(ignore-last-operand (+ 1 2 10)) ; 3

;; NOTE: you have to quote the form that you pass to macroexpand
(macroexpand '(ignore-last-operand (+ 1 2 10))) ; (+ 1 2)

;; This will not print anything.
(ignore-last-operand (+ 1 2 (println "look at me!!!"))) ; 3

(macroexpand '(ignore-last-operand (+ 1 2 (println "look at me!!!")))) ; (+ 1 2)

(defmacro infix
  [infixed]
  (list (second infixed)
        (first  infixed)))
        ;; (last   infixed)))

(infix (1 + 2)) ; 1

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Syntactic Abstraction and the -> Macro
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(require '[clojure.java.io :as io])

(defn read-resource1
  "Read a resource into a string"
  [path]
  (read-string (slurp (io/resource path))))

;; the same
;; -> is a macro (the threading or stabby macro)
(defn read-resource2
  "Read a resource into a string"
  [path]
  (-> path
      io/resource
      slurp
      read-string))
