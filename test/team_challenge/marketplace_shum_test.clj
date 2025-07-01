(ns team-challenge.marketplace-shum-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [team-challenge.repository.user-repository :as user-repo]
            [mount.core :as mount]
            [team-challenge.web :as web]
            [team-challenge.db :as db]
            [team-challenge.config :as config]
            [team-challenge.migrate :as migrate]
            [team-challenge.service.email-service :as email-service]
            [ring.mock.request :as mock]
            [reitit.ring :as reitit.ring]
            [team-challenge.api.routes :as api.routes]))

(defn parse-body [resp]
  (json/parse-string (:body resp) true))

(defn mock-send-confirmation-email [& _]
  (println "[MOCK] send-confirmation-email called"))

(use-fixtures :once
  (fn [f]
    (mount/start #'config/*config*)
    (mount/start #'migrate/migrations)
    (with-redefs [team-challenge.service.email-service/send-confirmation-email mock-send-confirmation-email]
      (mount/start #'db/datasource #'web/http-server)
      (try
        (f)
        (finally
          (mount/stop))))))

(deftest register-confirm-login-flow
  (testing "User registration, email confirmation, and login flow"
    (let [host (get-in config/*config* [:web-server :host])
          port (get-in config/*config* [:web-server :port])
          api-auth-url (str host ":" port "/api/v1/auth")
          email  (str "testuser-" (System/currentTimeMillis) "@example.com")
          password "TestPassword123!"
          first-name "Test"
          last-name "User"]
      (let [resp (http/post (str api-auth-url "/register")
                            {:headers {"Content-Type" "application/json"}
                             :body (json/generate-string {:email email
                                                          :password password
                                                          :first_name first-name
                                                          :last_name last-name})
                             :throw-exceptions false})
            body (parse-body resp)]
        (is (= 201 (:status resp)))
        (is (= "User registered. Please check your email for a confirmation link." (:message body))))

      ;; Get confirmation token from DB (simulate email)
      (let [user (user-repo/get-user-by-email email)
            token (:users/email_confirmation_token user)]
        (is (string? token))
        ;; Confirm email
        (let [resp (http/get (str api-auth-url "/confirm-email")
                             {:query-params {:token token}})
              body (parse-body resp)]
          (is (= 200 (:status resp)))
          (is (= "Email confirmed successfully. You can now log in." (:message body)))))

      ;; Login
      (let [resp (http/post (str api-auth-url "/login")
                            {:headers {"Content-Type" "application/json"}
                             :body (json/generate-string {:email email :password password})})
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (string? (:access-token body)))
        (is (string? (:refresh-token body)))))))

(deftest product-crud-api-test
  (let [app (web/http-server) ; or use (reitit.ring/ring-handler (routes/make-routes)) if needed
        product {:name "Test Product"
                 :description "Test description"
                 :price 123.45
                 :quantity 10
                 :user_id "00000000-0000-0000-0000-000000000001"}
        create-resp (app (-> (mock/request :post "/api/v1/products")
                             (mock/json-body product)))
        created (parse-body create-resp)
        product-id (:id created)]
    (testing "Create product"
      (is (= 201 (:status create-resp)))
      (is (= (:name created) "Test Product")))
    (testing "Get product"
      (let [resp (app (mock/request :get (str "/api/v1/products/" product-id)))
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (:id body) product-id))))
    (testing "Update product"
      (let [resp (app (-> (mock/request :put (str "/api/v1/products/" product-id))
                          (mock/json-body (assoc product :name "Updated Product"))))
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (:name body) "Updated Product"))))
    (testing "List products"
      (let [resp (app (mock/request :get "/api/v1/products"))
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (some #(= (:id %) product-id) body))))
    (testing "Delete product"
      (let [resp (app (mock/request :delete (str "/api/v1/products/" product-id)))]
        (is (= 204 (:status resp))))
      (let [resp (app (mock/request :get (str "/api/v1/products/" product-id)))]
        (is (= 404 (:status resp)))))))

(deftest product-photo-api-test
  (with-redefs [team-challenge.service.s3-service/upload-file! (fn [& _] {:mock "ok"})
                team-challenge.service.s3-service/delete-file! (fn [& _] {:mock "ok"})]
    (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
          product {:name "Photo Product" :description "desc" :price 1 :quantity 1 :user_id "00000000-0000-0000-0000-000000000001"}
          create-prod (app (-> (mock/request :post "/api/v1/products") (mock/json-body product)))
          prod-id (:id (parse-body create-prod))
          photo-bytes (.getBytes "fake image data")
          photo-file (java.io.File/createTempFile "test-photo" ".jpg")
          _ (spit photo-file "fake image data")
          req (-> (mock/request :post (str "/api/v1/products/" prod-id "/photos"))
                  (mock/content-type "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                  (mock/body (str "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"file\"; filename=\"test.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n" "fake image data" "\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--\r\n")))
          resp (app req)
          body (parse-body resp)]
      (testing "Upload product photo"
        (is (= 201 (:status resp)))
        (is (:url body)))
      (testing "List product photos"
        (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/photos")))
              body (parse-body resp)]
          (is (= 200 (:status resp)))))
      (testing "Delete product photo"
        (let [photo-id (:id (first (parse-body (app (mock/request :get (str "/api/v1/products/" prod-id "/photos"))))))
              resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/photos/" photo-id)))]
          (is (= 204 (:status resp))))))))

(deftest product-color-join-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        product {:name "Join Product" :description "desc" :price 1 :quantity 1 :user_id "00000000-0000-0000-0000-000000000001"}
        color {:name "Red" :hex "#ff0000" :description "red"}
        create-prod (app (-> (mock/request :post "/api/v1/products") (mock/json-body product)))
        prod-id (:id (parse-body create-prod))
        create-color (app (-> (mock/request :post "/api/v1/product-colors") (mock/json-body color)))
        color-id (:id (:color (parse-body create-color)))]
    (testing "Add color to product"
      (let [resp (app (-> (mock/request :post (str "/api/v1/products/" prod-id "/colors")) (mock/json-body {:color_id color-id})))]
        (is (= 201 (:status resp)))))
    (testing "List product colors"
      (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/colors")))]
        (is (= 200 (:status resp)))))
    (testing "Remove color from product"
      (let [resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/colors/" color-id)))]
        (is (= 204 (:status resp)))))))

(deftest product-category-crud-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        category {:name "Test Category" :photo "cat.jpg"}
        create-cat (app (-> (mock/request :post "/api/v1/product-categories") (mock/json-body category)))
        cat-id (:id (:category (parse-body create-cat)))]
    (testing "Create category"
      (is (= 201 (:status create-cat))))
    (testing "Get category"
      (let [resp (app (mock/request :get (str "/api/v1/product-categories/" cat-id)))]
        (is (= 200 (:status resp)))))
    (testing "Update category"
      (let [resp (app (-> (mock/request :put (str "/api/v1/product-categories/" cat-id)) (mock/json-body {:name "Updated Cat" :photo "cat2.jpg"})))
            body (:category (parse-body resp))]
        (is (= 200 (:status resp)))
        (is (= (:name body) "Updated Cat"))))
    (testing "List categories"
      (let [resp (app (mock/request :get "/api/v1/product-categories"))]
        (is (= 200 (:status resp)))))
    (testing "Delete category"
      (let [resp (app (mock/request :delete (str "/api/v1/product-categories/" cat-id)))]
        (is (= 204 (:status resp)))))))

(deftest product-color-crud-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        color {:name "Blue" :hex "#0000ff" :description "blue color"}
        create-color (app (-> (mock/request :post "/api/v1/product-colors") (mock/json-body color)))
        color-id (:id (:color (parse-body create-color)))]
    (testing "Create color"
      (is (= 201 (:status create-color))))
    (testing "Get color"
      (let [resp (app (mock/request :get (str "/api/v1/product-colors/" color-id)))]
        (is (= 200 (:status resp)))))
    (testing "Update color"
      (let [resp (app (-> (mock/request :put (str "/api/v1/product-colors/" color-id)) (mock/json-body {:name "Updated Blue" :hex "#00f" :description "upd"})))
            body (:color (parse-body resp))]
        (is (= 200 (:status resp)))
        (is (= (:name body) "Updated Blue"))))
    (testing "List colors"
      (let [resp (app (mock/request :get "/api/v1/product-colors"))]
        (is (= 200 (:status resp)))))
    (testing "Delete color"
      (let [resp (app (mock/request :delete (str "/api/v1/product-colors/" color-id)))]
        (is (= 204 (:status resp)))))))

(deftest product-size-crud-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        size {:name "Large" :description "Large size"}
        create-size (app (-> (mock/request :post "/api/v1/product-sizes") (mock/json-body size)))
        size-id (:id (:size (parse-body create-size)))]
    (testing "Create size"
      (is (= 201 (:status create-size))))
    (testing "Get size"
      (let [resp (app (mock/request :get (str "/api/v1/product-sizes/" size-id)))]
        (is (= 200 (:status resp)))))
    (testing "Update size"
      (let [resp (app (-> (mock/request :put (str "/api/v1/product-sizes/" size-id)) (mock/json-body {:name "XL" :description "Extra large"})))
            body (:size (parse-body resp))]
        (is (= 200 (:status resp)))
        (is (= (:name body) "XL"))))
    (testing "List sizes"
      (let [resp (app (mock/request :get "/api/v1/product-sizes"))]
        (is (= 200 (:status resp)))))
    (testing "Delete size"
      (let [resp (app (mock/request :delete (str "/api/v1/product-sizes/" size-id)))]
        (is (= 204 (:status resp)))))))

(deftest product-material-crud-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        material {:name "Cotton" :description "Natural material"}
        create-material (app (-> (mock/request :post "/api/v1/product-materials") (mock/json-body material)))
        material-id (:id (:material (parse-body create-material)))]
    (testing "Create material"
      (is (= 201 (:status create-material))))
    (testing "Get material"
      (let [resp (app (mock/request :get (str "/api/v1/product-materials/" material-id)))]
        (is (= 200 (:status resp)))))
    (testing "Update material"
      (let [resp (app (-> (mock/request :put (str "/api/v1/product-materials/" material-id)) (mock/json-body {:name "Wool" :description "Warm material"})))
            body (:material (parse-body resp))]
        (is (= 200 (:status resp)))
        (is (= (:name body) "Wool"))))
    (testing "List materials"
      (let [resp (app (mock/request :get "/api/v1/product-materials"))]
        (is (= 200 (:status resp)))))
    (testing "Delete material"
      (let [resp (app (mock/request :delete (str "/api/v1/product-materials/" material-id)))]
        (is (= 204 (:status resp)))))))

(deftest product-gender-crud-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        gender {:name "Unisex" :description "For all"}
        create-gender (app (-> (mock/request :post "/api/v1/product-genders") (mock/json-body gender)))
        gender-id (:id (:gender (parse-body create-gender)))]
    (testing "Create gender"
      (is (= 201 (:status create-gender))))
    (testing "Get gender"
      (let [resp (app (mock/request :get (str "/api/v1/product-genders/" gender-id)))]
        (is (= 200 (:status resp)))))
    (testing "Update gender"
      (let [resp (app (-> (mock/request :put (str "/api/v1/product-genders/" gender-id)) (mock/json-body {:name "Male" :description "For men"})))
            body (:gender (parse-body resp))]
        (is (= 200 (:status resp)))
        (is (= (:name body) "Male"))))
    (testing "List genders"
      (let [resp (app (mock/request :get "/api/v1/product-genders"))]
        (is (= 200 (:status resp)))))
    (testing "Delete gender"
      (let [resp (app (mock/request :delete (str "/api/v1/product-genders/" gender-id)))]
        (is (= 204 (:status resp)))))))

(deftest product-clothing-size-crud-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        size {:name "M" :description "Medium"}
        create-size (app (-> (mock/request :post "/api/v1/product-clothing-sizes") (mock/json-body size)))
        size-id (:id (parse-body create-size))]
    (testing "Create clothing size"
      (is (= 201 (:status create-size))))
    (testing "Get clothing size"
      (let [resp (app (mock/request :get (str "/api/v1/product-clothing-sizes/" size-id)))]
        (is (= 200 (:status resp)))))
    (testing "Update clothing size"
      (let [resp (app (-> (mock/request :put (str "/api/v1/product-clothing-sizes/" size-id)) (mock/json-body {:name "L" :description "Large"})))
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (:name body) "L"))))
    (testing "List clothing sizes"
      (let [resp (app (mock/request :get "/api/v1/product-clothing-sizes"))]
        (is (= 200 (:status resp)))))
    (testing "Delete clothing size"
      (let [resp (app (mock/request :delete (str "/api/v1/product-clothing-sizes/" size-id)))]
        (is (= 204 (:status resp)))))))

(deftest payment-option-crud-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        option {:name "Card" :description "Credit card"}
        create-option (app (-> (mock/request :post "/api/v1/payment-options") (mock/json-body option)))
        option-id (:id (parse-body create-option))]
    (testing "Create payment option"
      (is (= 201 (:status create-option))))
    (testing "Get payment option"
      (let [resp (app (mock/request :get (str "/api/v1/payment-options/" option-id)))]
        (is (= 200 (:status resp)))))
    (testing "Update payment option"
      (let [resp (app (-> (mock/request :put (str "/api/v1/payment-options/" option-id)) (mock/json-body {:name "Cash" :description "Cash payment"})))
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (:name body) "Cash"))))
    (testing "List payment options"
      (let [resp (app (mock/request :get "/api/v1/payment-options"))]
        (is (= 200 (:status resp)))))
    (testing "Delete payment option"
      (let [resp (app (mock/request :delete (str "/api/v1/payment-options/" option-id)))]
        (is (= 204 (:status resp)))))))

(deftest delivery-option-crud-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        option {:name "Nova Poshta" :description "Delivery service"}
        create-option (app (-> (mock/request :post "/api/v1/delivery-options") (mock/json-body option)))
        option-id (:id (parse-body create-option))]
    (testing "Create delivery option"
      (is (= 201 (:status create-option))))
    (testing "Get delivery option"
      (let [resp (app (mock/request :get (str "/api/v1/delivery-options/" option-id)))]
        (is (= 200 (:status resp)))))
    (testing "Update delivery option"
      (let [resp (app (-> (mock/request :put (str "/api/v1/delivery-options/" option-id)) (mock/json-body {:name "Ukrposhta" :description "National post"})))
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (:name body) "Ukrposhta"))))
    (testing "List delivery options"
      (let [resp (app (mock/request :get "/api/v1/delivery-options"))]
        (is (= 200 (:status resp)))))
    (testing "Delete delivery option"
      (let [resp (app (mock/request :delete (str "/api/v1/delivery-options/" option-id)))]
        (is (= 204 (:status resp)))))))

(deftest product-join-tables-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        product {:name "Join Product" :description "desc" :price 1 :quantity 1 :user_id "00000000-0000-0000-0000-000000000001"}
        create-prod (app (-> (mock/request :post "/api/v1/products") (mock/json-body product)))
        prod-id (:id (parse-body create-prod))]
    ;; Sizes
    (let [size {:name "S" :description "Small"}
          create-size (app (-> (mock/request :post "/api/v1/product-sizes") (mock/json-body size)))
          size-id (:id (:size (parse-body create-size)))]
      (testing "Add size to product"
        (let [resp (app (-> (mock/request :post (str "/api/v1/products/" prod-id "/sizes")) (mock/json-body {:size_id size-id})))]
          (is (= 201 (:status resp)))))
      (testing "List product sizes"
        (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/sizes")))]
          (is (= 200 (:status resp)))))
      (testing "Remove size from product"
        (let [resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/sizes/" size-id)))]
          (is (= 204 (:status resp))))))
    ;; Materials
    (let [material {:name "Polyester" :description "Synthetic"}
          create-material (app (-> (mock/request :post "/api/v1/product-materials") (mock/json-body material)))
          material-id (:id (:material (parse-body create-material)))]
      (testing "Add material to product"
        (let [resp (app (-> (mock/request :post (str "/api/v1/products/" prod-id "/materials")) (mock/json-body {:material_id material-id})))]
          (is (= 201 (:status resp)))))
      (testing "List product materials"
        (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/materials")))]
          (is (= 200 (:status resp)))))
      (testing "Remove material from product"
        (let [resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/materials/" material-id)))]
          (is (= 204 (:status resp))))))
    ;; Genders
    (let [gender {:name "Female" :description "For women"}
          create-gender (app (-> (mock/request :post "/api/v1/product-genders") (mock/json-body gender)))
          gender-id (:id (:gender (parse-body create-gender)))]
      (testing "Add gender to product"
        (let [resp (app (-> (mock/request :post (str "/api/v1/products/" prod-id "/genders")) (mock/json-body {:gender_id gender-id})))]
          (is (= 201 (:status resp)))))
      (testing "List product genders"
        (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/genders")))]
          (is (= 200 (:status resp)))))
      (testing "Remove gender from product"
        (let [resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/genders/" gender-id)))]
          (is (= 204 (:status resp))))))
    ;; Clothing Sizes
    (let [clothing-size {:name "XS" :description "Extra Small"}
          create-clothing-size (app (-> (mock/request :post "/api/v1/product-clothing-sizes") (mock/json-body clothing-size)))
          clothing-size-id (:id (parse-body create-clothing-size))]
      (testing "Add clothing size to product"
        (let [resp (app (-> (mock/request :post (str "/api/v1/products/" prod-id "/clothing-sizes")) (mock/json-body {:clothing_size_id clothing-size-id})))]
          (is (= 201 (:status resp)))))
      (testing "List product clothing sizes"
        (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/clothing-sizes")))]
          (is (= 200 (:status resp)))))
      (testing "Remove clothing size from product"
        (let [resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/clothing-sizes/" clothing-size-id)))]
          (is (= 204 (:status resp))))))
    ;; Categories
    (let [category {:name "Outerwear" :photo "outer.jpg"}
          create-category (app (-> (mock/request :post "/api/v1/product-categories") (mock/json-body category)))
          category-id (:id (:category (parse-body create-category)))]
      (testing "Add category to product"
        (let [resp (app (-> (mock/request :post (str "/api/v1/products/" prod-id "/categories")) (mock/json-body {:category_id category-id})))]
          (is (= 201 (:status resp)))))
      (testing "List product categories"
        (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/categories")))]
          (is (= 200 (:status resp)))))
      (testing "Remove category from product"
        (let [resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/categories/" category-id)))]
          (is (= 204 (:status resp))))))
    ;; Delivery Options
    (let [option {:name "Courier" :description "Fast delivery"}
          create-option (app (-> (mock/request :post "/api/v1/delivery-options") (mock/json-body option)))
          option-id (:id (parse-body create-option))]
      (testing "Add delivery option to product"
        (let [resp (app (-> (mock/request :post (str "/api/v1/products/" prod-id "/delivery-options")) (mock/json-body {:delivery_option_id option-id})))]
          (is (= 201 (:status resp)))))
      (testing "List product delivery options"
        (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/delivery-options")))]
          (is (= 200 (:status resp)))))
      (testing "Remove delivery option from product"
        (let [resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/delivery-options/" option-id)))]
          (is (= 204 (:status resp))))))
    ;; Payment Options
    (let [option {:name "PayPal" :description "Online payment"}
          create-option (app (-> (mock/request :post "/api/v1/payment-options") (mock/json-body option)))
          option-id (:id (parse-body create-option))]
      (testing "Add payment option to product"
        (let [resp (app (-> (mock/request :post (str "/api/v1/products/" prod-id "/payment-options")) (mock/json-body {:payment_option_id option-id})))]
          (is (= 201 (:status resp)))))
      (testing "List product payment options"
        (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/payment-options")))]
          (is (= 200 (:status resp)))))
      (testing "Remove payment option from product"
        (let [resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/payment-options/" option-id)))]
          (is (= 204 (:status resp))))))))

(deftest user-product-categories-join-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        user-id "00000000-0000-0000-0000-000000000001"
        category {:name "Accessories" :photo "acc.jpg"}
        create-category (app (-> (mock/request :post "/api/v1/product-categories") (mock/json-body category)))
        category-id (:id (:category (parse-body create-category)))]
    (testing "Add category to user"
      (let [resp (app (-> (mock/request :post (str "/api/v1/users/" user-id "/product-categories")) (mock/json-body {:category_id category-id})))]
        (is (= 201 (:status resp)))))
    (testing "List user product categories"
      (let [resp (app (mock/request :get (str "/api/v1/users/" user-id "/product-categories")))]
        (is (= 200 (:status resp)))))
    (testing "Remove category from user"
      (let [resp (app (mock/request :delete (str "/api/v1/users/" user-id "/product-categories/" category-id)))]
        (is (= 204 (:status resp)))))))

(deftest product-characteristic-crud-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        characteristic {:name "Waterproof" :description "Does not absorb water"}
        create-char (app (-> (mock/request :post "/api/v1/product-characteristics") (mock/json-body characteristic)))
        char-id (:id (parse-body create-char))]
    (testing "Create characteristic"
      (is (= 201 (:status create-char))))
    (testing "Get characteristic"
      (let [resp (app (mock/request :get (str "/api/v1/product-characteristics/" char-id)))]
        (is (= 200 (:status resp)))))
    (testing "Update characteristic"
      (let [resp (app (-> (mock/request :put (str "/api/v1/product-characteristics/" char-id)) (mock/json-body {:name "Breathable" :description "Lets air through"})))
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (:name body) "Breathable"))))
    (testing "List characteristics"
      (let [resp (app (mock/request :get "/api/v1/product-characteristics"))]
        (is (= 200 (:status resp)))))
    (testing "Delete characteristic"
      (let [resp (app (mock/request :delete (str "/api/v1/product-characteristics/" char-id)))]
        (is (= 204 (:status resp)))))))

(deftest product-materials-with-value-join-api-test
  (let [app (reitit.ring/ring-handler (team-challenge.api.routes/make-routes))
        product {:name "Value Product" :description "desc" :price 1 :quantity 1 :user_id "00000000-0000-0000-0000-000000000001"}
        create-prod (app (-> (mock/request :post "/api/v1/products") (mock/json-body product)))
        prod-id (:id (parse-body create-prod))
        characteristic {:name "Density" :description "Material density"}
        create-char (app (-> (mock/request :post "/api/v1/product-characteristics") (mock/json-body characteristic)))
        char-id (:id (parse-body create-char))]
    (testing "Add material with value to product"
      (let [resp (app (-> (mock/request :post (str "/api/v1/products/" prod-id "/materials")) (mock/json-body {:characteristic_id char-id :value "200g/m2"})))]
        (is (= 201 (:status resp)))))
    (testing "List product materials with value"
      (let [resp (app (mock/request :get (str "/api/v1/products/" prod-id "/materials")))]
        (is (= 200 (:status resp)))))
    (testing "Remove material with value from product"
      (let [resp (app (mock/request :delete (str "/api/v1/products/" prod-id "/materials/" char-id)))]
        (is (= 204 (:status resp)))))))
