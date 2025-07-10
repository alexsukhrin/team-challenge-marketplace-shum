(ns marketplace-shum.ads.repository
  (:require
   [datomic.client.api :as d]))

(defn create-ad! [conn ad]
  (let [tx {:tx-data [(assoc ad :ad/created-at (java.util.Date.))]}
        tx-result (d/transact conn tx)]
    {:tx-result tx-result}))

(defn find-ad-by-id [conn id]
  (d/pull (d/db conn)
          [:ad/id :ad/title :ad/description :ad/price :ad/created-at :ad/condition
           :ad/categories {:ad/photos [:ad-photo/url :ad-photo/position]}]
          [:ad/id (if (uuid? id) id (java.util.UUID/fromString id))]))

(defn find-ads-by-attr [conn attr value]
  (let [result (d/q '[:find ?e
                      :in $ ?attr ?value
                      :where [?e ?attr ?value]]
                    (d/db conn) attr value)
        eids (map first result)]
    (map #(d/pull (d/db conn) '[*] %) eids)))

(defn get-all-ads [conn]
  (let [result (d/q '[:find ?e
                      :where [?e :ad/id]]
                    (d/db conn))]
    (map #(d/pull (d/db conn) '[*] (first %)) result)))

(defn update-ad! [conn ad-id update-map]
  (let [eid (ffirst (d/q '[:find ?e :in $ ?id :where [?e :ad/id ?id]]
                         (d/db conn) (if (uuid? ad-id) ad-id (java.util.UUID/fromString ad-id))))]
    (when eid
      (d/transact conn {:tx-data [(merge {:db/id eid} update-map)]})
      (d/pull (d/db conn) '[*] eid))))

(defn delete-ad! [conn ad-id]
  (let [eid (ffirst (d/q '[:find ?e :in $ ?id :where [?e :ad/id ?id]]
                         (d/db conn) (if (uuid? ad-id) ad-id (java.util.UUID/fromString ad-id))))]
    (when eid
      (d/transact conn {:tx-data [[:db/retractEntity eid]]})
      true)))
