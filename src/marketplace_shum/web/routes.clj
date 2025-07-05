(ns marketplace-shum.web.routes
  (:require
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.coercion.spec]
   [reitit.openapi :as openapi]
   [marketplace-shum.web.middlewares :refer [middleware]]
   [marketplace-shum.health.handler :as health] 
   [marketplace-shum.users.handler :as user]))

(def swagger
  ["/swagger.json"
   {:get {:no-doc true
          :swagger {:info {:title "marketplace-shum-api"}}
          :handler (swagger/create-swagger-handler)}}])

(def openapi
  ["/openapi.json"
   {:get {:no-doc true
          :openapi {:info {:title "my-api"
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
   user/routes])

(def make-routes
  (ring/ring-handler
   (ring/router
    [swagger
     openapi
     health/route
     v1-routes]
    middleware)
   (swagger-ui)))
