(ns team-challenge.product-reference-api-test
  (:require [clojure.test :refer [deftest testing is]]
            [cheshire.core :as json]
            [ring.mock.request :as mock]
            [reitit.ring :as reitit.ring]
            [team-challenge.api.routes :as api.routes]))

(defn parse-body [resp]
  (json/parse-string (:body resp) true))

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