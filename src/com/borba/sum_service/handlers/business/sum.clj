(ns com.borba.sum-service.handlers.business.sum
  (:require [clojure.spec.alpha :as s]
            [com.borba.sum-service.specs.sum :as specs]))

(defn compute
  "Computes the sum of two integers from the input map.

  Domain Context
  --------------
  Core operation of the sum-service. Receives a validated input map
  containing two operands and returns a result map with their sum.

  Contract
  --------
  Input:  {:a <integer> :b <integer>}
  Output: {:result <integer>}

  Field Semantics
  ---------------
  :a — first operand
  :b — second operand
  :result — sum of :a and :b

  Example
  -------
  (compute {:a 10 :b 32}) => {:result 42}

  Invariants
  ----------
  - Both :a and :b must be present and satisfy ::specs/operand
  - Throws ex-info with :explain data when input is invalid

  Edge Cases
  ----------
  - Negative operands are allowed
  - Zero operands are allowed
  - Operands at Long boundary are supported
  "
  [{:keys [a b] :as input}]
  (when-not (s/valid? ::specs/sum-input input)
    (throw (ex-info "Invalid input"
                    {:input   input
                     :explain (s/explain-data ::specs/sum-input input)})))
  {:result (+ a b)})
