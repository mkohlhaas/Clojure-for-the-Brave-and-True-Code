# Chapter 9: The Sacred Art of Concurrent and Parallel Programming

[`code.clj`](code.clj) has all the code from the chapter.

- `future` creates a new thread

  - runs only once and caches result
  - doesn't block
  - use `realized?` to check if future has finished
  - future’s result value is the value of the last expression evaluated in its body
  - returns a reference to result value
    - get result with `deref`-function or `@`-reader macro
    - dereferencing blocks
    - you can use a timeout and default value when using `deref`
    - result is cached
  - if `future` is used only for side-effects you can forgo dereferencing
  - Summary: `future`, `realized?`, `deref`, `@`

- `delay` defines a task without immediately running it

  - runs only once and caches result
  - to run the task dereference it or `force` it
  - Summary: `delay`, `force`, `deref`, `@`

- `promise` expresses a result without to define a task
  - `deliver` the result to the promise
  - obtain result by dereferencing
    - blocks if no result has been delivered
  - Summary: `promise`, `deliver`, `deref`, `@`
