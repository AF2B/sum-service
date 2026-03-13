(ns com.borba.sum-service.handlers.business.math
  (:require [malli.core :as m]
            [com.borba.sum-service.specs.math :as schema]))

(defn sum
  [{:keys [a b] :as input}]
  (when-not (m/validate schema/SumInput input)
    (throw (ex-info "Invalid input"
                    {:input input
                     :schema schema/SumInput})))
  {:result (+ a b)})
