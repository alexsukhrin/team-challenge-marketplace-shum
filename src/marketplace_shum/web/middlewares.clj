(ns marketplace-shum.web.middlewares
  (:require
   [reitit.dev.pretty :as pretty]
   [muuntaja.core :as m]
   [reitit.swagger :as swagger]
   [reitit.coercion.spec]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.spec :as spec]
   [reitit.ring.middleware.dev :as dev]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]))

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