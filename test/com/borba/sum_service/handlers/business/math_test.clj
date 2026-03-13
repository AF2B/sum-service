(ns com.borba.sum-service.handlers.business.math-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.borba.sum-service.handlers.business.math :as math]))

(deftest test-sum
  (testing "when summing two numbers"
    (is (= {:result 5}
           (math/sum {:a 2 :b 3})))

    (is (= {:result -1}
           (math/sum {:a -2 :b 1})))

    (is (thrown? Exception
                 (math/sum {:a 2})))))
