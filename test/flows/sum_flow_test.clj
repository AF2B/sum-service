(ns flows.sum-flow-test
  (:require [borba.routes.component]
            [cheshire.core :as json]
            [clojure.test :refer [deftest is]]
            [com.borba.sum-service.handlers.http.routes]
            [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as ptest]
            [state-flow.api :as flow]))

(defn- build-service-fn []
  (let [handlers (ig/init-key :service/handlers {})
        routes   (ig/init-key :http/routes {:routes   [["/v1/sum" :get :math/sum]]
                                            :handlers handlers})]
    (-> {::http/routes (route/expand-routes routes)
         ::http/type   :jetty
         ::http/join?  false}
        http/default-interceptors
        http/create-servlet
        ::http/service-fn)))

(defn- request [method path]
  (flow/fmap #(ptest/response-for (:service-fn %) method path)
             (flow/get-state)))

(defn- json-body [resp]
  (json/parse-string (:body resp) true))

(def sum-integration-tests
  (flow/flow "sum HTTP integration tests"

    [resp (request :get "/v1/sum?a=2&b=3")]
    (flow/return
      (do (is (= 200 (:status resp))         "positive sum: status 200")
          (is (= {:result 5} (json-body resp)) "positive sum: correct result")))

    [resp (request :get "/v1/sum?a=-5&b=3")]
    (flow/return
      (do (is (= 200 (:status resp))          "negative numbers: status 200")
          (is (= {:result -2} (json-body resp)) "negative numbers: correct result")))

    [resp (request :get "/v1/sum?a=0&b=0")]
    (flow/return
      (do (is (= 200 (:status resp))         "zero sum: status 200")
          (is (= {:result 0} (json-body resp)) "zero sum: correct result")))

    [resp (request :get "/v1/sum?a=2")]
    (flow/return
      (is (= 500 (:status resp)) "missing param: status 500"))

    [resp (request :get "/v1/sum?a=abc&b=3")]
    (flow/return
      (is (= 500 (:status resp)) "invalid param: status 500"))))

(deftest run-integration-tests
  (flow/run sum-integration-tests {:service-fn (build-service-fn)}))
