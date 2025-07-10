(ns marketplace-shum.auth.domain
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))

(defn not-blank? [s]
  (not (str/blank? s)))

(s/def ::email (s/and string? not-blank?))
(s/def ::password (s/and string? not-blank?))
(s/def ::first-name (s/and string? not-blank?))
(s/def ::last-name (s/and string? not-blank?))
(s/def ::message string?)
(s/def ::error string?)
(s/def ::token string?)
(s/def ::access-token string?)
(s/def ::refresh-token string?)
(s/def ::register-params (s/keys :req-un [::first-name ::last-name ::email ::password]))
(s/def ::register-response (s/keys :req-un [::message]))
(s/def ::error-response (s/keys :req-un [::error ::message]))
(s/def ::login-params (s/keys :req-un [::email ::password]))
(s/def ::login-response (s/keys :req-un [::access-token ::refresh-token]))
(s/def ::refresh-params (s/keys :req-un [::refresh-token]))