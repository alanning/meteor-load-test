(ns meteor-load-test.util_test
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
      (is (= expected2 (replace-words s2 m2))))))

(deftest test-ensure-uri-string
  (testing "ensure-uri with string param"
    (is (= java.net.URI (type (ensure-uri "http://example.com/"))))))

(deftest test-ensure-uri-class
  (testing "ensure-uri with class param"
    (is (= java.net.URI (type (ensure-uri (java.net.URI/create "http://example.com/")))))))

(deftest test-get-port
  (testing "get-port"
    (testing "when port specified"
      (is (= (get-port (ensure-uri "http://example.com:8080/")) 8080))
      (is (= (get-port (ensure-uri "http://example.com:3769")) 3769))
      (is (= (get-port (ensure-uri "http://example.com:443")) 443)))
    (testing "when port not specified"
      (is (= (get-port (ensure-uri "http://example.com/")) 80))
      (is (= (get-port (ensure-uri "https://example.com")) 443)))))

(deftest test-isSSL
  (testing "isSSL"
    (testing "when port specified"
      (is (= (isSSL (ensure-uri "http://example.com:8080/")) false))
      (is (= (isSSL (ensure-uri "http://example.com:3769")) false))
      (is (= (isSSL (ensure-uri "http://example.com:443")) false))
      (is (= (isSSL (ensure-uri "https://example.com:443")) true)))
    (testing "when port not specified"
      (is (= (isSSL (ensure-uri "http://example.com/")) false))
      (is (= (isSSL (ensure-uri "https://example.com")) true)))))
