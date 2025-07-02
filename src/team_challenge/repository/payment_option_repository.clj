(ns team-challenge.repository.payment-option-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values update set delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (let [query (-> (select :*)
                  (from :payment_options)
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn get-by-id [id]
  (let [query (-> (select :*)
                  (from :payment_options)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn create! [{:keys [name description]}]
  (let [query (-> (insert-into :payment_options)
                  (columns :name :description)
                  (values [[name description]])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn update! [id option]
  (let [query (-> (update :payment_options)
                  (set {:name (:name option)
                        :description (:description option)})
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn delete! [id]
  (let [query (-> (delete-from :payment_options)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))