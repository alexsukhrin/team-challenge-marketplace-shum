(ns team-challenge.user-api-test
  (:require [clojure.test :refer [deftest testing is]]
            [cheshire.core :as json]
            [ring.mock.request :as mock]
            [reitit.ring :as reitit.ring]
            [team-challenge.api.routes :as api.routes]))

(defn parse-body [resp]
  (json/parse-string (:body resp) true))

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