(ns team-challenge.api.product-controller
  (:require
   [clojure.spec.alpha :as s]
   [team-challenge.repository.product-category-repository :as category-repo]
   [team-challenge.repository.product-color-repository :as color-repo]
   [team-challenge.repository.product-size-repository :as size-repo]
   [team-challenge.repository.product-material-repository :as material-repo]
   [team-challenge.repository.product-gender-repository :as gender-repo]
   [team-challenge.repository.product-clothing-size-repository :as product-clothing-size-repo]
   [team-challenge.repository.payment-option-repository :as payment-option-repo]
   [team-challenge.repository.delivery-option-repository :as delivery-option-repo]
   [team-challenge.repository.product-repository :as product-repo]
   [team-challenge.repository.product-photo-repository :as product-photo-repo]
   [team-challenge.repository.product-characteristic-repository :as product-characteristic-repo]
   [team-challenge.repository.product-materials-repository :as product-materials-repo]
   [marketplace-shum.aws.s3 :as s3]
   [aero.core :as aero]
   [clojure.java.io :as io]
   [team-challenge.config :as config]))

(s/def ::name string?)
(s/def ::description string?)
(s/def ::payment-options-params (s/keys :req-un [::name ::description]))

;; Product Categories
(defn list-product-categories-handler [_]
  {:status 200 :body {:categories (category-repo/get-all)}})

(defn get-product-category-handler [{:keys [path-params]}]
  (if-let [cat (category-repo/get-by-id (parse-long (:id path-params)))]
    {:status 200 :body {:category cat}}
    {:status 404 :body {:error "Not found"}}))

