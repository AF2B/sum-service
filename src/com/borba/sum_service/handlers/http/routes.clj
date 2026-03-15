(ns com.borba.sum-service.handlers.http.routes
  (:require [integrant.core :as ig]
            [com.borba.sum-service.handlers.http.interceptors :as i]))

(defmethod ig/init-key :service/handlers
  [_ _]
  {:sum/compute [i/parse-query 
                 i/sum-interceptor 
                 i/response-interceptor]})
