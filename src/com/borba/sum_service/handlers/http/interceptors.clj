(ns com.borba.sum-service.handlers.http.interceptors
  (:require [cheshire.core :as json]
            [com.borba.sum-service.handlers.business.sum :as sum]))

(def parse-query
  {:name ::parse-query
   :enter
   (fn [ctx]
     (let [params (get-in ctx [:request :params])
           a (Long/parseLong (get params :a))
           b (Long/parseLong (get params :b))]
       (assoc ctx :sum/input {:a a :b b})))})

(def sum-interceptor
  {:name ::sum
   :enter
   (fn [ctx]
     (let [input (:sum/input ctx)
           result (sum/compute input)]
       (assoc ctx :sum/result result)))})

(def response-interceptor
  {:name ::response
   :leave
   (fn [ctx]
     (assoc ctx :response
            {:status  200
             :headers {"Content-Type" "application/json; charset=utf-8"}
             :body    (json/generate-string (:sum/result ctx))}))})
