(ns marketplace-shum.aws.service
  (:require [cognitect.aws.client.api :as aws]
            [mount.core :refer [defstate]]
            [marketplace-shum.infra.config :as config]))

(defstate client-s3
  :start (aws/client {:api :s3
                      :region (get-in config/*config* [:s3 :region])}))

(defn upload-file!
  [bucket key file-bytes content-type]
  (aws/invoke client-s3
              {:op :PutObject
               :request {:Bucket bucket
                         :Key key
                         :Body (java.io.ByteArrayInputStream. file-bytes)
                         :ContentType content-type}}))

(defn delete-file!
  [bucket key]
  (aws/invoke client-s3
              {:op :DeleteObject
               :request {:Bucket bucket
                         :Key key}}))

(defn generate-url
  [bucket key]
  (str "https://" bucket ".s3.amazonaws.com/" key))
