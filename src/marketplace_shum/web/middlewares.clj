(ns marketplace-shum.web.middlewares
  (:require
   [reitit.dev.pretty :as pretty]
   [muuntaja.core :as m]
   [reitit.swagger :as swagger]
   [reitit.coercion.spec]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.spec :as spec]
   [ring.util.response :as response]
   [ring.middleware.cors :as cors]
   [clojure.walk :as walk]
   [clojure.string :as str]
   [reitit.ring.middleware.dev :as dev]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [marketplace-shum.auth.service :as auth-service]))

(defn wrap-authentication
  "Middleware to authenticate a request using a JWT in the Authorization header."
  [handler]
  (fn [request]
    (let [auth-header (get-in request [:headers "authorization"])
          [scheme token] (when auth-header (str/split auth-header #" " 2))]
      (cond
        (nil? auth-header)
        (-> (response/response {:error "token_missing"
                                :message "Authorization header is required"})
            (response/status 401))

        (not= (str/lower-case scheme) "bearer")
        (-> (response/response {:error "invalid_scheme"
                                :message "Authorization scheme must be Bearer"})
            (response/status 401))

        (or (nil? token) (str/blank? token))
        (-> (response/response {:error "token_missing"
                                :message "Bearer token is missing"})
            (response/status 401))

        :else
        (if-let [claims (auth-service/verify-access-token token)]
          (handler (assoc request :user claims))
          (-> (response/response {:message "Unauthorized"})
              (response/status 401)))))))

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
                  :access-control-allow-methods [:get :put :post :delete :patch]))

(defn wrap-keyword-query-params
  "Middleware to keywordize all query params."
  [handler]
  (fn [request]
    (handler (update request :query-params walk/keywordize-keys))))

(def common-middleware
  [wrap-cors
   wrap-exceptions])

(def api-middleware
  [wrap-keyword-query-params])

(def middleware {:reitit.middleware/transform dev/print-request-diffs
                 :validate spec/validate ;; enable spec validation for route data
              ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
                 :exception pretty/exception
                 :data {:coercion reitit.coercion.spec/coercion
                        :muuntaja m/instance
                        :middleware [;; swagger feature
                                     swagger/swagger-feature
                                     ;; query-params & form-params
                                     parameters/parameters-middleware
                                     ;; content-negotiation
                                     muuntaja/format-negotiate-middleware
                                     ;; encoding response body
                                     muuntaja/format-response-middleware
                                     ;; exception handling
                                     exception/exception-middleware
                                     ;; decoding request body
                                     muuntaja/format-request-middleware
                                     ;; coercing response bodys
                                     coercion/coerce-response-middleware
                                     ;; coercing request parameters
                                     coercion/coerce-request-middleware
                                     ;; multipart
                                     multipart/multipart-middleware]}})