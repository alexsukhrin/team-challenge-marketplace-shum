(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :refer [*config*]]
            [hikari-cp.core :as cp]))

(declare ^:dynamic *db*)

(defstate ^:dynamic *db*
  :start (let [store (cp/make-datasource (-> *config* :db :hikari))]
           {:datasource store})
  :stop (-> *db* :datasource cp/close-datasource))
