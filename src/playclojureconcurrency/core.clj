(ns playclojureconcurrency.core)

;; Threads and Atoms
;;
;; Atoms are for uncoordinated access from multiple threads.
;; The primary functions for using them are reset! and swap!
;; reset! brute-forces a new value.
;; swap! sets a new value using a function that takes the old value.
;;
;; Load this file into a repl, kick off the threads, and watch the state.
;; You might also fire up jconsole and watch threads and cpu usage.
;; When you are done cooking, reset! the continueflag to false and
;; watch the threads wrap up.

(def continueflag
  "This will be our signal for our looping threads to die."
  (atom true))

(def mystate
  "An atom to hold state that we will observe, changed in another thread.
   We could put anything in this atom.  I'm putting two things: 
   a function to use, and a value to use that function.  inc will return
   the next integer.  We could swap it with dec or inc 2."
  (atom  {:f inc
          :val 0}))

(defn beat
  "A fn that will be used to update the state, called from the thread.
   The f is a function we can change that is used on val to calc 
   the next value.  "
  [{f :f val :val}]
  (println "beat " val)
  {:f f :val (f val)})

(defn heartbeat
  "This is the fn that will be run in the thread. 
   It loops, checking the atom continueflag."
  []
  (while @continueflag
    (swap! mystate beat))
  (println "Terminating."))

(defn upbeat
  "To be called from a thread.  Increments the value."
  []
  (let [f #(assoc % :val (inc (:val %)))]
    (while @continueflag
      (swap! mystate f))
    (println "Terminating.")))

(defn downbeat
  "To be called from a thread.  Decrements the value."
  []
  (let [f #(assoc % :val (dec (:val %)))]
    (while @continueflag
      (swap! mystate f))
    (println "Terminating.")))

(def upthread
  "Create a thread to increment the value."
  (Thread. upbeat))

(def downthread
  "Create a thread to decrement the value."
  (Thread. downbeat))

(def mythread
  "Creates the thread, but does not start it."
  (Thread. heartbeat))

;(.start upthread)
;(.start downthread)
;(.start mythread)
;(reset! continueflag false) ; signal to exit loop 
