(ns team-challenge.service.s3-service
  (:require [cognitect.aws.client.api :as aws]
            [aero.core :as aero]
            [mount.core :refer [defstate]]
            [clojure.java.io :as io]))

(defn- config []
  (aero/read-config (io/resource (str "config/" (or (System/getenv "APP_ENV") "dev") ".edn"))))

(defstate s3-client
  :start (aws/client {:api :s3 :region (get-in (config) [:s3 :region])}))

(defn upload-file!
  [bucket key file-bytes content-type]
  (aws/invoke s3-client
              {:op :PutObject
               :request {:Bucket bucket
                         :Key key
                         :Body (java.io.ByteArrayInputStream. file-bytes)
                         :ContentType content-type}}))

(defn delete-file!
  [bucket key]
  (aws/invoke s3-client
              {:op :DeleteObject
               :request {:Bucket bucket
                         :Key key}}))

(defn generate-url
  [bucket key]
  (str "https://" bucket ".s3.amazonaws.com/" key))