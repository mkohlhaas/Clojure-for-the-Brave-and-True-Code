# Chapter 10: Clojure Metaphysics: Atoms, Refs, Vars, and Cuddle Zombies

[`code.clj`](code.clj) has all the Clojure code from the chapter.

- Atoms

  - compare-and-set semantics
  - dereferencing an atom (or any other reference type) will never block
  - Summary: `atom`, `swap!`, `reset!`, `@`

- Watches

  - (`add-watch` ref key watch-fn)
  - (watch-fn key watched-atom old-state new-state)
  - Summary: `add-watch`

- Validators

  - attach a validator during atom creation:
  - (`atom` value `:validator` validator-fn)
  - (validator-fn value) returns bool or throws exception
  - Summary: during `atom` creation

- Refs

  - Refs allow you to update the state of multiple identities using transaction semantics.
  - Transactions are:
    - atomic
    - consistent
    - isolated
  - Clojure implements refs with software transactional memory (STM).
  - Creating a ref: (`ref` value)
  - Deref: `@`
  - `dosync` starts a transaction
  - (`alter` red update-fn) changes ref inside a transaction
  - `commute` also changes ref inside a transaction but doesn't retry on different before and after values
  - -> `commute` doesn’t ever force a transaction retry
  - Summary: `ref`, `dosync`, `alter`, `commute`, `@`

- Vars

  - Created with `^:dynamic` and earmuffs `*`
    - (def `^:dynamic` `*notification-address*` "dobby@elf.org")
  - Change temporarily with `binding`.
  - Change with `set!`
  - `#'x` is reader macro for (var x)
  - `alter-var-root`
  - temporarily alter a var’s root `with-redefs`
    - changes will be visible in all threads
  - Summary: `binding`, `set!`, `alter-var-root`, `with-redefs`, `#'`
