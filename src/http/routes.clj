(ns http.routes
  (:require
   [integrant.core :as ig]
   [http.interceptors :as i]))

(def handlers
  {:math/sum
   [i/parse-query
    i/sum-interceptor
    i/response-interceptor]})

(defn expand-route
  [[path method handler]]

  (let [interceptors (handlers handler)]

    (when-not interceptors
      (throw (ex-info "Handler not found"
                      {:handler handler})))

    [path method interceptors :route-name handler]))

(defmethod ig/init-key :http/routes
  [_ {:keys [routes]}]

  (into #{} (map expand-route routes)))