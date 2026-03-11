(ns system.config
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]))

(defn read-config [resource profile]
  (aero/read-config
   resource
   {:profile profile}))

(defn load-config [profile]
  (let [base (io/resource "config/base.edn")
        env  (io/resource (str "config/" (name profile) ".edn"))]

    (merge
     (read-config base profile)
     (when env
       (read-config env profile)))))