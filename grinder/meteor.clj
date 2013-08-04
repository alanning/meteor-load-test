;;
;; Load-testing Meteor apps with The Grinder
;;

(ns meteor-load-test.http
  (:import [net.grinder.script Grinder Test]
           [net.grinder.plugin.http HTTPRequest]
           )
  (:use [meteor-load-test core util])
  )

(let [grinder Grinder/grinder
      test1 (Test. 1 "Retrieve initial payload")
      test2 (Test. 2 "DDP subscriptions")
      test3 (Test. 3 "DDP calls")
      properties (.getProperties grinder)
      ]

  (defn instrumented-get [url]
    (log "Requesting url: " url)
    (.. (HTTPRequest.) (GET url)))

  ;; record calls to the instrumented function
  (.. test1 (record instrumented-get))
  (.. test2 (record subscribe))
  (.. test3 (record call-method))

  (defn get-client-id []
    (str (.getAgentNumber grinder) "-"
         (.getProcessName grinder) "-"
         (.getThreadNumber grinder)))

  (defn get-run-id []
    (.getRunNumber grinder))

  ;; return function that is executed once per thread by each worker process
  (worker-thread-factory
    #(.stopThisWorkerThread grinder) 
    properties
    get-client-id
    get-run-id
    instrumented-get
    ))
