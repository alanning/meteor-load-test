(ns meteor-load-test.util
  (:require [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.net URI])
  )

(defn log [& text]
  (.. System out (println (apply str text))))

(defn make-keywords [client-id run-id]
  {"CLIENTID" client-id, "RUNID" (str run-id)})

(defn replace-words 
  "Replaces all occurances of words in sentence based 
   on replacement-map"
  [s replacement-map]
  (reduce (fn [sentence [match replacement]]
            (str/replace sentence match replacement)) s replacement-map))

(defn ensure-uri [targetUri]
  (cond 
    (string? targetUri) (URI/create targetUri)
    (= java.net.URI (type targetUri)) targetUri
    :else (throw (Exception. "Unsupported argument type"))))

(defn isSSL 
  "True if target url uses https, false otherwise"
  [url]
  (=  (.getScheme url) "https"))

(defn get-port 
  "Accepts a string or java.net.URI. If URI port not
   specified, returns port based on Scheme/Protocol."
  [targetUri]
  (let [port (.getPort (ensure-uri targetUri))]
    (cond 
      (= -1 port) 
        (cond
           (isSSL targetUri) (int 443)
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

