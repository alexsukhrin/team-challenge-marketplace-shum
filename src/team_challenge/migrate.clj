(ns team-challenge.migrate
  (:require [migratus.core :as migratus]
            [team-challenge.config :as config])
  (:gen-class))

(defn migrate
  "Запускає всі міграції через migratus."
  []
  (let [migratus-config (config/load-config "config/migratus.edn")]
    (println "--- Починаю міграцію бази даних ---")
    (migratus/migrate migratus-config)
    (println "--- Міграція завершена ---")))

(defn rollback
  "Відкат останньої міграції."
  []
  (let [migratus-config (config/load-config "config/migratus.edn")]
    (println "--- Відкат міграції ---")
    (migratus/rollback migratus-config)
    (println "--- Відкат завершено ---")))


(comment
  (def config (config/load-config "config/migratus.edn"))
  (migratus/init config)
  (migratus/migrate config)
  (migratus/create config "user_permissions")
  (migrate)
  )
