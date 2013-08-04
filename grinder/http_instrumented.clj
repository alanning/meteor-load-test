;;
;; Instrumenting HTTP with The Grinder
;;

(ns math.http
  (:import [net.grinder.script Grinder Test]
           [net.grinder.plugin.http HTTPRequest])
  (:use math.test)
  (:require [clj-http.client :as http])
  )
  
(let [grinder Grinder/grinder
      test (Test. 3 "HTTP Instrumented")]

  (defn log [& text]
    (.. grinder (getLogger) (info (apply str text))))

  ;; function that we can record
  (defn instrumented-get [url]
    (.. (HTTPRequest.) (GET url)))
  
  ;; record calls to the instrumented function
  (.. test (record instrumented-get))
  
  (fn []
 
    (fn []

      ;; request using a recorded function
      (instrumented-get (build-url (random-expr)))

      ;; request errors
      (instrumented-get (build-url "/err/401"))
      (instrumented-get (build-url "/err/500"))
      
      )
    )
  )