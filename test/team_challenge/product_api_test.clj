(ns team-challenge.product-api-test
  (:require [clojure.test :refer [deftest testing is]]
            [cheshire.core :as json]
            [ring.mock.request :as mock]
            [reitit.ring :as reitit.ring]
            [team-challenge.web :as web]
            [team-challenge.api.routes :as api.routes]
            [team-challenge.service.s3-service]))

(defn parse-body [resp]
  (json/parse-string (:body resp) true))

(deftest product-crud-api-test
  (let [app (web/http-server)
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
          (is (= 204 (:status resp)))))))))

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
        (is (= 204 (:status resp))))))