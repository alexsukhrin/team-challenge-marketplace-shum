(ns team-challenge.api.middleware
  (:require [ring.middleware.json :as json]
            [ring.util.response :as response]
            [ring.middleware.cors :as cors]
            [team-challenge.service.auth-service :as auth-service]
            [clojure.walk :as walk]
            [clojure.string :as str]))

(defn wrap-json-body
  "Parses JSON requests and keywordizes keys."
  [handler]
  (json/wrap-json-body handler {:keywords? true}))

(defn wrap-json-response
  "Middleware to serialize JSON responses."
  [handler]
  (json/wrap-json-response handler))

(defn- unauthorized-response []
  (-> (response/response {:message "Unauthorized"})
      (response/status 401)))

(defn wrap-authentication
  "Middleware to authenticate a request using a JWT in the Authorization header."
  [handler]
  (fn [request]
    (if-let [token (some-> (get-in request [:headers "authorization"])
                           (str/split #" ")
                           second)]
      (if-let [claims (auth-service/verify-access-token token)]
        (handler (assoc request :identity claims))
        (unauthorized-response))
      (unauthorized-response))))

(defn wrap-exceptions
  "Middleware that catches exceptions and returns a 500 response."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (println "Error:" e)
        {:status 500
         :body {:error "internal"
                :message "An unexpected error occurred."
                :details (.getMessage e)}}))))

(defn wrap-cors
  "Middleware to handle CORS requests."
  [handler]
  (cors/wrap-cors handler
                  :access-control-allow-origin [#".*"]
                  :access-control-allow-methods [:get :put :post :delete]))

(defn wrap-keyword-query-params
  "Middleware to keywordize all query params."
  [handler]
  (fn [request]
    (handler (update request :query-params walk/keywordize-keys))))