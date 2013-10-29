(ns meteor-load-test.initial_payload_test
  (:require [clojure.test :refer :all]
            [conjure.core :refer :all]
            [meteor-load-test.initial_payload :refer :all]))

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
  <link href=\"/client/load-test2.css\" rel=\"stylesheet\">
  <link href=\"/client/load-test3.css\" type=\"text/css\" rel=\"stylesheet\">
  <link rel=\"apple-touch-icon-precomposed\" href=\"appicon-60.png\">

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
    (verify-call-times-for f 7)
    (verify-first-call-args-for f "/client/load-test.css?c946c3d657a4acb7b5d72e3ad90c123dc170eb80")
    (verify-nth-call-args-for 2 f "/client/load-test2.css")
    (verify-nth-call-args-for 3 f "/client/load-test3.css")
    (verify-nth-call-args-for 4 f "appicon-60.png")
    (verify-nth-call-args-for 5 f "/packages/underscore/underscore.js?ed2d2b960c0e746b3e4f9282d5de66ef7b1a2b4d")
    (verify-nth-call-args-for 6 f "/packages/meteor/client_environment.js?07a7cfbe7a2389cf9855c7db833f12202a656c6b")
    (verify-nth-call-args-for 7 f "/packages/meteor/helpers.js?2968aa157e0a16667da224b8aa48edb17fbccf7c")))

