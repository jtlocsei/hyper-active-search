(ns hyper-active-search.test-runner
  (:require [clojure.test :as test]
            [hyper-active-search.app-test])
  (:gen-class))


(defn -main
  [& _]
  (let [{:keys [fail error]} (test/run-tests 'hyper-active-search.app-test)]
    (when (pos? (+ fail error))
      (System/exit 1))))
