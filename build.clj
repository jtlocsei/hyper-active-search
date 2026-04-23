(ns build
  (:require [clojure.tools.build.api :as b]))


(def class-dir
  "target/classes")


(def uber-file
  "target/hyper-active-search-standalone.jar")


(def main-class
  'hyper_active_search.Launcher)


;; Delay basis creation so normal app startup does not pay build-tooling cost.
(def basis
  (delay (b/create-basis {:project "deps.edn"})))


(defn clean
  [_]
  (b/delete {:path "target"}))


(defn uber
  [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/javac {:src-dirs ["java-src"]
            :class-dir class-dir
            :basis @basis
            :javac-opts ["--release" "17"]})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main main-class})
  {:uber-file uber-file})
