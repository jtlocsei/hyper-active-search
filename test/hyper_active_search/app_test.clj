(ns hyper-active-search.app-test
  (:require [clojure.test :refer [deftest is]]
            [hyper-active-search.app :as app]))


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


(deftest normalized-search-trims-and-drops-blank-values
  (is (= "Grace"
         (app/normalized-search "  Grace  ")))
  (is (nil? (app/normalized-search "   ")))
  (is (nil? (app/normalized-search nil))))


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
    (is (<= 120 delay))
    (is (<= delay 399))))


(deftest search-page-renders-debounced-ignored-input
  (let [rendered (app/search-view {:query "Ada"
                                   :apply-action "APPLY"
                                   :clear-action "CLEAR"})
        input-node (node-by-id rendered "live-search-input")
        clear-node (node-by-id rendered "clear-search-button")]
    (is (= "Ada"
           (get-in input-node [1 :value])))
    (is (= true
           (get-in input-node [1 :data-ignore-morph])))
    (is (= "APPLY"
           (get-in input-node [1 :data-on:input__debounce.180ms])))
    (is (= "CLEAR"
           (get-in clear-node [1 :data-on:click])))))


(deftest search-page-shows-full-list-and-no-clear-button-without-q
  (let [rendered (app/search-view {:query nil
                                   :apply-action "APPLY"
                                   :clear-action "CLEAR"})
        list-items (filter #(and (vector? %)
                                 (= :li (first %)))
                           (hiccup-node-seq rendered))
        clear-node (node-by-id rendered "clear-search-button")]
    (is (= (count app/names)
           (count list-items)))
    (is (nil? clear-node))))


(deftest clear-search-expression-clears-input-before-server-action
  (is (= "document.getElementById('live-search-input') && (document.getElementById('live-search-input').value = ''); CLEAR"
         (app/clear-search-expression "CLEAR"))))
