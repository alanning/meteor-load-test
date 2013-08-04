;;
;; Grinder template
;;

;; declare symbols used in the script
(let [grinder net.grinder.script.Grinder/grinder]
 
  ;; the factory function
  ;; called once by each worker thread
  (fn []
 
    ;; the test runner function
    ;; called once on each test run
    (fn []

      ) ;; end of test runner function
    ) ;; end of factory function
  ) ;; end of script let form