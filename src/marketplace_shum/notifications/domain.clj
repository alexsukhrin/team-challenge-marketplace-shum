(ns marketplace-shum.notifications.domain
  (:require [clojure.string :as str]))

(defn valid-message?
  "Validates that a notification message is a non-empty, non-blank string."
  [msg]
  (and (string? msg)
       (not (str/blank? msg))
       (< (count msg) 1000)))
