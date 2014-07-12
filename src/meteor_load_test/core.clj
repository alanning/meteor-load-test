(ns meteor-load-test.core
  (:require [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.net URI]
           [com.keysolutions.ddpclient DDPClient])
  (:use [meteor-load-test util method_calls subscriptions initial_payload])
  )

(declare test-runner-factory)
(def ddp-connected
  com.keysolutions.ddpclient.DDPClient$CONNSTATE/Connected)

(defn worker-thread-factory 
  "Returns an anonymous function which is called once by each 
   worker thread and which returns a test runner function. 
   Responsible for: initial payload get, ddp connection, and 
   subscriptions"
  [stop-fn sleep-fn propertyBag get-client-id get-run-id get-html]

  (let [target-url-str (.getProperty propertyBag "grinder.targetUrl")
        users (get-json-property propertyBag "grinder.users")
        resume-tokens (.getProperty propertyBag "grinder.resumeTokens")
        call-delay-ms (if-let [delay-ms (.getProperty propertyBag "grinder.callDelayMs")]
                        (Integer. delay-ms)
                        0)
        calls-raw (.getProperty propertyBag "grinder.calls")
        subscriptions (get-json-property propertyBag "grinder.subscriptions")
        download-payload? (.getProperty propertyBag "grinder.downloadPayload")
        debug? (.getBoolean propertyBag "grinder.debug" false)]

    (fn []

      (when (empty? target-url-str)
        (log "Missing required setting 'grinder.targetUrl'")
        (stop-fn))

      (def targetUrl (URI/create target-url-str))

      (when debug?
        (log "grinder.targetUrl: " target-url-str)
        (log "grinder.downloadPayload?: " download-payload?)
        (log "grinder.users: " users)
        (log "grinder.resumeTokens: " resume-tokens)
        (log "grinder.subscriptions: " subscriptions)
        (log "grinder.calls: " calls-raw)
        (log "host: " (.getHost targetUrl) ", port: " (get-port targetUrl)))

      (let [ddp (DDPClient. (.getHost targetUrl) 
                            (get-port targetUrl) 
                            (isSSL targetUrl))
            client-id (get-client-id)
           ]

        (when debug?
          (log "client id: " client-id))

        ;; download initial html payload and all referenced files
        (when download-payload?
          (fetch-static-assets get-html target-url-str))

        ;(if debug? (.addObserver ddp (SimpleDdpClientObserver.)))
        
        ;; connect ddp client
        (.connect ddp)

        ;; wait for the websocket to connect and handshake
        (loop [retries 5]
          (try
            (Thread/sleep 1000);
            (catch InterruptedException e))
          (when (and (pos? retries) (not= ddp-connected (.getState ddp)))
            (log "Waiting for websocket connection to handshake")
            (recur (dec retries))))

        (when-not (= ddp-connected (.getState ddp))
          (throw (ex-info "Websocket connection failed to handshake" {})))

        ;; perform login via random user info, if provided
        (cond
          (not-empty users)
            (let [credentials (first (rand-nth users))]
              (when debug?
                (log "logging in with user credentials " credentials)) 
              (call-method ddp "login" [{"user" {"email" (key credentials)} "password" (val credentials)}]))
          (not-empty resume-tokens)
            (let [tokens (clojure.string/split resume-tokens #",")
                  resume-token (rand-nth tokens)]
              (when debug?
                (log "logging in with resume token " resume-token))
              (call-method ddp "login" [{"resume" resume-token}])))

        ;; initiate subscriptions
        (perform-subscriptions ddp client-id subscriptions)

        ;; return function that will be executed for each test run
        (let [sleep #(sleep-fn call-delay-ms)]
          (test-runner-factory sleep client-id get-run-id (partial call-method ddp) calls-raw))
        
      )  ; let ddp-client, id
    )  ; returned fn
  )  ; let
)  ; worker-thread


(defn test-runner-factory
  "Returns an anonymous function which is run by each worker thread."
  [sleep client-id get-run-id call-method-fn calls-raw]
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

        (perform-calls sleep call-method-fn calls)

        ))  ; non-empty calls

    )  ; returned anonymous fn
  )  ; test-runner-factory  
