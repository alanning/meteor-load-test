;;
;; Sharing tests across threads with The Grinder
;;

(ns math.http
  (:import [net.grinder.script Grinder Test]
           [net.grinder.plugin.http HTTPRequest])
  (:use math.test)
  (:require [clj-http.client :as http])
  )
  
(let [grinder Grinder/grinder
      stats (.getStatistics grinder)
      ;; here we use a custom property to indicate sharing among threads
      shared? (.. grinder getProperties (getBoolean "grinder.shared" false))
      test (Test. 6 "Sharing across threads")
      ;; here we declare an atom for sharing among threads
      test-atom (atom {:test-fn nil :tests (repeat 100 test-operation)})]

  (defn log [& text]
    (.. grinder (getLogger) (info (apply str text))))

  ;; again the arity must match the rebound fn
  ;; and the return value converted
  (defn instrumented-get [url & _]
    {:body (.. (HTTPRequest.) (GET url) getText)})

  (.. test (record instrumented-get))

  ;; a swapping function to setup 
  (defn next-test [{_ :test-fn remaining :tests}]
    {:test-fn (first remaining) :tests (rest remaining)})

  ;; 
  (defn shared-tests [test-atom]
    (loop [{current-test :test-fn remaining-tests :tests}
           (swap! test-atom next-test)]
      (if (and (empty? remaining-tests) (nil? current-test))
        nil
        (do (when-not (nil? current-test) (current-test))
            (recur (swap! test-atom next-test))))))

  (fn []

    (fn []

      ;; request using a recorded function
      (binding [wrapped-get instrumented-get]

          (if shared?
            (shared-tests test-atom)
            (doall (map #(%) (:tests @test-atom))))
        
        )
      )
    )
  )