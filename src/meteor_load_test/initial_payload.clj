(ns meteor-load-test.initial_payload)

(declare process-urls)
(declare drop-last-if)

(defn fetch-static-assets 
  "Fetch initial html payload and all referenced assets.
     get-html = function that returns result of HTTPRequest get
     target-url-str = url of system under test in string format"
  [get-html target-url-str]

  ;; make initial http fetch
  (let [initial-html (.getText (get-html target-url-str))]

    (defn- get-relative-url 
      "Gets a url relative to base url. Relative url may
       start with /"
      [base-url relative-url]
      (let [base (drop-last-if '\/ base-url)
            rel  (if (= \/ (first relative-url))
                    relative-url
                    (str \/ relative-url))]
        (get-html (str base rel))))

    ;; make subsequent javascript / css fetches
    (process-urls
      (partial get-relative-url target-url-str)
      initial-html)))

(defn process-urls
  "Executes f for each css link or javascript src in 
   html."
  [f html]
  (let [css (re-seq #"href=\"([^\"]+)\"" html)
        scripts (re-seq #"src=\"([^\"]+)\"" html)
        coll (concat css scripts)]
    (doseq [[_ url] coll]
      (f url))))

(defn drop-last-if 
  "Returns string s without last character c, or s as appropriate"
  [c s]
  (if (= c (last s))
    (apply str (pop (vec s)))
    s))

