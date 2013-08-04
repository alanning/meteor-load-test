;;
;; A simple test with The Grinder
;;

(import '(net.grinder.script Grinder Test))

;; declare symbols used by the script
(let [grinder Grinder/grinder
      test (Test. 1 "Logging")]

  ;; utility function for logging
  (defn log [& text]
    (.. grinder (getLogger) (info (apply str text))))

  ;; record calls to the logging function
  (.. test (record log))
  
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