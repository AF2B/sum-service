(ns com.borba.sum-service.specs.math)

(def SumInput
  "Schema for the input of the sum function
   Schema expected:
   {:a 1
    :b 2}"
  [:map
   [:a int?]
   [:b int?]])

(def SumResult
  "Schema for the result of the sum function
   Schema expected:
   {:result 3}"
  [:map
   [:result int?]])
