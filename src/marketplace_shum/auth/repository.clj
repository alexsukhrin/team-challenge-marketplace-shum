(ns marketplace-shum.auth.repository
  (:require 
   [datomic.client.api :as d]))

(defn generate-expiry []
  (java.util.Date/from
    (.plusSeconds (java.time.Instant/now) (* 10 60))))

(defn otp-not-expired? [otp-expires-at]
  (let [now (java.util.Date.)]
    (.after otp-expires-at now)))

(defn set-otp! [conn id otp]
  (let [user-eid [:user/id (if (uuid? id) id (java.util.UUID/fromString id))]
        expiry (generate-expiry)]
    (d/transact conn {:tx-data [[:db/add user-eid :user/otp otp]
                                [:db/add user-eid :user/otp-expires-at expiry]]})))

(comment

  (require '[marketplace-shum.infra.db :refer [db]])
  (require '[marketplace-shum.users.repository :as user-repo])


  )