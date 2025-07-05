(ns marketplace-shum.infra.config
  (:require
   [mount.core :refer [defstate]]
   [aero.core :refer [read-config]]))

(defstate ^:dynamic *config*
  :start (read-config (str "config/" (System/getenv "APP_ENV") ".edn")))
