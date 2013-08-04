;;
;; Recording HTTP with The Grinder
;;

(ns meteor-load-test.core
  (:import [net.grinder.script Grinder Test]
           [net.grinder.plugin.http HTTPRequest]
           [me.kutrumbos DdpClient]
           [me.kutrumbos.examples SimpleDdpClientObserver])
  (:require [clj-http.client :as http]))
            ;[cemerick.url :as url]))

(let [grinder Grinder/grinder
      test1 (Test. 1 "Retrieve initial payload")
      test2 (Test. 2 "DDP object")
      ;properties (.getProperties grinder)
      ;targetUrl (url (.getProperty properties "targetUrl"))
      ]

  (defn log [& text]
    (.. grinder (getLogger) (info (apply str text))))

  (defn get-port [targetUrl]
    (let [port (:port targetUrl)]
      (cond 
        (= -1 port) 
          (cond
            (= "http" (:protocol targetUrl)) (int 80)
            :else (int 443))
        :else (int port))))

  ;; function that we can record
  (defn instrumented-get [url]
    (.. (HTTPRequest.) (GET url)))
  
  ;; record calls to the instrumented function
  (.. test1 (record instrumented-get))
  
  (fn []
 
    (fn []

      ;; simulate initial http fetch
      (instrumented-get "http://localhost:3000/")
      ;(http/get "http://localhost:3000")
      ;(http/get targetUrl)

      ;; simulate subscription and submission
      ;(let [ddp (DdpClient. (:host targetUrl) (get-port targetUrl))
      (let [ddp (DdpClient. "localhost" (int 3000))
            ;obs (SimpleDdpClientObserver.)
           ]
        ;(.addObserver ddp obs)
        (.connect ddp)
        (.sleep grinder 1000)
        ;(Thread/sleep 1000)
        (.subscribe ddp "entries" (object-array []))

        (.. test2 (record ddp))
        (.call ddp "addEntry" (object-array []))
        ;; ensure we receive other updates over time
        ;(Thread/sleep 10000)
        (.sleep grinder 10000)
        (.unsubscribe ddp "entries")
        )
      )
    )
  )
