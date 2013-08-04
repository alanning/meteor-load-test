;;
;; Using HTTP with The Grinder
;;

(ns math.http
  (:import [net.grinder.script Grinder])
  (:use math.test)
  (:require [clj-http.client :as http])
  )
  
;; declare symbols used by the script
(let [grinder Grinder/grinder]

  ;; utility function for logging
  (defn log [& text]
    (.. grinder (getLogger) (info (apply str text))))
  
  ;; the factory function
  ;; called once by each worker thread
  (fn []
 
    ;; the test runner function
    ;; called on each run
    (fn []

      ;; log the output of a random math request
      (let [op (random-expr)]    
        (log op " = " (:body (http/get (build-url op)))))

      ) ;; end of test runner function
    ) ;; end of factory function
  ) ;; end of script let form