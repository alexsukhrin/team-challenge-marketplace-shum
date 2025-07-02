(ns team-challenge.api.routes
  (:require
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [team-challenge.api.middleware :as middleware]
   [team-challenge.api.user-controller :as user-controller]
   [team-challenge.api.product-controller :as product-controller]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.middleware.parameters :refer [parameters-middleware]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]))

(def common-middleware
  [middleware/wrap-cors
   middleware/wrap-exceptions])

(def api-middleware
  [wrap-multipart-params
   middleware/wrap-json-response
   middleware/wrap-json-body
   parameters-middleware
   middleware/wrap-keyword-query-params])

;; --------------------------
;; Swagger routes
;; --------------------------

(def swagger-routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "Marketplace API"
                            :description "swagger api docs"
                            :version "0.0.1"}
                     :securityDefinitions {:apiAuth {:type :apiKey
                                                     :in :header
                                                     :name "authorization"}}
                     :tags [{:name "auth" :description "registration and authorization routes api"}
                            {:name "users", :description "create users routes api"}
                            {:name "products", :description "create products routes api"}]}
           :handler (swagger/create-swagger-handler)}}]

   ["/docs/*"
    {:get {:no-doc true
           :handler (swagger-ui/create-swagger-ui-handler {:url "/swagger.json"})}}]])

;; --------------------------
;; Auth routes
;; --------------------------

