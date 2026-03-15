(ns flows.sum-flow-test
  (:require [borba.flow :as sf]
            [borba.routes.component]
            [clojure.test :refer [is]]
            [com.borba.sum-service.handlers.http.routes]
            [state-flow.api :as flow]))

(sf/defflow run-integration-tests
  [["/v1/sum" :get :sum/compute]]

  [resp (sf/request :get "/v1/sum?a=2&b=3")]
  (flow/return
    (do (is (= 200 (:status resp))           "positive sum: status 200")
        (is (= {:result 5} (sf/json-body resp)) "positive sum: correct result")))

  [resp (sf/request :get "/v1/sum?a=-5&b=3")]
  (flow/return
    (do (is (= 200 (:status resp))            "negative numbers: status 200")
        (is (= {:result -2} (sf/json-body resp)) "negative numbers: correct result")))

  [resp (sf/request :get "/v1/sum?a=0&b=0")]
  (flow/return
    (do (is (= 200 (:status resp))           "zero sum: status 200")
        (is (= {:result 0} (sf/json-body resp)) "zero sum: correct result")))

  [resp (sf/request :get "/v1/sum?a=2")]
  (flow/return
    (is (= 500 (:status resp)) "missing param: status 500"))

  [resp (sf/request :get "/v1/sum?a=abc&b=3")]
  (flow/return
    (is (= 500 (:status resp)) "invalid param: status 500")))
