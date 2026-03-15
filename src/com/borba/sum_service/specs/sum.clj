(ns com.borba.sum-service.specs.sum
  (:require [clojure.spec.alpha :as s]))

;; -----------------------------------------------------------------------------
;; Primitive Specs
;;
;; Defines the atomic building blocks used to compose the operation contracts.
;; Each spec is named with the domain term it represents.
;; -----------------------------------------------------------------------------

;; Represents a valid integer operand for the sum operation.
;;
;; Domain Context
;; --------------
;; An operand is any integer value that can participate in the sum operation.
;; Values are constrained to the JVM Long range, which is the native type
;; produced by the HTTP layer (Long/parseLong).
;;
;; Invariants
;; ----------
;; - Must satisfy integer? (Long or BigInteger on JVM)
;; - Must be within Long/MIN_VALUE..Long/MAX_VALUE
;; - Negative values, zero, and positive values are all valid
;;
;; Edge Cases
;; ----------
;; - Long/MIN_VALUE and Long/MAX_VALUE are accepted
;; - Zero is a valid operand (identity element of sum)
(s/def ::operand
  (s/and integer?
         #(<= Long/MIN_VALUE % Long/MAX_VALUE)))

;; Represents the first operand of the sum operation.
(s/def ::a ::operand)

;; Represents the second operand of the sum operation.
(s/def ::b ::operand)

;; Represents the computed result of the sum operation.
;;
;; Invariants
;; ----------
;; - Must be an integer
;; - Produced exclusively by the sum operation over ::a and ::b
(s/def ::result integer?)

;; -----------------------------------------------------------------------------
;; Composite Specs — Input Contract
;; -----------------------------------------------------------------------------

;; Represents the input contract for the sum operation.
;;
;; Domain Context
;; --------------
;; This spec defines the complete set of operands required to execute a sum.
;; Both fields are mandatory. The absence of any operand constitutes an
;; invalid request and must be rejected at the boundary.
;;
;; Data Contract
;; -------------
;; Expected structure:
;;
;;   {:a <integer>
;;    :b <integer>}
;;
;; Field Semantics
;; ---------------
;; :a — first operand of the sum operation
;; :b — second operand of the sum operation
;;
;; Example
;; -------
;;   {:a 10
;;    :b 32}
;;
;; Invariants
;; ----------
;; - Both :a and :b must be present
;; - Both must satisfy ::operand (integer within Long range)
;;
;; Edge Cases
;; ----------
;; - Extra keys beyond :a and :b are ignored by s/keys but not stripped
;; - Zero values for both operands are valid
;; - Negative operands are valid
(s/def ::sum-input
  (s/keys :req-un [::a ::b]))

;; -----------------------------------------------------------------------------
;; Composite Specs — Output Contract
;; -----------------------------------------------------------------------------

;; Represents the output contract of the sum operation.
;;
;; Domain Context
;; --------------
;; This spec defines the structure returned after a successful sum computation.
;; The result is always a map containing a single key, :result, holding the
;; integer product of the addition.
;;
;; Data Contract
;; -------------
;; Expected structure:
;;
;;   {:result <integer>}
;;
;; Field Semantics
;; ---------------
;; :result — the computed sum of the two operands
;;
;; Example
;; -------
;;   {:result 42}
;;
;; Invariants
;; ----------
;; - :result must be present
;; - :result must be an integer
;;
;; Edge Cases
;; ----------
;; - :result may be negative if operands are negative
;; - :result may be zero if operands cancel each other
(s/def ::sum-result
  (s/keys :req-un [::result]))
