(ns hyper-active-search.app-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [hyper-active-search.app :as app]
            [hyper.test :as ht]))


(defn- hiccup-node-seq
  [root]
  (tree-seq (fn [node]
              (or (vector? node)
                  (seq? node)))
            seq
            root))


(defn- node-by-id
  [rendered target-id]
  (some (fn [node]
          (when (and (vector? node)
                     (= target-id (get-in node [1 :id])))
            node))
        (hiccup-node-seq rendered)))


(deftest filter-names-is-case-insensitive
  (is (= ["Grace Hopper"]
         (app/filter-names "grace")))
  (is (= ["Margaret Hamilton"]
         (app/filter-names "  ham  ")))
  (is (= app/names
         (app/filter-names nil))))


(deftest response-delay-is-zero-for-blank-and-bounded-for-text
  (is (= 0
         (app/response-delay-ms nil)))
  (is (= 0
         (app/response-delay-ms "   ")))
  (let [delay (app/response-delay-ms "ada")]
    (is (<= app/min-apply-delay-ms delay))
    (is (<= delay app/max-apply-delay-ms))))


(deftest search-page-renders-both-demos-with-seeded-signal-defaults
  (let [result (ht/test-page app/search-page
                             {:route {:name :home
                                      :path "/"
                                      :path-params {}
                                      :query-params {:local-q "Ada"
                                                     :synced-q "Grace"}}})
        rendered (:body result)
        local-input (node-by-id rendered "local-search-input")
        synced-input (node-by-id rendered "synced-search-input")
        local-clear-node (node-by-id rendered "local-clear-search-button")
        synced-clear-node (node-by-id rendered "synced-clear-search-button")]
    (is (str/includes? (:body-html result)
                       "Local-only draft signal demo"))
    (is (str/includes? (:body-html result)
                       "Synced draft signal demo"))
    (is (= "Ada"
           (get-in local-input [1 :value])))
    (is (= "Grace"
           (get-in synced-input [1 :value])))
    (is (= app/search-input-style
           (get-in local-input [1 :style])))
    (is (= app/search-input-style
           (get-in synced-input [1 :style])))
    (is (= true
           (get-in local-input [1 :data-ignore-morph])))
    (is (= true
           (get-in synced-input [1 :data-ignore-morph])))
    (is (string? (get-in local-input [1 :data-on:input__debounce.30ms])))
    (is (string? (get-in synced-input [1 :data-on:input__debounce.30ms])))
    (is (string? (get-in local-clear-node [1 :data-on:click])))
    (is (string? (get-in synced-clear-node [1 :data-on:click])))
    (is (= "Ada"
           (get-in result [:signals :local-search-draft :default-val])))
    (is (= true
           (get-in result [:signals :local-search-draft :local?])))
    (is (= "Grace"
           (get-in result [:signals :synced-search-draft :default-val])))
    (is (= false
           (get-in result [:signals :synced-search-draft :local?])))))


(deftest search-page-shows-full-lists-when-committed-queries-are-absent
  (let [result (ht/test-page app/search-page
                             {:route {:name :home
                                      :path "/"
                                      :path-params {}
                                      :query-params {}}})
        rendered (:body result)
        list-items (filter #(and (vector? %)
                                 (= :li (first %)))
                           (hiccup-node-seq rendered))]
    (is (= (* 2 (count app/names))
           (count list-items)))
    (is (= ""
           (get-in result [:signals :local-search-draft :default-val])))
    (is (= ""
           (get-in result [:signals :synced-search-draft :default-val])))))
