(ns meteor-load-test.method_calls_test
  (:require [clojure.test :refer :all]
            [conjure.core :refer :all]
            [meteor-load-test.util :refer :all]
            [meteor-load-test.method_calls :refer :all]))

(deftest test-perform-calls
  (defn do-call 
    ([method-name])
    ([method-name params]))
  (let [call-with-args {"addEntry" [{"ownerId" "client1"
                                     "name" "load-1"
                                     "type" "client"}]}
        calls ["no-args" call-with-args]]
    (mocking [do-call log]
      (perform-calls nil do-call calls)
      (verify-call-times-for do-call 2)
      (verify-first-call-args-for do-call "no-args")
      (verify-nth-call-args-for 2 do-call "addEntry" (get call-with-args "addEntry")))))

