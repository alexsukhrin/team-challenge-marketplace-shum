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
            [team-challenge.service.email-service :as email-service]))

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
