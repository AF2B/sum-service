(ns system.core
  (:require
   [integrant.core :as ig]
   [system.config :as config]
   [http.routes]
   [http.server]))

(defonce system (atom nil))

(defn start [profile]
  (when-not @system
    (let [cfg (config/load-config profile)] ;; dev, prod, test
      (reset! system (ig/init cfg)))))

(defn stop []
  (when @system
    (ig/halt! @system)))