(ns com.borba.sum-service.handlers.business.sum-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.borba.sum-service.handlers.business.sum :as sum]))

;; =============================================================================
;; Valid Inputs — Happy Path
;; =============================================================================

(deftest test-compute-positive-operands
  (testing "positive operands produce positive result"
    (is (= {:result 5}
           (sum/compute {:a 2 :b 3}))
        "2 + 3 should equal 5")

    (is (= {:result 100}
           (sum/compute {:a 50 :b 50}))
        "50 + 50 should equal 100")

    (is (= {:result 1}
           (sum/compute {:a 0 :b 1}))
        "0 + 1 should equal 1")))

(deftest test-compute-negative-operands
  (testing "both operands negative produce negative result"
    (is (= {:result -5}
           (sum/compute {:a -2 :b -3}))
        "-2 + -3 should equal -5")

    (is (= {:result -100}
           (sum/compute {:a -50 :b -50}))
        "-50 + -50 should equal -100")))

(deftest test-compute-mixed-sign-operands
  (testing "mixed sign operands produce correct signed result"
    (is (= {:result -1}
           (sum/compute {:a -2 :b 1}))
        "-2 + 1 should equal -1")

    (is (= {:result 1}
           (sum/compute {:a 2 :b -1}))
        "2 + -1 should equal 1")

    (is (= {:result -7}
           (sum/compute {:a -10 :b 3}))
        "-10 + 3 should equal -7")))

;; =============================================================================
;; Algebraic Properties
;; =============================================================================

(deftest test-compute-zero-identity
  (testing "zero is the identity element of sum"
    (is (= {:result 0}
           (sum/compute {:a 0 :b 0}))
        "0 + 0 should equal 0")

    (is (= {:result 42}
           (sum/compute {:a 42 :b 0}))
        "x + 0 should equal x (right identity)")

    (is (= {:result 42}
           (sum/compute {:a 0 :b 42}))
        "0 + x should equal x (left identity)")

    (is (= {:result -7}
           (sum/compute {:a -7 :b 0}))
        "-7 + 0 should preserve sign (right identity)")))

(deftest test-compute-commutativity
  (testing "sum is commutative: compute(a,b) = compute(b,a)"
    (is (= (sum/compute {:a 7 :b 3})
           (sum/compute {:a 3 :b 7}))
        "7 + 3 should equal 3 + 7")

    (is (= (sum/compute {:a -10 :b 4})
           (sum/compute {:a 4 :b -10}))
        "-10 + 4 should equal 4 + -10")

    (is (= (sum/compute {:a 0 :b 5})
           (sum/compute {:a 5 :b 0}))
        "0 + 5 should equal 5 + 0")))

;; =============================================================================
;; Edge Cases — Boundary Values
;; =============================================================================

(deftest test-compute-long-max-boundary
  (testing "Long/MAX_VALUE is accepted as operand"
    (is (= {:result Long/MAX_VALUE}
           (sum/compute {:a Long/MAX_VALUE :b 0}))
        "Long/MAX_VALUE + 0 should equal Long/MAX_VALUE")))

(deftest test-compute-long-min-boundary
  (testing "Long/MIN_VALUE is accepted as operand"
    (is (= {:result Long/MIN_VALUE}
           (sum/compute {:a Long/MIN_VALUE :b 0}))
        "Long/MIN_VALUE + 0 should equal Long/MIN_VALUE")))

(deftest test-compute-cancellation
  (testing "opposite operands cancel to zero"
    (is (= {:result 0}
           (sum/compute {:a 1000 :b -1000}))
        "1000 + -1000 should cancel to 0")

    (is (= {:result 0}
           (sum/compute {:a -999 :b 999}))
        "-999 + 999 should cancel to 0")))

;; =============================================================================
;; Result Shape — Output Contract
;; =============================================================================

(deftest test-compute-result-shape
  (testing "result always conforms to the output contract {:result <integer>}"
    (let [result (sum/compute {:a 1 :b 2})]
      (is (map? result)
          "result should be a map")

      (is (contains? result :result)
          "result map must contain :result key")

      (is (integer? (:result result))
          ":result value must be an integer")

      (is (= 1 (count result))
          "result map should contain exactly one key"))))

;; =============================================================================
;; Invalid Inputs — Missing Operands
;; =============================================================================

(deftest test-compute-missing-operand-b
  (testing "missing :b operand is rejected"
    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a 2}))
        "missing :b should throw ExceptionInfo")))

(deftest test-compute-missing-operand-a
  (testing "missing :a operand is rejected"
    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:b 2}))
        "missing :a should throw ExceptionInfo")))

(deftest test-compute-empty-input
  (testing "empty map is rejected"
    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {}))
        "empty map should throw ExceptionInfo")))

;; =============================================================================
;; Invalid Inputs — Wrong Types
;; =============================================================================

(deftest test-compute-nil-operand-values
  (testing "nil operand values are rejected"
    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a nil :b 1}))
        "nil :a should throw ExceptionInfo")

    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a 1 :b nil}))
        "nil :b should throw ExceptionInfo")

    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a nil :b nil}))
        "both nil should throw ExceptionInfo")))

(deftest test-compute-string-operands
  (testing "string operands are rejected"
    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a "2" :b 3}))
        "string :a should throw ExceptionInfo")

    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a 2 :b "3"}))
        "string :b should throw ExceptionInfo")

    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a "two" :b "three"}))
        "both string operands should throw ExceptionInfo")))

(deftest test-compute-float-operands
  (testing "floating-point operands are rejected"
    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a 2.5 :b 3}))
        "float :a should throw ExceptionInfo")

    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a 2 :b 3.5}))
        "float :b should throw ExceptionInfo")))

(deftest test-compute-boolean-operands
  (testing "boolean operands are rejected"
    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a true :b 1}))
        "boolean :a should throw ExceptionInfo")

    (is (thrown? clojure.lang.ExceptionInfo
                 (sum/compute {:a 1 :b false}))
        "boolean :b should throw ExceptionInfo")))

;; =============================================================================
;; Exception Contract — Error Context
;; =============================================================================

(deftest test-compute-exception-carries-context
  (testing "thrown ExceptionInfo carries structured context"
    (try
      (sum/compute {:a "not-an-int" :b 1})
      (is false "should have thrown — this line must not be reached")
      (catch clojure.lang.ExceptionInfo e
        (is (= "Invalid input" (ex-message e))
            "exception message should be 'Invalid input'")

        (is (contains? (ex-data e) :input)
            "ex-data must contain :input key with the rejected value")

        (is (contains? (ex-data e) :explain)
            "ex-data must contain :explain key with spec explain-data")

        (is (= {:a "not-an-int" :b 1} (:input (ex-data e)))
            ":input should reflect the exact rejected input map")))))