(def auth-routes
  ["/auth"
   {:tags #{"auth"}}

   ["/register"
    {:post {:summary "Register a new user"
            :description "This route does not require authorization."
            :parameters {:body ::user-controller/register-params}
            :response {200 {:body {:message string?}}}
            :handler #'user-controller/register-user-handler}}]

   ["/login"
    {:post {:summary "Login and get token pair"
            :parameters {:body ::user-controller/login-params}
            :handler #'user-controller/login-handler}}]

   ["/confirm-email"
    {:get {:summary "Confirm email with token"
           :parameters {:query ::user-controller/confirm-email-params}
           :handler #'user-controller/confirm-email-handler}}]

   ["/logout"
    {:post {:summary "Logout user"
            :handler #'user-controller/logout-handler}}]

   ["/refresh"
    {:post {:summary "Get a new token pair using a refresh token"
            :parameters {:body ::user-controller/refresh-params}
            :handler #'user-controller/refresh-token-handler}}]

   ["/request-password-reset"
    {:post {:summary "Request password reset"
            :parameters {:body ::user-controller/request-reset-params}
            :handler #'user-controller/request-password-reset-handler}}]

   ["/reset-password"
    {:post {:summary "Reset password with token"
            :parameters {:body ::user-controller/reset-password-params}
            :handler #'user-controller/reset-password-handler}}]])

;; --------------------------
;; User routes
;; --------------------------

(def user-routes
  ["/users"
   {:tags #{"users"}}

   ["/me"
    {:get {:summary "Get current user info (protected)"
           :middleware [middleware/wrap-authentication]
           :handler #'user-controller/get-current-user-handler}}]

   ["/:user_id/product-categories"
    {:get #'user-controller/list-user-product-categories
     :post #'user-controller/add-user-product-category}]
   ["/:user_id/product-categories/:category_id"
    {:delete #'user-controller/remove-user-product-category}]])

;; --------------------------
;; Product reference routes
;; --------------------------

(def product-reference-routes
  [;; Product Categories
   ""
   {:tags #{"products"}}

   ["/product-categories"
    {:get #'product-controller/list-product-categories-handler
     :post #'product-controller/create-product-category-handler}]
   ["/product-categories/:id"
    {:get #'product-controller/get-product-category-handler
     :put #'product-controller/update-product-category-handler
     :delete #'product-controller/delete-product-category-handler}]

   ;; Product Colors
   ["/product-colors"
    {:get #'product-controller/list-product-colors-handler
     :post #'product-controller/create-product-color-handler}]
   ["/product-colors/:id"
    {:get #'product-controller/get-product-color-handler
     :put #'product-controller/update-product-color-handler
     :delete #'product-controller/delete-product-color-handler}]

   ;; Product Sizes
   ["/product-sizes"
    {:get #'product-controller/list-product-sizes-handler
     :post #'product-controller/create-product-size-handler}]
   ["/product-sizes/:id"
    {:get #'product-controller/get-product-size-handler
     :put #'product-controller/update-product-size-handler
     :delete #'product-controller/delete-product-size-handler}]

   ;; Product Materials
   ["/product-materials"
    {:get #'product-controller/list-product-materials-handler
     :post #'product-controller/create-product-material-handler}]
   ["/product-materials/:id"
    {:get #'product-controller/get-product-material-handler
     :put #'product-controller/update-product-material-handler
     :delete #'product-controller/delete-product-material-handler}]

   ;; Product Genders
   ["/product-genders"
    {:get #'product-controller/list-product-genders-handler
     :post #'product-controller/create-product-gender-handler}]
   ["/product-genders/:id"
    {:get #'product-controller/get-product-gender-handler
     :put #'product-controller/update-product-gender-handler
     :delete #'product-controller/delete-product-gender-handler}]

   ;; Product Clothing Sizes
   ["/product-clothing-sizes"
    {:get #'product-controller/get-all-product-clothing-sizes
     :post #'product-controller/create-product-clothing-size}]
   ["/product-clothing-sizes/:id"
    {:get #'product-controller/get-product-clothing-size
     :put #'product-controller/update-product-clothing-size
     :delete #'product-controller/delete-product-clothing-size}]

   ;; Payment Options
   ["/payment-options"
    {:get #'product-controller/get-all-payment-options
     :post {:summary "Create payment options"
            :parameters {:body ::product-controller/payment-options-params}
            :handler #'product-controller/create-payment-option}}]
   ["/payment-options/:id"
    {:get #'product-controller/get-payment-option
     :put #'product-controller/update-payment-option
     :delete #'product-controller/delete-payment-option}]

   ;; Delivery Options
   ["/delivery-options"
    {:get #'product-controller/get-all-delivery-options
     :post #'product-controller/create-delivery-option}]
   ["/delivery-options/:id"
    {:get #'product-controller/get-delivery-option
     :put #'product-controller/update-delivery-option
     :delete #'product-controller/delete-delivery-option}]

   ;; Product Photos
   ["/products/:product_id/photos"
    {:get #'product-controller/list-product-photos
     :post #'product-controller/create-product-photo}]
   ["/products/:product_id/photos/:photo_id"
    {:delete #'product-controller/delete-product-photo}]

   ;; Product <-> Colors
   ["/products/:product_id/colors"
    {:get #'product-controller/list-product-colors-for-product-handler
     :post #'product-controller/add-product-color-handler}]
   ["/products/:product_id/colors/:color_id"
    {:delete #'product-controller/remove-product-color-handler}]

   ;; Product <-> Sizes
   ["/products/:product_id/sizes"
    {:get #'product-controller/list-product-sizes-for-product-handler
     :post #'product-controller/add-product-size-handler}]
   ["/products/:product_id/sizes/:size_id"
    {:delete #'product-controller/remove-product-size-handler}]

   ;; Product <-> Materials (with value)
   ["/products/:product_id/materials"
    {:get #'product-controller/list-product-materials-for-product
     :post #'product-controller/add-product-material-for-product}]
   ["/products/:product_id/materials/:characteristic_id"
    {:delete #'product-controller/remove-product-material-for-product}]

   ;; Product <-> Genders
   ["/products/:product_id/genders"
    {:get #'product-controller/list-product-genders-for-product-handler
     :post #'product-controller/add-product-gender-handler}]
   ["/products/:product_id/genders/:gender_id"
    {:delete #'product-controller/remove-product-gender-handler}]

   ;; Product <-> Clothing Sizes
   ["/products/:product_id/clothing-sizes"
    {:get #'product-controller/list-product-clothing-sizes-for-product-handler
     :post #'product-controller/add-product-clothing-size-handler}]
   ["/products/:product_id/clothing-sizes/:clothing_size_id"
    {:delete #'product-controller/remove-product-clothing-size-handler}]

   ;; Product <-> Categories
   ["/products/:product_id/categories"
    {:get #'product-controller/list-product-categories-for-product-handler
     :post #'product-controller/add-product-category-handler}]
   ["/products/:product_id/categories/:category_id"
    {:delete #'product-controller/remove-product-category-handler}]])

;; --------------------------
;; API router
;; --------------------------

(def api-routes
  ["/api/v1"
   {:middleware api-middleware}
   auth-routes
   user-routes
   product-reference-routes])

;; --------------------------
;; Main router
;; --------------------------

(defn make-routes []
  (ring/router
   [["" {:middleware common-middleware
         :get {:handler (fn [_] {:status 302
                                 :headers {"Location" "/api-docs/"}
                                 :body ""})}
         :options {:strip-extra-keys false
                   :handler (fn [_] {:status 200})}}
     ;; Add sections here
     swagger-routes
     api-routes]]
   {:data {:coercion spec-coercion/coercion}
    :exception
    {:coercion
     {:default
      (fn [e _]
        {:status 400
         :body {:error "validation"
                :message "Invalid request data"
                :details (ex-data e)}})}}}))
