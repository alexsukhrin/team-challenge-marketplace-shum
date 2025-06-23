(ns team-challenge.marketplace-shum-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [team-challenge.marketplace-shum :as system]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [postal.core :as postal]
            [team-challenge.repository.user-repository :as user-repo]
            [team-challenge.api.user-controller :as user-controller]))

(def test-port 8081)
(def base-url (str "http://localhost:" test-port))

(defn- parse-body [body]
  (json/parse-string body true))

(defn with-system [f]
  (with-redefs [team-challenge.config/config-file "dev.edn"
                team-challenge.config/*config* (assoc-in @team-challenge.config/*config* [:http-port] test-port)]
    (system/start-system)
    (f)
    (system/stop-system)))

(use-fixtures :once with-system)

(deftest auth-flow-test
  (testing "Full authentication and user flow"
    (let [user-email "test@example.com"
          user-password "password123"
          outbox (atom [])]

      (with-redefs [postal/send-message (fn [_ message] (swap! outbox conj message) {:code 0 :error :SUCCESS :message "OK"})]

        (testing "1. Register user"
          (let [response @(http/post (str base-url "/api/auth/register")
                                     {:headers {"Content-Type" "application/json"}
                                      :body (json/generate-string {:email user-email :password user-password})})
                body (parse-body (:body response))]
            (is (= 201 (:status response)))
            (is (= "User registered. Please check your email for a confirmation link." (:message body)))
            (is (= 1 (count @outbox)))
            (is (= user-email (-> @outbox first :to)))))

        (testing "2. Cannot login before email confirmation"
          (let [response @(http/post (str base-url "/api/auth/login")
                                     {:headers {"Content-Type" "application/json"}
                                      :body (json/generate-string {:email user-email :password user-password})})
                body (parse-body (:body response))]
            (is (= 401 (:status response)))
            (is (= "Invalid credentials or email not confirmed" (:message body)))))

        (testing "3. Confirm email"
          (let [user (user-repo/find-user-by-email user-email)
                token (:user/email-confirmation-token user)
                response @(http/post (str base-url "/api/auth/confirm-email")
                                     {:headers {"Content-Type" "application/json"}
                                      :body (json/generate-string {:token token})})
                body (parse-body (:body response))]
            (is (= 200 (:status response)))
            (is (= "Email confirmed successfully. You can now log in." (:message body)))))

        (testing "4. Login successfully after confirmation"
          (let [response @(http/post (str base-url "/api/auth/login")
                                     {:headers {"Content-Type" "application/json"}
                                      :body (json/generate-string {:email user-email :password user-password})})
                body (parse-body (:body response))]
            (is (= 200 (:status response)))
            (is (contains? body :access-token))
            (is (contains? body :refresh-token))

            (let [access-token (:access-token body)
                  refresh-token (:refresh-token body)]

              (testing "5. Access protected route"
                (let [me-response @(http/get (str base-url "/api/users/me")
                                            {:headers {"Authorization" (str "Bearer " access-token)}})
                      me-body (parse-body (:body me-response))]
                  (is (= 200 (:status me-response)))
                  (is (= user-email (get-in me-body [:user :user-id])))))

              (testing "6. Refresh tokens"
                (let [refresh-response @(http/post (str base-url "/api/auth/refresh")
                                                   {:headers {"Content-Type" "application/json"}
                                                    :body (json/generate-string {:refresh-token refresh-token})})
                      refresh-body (parse-body (:body refresh-response))]
                  (is (= 200 (:status refresh-response)))
                  (is (contains? refresh-body :access-token))
                  (is (not= access-token (:access-token refresh-body)))))

              (testing "7. Logout"
                (let [logout-response @(http/post (str base-url "/api/auth/logout")
                                                  {:headers {"Authorization" (str "Bearer " access-token)}})]
                  (is (= 200 (:status logout-response)))

                  (let [me-response-after-logout @(http/get (str base-url "/api/users/me")
                                                             {:headers {"Authorization" (str "Bearer " access-token)}})]
                    (is (= 401 (:status me-response-after-logout)))))))))))))

(deftest password-reset-flow-test
  (testing "Full password reset flow"
    (let [user-email "reset-test@example.com"
          user-password "password123"
          new-password "newPassword456"
          outbox (atom [])]
      ;; 1. Create a user to test with
      @(http/post (str base-url "/api/auth/register")
                  {:headers {"Content-Type" "application/json"}
                   :body (json/generate-string {:email user-email :password user-password})})
      (let [user (user-repo/find-user-by-email user-email)
            confirm-token (:user/email-confirmation-token user)]
        @(http/post (str base-url "/api/auth/confirm-email")
                    {:headers {"Content-Type" "application/json"}
                     :body (json/generate-string {:token confirm-token})}))

      (with-redefs [postal/send-message (fn [_ message] (swap! outbox conj message) {:code 0 :error :SUCCESS :message "OK"})]
        (testing "1. Request password reset"
          (let [response @(http/post (str base-url "/api/auth/request-password-reset")
                                     {:headers {"Content-Type" "application/json"}
                                      :body (json/generate-string {:email user-email})})
                body (parse-body (:body response))]
            (is (= 200 (:status response)))
            (is (contains? body :token))

            (let [reset-token (:token body)]
              (testing "2. Reset password with token"
                (let [response @(http/post (str base-url "/api/auth/reset-password")
                                           {:headers {"Content-Type" "application/json"}
                                            :body (json/generate-string {:token reset-token :password new-password})})
                      body (parse-body (:body response))]
                  (is (= 200 (:status response)))
                  (is (= "Password has been reset successfully" (:message body)))))

              (testing "3. Login with old password fails"
                (let [response @(http/post (str base-url "/api/auth/login")
                                           {:headers {"Content-Type" "application/json"}
                                            :body (json/generate-string {:email user-email :password user-password})})]
                  (is (= 401 (:status response)))))

              (testing "4. Login with new password succeeds"
                (let [response @(http/post (str base-url "/api/auth/login")
                                           {:headers {"Content-Type" "application/json"}
                                            :body (json/generate-string {:email user-email :password new-password})})]
                  (is (= 200 (:status response))))))))))))

(deftest register-user-handler-unit-test
  (testing "register-user-handler: validation and success"
    (with-redefs [team-challenge.service.user-service/register-user (fn [user] {:user/id (java.util.UUID/randomUUID)})
                  team-challenge.service.user-service/login (fn [email password] {:access-token "access" :refresh-token "refresh"})]
      (testing "validation error"
        (let [request {:body-params {}} ; порожній body
              response (user-controller/register-user-handler request)]
          (is (= 400 (:status response)))
          (is (= "validation" (get-in response [:body :error])))))
      (testing "success"
        (let [request {:body-params {:email "test@example.com"
                                     :password "pass"
                                     :first_name "Test"
                                     :last_name "User"}}
              response (user-controller/register-user-handler request)]
          (is (= 201 (:status response)))
          (is (= "User registered. Please check your email for a confirmation link."
                 (get-in response [:body :message])))
          (is (= {:access-token "access" :refresh-token "refresh"}
                 (get-in response [:body :token]))))))))
