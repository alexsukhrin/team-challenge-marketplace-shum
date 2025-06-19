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

(defstate ^:dynamic *config*
  :start
  (read-config (str "config/" (env! "APP_ENV") ".edn")))

(comment

  (System/getenv "APP_ENV"))