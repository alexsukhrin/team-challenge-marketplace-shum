(ns marketplace-shum.infra.config
  (:require
   [mount.core :refer [defstate]]
   [aero.core :refer [read-config]]))

(defn env!
  ([varname]
   (or (System/getenv varname)
       (throw (RuntimeException.
               (str "Missing required environment variable: " varname)))))
  ([varname default]
   (or (System/getenv varname)
       default)))

(defstate ^:dynamic *config*
  :start (try (read-config (str "config/" (env! "APP_ENV") ".edn"))
              (catch NumberFormatException _
                (println "Not valid format config")
                (System/exit 1))))
