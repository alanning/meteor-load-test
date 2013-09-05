(ns meteor-load-test.util
  (:require [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.net URI])
  )

(defn log [& text]
  (.. System out (println (apply str text))))

(declare subscribe)
(declare perform-ddp-action)

(def invalid-subscription-msg 
  "Property must be of form: 
     grinder.subscriptions = [<subscription>, <subscription>, etc...]
                 where <subscription> is one of: 
                   * \"collection-name\" - ex. [\"entries\"] 
                   * {\"collection-name\":[\"param1\",2,\"param3\"]} ")

(defn perform-subscriptions
  "Subscribes to collections specified in s. Elements of s
   should be of the form:
     * string - collection name with no parameters
     * map - solleciton name with parameters of form 
             'method-name':[arg1, arg2, etc.]"
  [ddp client-id s]
  (let [do-action (partial subscribe ddp client-id)]
    (perform-ddp-action invalid-subscription-msg do-action s)))


(defn make-keywords [client-id run-id]
  {"CLIENTID" client-id, "RUNID" (str run-id)})

(defn replace-words 
  "Replaces all occurances of words in sentence based 
   on replacement-map"
  [s replacement-map]
  (reduce (fn [sentence [match replacement]]
            (str/replace sentence match replacement)) s replacement-map))

(declare ensure-uri)

(defn get-port 
  "Accepts a string or java.net.URI. If URI port not
   specified, returns port based on Scheme/Protocol."
  [targetUri]
  (let [port (.getPort (ensure-uri targetUri))]
    (cond 
      (= -1 port) 
        (cond
           (= "https" (.getScheme targetUri)) (int 443)
           :else (int 80))
      :else (int port))))

(defn get-json-property
  "Get JSON from a Java property bag. Returns nil 
   if property doesn't exist"
  [bag name]
  (let [prop (.getProperty bag name)]
    (if (empty? prop)
      nil
      (json/read-str prop))))

(defn call-method 
  "Calls a Meteor method. Converts args to an Object[] 
   before passing to DDP client"
  ([ddp method-name]
    (log "calling: " method-name)
    (.call ddp method-name (object-array [])))
  ([ddp method-name v]
    (log "calling: " method-name " with args: " v)
    (.call ddp method-name (object-array v))))


(def invalid-calls-msg "Property must be of form: 
     grinder.calls = [<method-call>, <method-call>, etc...]
                 where <method-call> is one of: 
                   * \"method-name\" - ex. [\"keepAlive\"] 
                   * {\"addEntry\":[{\"ownerId\":\"CLIENTID\",\"name\":\"load-RUNID\",\"type\":\"client\"}]} ")

(defn perform-calls 
  "Calls meteor methods using supplied fn. Valid elements 
   of seq s include: 
     * string = method name with no parameters
     * map = of form 'method-name':[arg1, arg2, etc.]"
  [do-call s]
  (perform-ddp-action invalid-calls-msg do-call s))


(defn- perform-ddp-action
  "Calls function f once for each element in vector v. 
   f is a function of 1 or 2 arguments. v should contain 
   elements of the following form:
     * string - interpreted as method name or subscription 
                with no parameters
     * map - interpreted as method name or subscription 
             with parameter array.
             ex. 'method-name':[arg1, arg2, etc.]"
  [invalid-msg f v]
  (doseq [item v] 
    (cond
      (map? item)
        (doseq [[method-name params] item]
          (if (vector? params)
            (f method-name params)
            (log invalid-msg)))
      (string? item)
        (f item)
      :else 
        (log "unsupported: " item))))

(defn ensure-uri [targetUri]
  (cond 
    (string? targetUri) (URI/create targetUri)
    (= java.net.URI (type targetUri)) targetUri
    :else (throw (Exception. "Unsupported argument type"))))

(defn subscribe 
  "Subscribes to a Meteor collection. Converts args to 
   an Object[] before passing to DDP client"
  ([ddp client-id collection-name]
    (log client-id " subscribing to: " collection-name)
    (.subscribe ddp collection-name (object-array [])))
  ([ddp client-id collection-name v]
    (log client-id " subscribing to: " collection-name v)
    (.subscribe ddp collection-name (object-array v))))

(defn drop-last-if 
  "Returns s without last character c, or s as appropriate"
  [c s]
  (if (= c (last s))
    (apply str (pop (vec s)))
    s))

(defn process-urls
  "Executes f for each css link or javascript src in 
   html."
  [f html]
  (let [css (re-seq #"href=\"([^\"]+)\"" html)
        scripts (re-seq #"src=\"([^\"]+)\"" html)
        coll (concat css scripts)]
    (doseq [[_ url] coll]
      (f url))))