(defn create-product-category-handler [{:keys [multipart-params]}]
  (let [file (get multipart-params "photo")
        name (get multipart-params "name")
        filename (str (System/currentTimeMillis) "-" (:filename file))
        bucket (get-in config/*config* [:s3 :bucket])
        file-bytes (slurp (:tempfile file) :encoding "ISO-8859-1")
        content-type (:content-type file)]
    (try
      (s3/upload-file! bucket filename (.getBytes file-bytes) content-type)
      (let [category {:name name :photo filename}
            created (category-repo/create! category)]
        {:status 201 :body {:category created}})
      (catch Exception e
        {:status 400 :body {:error (.getMessage e)}}))))

(defn update-product-category-handler [{:keys [path-params body]}]
  (if-let [cat (category-repo/update! (parse-long (:id path-params)) body)]
    {:status 200 :body {:category cat}}
    {:status 404 :body {:error "Not found"}}))

(defn delete-product-category-handler [{:keys [path-params]}]
  (if (category-repo/delete! (parse-long (:id path-params)))
    {:status 204}
    {:status 404 :body {:error "Not found"}}))

;; Product Colors
(defn list-product-colors-handler [_]
  {:status 200 :body {:colors (color-repo/get-all)}})

(defn get-product-color-handler [{:keys [path-params]}]
  (if-let [color (color-repo/get-by-id (parse-long (:id path-params)))]
    {:status 200 :body {:color color}}
    {:status 404 :body {:error "Not found"}}))

(defn create-product-color-handler [{:keys [body]}]
  {:status 201 :body {:color (color-repo/create! body)}})

(defn update-product-color-handler [{:keys [path-params body]}]
  (if-let [color (color-repo/update! (parse-long (:id path-params)) body)]
    {:status 200 :body {:color color}}
    {:status 404 :body {:error "Not found"}}))

(defn delete-product-color-handler [{:keys [path-params]}]
  (if (color-repo/delete! (parse-long (:id path-params)))
    {:status 204}
    {:status 404 :body {:error "Not found"}}))

;; Product Sizes
(defn list-product-sizes-handler [_]
  {:status 200 :body {:sizes (size-repo/get-all)}})

(defn get-product-size-handler [{:keys [path-params]}]
  (if-let [size (size-repo/get-by-id (parse-long (:id path-params)))]
    {:status 200 :body {:size size}}
    {:status 404 :body {:error "Not found"}}))

(defn create-product-size-handler [{:keys [body]}]
  {:status 201 :body {:size (size-repo/create! body)}})

(defn update-product-size-handler [{:keys [path-params body]}]
  (if-let [size (size-repo/update! (parse-long (:id path-params)) body)]
    {:status 200 :body {:size size}}
    {:status 404 :body {:error "Not found"}}))

(defn delete-product-size-handler [{:keys [path-params]}]
  (if (size-repo/delete! (parse-long (:id path-params)))
    {:status 204}
    {:status 404 :body {:error "Not found"}}))

;; Product Materials
(defn list-product-materials-handler [_]
  {:status 200 :body {:materials (material-repo/get-all)}})

(defn get-product-material-handler [{:keys [path-params]}]
  (if-let [material (material-repo/get-by-id (parse-long (:id path-params)))]
    {:status 200 :body {:material material}}
    {:status 404 :body {:error "Not found"}}))

(defn create-product-material-handler [{:keys [body]}]
  {:status 201 :body {:material (material-repo/create! body)}})

(defn update-product-material-handler [{:keys [path-params body]}]
  (if-let [material (material-repo/update! (parse-long (:id path-params)) body)]
    {:status 200 :body {:material material}}
    {:status 404 :body {:error "Not found"}}))

(defn delete-product-material-handler [{:keys [path-params]}]
  (if (material-repo/delete! (parse-long (:id path-params)))
    {:status 204}
    {:status 404 :body {:error "Not found"}}))

;; Product Genders
(defn list-product-genders-handler [_]
  {:status 200 :body {:genders (gender-repo/get-all)}})

(defn get-product-gender-handler [{:keys [path-params]}]
  (if-let [gender (gender-repo/get-by-id (parse-long (:id path-params)))]
    {:status 200 :body {:gender gender}}
    {:status 404 :body {:error "Not found"}}))

(defn create-product-gender-handler [{:keys [body]}]
  {:status 201 :body {:gender (gender-repo/create! body)}})

(defn update-product-gender-handler [{:keys [path-params body]}]
  (if-let [gender (gender-repo/update! (parse-long (:id path-params)) body)]
    {:status 200 :body {:gender gender}}
    {:status 404 :body {:error "Not found"}}))

(defn delete-product-gender-handler [{:keys [path-params]}]
  (if (gender-repo/delete! (parse-long (:id path-params)))
    {:status 204}
    {:status 404 :body {:error "Not found"}}))

;; Product <-> Materials
(defn list-product-materials-for-product [{{:keys [product_id]} :path-params}]
  {:status 200
   :body (product-materials-repo/get-all-for-product (parse-long product_id))})

(defn add-product-material-for-product [{{:keys [product_id]} :path-params :keys [body]}]
  (let [{:keys [characteristic_id value]} body]
    (try
      (let [result (product-materials-repo/add! (parse-long product_id) characteristic_id value)]
        {:status 201 :body result})
      (catch Exception e
        {:status 400 :body {:error (.getMessage e)}}))))

(defn remove-product-material-for-product [{{:keys [product_id characteristic_id]} :path-params}]
  (try
    (if-let [result (product-materials-repo/remove! (parse-long product_id) (parse-long characteristic_id))]
      {:status 204}
      {:status 404 :body {:error "Product material not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

;; Payment Options Handlers
(defn get-all-payment-options [_]
  {:status 200
   :body (payment-option-repo/get-all)})

(defn get-payment-option [{{:keys [id]} :path-params}]
  (if-let [option (payment-option-repo/get-by-id (parse-long id))]
    {:status 200 :body option}
    {:status 404 :body {:error "Payment option not found"}}))

(defn create-payment-option [{:keys [body]}]
  (try
    (let [created (payment-option-repo/create! body)]
      {:status 201 :body created})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn update-payment-option [{{:keys [id]} :path-params :keys [body]}]
  (try
    (if-let [updated (payment-option-repo/update! (parse-long id) body)]
      {:status 200 :body updated}
      {:status 404 :body {:error "Payment option not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn delete-payment-option [{{:keys [id]} :path-params}]
  (try
    (if-let [_ (payment-option-repo/delete! (parse-long id))]
      {:status 204}
      {:status 404 :body {:error "Payment option not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

;; Delivery Options Handlers
(defn get-all-delivery-options [_]
  {:status 200
   :body (delivery-option-repo/get-all)})

(defn get-delivery-option [{{:keys [id]} :path-params}]
  (if-let [option (delivery-option-repo/get-by-id (parse-long id))]
    {:status 200 :body option}
    {:status 404 :body {:error "Delivery option not found"}}))

(defn create-delivery-option [{:keys [body]}]
  (try
    (let [created (delivery-option-repo/create! body)]
      {:status 201 :body created})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn update-delivery-option [{{:keys [id]} :path-params :keys [body]}]
  (try
    (if-let [updated (delivery-option-repo/update! (parse-long id) body)]
      {:status 200 :body updated}
      {:status 404 :body {:error "Delivery option not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn delete-delivery-option [{{:keys [id]} :path-params}]
  (try
    (if-let [deleted (delivery-option-repo/delete! (parse-long id))]
      {:status 204}
      {:status 404 :body {:error "Delivery option not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

;; Products Handlers
(defn get-all-products [{:keys [query-params]}]
  {:status 200
   :body (if (seq query-params)
           (product-repo/get-all query-params)
           (product-repo/get-all))})

(defn get-product [{{:keys [id]} :path-params}]
  (if-let [product (product-repo/get-by-id (parse-long id))]
    {:status 200 :body product}
    {:status 404 :body {:error "Product not found"}}))

(defn create-product [{:keys [body]}]
  (try
    (let [created (product-repo/create! body)]
      {:status 201 :body created})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn update-product [{{:keys [id]} :path-params :keys [body]}]
  (try
    (if-let [updated (product-repo/update! (parse-long id) body)]
      {:status 200 :body updated}
      {:status 404 :body {:error "Product not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn delete-product [{{:keys [id]} :path-params}]
  (try
    (if-let [deleted (product-repo/delete! (parse-long id))]
      {:status 204}
      {:status 404 :body {:error "Product not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn list-product-photos [{{:keys [product_id]} :path-params}]
  {:status 200
   :body (product-photo-repo/get-all-for-product (parse-long product_id))})

(defn create-product-photo [{{:keys [product_id]} :path-params :keys [multipart-params]}]
  (let [file (get multipart-params "file")
        bucket (get-in config/*config* [:s3 :bucket])
        key (str "products/" product_id "/" (System/currentTimeMillis) "-" (:filename file))
        file-bytes (slurp (:tempfile file) :encoding "ISO-8859-1")
        content-type (:content-type file)]
    (try
      (s3/upload-file! bucket key (.getBytes file-bytes) content-type)
      (let [url (s3/generate-url bucket key)
            photo (product-photo-repo/create! {:product_id (parse-long product_id)
                                               :url url
                                               :s3_key key})]
        {:status 201 :body photo})
      (catch Exception e
        {:status 400 :body {:error (.getMessage e)}}))))

(defn delete-product-photo [{{:keys [product_id photo_id]} :path-params}]
  (let [photo (product-photo-repo/delete! (parse-long photo_id))]
    (if photo
      (let [bucket (get-in config/*config* [:s3 :bucket])
            key (:s3_key photo)]
        (try
          (s3/delete-file! bucket key)
          {:status 204}
          (catch Exception e
            {:status 400 :body {:error (.getMessage e)}})))
      {:status 404 :body {:error "Photo not found"}})))

;; Product Characteristics Handlers
(defn get-all-product-characteristics [_]
  {:status 200
   :body (product-characteristic-repo/get-all)})

(defn get-product-characteristic [{{:keys [id]} :path-params}]
  (if-let [characteristic (product-characteristic-repo/get-by-id (parse-long id))]
    {:status 200 :body characteristic}
    {:status 404 :body {:error "Product characteristic not found"}}))

(defn create-product-characteristic [{:keys [body]}]
  (try
    (let [created (product-characteristic-repo/create! body)]
      {:status 201 :body created})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn update-product-characteristic [{{:keys [id]} :path-params :keys [body]}]
  (try
    (if-let [updated (product-characteristic-repo/update! (parse-long id) body)]
      {:status 200 :body updated}
      {:status 404 :body {:error "Product characteristic not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn delete-product-characteristic [{{:keys [id]} :path-params}]
  (try
    (if-let [deleted (product-characteristic-repo/delete! (parse-long id))]
      {:status 204}
      {:status 404 :body {:error "Product characteristic not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))