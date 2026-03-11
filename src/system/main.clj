(ns system.main
  (:gen-class)
  (:require [system.core :as core]))

(defn -main
  [& [profile]]
  (core/start profile)
  (let [profile (keyword (or profile "dev"))]
    (println "System started with profile: " profile))
  
  @(promise) "Press Enter to stop the system...")