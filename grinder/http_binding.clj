;;
;; Using rebinding and reporting with The Grinder
;;

(ns math.http
  (:import [net.grinder.script Grinder Test]
           [net.grinder.plugin.http HTTPRequest])
  (:use math.test)
  (:require [clj-http.client :as http])
  )
  
(let [grinder Grinder/grinder
      stats (.getStatistics grinder)
      test (Test. 5 "Rebinding and reporting")]

  (defn log [& text]
    (.. grinder (getLogger) (info (apply str text))))

  ;; if testing reports an error, let the grinder know about it
  (defn report [event]
    (when-not (= (:type event) :pass)
      (log event)
      (.. stats getForLastTest (setSuccess false))))

  ;; the arity of the instrumented fn changes to match the rebound fn
  ;; the return value of HTTPRequest must be converted as well
  (defn instrumented-get [url & _]
    (let [resp (.. (HTTPRequest.) (GET url))]
      {:body (.getText resp)
       :status (.getStatusCode resp)}))
  
  (.. test (record instrumented-get))
  
  (fn []
 
    (fn []

      ;; rebind the http/get fn to our instrumented fn
      ;; rebind test reporting to capture errors
      (binding [wrapped-get instrumented-get
                clojure.test/report report]
        ;; delay grinder reporting for test reporting to work
        (.setDelayReports stats true)
        (test-operation)
        (test-error))

      )
    )
  )