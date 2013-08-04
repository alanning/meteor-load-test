(ns meteor-load-test.util-test
  (:require [clojure.test :refer :all]
            [conjure.core :refer :all]
            [meteor-load-test.util :refer :all]))

(deftest test-replace-words
  (testing "Test replace words"
    (let [m {"CLIENTID" "client1", "RUNID" "run1"}
          s "My clientid is 'CLIENTID'. My runid is 'RUNID'."
          m2 {"CLIENTID" "0-156.122.171.108.client.dyn.strong-in125.as13926.net-16-0", "RUNID" "5"}
          s2 "[\"addEntry\", {\"addEntry\":[{\"ownerId\":\"CLIENTID\",\"name\":\"load-RUNID\",\"type\":\"client\"}]}]"
          expected1 "My clientid is 'client1'. My runid is 'run1'."
          expected2 "[\"addEntry\", {\"addEntry\":[{\"ownerId\":\"0-156.122.171.108.client.dyn.strong-in125.as13926.net-16-0\",\"name\":\"load-5\",\"type\":\"client\"}]}]"
      ]
      (is (= expected1 (replace-words s m)))
      (is (= expected2 (replace-words s2 m2)))
      )))

(deftest test-perform-calls
  (defn do-call 
    ([method-name])
    ([method-name params]))
  (let [call-with-args {"addEntry" [{"ownerId" "client1"
                                     "name" "load-1"
                                     "type" "client"}]}
        calls ["no-args" call-with-args]]
    (mocking [do-call log]
      (perform-calls do-call calls)
      (verify-call-times-for do-call 2)
      (verify-first-call-args-for do-call "no-args")
      (verify-nth-call-args-for 2 do-call "addEntry" (get call-with-args "addEntry")))))

(deftest test-ensure-uri-string
  (testing "ensure-uri with string param"
    (is (= java.net.URI (type (ensure-uri "http://example.com/"))))))

(deftest test-ensure-uri-class
  (testing "ensure-uri with class param"
    (is (= java.net.URI (type (ensure-uri (java.net.URI/create "http://example.com/")))))))

(deftest test-drop-last-if
  (testing "drop-last-if"
    (let [s1 "/client/load-test.css?51243234"
          s2 "/client/load-test.css?51243234/"
          s3 "http://localhost:3000"
          s4 "http://localhost:3000/"]
      (is (= s1 (drop-last-if '\/ s1)))
      (is (= s1 (drop-last-if '\/ s2)))
      (is (= s3 (drop-last-if '\/ s3)))
      (is (= s3 (drop-last-if '\/ s4))))))


(def html "<head>
  <link rel=\"stylesheet\" href=\"/client/load-test.css?c946c3d657a4acb7b5d72e3ad90c123dc170eb80\">

<script type=\"text/javascript\">
__meteor_runtime_config__ = {\"meteorRelease\":\"0.6.4.1\",\"ROOT_URL\":\"http://localhost:3000\",\"serverId\":\"7oY9wH5mjBf5rTd7Q\"};
</script>

  <script type=\"text/javascript\" src=\"/packages/underscore/underscore.js?ed2d2b960c0e746b3e4f9282d5de66ef7b1a2b4d\"></script>
  <script type=\"text/javascript\" src=\"/packages/meteor/client_environment.js?07a7cfbe7a2389cf9855c7db833f12202a656c6b\"></script>
  <script type=\"text/javascript\" src=\"/packages/meteor/helpers.js?2968aa157e0a16667da224b8aa48edb17fbccf7c\"></script>

<title>load-test</title>
</head>")

(deftest test-process-urls
  (defn f [method-name])
  (mocking [f]
    (process-urls f html)
    (verify-call-times-for f 4)
    (verify-first-call-args-for f "/client/load-test.css?c946c3d657a4acb7b5d72e3ad90c123dc170eb80")
    (verify-nth-call-args-for 2 f "/packages/underscore/underscore.js?ed2d2b960c0e746b3e4f9282d5de66ef7b1a2b4d")
    (verify-nth-call-args-for 3 f "/packages/meteor/client_environment.js?07a7cfbe7a2389cf9855c7db833f12202a656c6b")
    (verify-nth-call-args-for 4 f "/packages/meteor/helpers.js?2968aa157e0a16667da224b8aa48edb17fbccf7c")))
