(ns marketplace-shum.users.repository
  (:require
   [marketplace-shum.infra.db :as d]))

(defn create-user! [{:keys [email password first-name last-name]}]
  (let [user-id (random-uuid)]
    (d/tx! [{:user/id (random-uuid)
             :user/email email
             :user/password password
             :user/first-name first-name
             :user/last-name last-name
             :user/email-confirmed? false
             :user/created-at (java.util.Date.)}])
    user-id))

(create-user! {:emal "alexandrvirtual@gmail.com"
               :password "password123"
               :first-name "Alexandr"
               :last-name "Sukhryn"})

(defn find-user-by-id
  [db id]
  (d/pull db '[*] [:user/id id]))

(defn find-user-by-email
  [db email]
  (first (d/q '[:find (pull ?e [*])
                :in $ ?email
                :where [?e :user/email ?email]]
              db email)))

(defn set-email-confirmed!
  [conn id confirmed?]
  (d/transact conn {:tx-data [[:db/add [:user/id id] :user/email-confirmed? confirmed?]]}))

(defn register-user!
  [conn {:keys [email]}]
  (let [id (uuid/v4)]
    (user-repo/create-user! conn {:id id :email email})
    id))

(defn get-user-by-id
  [db id]
  (user-repo/find-user-by-id db id))

(defn get-user-by-email
  [db email]
  (user-repo/find-user-by-email db email))

(defn confirm-email!
  [conn id]
  (user-repo/set-email-confirmed! conn id true))