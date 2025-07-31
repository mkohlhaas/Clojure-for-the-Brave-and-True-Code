# Chapter 10: Clojure Metaphysics: Atoms, Refs, Vars, and Cuddle Zombies

[`code.clj`](code.clj) has all the Clojure code from the chapter.

- Atoms

  - compare-and-set semantics
  - `atom`
  - `swap!`
  - `reset!`
  - `@`
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
