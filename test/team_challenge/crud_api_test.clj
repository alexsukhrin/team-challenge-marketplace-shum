(ns team-challenge.crud-api-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [mount.core :as mount]
            [team-challenge.web :as web]
            [team-challenge.db :as db]
            [team-challenge.config :as config]
            [team-challenge.migrate :as migrate]
            [team-challenge.service.email-service :as email-service]))

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
