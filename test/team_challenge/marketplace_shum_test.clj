(ns team-challenge.marketplace-shum-test
  (:require [clojure.test :refer [deftest testing is]]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [team-challenge.repository.user-repository :as user-repo]))

(def base-url "http://localhost:3000/api/v1/auth")

(defn parse-body [resp]
  (json/parse-string (:body resp) true))

(deftest register-confirm-login-flow
  (testing "User registration, email confirmation, and login flow"
    (let [email (str "testuser-" (System/currentTimeMillis) "@example.com")
          password "TestPassword123!"
          first-name "Test"
          last-name "User"]
      ;; Register user
      (let [resp @(http/post (str base-url "/register")
                             {:headers {"Content-Type" "application/json"}
                              :body (json/generate-string {:email email
                                                           :password password
                                                           :first_name first-name
                                                           :last_name last-name})})
            body (parse-body resp)]
        (is (= 201 (:status resp)))
        (is (= "User registered. Please check your email for a confirmation link." (:message body))))

      ;; Get confirmation token from DB (simulate email)
      (let [user (user-repo/find-user-by-email email)
            token (:user/email-confirmation-token user)]
        (is (string? token))
        ;; Confirm email
        (let [resp @(http/get (str base-url "/confirm-email")
                              {:query-params {:token token}})
              body (parse-body resp)]
          (is (= 200 (:status resp)))
          (is (= "Email confirmed successfully. You can now log in." (:message body)))))

      ;; Login
      (let [resp @(http/post (str base-url "/login")
                             {:headers {"Content-Type" "application/json"}
                              :body (json/generate-string {:email email :password password})})
            body (parse-body resp)]
        (is (= 200 (:status resp)))
        (is (string? (:access-token body)))
        (is (string? (:refresh-token body)))))))

