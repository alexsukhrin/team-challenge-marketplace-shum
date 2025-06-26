(ns team-challenge.marketplace-shum-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [team-challenge.repository.user-repository :as user-repo]
            [mount.core :as mount]
            [team-challenge.web :as web]
            [team-challenge.db :as db]
            [team-challenge.config :as config]
            [datomic.api :as d]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [team-challenge.service.email-service :as email-service]))

(defn parse-body [resp]
  (json/parse-string (:body resp) true))

(defn create-test-db-and-load-schema []
  (let [uri (get-in config/*config* [:datomic :db-uri])
        _ (try (d/create-database uri) (catch Exception _ nil))
        conn (d/connect uri)
        schema-dir "resources/schema"
        schema-files (->> (io/file schema-dir)
                          file-seq
                          (filter #(.isFile %))
                          (filter #(clojure.string/ends-with? (.getName %) ".edn"))
                          (map #(.getPath %)))]
    (doseq [file schema-files]
      (let [schema (edn/read-string (slurp file))]
        (d/transact conn schema)))))

(defn mock-send-confirmation-email [& _]
  (println "[MOCK] send-confirmation-email called"))

(use-fixtures :once
  (fn [f]
    (mount/start #'config/*config*)
    (with-redefs [team-challenge.service.email-service/send-confirmation-email mock-send-confirmation-email]
      (mount/start #'db/conn #'web/http-server)
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
      (let [user (user-repo/find-user-by-email email)
            token (:user/email-confirmation-token user)]
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
