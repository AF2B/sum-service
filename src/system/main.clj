(ns system.main
  (:gen-class)
  (:require
   [borba.runtime.main :as runtime]
   [system.core]))

(defn -main
  [& [profile]]

  (runtime/run
   {:profile (keyword (or profile "dev"))}))