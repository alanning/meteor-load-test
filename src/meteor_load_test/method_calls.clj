(ns meteor-load-test.method_calls
  (:use [meteor-load-test util ddp_action]))

(def invalid-calls-msg "Property must be of form: 
     grinder.calls = [<method-call>, <method-call>, etc...]
                 where <method-call> is one of: 
                   * \"method-name\" - ex. [\"keepAlive\"] 
                   * {\"addEntry\":[{\"ownerId\":\"CLIENTID\",\"name\":\"load-RUNID\",\"type\":\"client\"}]} ")

(defn call-method 
  "Calls a Meteor method. Converts args to an Object[] 
   before passing to DDP client"
  ([ddp method-name]
    (log "calling: " method-name)
    (.call ddp method-name (object-array [])))
  ([ddp method-name v]
    (log "calling: " method-name " with args: " v)
    (.call ddp method-name (object-array v))))

(defn perform-calls 
  "Calls meteor methods using supplied fn. Valid elements 
   of seq s include: 
     * string = method name with no parameters
     * map = of form 'method-name':[arg1, arg2, etc.]"
  [sleep call-method-fn s]
  (perform-ddp-action sleep invalid-calls-msg call-method-fn s))

