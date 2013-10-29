(ns meteor-load-test.core
  (:require [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.net URI]
           [com.keysolutions.ddpclient DDPClient])
  (:use [meteor-load-test util method_calls subscriptions initial_payload])
  )

(declare test-runner-factory)

(defn worker-thread-factory 
  "Returns an anonymous function which is called once by each 
   worker thread and which returns a test runner function. 
   Responsible for: initial payload get, ddp connection, and 
   subscriptions"
  [stop-thread propertyBag get-client-id get-run-id get-html]

  (let [target-url-str (.getProperty propertyBag "grinder.targetUrl")
        resume-tokens (.getProperty propertyBag "grinder.resumeTokens")
        calls-raw (.getProperty propertyBag "grinder.calls")
        subscriptions (get-json-property propertyBag "grinder.subscriptions")
        simulate-payload? (.getProperty propertyBag "grinder.simulatePayload")
        debug? (.getBoolean propertyBag "grinder.debug" false)]

    (fn []

      (when (empty? target-url-str)
        (log "Missing required setting 'grinder.targetUrl'")
        (stop-thread))

      (def targetUrl (URI/create target-url-str))

      (when debug?
        (log "grinder.targetUrl: " target-url-str)
        (log "grinder.resumeTokens: " resume-tokens)
        (log "grinder.calls: " calls-raw)
        (log "grinder.subscriptions: " subscriptions)
        (log "host: " (.getHost targetUrl) ", port: " (get-port targetUrl)))

      (let [ddp (DDPClient. (.getHost targetUrl) (get-port targetUrl))
            client-id (get-client-id)
           ]

        (when debug?
          (log "client id: " client-id))

        ;; download initial html payload and all referenced files
        (when simulate-payload?
          (fetch-static-assets get-html target-url-str))

        ;(if debug? (.addObserver ddp (SimpleDdpClientObserver.)))
        
        ;; connect ddp client
        (.connect ddp)

        ;; perform login via random resume token, if provided
        (when resume-tokens
          (let [tokens (clojure.string/split resume-tokens #",")
                resume-token (rand-nth tokens)]
            (when debug?
              (log "logging in with resume token" resume-token))
            (call-method ddp "login" [{"resume" resume-token}])))

        ;; initiate subscriptions
        (perform-subscriptions ddp client-id subscriptions)

        ;; return function that will be executed for each test run
        (test-runner-factory client-id get-run-id (partial call-method ddp) calls-raw)
      )  ; let ddp-client, id
    )  ; returned fn
  )  ; let
)  ; worker-thread


(defn test-runner-factory
  "Returns an anonymous function which is run by each worker thread."
  [client-id get-run-id do-call calls-raw]
  (fn []

    (comment
      (log "test run: " (str client-id "-" (get-run-id))))

    (if (empty? calls-raw)
      (log "No DDP calls to perform")
      (let [run-id (get-run-id)
            entry-name (str "load-" run-id)
            keywords (make-keywords client-id run-id)
            calls (-> calls-raw
                     (replace-words keywords)
                     json/read-str)]

        (comment
          (log "keywords " keywords)
          (log "run-id " run-id)
          (log "entry-name " entry-name)
          (log "calls " calls))

        (perform-calls do-call calls)

        ))  ; non-empty calls

    )  ; returned anonymous fn
  )  ; test-runner-factory  
