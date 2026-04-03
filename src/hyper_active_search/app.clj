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


(defn request-query-param
  [request key]
  (or (get-in request [:parameters :query key])
      (get-in request [:parameters :query (name key)])
      (get-in request [:hyper/route :query-params key])
      (get-in request [:hyper/route :query-params (name key)])
      (get-in request [:query-params key])
      (get-in request [:query-params (name key)])))


(defn normalized-search
  [value]
  (let [trimmed (some-> value str str/trim)]
    (when (seq trimmed)
      trimmed)))


(defn response-delay-ms
  [query]
  (if-let [text (normalized-search query)]
    (+ 120 (mod (Math/abs (long (hash text))) 280))
    0))


(defn filter-names
  [query]
  (if-let [needle (some-> query normalized-search str/lower-case)]
    (filterv #(str/includes? (str/lower-case %) needle) names)
    names))


(defn- query-params-with-search
  [existing-query-params query]
  (cond-> (dissoc existing-query-params :q "q")
    query (assoc :q query)))


(defn set-search!
  [form-data]
  (let [query (normalized-search (or (:q form-data)
                                     (get form-data "q")
                                     (:search form-data)
                                     (get form-data "search")))
        delay-ms (response-delay-ms query)]
    (when (pos? delay-ms)
      (Thread/sleep delay-ms))
    (swap! (h/path-cursor [] {})
           #(query-params-with-search % query))))


(defn clear-search!
  []
  (swap! (h/path-cursor [] {})
         #(query-params-with-search % nil)))


(defn clear-search-expression
  [server-action]
  (str "document.getElementById('live-search-input') && "
       "(document.getElementById('live-search-input').value = ''); "
       server-action))


(defn search-view
  [{:keys [query apply-action clear-action]}]
  (let [delay-ms (response-delay-ms query)
        matches (filter-names query)]
    [:main
     [:h1 "Hyper Active Search"]
     [:form {:data-on:submit__prevent apply-action}
      [:input {:id "live-search-input"
               :name "q"
               :type "search"
               :value (or query "")
               :autocomplete "off"
               :data-ignore-morph true
               :data-on:input__debounce.180ms apply-action}]
      (when query
        [:button {:id "clear-search-button"
                  :type "button"
                  :data-on:click clear-action}
         "Clear"])]
     [:p
      (str "Committed query: "
           (pr-str (or query ""))
           " | Simulated delay: "
           delay-ms
           "ms")]
     [:ul
      (into []
            (map (fn [name]
                   [:li name]))
            matches)]]))


(defn search-page
  [request]
  (let [query (normalized-search (request-query-param request :q))
        apply-action (h/action (set-search! $form-data))
        clear-action (clear-search-expression
                       (h/action (clear-search!)))]
    (search-view {:query query
                  :apply-action apply-action
                  :clear-action clear-action})))


(def routes
  [["/" {:name :home
         :title "Hyper Active Search"
         :get #'search-page}]])


(defonce server*
  (atom nil))


(defn start!
  []
  (when-let [server @server*]
    (h/stop! server))
  (reset! server*
          (h/start! (h/create-handler #'routes) {:port 4000})))


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
