(ns marketplace-shum.ads.handler
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [marketplace-shum.ads.domain :as domain]
            [marketplace-shum.ads.repository :as ads-repo]
            [marketplace-shum.infra.db :refer [db]]
            [clojure.set :as set]
            [marketplace-shum.aws.service :as aws]
            [marketplace-shum.infra.config :as config]))

(defn get-categories [_]
  (let [categories (edn/read-string (slurp (io/resource "schemas/category.edn")))
        categories-with-url (mapv #(update % :ad-category/photo (fn [photo] (str "/category/" photo))) categories)]
    {:status 200
     :body categories-with-url}))

(defn get-attributes [_]
  (let [attributes (edn/read-string (slurp (io/resource "schemas/ads-enums.edn")))
        grouped (->> attributes
                     (map :db/ident)
                     (group-by #(-> (str %)
                                    (str/split #"/")
                                    first
                                    (str/split #":")
                                    last))
                     (map (fn [[k vs]]
                            [k (mapv #(-> % str (str/split #":") last) vs)]))
                     (into {}))]
    {:status 200
     :body grouped}))

(defonce ads-db (atom []))

(defn qualify-ad-keys [ad]
  (into {}
        (map (fn [[k v]]
               (if (namespace k)
                 [k v]
                 [(keyword "ad" (name k)) v])))
        ad))

(defn create-ad [{{:keys [multipart]} :parameters}]
  (println "[DEBUG] multipart:" multipart)
  (let [photos (or (:ad/photos multipart) [])
        bucket (get-in config/*config* [:s3 :bucket])
        uploaded-urls (mapv (fn [photo]
                              (let [filename (:filename photo)
                                    content-type (:content-type photo)
                                    tempfile (:tempfile photo)
                                    file-bytes (java.nio.file.Files/readAllBytes (.toPath tempfile))
                                    key (str "ads/" (java.util.UUID/randomUUID) "-" filename)]
                                (aws/upload-file! bucket key file-bytes content-type)
                                (aws/generate-url bucket key)))
                            photos)
        base-body (dissoc multipart :ad/photos)
        body (-> base-body
                 (assoc :ad/photos (mapv #(hash-map :ad-photo/url %) uploaded-urls))
                 (update :ad/price #(when % (bigdec (str %))))
                 (update :ad/condition #(when % [:db/ident (if (keyword? %) % (keyword %))]))
                 (update :ad/categories #(when % (mapv (fn [cat] [:db/ident (if (keyword? cat) cat (keyword "ad-category" (name cat)))]) %))))
        raw-categories (:ad/categories multipart)]
    (println "[DEBUG] body after photo upload:" body)
    (let [allowed-keys (conj (domain/allowed-keys-for-categories raw-categories) :ad/categories)
          body-keys (set (keys body))
          extra-keys (clojure.set/difference body-keys allowed-keys)]
      (println "[DEBUG] allowed-keys:" allowed-keys)
      (println "[DEBUG] body-keys:" body-keys)
      (println "[DEBUG] extra-keys:" extra-keys)
      (if (seq extra-keys)
        {:status 400
         :body {:error (str "Not allowed attributes for categories: " (pr-str extra-keys))}}
        (let [ad-id (random-uuid)
              ad (-> body
                     (assoc :ad/id ad-id :ad/created-at (java.util.Date.)))]
          (println "[DEBUG] ad-id:" ad-id)
          (println "[DEBUG] ad to save:" ad)
          (let [create-result (ads-repo/create-ad! db ad)]
            (println "[DEBUG] create-result:" create-result)
            (let [found (ads-repo/find-ad-by-id db ad-id)]
              (println "[DEBUG] found by id:" found)
              {:status 201
               :body found})))))))

(defn get-all-ads [_]
  {:status 200
   :body (ads-repo/get-all-ads db)})

(defn get-ad-by-id [{{:keys [id]} :path-params}]
  (if-let [ad (ads-repo/find-ad-by-id db id)]
    {:status 200 :body ad}
    {:status 404 :body {:error "Ad not found"}}))

(defn update-ad [{{:keys [id]} :path-params {:keys [body]} :parameters}]
  (if-let [ad (ads-repo/update-ad! db id body)]
    {:status 200 :body ad}
    {:status 404 :body {:error "Ad not found"}}))

(defn delete-ad [{{:keys [id]} :path-params}]
  (if (ads-repo/delete-ad! db id)
    {:status 204}
    {:status 404 :body {:error "Ad not found"}}))
