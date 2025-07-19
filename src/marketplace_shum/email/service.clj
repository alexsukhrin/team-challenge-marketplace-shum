(ns marketplace-shum.email.service
  (:require [postal.core :as postal]
            [marketplace-shum.infra.config :as config]
            [clojure.java.io :as io]
            [selmer.parser :as selmer]))

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

(defn- render-template [template-path params]
  (selmer/render (slurp (io/resource template-path)) params))

(defn- get-header-img [path]
  (let [host (get-in config/*config* [:web-server :host])
        port (get-in config/*config* [:web-server :port])]
    (str host ":" port path)))

(defn- create-confirmation-email [to token user-name]
  (let [base-url (get-in config/*config* [:web-server :host])
        header-img (get-header-img "/email/email.png")
        confirmation-link (str base-url "/confirm?token=" token)
        html-body (render-template "templates/confirm.html" {"user" user-name
                                                             "link" confirmation-link
                                                             "header-img" header-img})]
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

(defn- create-otp-email [to otp user-name]
  (let [header-img (get-header-img "/email/reset.png")
        shum (get-header-img "/email/shum.png")
        html-body (render-template "templates/otp.html" {"shum" shum
                                                         "name" user-name
                                                         "otp" otp
                                                         "reset" header-img})]
    {:from (get-in config/*config* [:email :from])
     :to to
     :subject "Please reset your password"
     :body [{:type "text/html; charset=utf-8"
             :content html-body}]}))

(defn send-otp-email
  "Queues a confirmation email to be sent asynchronously."
  [to otp user-name]
  (let [email-config (get config/*config* :email)
        email-message (create-otp-email to otp user-name)]
    (send-off email-agent (fn [_] (do-send-message email-config email-message)))
    (println "Queued confirmation email for" to)))
