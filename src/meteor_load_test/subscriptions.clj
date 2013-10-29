(ns meteor-load-test.subscriptions
  (:use [meteor-load-test util ddp_action]))

(def invalid-subscription-msg 
  "Property must be of form: 
     grinder.subscriptions = [<subscription>, <subscription>, etc...]
                 where <subscription> is one of: 
                   * \"collection-name\" - ex. [\"entries\"] 
                   * {\"collection-name\":[\"param1\",2,\"param3\"]} ")

(defn subscribe 
  "Subscribes to a Meteor collection. Converts args to 
   an Object[] before passing to DDP client"
  ([ddp client-id collection-name]
    (log client-id " subscribing to: " collection-name)
    (.subscribe ddp collection-name (object-array [])))
  ([ddp client-id collection-name v]
    (log client-id " subscribing to: " collection-name v)
    (.subscribe ddp collection-name (object-array v))))

(defn perform-subscriptions
  "Subscribes to collections specified in s. Elements of s
   should be of the form:
     * string - collection name with no parameters
     * map - solleciton name with parameters of form 
             'method-name':[arg1, arg2, etc.]"
  [ddp client-id s]
  (let [do-action (partial subscribe ddp client-id)]
    (perform-ddp-action invalid-subscription-msg do-action s)))

