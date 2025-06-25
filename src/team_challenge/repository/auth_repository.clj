(ns team-challenge.repository.auth-repository
  (:require [datomic.client.api :as d]
            [team-challenge.db :as db]))

(defn is-token-blacklisted?
  "Checks if the given JTI is blacklisted."
  [jti]
  (some? (d/q '[:find ?e :in $ ?jti :where [?e :auth/blacklisted-token ?jti]]
              (d/db db/conn) (str jti))))

(defn is-refresh-token-revoked?
  "Checks if the given JTI is in the revoked refresh token list."
  [jti]
  (not (some? (d/q '[:find ?e :in $ ?jti :where [?e :auth/refresh-token ?jti]]
                   (d/db db/conn) (str jti)))))

(defn add-token-to-blacklist!
  "Adds the given JTI to the blacklist."
  [jti]
  @(d/transact db/conn {:tx-data [{:auth/blacklisted-token (str jti)}]}))

(defn add-refresh-token-to-revoked-list!
  "Removes the given JTI from the valid refresh token list (revokes it)."
  [jti]
  (let [dbval (d/db db/conn)
        eid (ffirst (d/q '[:find ?e :in $ ?jti :where [?e :auth/refresh-token ?jti]] dbval (str jti)))]
    (when eid
      @(d/transact db/conn {:tx-data [[:db.fn/retractEntity eid]]}))))
