(ns marketplace-shum.notifications-test
  (:require [clojure.test :refer :all]
            [marketplace-shum.infra.server :as server]
            [aleph.http :as http]
            [manifold.stream :as s]
            [cheshire.core :as json]
            [marketplace-shum.infra.db :as db]
            [marketplace-shum.users.repository :as users.repository]))

(def test-port 8081)
(def url (str "ws://localhost:" test-port "/api/v1/notifications/"))

(defn- start-server
  []
  (server/run-server {:port test-port}))

(defonce test-server (atom nil))

(defn with-server [f]
  (reset! test-server (start-server))
  (f)
  (.close @test-server))

(use-fixtures :once with-server)

(deftest ^:integration notifications-test
  (let [user-id (users.repository/create-user db/db {:user/name "test-user"
                                                     :user/email "test@test.com"
                                                     :user/password "password"})]
    (testing "WebSocket connection"
      (let [conn @(http/websocket-client (str url user-id))]
        (s/put! conn (json/generate-string {:message "hello"}))
        (let [response (json/parse-string @(s/take! conn) true)]
          (is (= {:message "hello test-user"} response))
          (s/close! conn))))))