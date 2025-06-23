(ns team-challenge.service.email-service
  (:require [postal.core :as postal]
            [team-challenge.config :as config]))

(defn- create-confirmation-email [to token]
  (let [base-url (get-in @config/*config* [:app :base-url])
        confirmation-link (str base-url "/api/v1/auth/confirm-email?token=" token)]
    {:from (get-in @config/*config* [:email :from])
     :to to
     :subject "Please confirm your email address"
     :body [{:type "text/html"
             :content (str "<h1>Welcome to Marketplace SHUM!</h1>"
                           "<p>Please click the link below to confirm your email address:</p>"
                           "<a href=\"" confirmation-link "\">Confirm Email</a>")}]}))

(defn send-confirmation-email
  "Sends a confirmation email to the user."
  [to token]
  (let [email-config (get @config/*config* :email)
        email-message (create-confirmation-email to token)]
    (try
      (postal/send-message email-config email-message)
      (println "Confirmation email sent to" to)
      (catch Exception e
        (println "Failed to send confirmation email:" (.getMessage e))))))