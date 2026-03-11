(ns http.interceptors 
  (:require
   [domain.math :as math]))

(def parse-query
  {:name ::parse-query
   :enter
   (fn [ctx]
     (println "REQUEST: " (:request ctx))
     (let [params (get-in ctx [:request :params])
           a (Long/parseLong (get params :a))
           b (Long/parseLong (get params :b))]
       (assoc ctx :math/input {:a a :b b})))})

(def sum-interceptor
  {:name ::sum
   :enter
   (fn [ctx]
     (let [{:keys [a b]} (:math/input ctx)
           result (math/sum a b)]
       (assoc ctx :math/result result)))})

(def response-interceptor
  {:name ::response
   :leave
   (fn [ctx]
     (assoc ctx :response
            {:status 200
             :body {:result (:math/result ctx)}}))})