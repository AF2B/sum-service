(ns generators.sum
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [com.borba.sum-service.specs.sum :as specs]))

;; -----------------------------------------------------------------------------
;; Primitive Generators
;;
;; Generators for individual operand values. Use these when you need control
;; over the range or sign of values in a test scenario.
;; -----------------------------------------------------------------------------

(def operand-gen
  "Generates valid integer operands derived from ::specs/operand.
   Covers the full Long range including negative values, zero, and boundaries."
  (s/gen ::specs/operand))

(def small-operand-gen
  "Generates integers in the range [-1000, 1000].
   Use this when test assertions depend on readable, predictable values."
  (gen/choose -1000 1000))

(def positive-operand-gen
  "Generates strictly positive integers in the range [1, 10000].
   Use this when both operands must be positive (e.g. balance checks)."
  (gen/choose 1 10000))

(def negative-operand-gen
  "Generates strictly negative integers in the range [-10000, -1].
   Use this for boundary and sign-inversion scenarios."
  (gen/choose -10000 -1))

(def zero-gen
  "Always generates zero.
   Use this to test the identity element property: (+ x 0) = x."
  (gen/return 0))

(def boundary-operand-gen
  "Generates Long boundary values: MIN_VALUE and MAX_VALUE.
   Use this to verify the system handles extreme integer inputs without overflow."
  (gen/one-of [(gen/return Long/MIN_VALUE)
               (gen/return Long/MAX_VALUE)]))

;; -----------------------------------------------------------------------------
;; Composite Input Generators
;;
;; Generators for complete SumInput maps. Each generator targets a specific
;; test scenario. Compose these in property-based tests via gen/sample or
;; clojure.test.check.
;; -----------------------------------------------------------------------------

(def sum-input-gen
  "Generates valid SumInput maps {:a <int> :b <int>} over the full Long range.
   Derived from ::specs/sum-input. Use for broad property-based coverage."
  (gen/hash-map :a operand-gen :b operand-gen))

(def small-sum-input-gen
  "Generates SumInput maps with small integers [-1000, 1000].
   Use when expected results should be easy to verify manually in assertions."
  (gen/hash-map :a small-operand-gen :b small-operand-gen))

(def positive-sum-input-gen
  "Generates SumInput maps where both operands are positive.
   Result is always greater than or equal to both operands."
  (gen/hash-map :a positive-operand-gen :b positive-operand-gen))

(def negative-sum-input-gen
  "Generates SumInput maps where both operands are negative.
   Result is always less than both operands."
  (gen/hash-map :a negative-operand-gen :b negative-operand-gen))

(def zero-sum-input-gen
  "Generates SumInput maps where both operands are zero.
   Tests identity: (+ 0 0) = 0."
  (gen/hash-map :a zero-gen :b zero-gen))

(def mixed-sign-sum-input-gen
  "Generates SumInput maps where :a is positive and :b is negative.
   Use to test cancellation and sign-change behavior."
  (gen/hash-map :a positive-operand-gen :b negative-operand-gen))

(def boundary-sum-input-gen
  "Generates SumInput maps using Long boundary values.
   Use for overflow boundary tests — handle arithmetic carefully."
  (gen/hash-map :a boundary-operand-gen :b boundary-operand-gen))

;; -----------------------------------------------------------------------------
;; Result Generators
;; -----------------------------------------------------------------------------

(def sum-result-gen
  "Generates valid SumResult maps {:result <int>}.
   Derived from ::specs/sum-result. Use for response shape validation tests."
  (s/gen ::specs/sum-result))

;; -----------------------------------------------------------------------------
;; Sampling Utilities
;;
;; Convenience functions for generating samples in the REPL or test setup.
;; These are not intended for production use.
;; -----------------------------------------------------------------------------

(defn sample-inputs
  "Returns n generated SumInput samples using the full-range generator.
   Defaults to 10 samples.

   Example
   -------
   (sample-inputs)    ;; => 10 samples
   (sample-inputs 5)  ;; => 5 samples
   "
  ([]  (sample-inputs 10))
  ([n] (gen/sample sum-input-gen n)))

(defn sample-small-inputs
  "Returns n generated SumInput samples with small integers.
   Defaults to 10 samples. Useful for readable REPL exploration."
  ([]  (sample-small-inputs 10))
  ([n] (gen/sample small-sum-input-gen n)))

(defn sample-results
  "Returns n generated SumResult samples.
   Defaults to 10 samples."
  ([]  (sample-results 10))
  ([n] (gen/sample sum-result-gen n)))
