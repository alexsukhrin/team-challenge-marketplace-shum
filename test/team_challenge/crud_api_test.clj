(ns team-challenge.crud-api-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [mount.core :as mount]
            [team-challenge.web :as web]
            [team-challenge.db :as db]
            [team-challenge.config :as config]
            [team-challenge.migrate :as migrate]
            [team-challenge.service.email-service :as email-service]
            [clojure.java.io]))

(defn parse-body [resp]
  (json/parse-string (:body resp) true))

(defn get-api-url [url]
  (let [host (get-in config/*config* [:web-server :host]) 
        port (get-in config/*config* [:web-server :port])]
    (str host ":" port url)))

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

(deftest payment-option-crud-api-test []
  (let [api-auth-url (get-api-url "/api/v1/payment-options") 
        params {:name "Card" :description "Credit card"}
        resp (http/post api-auth-url 
                          {:headers {"Content-Type" "application/json"}
                           :body (json/generate-string params)
                           :throw-exceptions false})
        body (parse-body resp)
        option-id (:payment_options/id body)]
    
    (testing "Create payment option"
      (is (= 201 (:status resp))))
    
    (testing "Get payment option"
      (let [resp (http/get (str api-auth-url "/" option-id))]
        (is (= 200 (:status resp)))))
    
    (testing "Update payment option"
      (let [resp (http/put (str api-auth-url "/" option-id)
                           {:headers {"Content-Type" "application/json"}
                            :body (json/generate-string {:name "Cash" :description "Cash payment"})
                            :throw-exceptions false})
            body (parse-body resp)]
         
        (is (= 200 (:status resp)))
        (is (= (:payment_options/name body) "Cash"))))
    
    (testing "List payment options"
      (let [resp (http/get api-auth-url)]
        (is (= 200 (:status resp)))))
    
    (testing "Delete payment option"
      (let [resp (http/delete (str api-auth-url "/" option-id))]
        (is (= 204 (:status resp)))))))

(deftest delivery-option-crud-api-test
  (let [api-auth-url (get-api-url "/api/v1/delivery-options") 
        params {:name "Nova Poshta" :description "Доставка по Україні"}
        resp (http/post api-auth-url 
                          {:headers {"Content-Type" "application/json"}
                           :body (json/generate-string params)
                           :throw-exceptions false})
        body (parse-body resp)
        option-id (:delivery_options/id body)]
    
    (testing "Create delivery option"
      (is (= 201 (:status resp))))
    
    (testing "Get delivery option"
      (let [resp (http/get (str api-auth-url "/" option-id))]
        (is (= 200 (:status resp)))))
    
    (testing "Update delivery option"
      (let [resp (http/put (str api-auth-url "/" option-id)
                           {:headers {"Content-Type" "application/json"}
                            :body (json/generate-string {:name "Самовивіз" :description "Забрати з магазину"})
                            :throw-exceptions false})
            body (parse-body resp)]
         
        (is (= 200 (:status resp)))
        (is (= (:delivery_options/name body) "Самовивіз"))))
    
    (testing "List delivery options"
      (let [resp (http/get api-auth-url)]
        (is (= 200 (:status resp)))))
    
    (testing "Delete delivery option"
      (let [resp (http/delete (str api-auth-url "/" option-id))]
        (is (= 204 (:status resp)))))))

(deftest product-category-crud-api-test
  (let [api-auth-url (get-api-url "/api/v1/product-categories")
        file-path "test/team_challenge/Автотовари.jpg"
        resp (http/post api-auth-url
                        {:multipart [{:name "name" :content "Test Category"}
                                     {:name "photo" :content (clojure.java.io/file file-path)}]
                         :throw-exceptions false})
        body (parse-body resp)
        option-id (get-in body [:category :product_categories/id])]
    
    (testing "Create category"
      (is (= 201 (:status resp))))
    
    (testing "Get category"
      (let [resp (http/get (str api-auth-url "/" option-id))]
        (is (= 200 (:status resp)))))
    
    (testing "Update category"
      (let [resp (http/put (str api-auth-url "/" option-id)
                           {:headers {"Content-Type" "application/json"}
                            :body (json/generate-string {:name "Самовивіз" :photo "new-photo.jpg"})
                            :throw-exceptions false})
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (get-in body [:category :product_categories/name]) "Самовивіз"))))
    
    (testing "List categories"
      (let [resp (http/get api-auth-url)]
        (is (= 200 (:status resp)))))
    
    (testing "Delete category"
      (let [resp (http/delete (str api-auth-url "/" option-id))]
        (is (= 204 (:status resp)))))))

(deftest product-color-crud-api-test
  (let [api-auth-url (get-api-url "/api/v1/product-colors")
        resp (http/post api-auth-url
                        {:headers {"Content-Type" "application/json"}
                         :body (json/generate-string {:name "Blue" :hex "#0000ff" :description "blue color"})
                         :throw-exceptions false})
        body (parse-body resp)
        color-id (get-in body [:color :product_colors/id])]
    
    (testing "Create color"
      (is (= 201 (:status resp))))
    
    (testing "Get color"
      (let [resp (http/get (str api-auth-url "/" color-id))]
        (is (= 200 (:status resp)))))
    
    (testing "Update color"
      (let [resp (http/put (str api-auth-url "/" color-id)
                           {:headers {"Content-Type" "application/json"}
                            :body (json/generate-string {:name "Red" :hex "#0000ff" :description "red color"})
                            :throw-exceptions false})
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (get-in body [:color :product_colors/name]) "Red"))))
    
    (testing "List colors"
      (let [resp (http/get api-auth-url)]
        (is (= 200 (:status resp)))))
    
    (testing "Delete color"
      (let [resp (http/delete (str api-auth-url "/" color-id))]
        (is (= 204 (:status resp)))))))

(deftest product-size-crud-api-test
  (let [api-auth-url (get-api-url "/api/v1/product-sizes")
        resp (http/post api-auth-url
                        {:headers {"Content-Type" "application/json"}
                         :body (json/generate-string {:name "Large" :description "Large size"})
                         :throw-exceptions false})
        body (parse-body resp)
        size-id (get-in body [:size :product_sizes/id])]
    
    (testing "Create size"
      (is (= 201 (:status resp))))
    
    (testing "Get size"
      (let [resp (http/get (str api-auth-url "/" size-id))]
        (is (= 200 (:status resp)))))
    
    (testing "Update size"
      (let [resp (http/put (str api-auth-url "/" size-id)
                           {:headers {"Content-Type" "application/json"}
                            :body (json/generate-string {:name "XL" :description "Extra large"})
                            :throw-exceptions false})
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (get-in body [:size :product_sizes/name]) "XL"))))
    
    (testing "List sizes"
      (let [resp (http/get api-auth-url)]
        (is (= 200 (:status resp)))))
    
    (testing "Delete size"
      (let [resp (http/delete (str api-auth-url "/" size-id))]
        (is (= 204 (:status resp)))))))

(deftest product-material-crud-api-test
  (let [api-auth-url (get-api-url "/api/v1/product-materials")
        resp (http/post api-auth-url
                        {:headers {"Content-Type" "application/json"}
                         :body (json/generate-string {:name "Large" :description "Large size"})
                         :throw-exceptions false})
        body (parse-body resp)
        material-id (get-in body [:material :materials/id])]
    
    (testing "Create material"
      (is (= 201 (:status resp))))
    
    (testing "Get material"
      (let [resp (http/get (str api-auth-url "/" material-id))]
        (is (= 200 (:status resp)))))
    
    (testing "Update material"
      (let [resp (http/put (str api-auth-url "/" material-id)
                           {:headers {"Content-Type" "application/json"}
                            :body (json/generate-string {:name "Wool" :description "Warm material"})
                            :throw-exceptions false})
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (= (get-in body [:material :materials/name]) "Wool"))))
    
    (testing "List materials"
      (let [resp (http/get api-auth-url)]
        (is (= 200 (:status resp)))))
    
    (testing "Delete material"
      (let [resp (http/delete (str api-auth-url "/" material-id))]
        (is (= 204 (:status resp)))))))

