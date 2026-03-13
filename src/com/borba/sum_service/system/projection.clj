(ns com.borba.sum-service.system.projection
  (:require [integrant.core :as ig]))

(defmethod ig/init-key ::folding-funcs
  [_key _settings]
  {})
