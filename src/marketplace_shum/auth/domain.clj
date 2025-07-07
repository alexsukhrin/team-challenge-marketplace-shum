(ns marketplace-shum.auth.domain
  (:require 
   [clojure.spec.alpha :as s]))

(s/def ::email string?)
(s/def ::password string?)
(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::message string?)
(s/def ::error string?)
(s/def ::token string?)
(s/def ::register-params (s/keys :req-un [::first-name ::last-name ::email ::password]))
(s/def ::register-response (s/keys :req-un [::message]))
(s/def ::error-response (s/keys :req-un [::error ::message]))
(s/def ::login-params (s/keys :req-un [::email ::password]))
(s/def ::login-response (s/keys :req-un [::message ::token]))
