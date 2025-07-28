(ns pegthing.core
  (:require [clojure.string :as str])
  (:gen-class))

(declare successful-move prompt-move game-over prompt-rows)

;; ;;;;;;;;;;;;;;;;
;; Create the board
;; ;;;;;;;;;;;;;;;;

(defn tri*
  "Generates lazy sequence of triangular numbers"
  ([] (tri* 0 1))
  ([sum n]
   (let [new-sum (+ sum n)]
     (cons new-sum (lazy-seq (tri* new-sum (inc n)))))))

(def tri (tri*))

(defn triangular?
  "Is the number triangular? e.g. 1, 3, 6, 10, 15, etc"
  [n]
  (= n (last (take-while #(>= n %) tri))))

(defn row-tri
  "The triangular number at the end of row n"
  [n]
  (last (take n tri)))

(defn row-num
  "Returns row number the position belongs to:
   pos 1 in row 1, positions 2 and 3 in row 2, etc"
  [pos]
  (inc (count (take-while #(> pos %) tri))))

(defn in-bounds?
  "Is every position less than or equal the max position?"
  [max-pos & positions]
  (= max-pos (apply max max-pos positions)))

(defn connect
  "Form a mutual connection between two positions"
  [board max-pos pos neighbor destination]
  (if (in-bounds? max-pos neighbor destination)
    (reduce (fn [new-board [p1 p2]] (assoc-in new-board [p1 :connections p2] neighbor))
            board
            [[pos destination] [destination pos]])
    board))

(defn connect-right
  [board max-pos pos]
  (let [neighbor (inc pos)
        destination (inc neighbor)]
    (if-not (or (triangular? neighbor) (triangular? pos))
      (connect board max-pos pos neighbor destination)
      board)))

(defn connect-down-left
  [board max-pos pos]
  (let [row (row-num pos)
        neighbor (+ row pos)
        destination (+ 1 row neighbor)]
    (connect board max-pos pos neighbor destination)))

(defn connect-down-right
  [board max-pos pos]
  (let [row (row-num pos)
        neighbor (+ 1 row pos)
        destination (+ 2 row neighbor)]
    (connect board max-pos pos neighbor destination)))

(defn add-pos
  "Pegs the position and performs connections"
  [board max-pos pos]
  (let [pegged-board (assoc-in board [pos :pegged] true)]
    (reduce (fn [new-board connector] (connector new-board max-pos pos))
            pegged-board
            [connect-right connect-down-left connect-down-right])))

(defn new-board
  [rows]
  (let [initial-board {:rows rows}
        max-pos (row-tri rows)]
    (reduce (fn [board pos] (add-pos board max-pos pos))
            initial-board
            (range 1 (inc max-pos)))))

;; ;;;;;;;;;
;; Move pegs
;; ;;;;;;;;;

(defn pegged?
  "Does the position have a peg in it?"
  [board pos]
  (get-in board [pos :pegged]))

(defn valid-moves
  "Return a map of all valid moves for pos, where the key is the
   destination and the value is the jumped position"
  [board pos]
  (into {}
        (filter (fn [[destination jumped]]
                  (and (not (pegged? board destination))
                       (pegged? board jumped)))
                (get-in board [pos :connections]))))

(defn valid-move?
  "Return jumped position if the move from p1 to p2 is valid, nil otherwise"
  [board p1 p2]
  (get (valid-moves board p1) p2))

(defn remove-peg
  "Take the peg at given position out of the board"
  [board pos]
  (assoc-in board [pos :pegged] false))

(defn place-peg
  "Put a peg in the board at given position"
  [board pos]
  (assoc-in board [pos :pegged] true))

(defn move-peg
  "Take peg out of p1 and place it in p2"
  [board p1 p2]
  (place-peg (remove-peg board p1) p2))

(defn make-move
  "Move peg from p1 to p2, removing jumped peg"
  [board p1 p2]
  (when-let [jumped (valid-move? board p1 p2)]
    (move-peg (remove-peg board jumped) p1 p2)))

(defn can-move?
  "Do any of the pegged positions have valid moves?"
  [board]
  (some (comp not-empty (partial valid-moves board))
        (map first (filter #(get (second %) :pegged) board)))) ;; pegged positions

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Represent board textually and print it
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ansi-styles
  {:red   "[31m"
   :green "[32m"
   :blue  "[34m"
   :reset "[0m"})

(defn ansi
  "Produce a string which will apply an ansi style"
  [style]
  (str \u001b (style ansi-styles)))

(defn colorize
  "Apply ansi color to text"
  [text color]
  (str (ansi color) text (ansi :reset)))

(def alpha-start 97)

(defn render-pos
  [board pos]
  (let [letters (map (comp str char) (range alpha-start (+ alpha-start 26)))] ; ("a" "b" ... "x" "y" "z")
    (str (nth letters (dec pos))
         (if (get-in board [pos :pegged])
           (colorize "0" :blue)
           (colorize "-" :red)))))

(defn row-positions
  "Return all positions in the given row"
  [row-num]
  (range (inc (or (row-tri (dec row-num)) 0))
         (inc (row-tri row-num))))

(defn row-padding
  "String of spaces to add to the beginning of a row to center it"
  [row-num rows]
  (let [pos-chars 3
        pad-length (/ (* (- rows row-num) pos-chars) 2)]
    (apply str (take pad-length (repeat " ")))))

(defn render-row
  [board row-num]
  (str (row-padding row-num (:rows board))
       (str/join " " (map (partial render-pos board) (row-positions row-num)))))

(defn print-board
  [board]
  (doseq [row-num (range 1 (inc (:rows board)))]
    (println (render-row board row-num))))

;; ;;;;;;;;;;;
;; Interaction
;; ;;;;;;;;;;;

(defn letter->pos
  "Converts a letter string to the corresponding position number"
  [letter]
  (inc (- (int (first letter)) alpha-start)))

(defn get-input
  "Waits for user to enter text and hit enter, then cleans the input"
  ([] (get-input ""))
  ([default]
   (let [input (str/trim (read-line))]
     (if (empty? input)
       default
       (str/lower-case input)))))

(defn characters-as-strings
  "Given a string, return a collection consisting of each individual character"
  [string]
  (re-seq #"[a-zA-Z]" string))

(defn prompt-move
  [board]
  (println "\nHere's your board:")
  (print-board board)
  (println "Move from where to where? Enter two letters:")
  (let [input (map letter->pos (characters-as-strings (get-input)))]
    (if-let [new-board (make-move board (first input) (second input))]
      (successful-move new-board)
      (do
        (println "\n!!! That was an invalid move :(\n")
        (prompt-move board)))))

(defn successful-move
  [board]
  (if (can-move? board)
    (prompt-move board)
    (game-over board)))

(defn game-over
  [board]
  (let [remaining-pegs (count (filter :pegged (vals board)))]
    (println "Game over! You had" remaining-pegs "pegs left:")
    (print-board board)
    (println "Play again? y/n [y]")
    (let [input (get-input "y")]
      (if (= "y" input)
        (prompt-rows)
        (do
          (println "Bye!")
          (System/exit 0))))))

(defn prompt-empty-peg
  [board]
  (println "Here's your board:")
  (print-board board)
  (println "Remove which peg? [e]")
  (prompt-move (remove-peg board (letter->pos (get-input "e")))))

(defn prompt-rows
  []
  (println "How many rows? [5]")
  (let [rows (Integer. (get-input 5))
        board (new-board rows)]
    (prompt-empty-peg board)))

(defn -main
  [& _args]
  (println "Get ready to play peg thing!")
  (prompt-rows))

;; ==================
;; Peg Thing Examples
;; ==================

(take 5 tri) ; (1 3 6 10 15)

(triangular? 5) ; false

(triangular? 6) ; true

(row-tri 1) ; 1
(row-tri 2) ; 3
(row-tri 3) ; 6
(row-tri 4) ; 10
(row-tri 5) ; 15

(row-num 1)  ; 1
(row-num 2)  ; 2
(row-num 3)  ; 2
(row-num 4)  ; 3
(row-num 5)  ; 3
(row-num 6)  ; 3
(row-num 7)  ; 4
(row-num 8)  ; 4
(row-num 9)  ; 4
(row-num 10) ; 4
(row-num 12) ; 5
(row-num 13) ; 5
(row-num 14) ; 5
(row-num 15) ; 5

(connect {} 15 1 2 4) ; {1 {:connections {4 2}}, 4 {:connections {1 2}}}

(assoc-in {} [:cookie :monster :vocals] "Finntroll") ; {:cookie {:monster {:vocals "Finntroll"}}}
(assoc-in {} [1 :connections 4] 2)                   ; {1 {:connections {4 2}}}
(assoc-in {} [1 3 :connections 4] 2)                 ; {1 {3 {:connections {4 2}}}}
(assoc-in {} [1 "three" :connections 4] 2)           ; {1 {"three" {:connections {4 2}}}}

(assoc-in {} [:cookie  :monster  :vocals] "Finntroll")                       ; {:cookie {:monster {:vocals "Finntroll"}}}
(get-in      {:cookie {:monster {:vocals  "Finntroll"}}} [:cookie :monster]) ; {:vocals "Finntroll"}

(connect-down-left {} 15 1)  ; {1 {:connections {4 2}}, 4 {:connections {1 2}}}
(connect-down-left {} 15 6)  ; {6 {:connections {13 9}}, 13 {:connections {6 9}}}

(connect-down-right {} 15 3) ; {3 {:connections {10 6}}, 10 {:connections {3 6}}}
(connect-down-right {} 15 6) ; {6 {:connections {15 10}}, 15 {:connections {6 10}}}

(add-pos {} 15 1)
; {1 {:pegged true, :connections {4 2, 6 3}},
;  4 {:connections {1 2}},
;  6 {:connections {1 3}}}
(add-pos (add-pos {} 15 1) 15 2)
; {1 {:pegged true, :connections {4 2, 6 3}},
;  4 {:connections {1 2}},
;  6 {:connections {1 3}},
;  2 {:pegged true, :connections {7 4, 9 5}},
;  7 {:connections {2 4}},
;  9 {:connections {2 5}}}
(add-pos (add-pos (add-pos {} 15 1) 15 2) 15 3)
; {7 {:connections {2 4}},
;  1 {:pegged true, :connections {4 2, 6 3}},
;  4 {:connections {1 2}},
;  6 {:connections {1 3}},
;  3 {:pegged true, :connections {8 5, 10 6}},
;  2 {:pegged true, :connections {7 4, 9 5}},
;  9 {:connections {2 5}},
;  10 {:connections {3 6}},
;  8 {:connections {3 5}}}
(add-pos (add-pos (add-pos (add-pos {} 15 1) 15 2) 15 3) 15 4)
; {7 {:connections {2 4}},
;  1 {:pegged true, :connections {4 2, 6 3}},
;  4 {:connections {1 2, 6 5, 11 7, 13 8}, :pegged true},   ;; this entry is just overwritten!
;  13 {:connections {4 8}},
;  6 {:connections {1 3, 4 5}},
;  3 {:pegged true, :connections {8 5, 10 6}},
;  2 {:pegged true, :connections {7 4, 9 5}},
;  11 {:connections {4 7}},
;  9 {:connections {2 5}},
;  10 {:connections {3 6}},
;  8 {:connections {3 5}}}

(new-board 5)
;  {1  {:pegged true, :connections {6 3, 4 2}},
;   2  {:pegged true, :connections {9 5, 7 4}},
;   3  {:pegged true, :connections {10 6, 8 5}},
;   4  {:pegged true, :connections {13 8, 11 7, 6 5, 1 2}},
;   5  {:pegged true, :connections {14 9, 12 8}},
;   6  {:pegged true, :connections {15 10, 13 9, 4 5, 1 3}},
;   7  {:pegged true, :connections {9 8, 2 4}},
;   8  {:pegged true, :connections {10 9, 3 5}},
;   9  {:pegged true, :connections {7 8, 2 5}},
;   10 {:pegged true, :connections {8 9, 3 6}},
;   11 {:pegged true, :connections {13 12, 4 7}},
;   12 {:pegged true, :connections {14 13, 5 8}},
;   13 {:pegged true, :connections {15 14, 11 12, 6 9, 4 8}},
;   14 {:pegged true, :connections {12 13, 5 9}},
;   15 {:pegged true, :connections {13 14, 6 10}},
;   :rows 5)

(def my-board (assoc-in (new-board 5) [4 :pegged] false))
(print-board my-board) ; nil
; (out)       a0
; (out)      b0 c0
; (out)    d- e0 f0
; (out)   g0 h0 i0 j0
; (out) k0 l0 m0 n0 o0

(valid-moves my-board 1)  ; {4 2}
(valid-moves my-board 2)  ; {}
(valid-moves my-board 3)  ; {}
(valid-moves my-board 4)  ; {}
(valid-moves my-board 5)  ; {}
(valid-moves my-board 6)  ; {4 5}
(valid-moves my-board 7)  ; {}
(valid-moves my-board 8)  ; {}
(valid-moves my-board 9)  ; {}
(valid-moves my-board 10) ; {}
(valid-moves my-board 11) ; {4 7}
(valid-moves my-board 12) ; {}
(valid-moves my-board 13) ; {4 8}
(valid-moves my-board 14) ; {}
(valid-moves my-board 15) ; {}

(valid-move? my-board 1 4) ; 2
(valid-move? my-board 2 4) ; nil
(valid-move? my-board 6 4) ; 5

(remove-peg (new-board 5) 5)
; {:rows 5,
;  1 {:pegged true, :connections {4 2, 6 3}},
;  2 {:pegged true, :connections {7 4, 9 5}},
;  3 {:pegged true, :connections {8 5, 10 6}},
;  4 {:connections {1 2, 6 5, 11 7, 13 8}, :pegged true},
;  5 {:pegged false, :connections {12 8, 14 9}},             ;; peg removed
;  6 {:connections {1 3, 4 5, 13 9, 15 10}, :pegged true},
;  7 {:connections {2 4, 9 8}, :pegged true},
;  8 {:connections {3 5, 10 9}, :pegged true}}
;  9 {:connections {2 5, 7 8}, :pegged true},
;  10 {:connections {3 6, 8 9}, :pegged true},
;  11 {:connections {4 7, 13 12}, :pegged true},
;  12 {:connections {5 8, 14 13}, :pegged true},
;  13 {:connections {4 8, 6 9, 11 12, 15 14}, :pegged true},
;  14 {:connections {5 9, 12 13}, :pegged true},
;  15 {:connections {6 10, 13 14}, :pegged true},

(row-positions 1) ; (1)
(row-positions 2) ; (2 3)
(row-positions 3) ; (4 5 6)
(row-positions 4) ; (7 8 9 10)
(row-positions 5) ; (11 12 13 14 15)

(render-row my-board 1) ; "      a[34m0[0m"
(render-row my-board 2) ; "     b[34m0[0m c[34m0[0m"
(render-row my-board 3) ; "   d[31m-[0m e[34m0[0m f[34m0[0m"
(render-row my-board 4) ; "  g[34m0[0m h[34m0[0m i[34m0[0m j[34m0[0m"
(render-row my-board 5) ; "k[34m0[0m l[34m0[0m m[34m0[0m n[34m0[0m o[34m0[0m"

(print-board my-board)
; (out)       a0
; (out)      b0 c0
; (out)    d- e0 f0
; (out)   g0 h0 i0 j0
; (out) k0 l0 m0 n0 o0

;; valid move
(make-move my-board 6 4)
; {:rows 5,
;  1 {:pegged true, :connections {4 2, 6 3},
;  2 {:pegged true, :connections {7 4, 9 5}},
;  3 {:pegged true, :connections {8 5, 10 6}},
;  4 {:connections {1 2, 6 5, 11 7, 13 8}, :pegged true},
;  5 {:pegged false, :connections {12 8, 14 9}},
;  6 {:connections {1 3, 4 5, 13 9, 15 10}, :pegged false},
;  7 {:connections {2 4, 9 8}, :pegged true}}
;  8 {:connections {3 5, 10 9}, :pegged true}}
;  9 {:connections {2 5, 7 8}, :pegged true},
;  10 {:connections {3 6, 8 9}, :pegged true},
;  11 {:connections {4 7, 13 12}, :pegged true},
;  12 {:connections {5 8, 14 13}, :pegged true},
;  13 {:connections {4 8, 6 9, 11 12, 15 14}, :pegged true},
;  14 {:connections {5 9, 12 13}, :pegged true},
;  15 {:connections {6 10, 13 14}, :pegged true},

;; invalid move
(make-move my-board 6 5) ; nil

(can-move? my-board) ; {4 2}
(can-move? nil)      ; nil

(count (filter :pegged (vals my-board))) ; 14

(letter->pos "a")  ; 1
(letter->pos "z")  ; 26
(letter->pos "a-") ; 1
(letter->pos "z0") ; 26

(characters-as-strings "a   b")       ; ("a" "b")
(characters-as-strings "a   bc")      ; ("a" "b" "c")
(characters-as-strings "a   b  c  ")  ; ("a" "b" "c")
