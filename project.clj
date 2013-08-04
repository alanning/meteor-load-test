(defproject meteor-load-test "0.5.0"
  :description "Meteor load testing tool"
  :min-lein-version "2.0.0"
  :url "https://github.com/alanning/meteor-load-test"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-localrepo "0.5.2"]]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.2.2"]
                 [com.google.code.gson/gson "2.2.2"]
                 [local/java-ddp-client "0.5.7"]
                 ]
  :profiles {:dev
             {:dependencies 
              [[clj-http "0.2.7"]
               [clj-stacktrace "0.2.4"]
               [org.clojars.runa/conjure "2.1.3"]
               [net.sf.grinder/grinder "3.11"]]
             }}
  :repositories {"sonatype" "https://oss.sonatype.org/content/repositories/releases/"}
  )
