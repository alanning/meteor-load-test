;;
;; Custom logging with The Grinder
;;

(ns math.http
  (:import [net.grinder.script Grinder Test]
           [net.grinder.plugin.http HTTPRequest])
  (:use math.test)
  (:require [clj-http.client :as http])
  )
  
;; declare symbols used by the script
(let [grinder Grinder/grinder
      test (Test. 1 "HTTP")]

  ;; utility function for logging
  (defn log [& text]
    (.. grinder (getLogger) (output (apply str text))))

  ;; function that we can record
  (defn instrumented-get [url]
    (.. (HTTPRequest.) (GET url)))
  
  ;; record calls to the get function
  (.. test (record instrumented-get))
  
  ;; the factory function
  ;; called once by each worker thread
  (fn []
 
    ;; the test runner function
    ;; called on each run
    (fn []

      ;; request using a recorded function
      (instrumented-get (build-url (random-operation)))     

      ) ;; end of test runner function
    ) ;; end of factory function
  ) ;; end of script let form