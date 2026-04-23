(ns hyper-active-search.app
  (:require [clojure.string :as str]
            [hyper.core :as h]))


(def names
  ["Ada Lovelace"
   "Alan Turing"
   "Annie Easley"
   "Barbara Liskov"
   "Claude Shannon"
   "Donald Knuth"
   "Edsger Dijkstra"
   "Frances Allen"
   "Grace Hopper"
   "Hedy Lamarr"
   "John McCarthy"
   "Katherine Johnson"
   "Margaret Hamilton"
   "Radia Perlman"
   "Sister Mary Kenneth Keller"])


(def min-apply-delay-ms 1200)
(def max-apply-delay-ms 3000)
(def default-port 8080)


(defn rand-delay-ms
  []
  (+ min-apply-delay-ms
    (rand-int (inc (- max-apply-delay-ms min-apply-delay-ms)))))



(def search-input-style "width: min(32rem, 100%);")


(defn filter-names
  "Filter the demo name list by a case-insensitive substring match."
  [query]
  (let [needle (str/lower-case (str/trim (str query)))]
    (if (seq needle)
      (filterv #(str/includes? (str/lower-case %) needle) names)
      names)))


(defn search-results-view
  "Render the committed query summary and filtered result list."
  [query]
  (let [matches (filter-names query)]
    [:div
     [:p
      (str "Committed query: "
           (pr-str (or query ""))
           " | Apply delay: random "
           min-apply-delay-ms
           "-"
           max-apply-delay-ms
           "ms")]
     [:ul
      (into []
            (map (fn [name]
                   [:li name]))
            matches)]]))


(defn local-search-demo
  "Render the local-draft variant where typing stays client-only and `$value`
   is used to hand off the committed search term."
  []
  (let [search-draft* (h/local-signal :local-search-draft "")
        search*       (h/path-cursor :local-q "")]
    [:section
     [:h2 "Local-only draft signal demo"]
     [:input {:value                        (or @search* "")
              :data-bind                    search-draft*
              :data-ignore-morph            true
              :data-on:input__debounce.30ms (h/action
                                              (Thread/sleep (rand-delay-ms))
                                              (reset! search* $value))
              :style                        search-input-style}]
     [:button {:id            "local-clear-search-button"
               :type          "button"
               :data-on:click (str @search-draft* "='';"
                                (h/action (reset! search* nil)))}
      "Clear"]
     (search-results-view @search*)]))


(defn synced-search-demo
  "Render the synced-draft variant where the draft signal round-trips through
   the server and is read inside the apply action."
  []
  (let [search-draft*       (h/signal :synced-search-draft "")
        search*      (h/path-cursor :synced-q "")]
    [:section
     [:h2 "Synced draft signal demo"]
     [:input {:value                        (or @search* "")
              :data-bind                    search-draft*
              :data-ignore-morph            true
              :data-on:input__debounce.30ms (h/action
                                              (Thread/sleep (rand-delay-ms))
                                              (reset! search* @search-draft*))
              :style                        search-input-style}]
     [:button {:id            "synced-clear-search-button"
               :type          "button"
               :data-on:click (h/action
                                (reset! search-draft* "")
                                (reset! search* nil))}
      "Clear"]
     (search-results-view @search*)]))


(defn search-page
  "Render the comparison page with both live-search variants."
  [_request]
  [:main
   [:h1 "Hyper Active Search"]
   [:p
    (str "Both demos use data-on:input__debounce.30ms and a random "
         min-apply-delay-ms
         "-"
         max-apply-delay-ms
         "ms server delay for apply actions. Clear buttons are immediate.")]
   (local-search-demo)
   (synced-search-demo)])


(def routes
  [["/" {:name :home
         :title "Hyper Active Search"
         :get #'search-page}]])


(defonce server*
  (atom nil))


(defn listen-port
  "Return the port the app should bind to.

   Uses the `PORT` environment variable when present, otherwise defaults to
   `8080` for hosting environments that expect that port."
  []
  (if-let [port-str (some-> (System/getenv "PORT") str/trim not-empty)]
    (try
      (Integer/parseInt port-str)
      (catch NumberFormatException _
        (throw (ex-info "PORT must be an integer."
                        {:port port-str}))))
    default-port))


(defn start!
  []
  (when-let [server @server*]
    (h/stop! server))
  (reset! server*
          (h/start! (h/create-handler #'routes) {:port (listen-port)})))


(defn stop!
  []
  (when-let [server @server*]
    (h/stop! server)
    (reset! server* nil)))


(defn -main
  [& _]
  (start!)
  @(promise))


(comment
  ;; REPL helpers
  (start!)
  (stop!)
  :_)
