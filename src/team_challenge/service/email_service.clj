(ns team-challenge.service.email-service
  (:require [postal.core :as postal]
            [team-challenge.config :as config]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def ^:private email-template
  (delay (slurp (io/resource "templates/email.html") :encoding "UTF-8")))

(defn- do-send-message
  "The actual function that sends the email. To be run by the agent."
  [email-config email-message]
  (try
    (postal/send-message email-config email-message)
    (println "Asynchronously sent confirmation email to" (first (:to email-message)))
    (catch Exception e
      (println "Failed to send confirmation email via agent:" (.getMessage e)))))

(def ^:private email-agent
  (agent nil :error-handler (fn [_ err] (println "Email agent error:" err))))

(defn- render-template [template params]
  (reduce (fn [t [k v]]
            (str/replace t (re-pattern (str "\\{" k "\\}")) (str v)))
          template
          params))

(defn- create-confirmation-email [to token user-name]
  (let [base-url (get-in config/*config* [:app :base-url])
        confirmation-link (str base-url "/api/v1/auth/confirm-email?token=" token)
        html-body (render-template @email-template {"user" user-name "link" confirmation-link})]
    {:from (get-in config/*config* [:email :from])
     :to to
     :subject "Please confirm your email address"
     :body [{:type "text/html; charset=utf-8"
             :content html-body}]}))

(defn send-confirmation-email
  "Queues a confirmation email to be sent asynchronously."
  [to token user-name]
  (let [email-config (get config/*config* :email)
        email-message (create-confirmation-email to token user-name)]
    (send-off email-agent (fn [_] (do-send-message email-config email-message)))
    (println "Queued confirmation email for" to)))

(comment
  (def to "alexandrvirtual@gmail.com")
  (def token "161a3c65-5e9f-44c1-841f-63c237600bab")
  (def user-name "Олександр")
  (def email-config (get config/*config* :email))
  (def email-message (create-confirmation-email to token user-name))
  (postal/send-message email-config email-message))