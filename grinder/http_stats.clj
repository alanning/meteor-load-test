;;
;; Custom statistics with The Grinder
;;

(ns math.http
  (:import [net.grinder.script Grinder Test]
           [net.grinder.plugin.http HTTPRequest])
  (:use math.test)
  (:require [clj-http.client :as http])
  )
  
(let [grinder Grinder/grinder
      stats (.getStatistics grinder) 
      test (Test. 4 "Custom Stats")]

  (defn log [& text]
    (.. grinder (getLogger) (info (apply str text))))

  ;; utility to return the number operations op in a mathematical expression expr
  (defn count-op [op expr]
    (count (re-seq (re-pattern (str "\\" op)) expr)))
  
  (defn instrumented-get [expr]
    ;; use getForCurrentTest when recording stats within an instrumented function
    ;;(.. stats getForCurrentTest (setLong "userLong0" (count-op '+ expr)))
    (.. (HTTPRequest.) (GET (build-url expr))))

  (.. test (record instrumented-get))

  (defn register-stat [idx op]
    ;; grinder seems to have a problem
    ;; using '* without a leading space
    ;; and using '/ without a trailing space
    (.. stats (registerDataLogExpression
               (str " " op " ") (str "userLong" idx)))
    (.. stats (registerSummaryExpression
               (str " " op " ") (str "userLong" idx))))

  (defn record-stat [expr idx op]
    (.. stats getForLastTest
        (setLong (str "userLong" idx) (count-op op expr))))

  ;; register stats for counting operations
  (register-stat 0 '+)
  (register-stat 1 '-)
  (register-stat 2 '*)
  (register-stat 3 '/)
    
  (fn []
    
    (fn []
          
      ;; request using a recorded function
      (let [expr (random-expr)]
        
        ;; prevent reporting until after the test is called
        (.. stats (setDelayReports true))
        
        (instrumented-get expr)
        
        ;; record the stats
        (record-stat expr 0 '+)
        (record-stat expr 1 '-)
        (record-stat expr 2 '*)
        (record-stat expr 3 '/)
        
        )
      )
    )
  )