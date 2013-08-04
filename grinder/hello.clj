;;
;; Saying hello with The Grinder
;;

(import '(net.grinder.script Grinder))
  
;; declare symbols used by the script
(let [grinder Grinder/grinder]

  ;; utility function for logging
  (defn log [& text]
    (.. grinder (getLogger) (info (apply str text))))
  
  ;; the factory function
  ;; called once by each worker thread
  (fn []
 
    ;; the test runner function
    ;; called on each run
    (fn []
      
      ;; say hello
      (log "Hello World!")

      ) ;; end of test runner function
    ) ;; end of factory function
  ) ;; end of script let form