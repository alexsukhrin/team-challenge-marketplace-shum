(ns marketplace-shum.web.routes
  (:require
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.coercion.spec]
   [reitit.openapi :as openapi]
   [marketplace-shum.web.middlewares :refer [middleware api-middleware common-middleware]]
   [marketplace-shum.health.handler :as health]
   [marketplace-shum.auth.routes :as auth-routes]))

(def swagger
  ["/swagger.json"
   {:get {:no-doc true
          :swagger {:info {:title "marketplace shum api"}}
          :handler (swagger/create-swagger-handler)}}])

(def openapi
  ["/openapi.json"
   {:get {:no-doc true
          :openapi {:info {:title "marketplace shum api"
                           :description "openapi3-docs with reitit-http"
                           :version "0.0.1"}}
          :handler (openapi/create-openapi-handler)}}])

(defn swagger-ui []
  (ring/routes
   (swagger-ui/create-swagger-ui-handler
    {:path "/"
     :config {:validatorUrl nil
              :urls [{:name "swagger" :url "swagger.json"}
                     {:name "openapi" :url "openapi.json"}]
              :urls.primaryName "openapi"
              :operationsSorter "alpha"}})
   (ring/create-default-handler)))

(def v1-routes
  ["/api/v1"
   {:middleware api-middleware}
   auth-routes/routes])

(def make-routes
  (ring/ring-handler
   (ring/router
    [["" {:middleware common-middleware}
      swagger
      openapi
      health/route
      v1-routes]]
    middleware)
   (swagger-ui)))
