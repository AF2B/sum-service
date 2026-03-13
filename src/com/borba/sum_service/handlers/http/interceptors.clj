(ns com.borba.sum-service.handlers.http.interceptors
  (:require [cheshire.core :as json]
            [com.borba.sum-service.handlers.business.math :as math]))

(def parse-query
  {:name ::parse-query
   :enter
   (fn [ctx]
     (let [params (get-in ctx [:request :params])
           a (Long/parseLong (get params :a))
           b (Long/parseLong (get params :b))]
       (assoc ctx :math/input {:a a :b b})))})

(def sum-interceptor
  {:name ::sum
   :enter
   (fn [ctx]
     (let [input (:math/input ctx)
           result (math/sum input)]
       (assoc ctx :math/result result)))})

(def response-interceptor
  {:name ::response
   :leave
   (fn [ctx]
     (assoc ctx :response
            {:status  200
             :headers {"Content-Type" "application/json; charset=utf-8"}
             :body    (json/generate-string (:math/result ctx))}))})
