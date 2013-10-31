(ns meteor-load-test.ddp_action
  (:use [meteor-load-test util]))

(defn perform-ddp-action
  "Calls function f once for each element in vector v with
   delay if supplied. f is a function of 1 or 2 arguments.
   v should contain elements of the following form:
     * string - interpreted as method name or subscription 
                with no parameters
     * map - interpreted as method name or subscription 
             with parameter array.
             ex. 'method-name':[arg1, arg2, etc.]"
  [sleep invalid-msg f v]
  (doseq [item v] 
    (when sleep
      ;(Thread/sleep sleep-ms))
      (sleep))
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

