(ns team-challenge.register-confirm-login-flow-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [team-challenge.crud-api-test :as utils]
   [team-challenge.repository.user-repository :as user-repo]
   [clj-http.client :as http]
   [cheshire.core :as json]))

(deftest register-confirm-login-flow
  (testing "User registration, email confirmation, and login flow"
    (let [api-auth-url (utils/get-api-url "/api/v1/auth")
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
            body (utils/parse-body resp)]
        (is (= 201 (:status resp)))
        (is (= "User registered. Please check your email for a confirmation link." (:message body))))

      ;; Get confirmation token from DB (simulate email)
      (let [user (user-repo/get-user-by-email email)
            token (:users/email_confirmation_token user)]
        (is (string? token))
        ;; Confirm email
        (let [resp (http/get (str api-auth-url "/confirm-email")
                             {:query-params {:token token}})
              body (utils/parse-body resp)]
          (is (= 200 (:status resp)))
          (is (= "Email confirmed successfully. You can now log in." (:message body)))))

      ;; Login
      (let [resp (http/post (str api-auth-url "/login")
                            {:headers {"Content-Type" "application/json"}
                             :body (json/generate-string {:email email :password password})})
            body (utils/parse-body resp)]
        (is (= 200 (:status resp)))
        (is (string? (:access-token body)))
        (is (string? (:refresh-token body)))))))