(ns team-challenge.config
  (:require [mount.core :refer [defstate]]
            [aero.core :refer [read-config]]))

(defn env!
  ([varname]
   (or (System/getenv varname)
       (throw (RuntimeException.
               (str "Missing required environment variable: " varname)))))
  ([varname default]
   (or (System/getenv varname)
       default)))

(declare ^:dynamic *config*)

(defn load-config []
  (read-config (str "config/" (env! "APP_ENV" "dev") ".edn")))

(defstate ^:dynamic *config*
  :start
  (load-config))

(comment

  (System/getenv "APP_ENV"))