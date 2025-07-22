(ns marketplace-shum.ws-client
  (:require [aleph.http :as aleph-http]
            [manifold.stream :as s] 
            [manifold.deferred :as d] 
            [cheshire.core :as json]
            [clj-http.client :as http-client]))


(defn connect-ws
  [{:keys [url token on-message]}]
  (let [headers {"authorization" (str "Bearer " token)}
        d-conn (aleph-http/websocket-client url {:headers headers})]

    (d/chain d-conn
      (fn [conn]
        (println "✅ WebSocket connected to" url)

        ;; Обробка вхідних повідомлень
        (s/consume
          (fn [msg]
            (try
              (let [data (json/parse-string msg true)]
                (on-message data))
              (catch Exception e
                (println "❌ JSON parse error:" msg))))
          conn)

        ;; Повертаємо з'єднання для подальших дій
        conn))))

(defn get-token []
  (let [login (http-client/post "http://localhost:3000/api/v1/auth/login"
                          {:content-type :json
                           :body (json/generate-string {:email "rashiki44@gmail.com"
                                                        :password "password1986"})
                           :accept :json
                           :body-encoding "UTF-8"})
        login-keys (json/parse-string (:body login) keyword)]
    (:access-token login-keys)))

(defn send-msg [conn data]
  (let [msg (json/generate-string data)]
    (s/put! conn msg)))


(def conn
  (connect-ws
    {:url "ws://localhost:3000/api/v1/notifications"
     :token (get-token)
     :on-message (fn [data] (println "Received:" data))}))



(comment 

  (d/chain conn #(send-msg % {:type "ping" :message "Hello"}))

  npx wscat -c ws://localhost:3000/api/v1/notifications -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoiYWNjZXNzIiwidXNlci1pZCI6Ijk2Njc1NjE4LTA4N2ItNDMxNS1hZTY1LTE3YzBjYzc0ZmQ3MiIsInJvbGVzIjpbXSwianRpIjoiYWEzZTFkMDktOGI2Mi00NWEwLWI5MTYtODVjNGFlMzJjZDA3IiwiZXhwIjoxNzUzMTczOTQ5fQ.X6KUHg-MEnC3ehNNwznzFOZJHCGUnRmdgsiIRB2aHlo"

  )