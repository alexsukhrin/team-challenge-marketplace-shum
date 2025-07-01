(ns team-challenge.api.product-controller)

(require '[clojure.spec.alpha :as s])
(require '[team-challenge.repository.product-category-repository :as category-repo])
(require '[team-challenge.repository.product-color-repository :as color-repo])
(require '[team-challenge.repository.product-size-repository :as size-repo])
(require '[team-challenge.repository.product-material-repository :as material-repo])
(require '[team-challenge.repository.product-gender-repository :as gender-repo])
(require '[team-challenge.repository.product-clothing-size-repository :as product-clothing-size-repo])
(require '[team-challenge.repository.payment-option-repository :as payment-option-repo])
(require '[team-challenge.repository.delivery-option-repository :as delivery-option-repo])
(require '[team-challenge.repository.product-repository :as product-repo])
(require '[team-challenge.repository.product-photo-repository :as product-photo-repo])
(require '[team-challenge.repository.product-characteristic-repository :as product-characteristic-repo])
(require '[team-challenge.repository.product-materials-repository :as product-materials-repo])
(require '[team-challenge.service.s3-service :as s3-service])
(require '[aero.core :as aero])
(require '[clojure.java.io :as io])

(s/def ::category_id int?)
(s/def ::name string?)
(s/def ::photo string?)
(s/def ::category (s/keys :req-un [::category_id ::name ::photo]))
(s/def ::categories (s/coll-of ::category))
(s/def ::categories-response (s/keys :req-un [::categories]))

(defn get-categories-handler []
  {:status 200
   :body {:categories []}})

;; Product Categories
(defn list-product-categories-handler [_]
  {:status 200 :body {:categories (category-repo/get-all)}})

(defn get-product-category-handler [{:keys [path-params]}]
  (if-let [cat (category-repo/get-by-id (parse-long (:id path-params)))]
    {:status 200 :body {:category cat}}
    {:status 404 :body {:error "Not found"}}))

(defn create-product-category-handler [{:keys [body-params]}]
  {:status 201 :body {:category (category-repo/create! body-params)}})

(defn update-product-category-handler [{:keys [path-params body-params]}]
  (if-let [cat (category-repo/update! (parse-long (:id path-params)) body-params)]
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

(defn create-product-color-handler [{:keys [body-params]}]
  {:status 201 :body {:color (color-repo/create! body-params)}})

(defn update-product-color-handler [{:keys [path-params body-params]}]
  (if-let [color (color-repo/update! (parse-long (:id path-params)) body-params)]
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

(defn create-product-size-handler [{:keys [body-params]}]
  {:status 201 :body {:size (size-repo/create! body-params)}})

(defn update-product-size-handler [{:keys [path-params body-params]}]
  (if-let [size (size-repo/update! (parse-long (:id path-params)) body-params)]
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

(defn create-product-material-handler [{:keys [body-params]}]
  {:status 201 :body {:material (material-repo/create! body-params)}})

(defn update-product-material-handler [{:keys [path-params body-params]}]
  (if-let [material (material-repo/update! (parse-long (:id path-params)) body-params)]
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

(defn create-product-gender-handler [{:keys [body-params]}]
  {:status 201 :body {:gender (gender-repo/create! body-params)}})

(defn update-product-gender-handler [{:keys [path-params body-params]}]
  (if-let [gender (gender-repo/update! (parse-long (:id path-params)) body-params)]
    {:status 200 :body {:gender gender}}
    {:status 404 :body {:error "Not found"}}))

(defn delete-product-gender-handler [{:keys [path-params]}]
  (if (gender-repo/delete! (parse-long (:id path-params)))
    {:status 204}
    {:status 404 :body {:error "Not found"}}))

;; Product Clothing Sizes
(defn get-all-product-clothing-sizes [_]
  {:status 200
   :body (product-clothing-size-repo/get-all)})

(defn get-product-clothing-size [{{:keys [id]} :path-params}]
  (if-let [size (product-clothing-size-repo/get-by-id (parse-long id))]
    {:status 200 :body size}
    {:status 404 :body {:error "Product clothing size not found"}}))

(defn create-product-clothing-size [{:keys [body-params]}]
  (try
    (let [created (product-clothing-size-repo/create! body-params)]
      {:status 201 :body created})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn update-product-clothing-size [{{:keys [id]} :path-params :keys [body-params]}]
  (try
    (if-let [updated (product-clothing-size-repo/update! (parse-long id) body-params)]
      {:status 200 :body updated}
      {:status 404 :body {:error "Product clothing size not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn delete-product-clothing-size [{{:keys [id]} :path-params}]
  (try
    (if-let [deleted (product-clothing-size-repo/delete! (parse-long id))]
      {:status 204}
      {:status 404 :body {:error "Product clothing size not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

;; Payment Options
(defn list-payment-options-handler [_]
  {:status 200 :body {:payment_options []}})
(defn get-payment-option-handler [_]
  {:status 200 :body {:payment_option {}}})
(defn create-payment-option-handler [_]
  {:status 201 :body {:payment_option {}}})
(defn update-payment-option-handler [_]
  {:status 200 :body {:payment_option {}}})
(defn delete-payment-option-handler [_]
  {:status 204})

;; Delivery Options
(defn list-delivery-options-handler [_]
  {:status 200 :body {:delivery_options []}})
(defn get-delivery-option-handler [_]
  {:status 200 :body {:delivery_option {}}})
(defn create-delivery-option-handler [_]
  {:status 201 :body {:delivery_option {}}})
(defn update-delivery-option-handler [_]
  {:status 200 :body {:delivery_option {}}})
(defn delete-delivery-option-handler [_]
  {:status 204})

;; Product Photos
(defn list-product-photos-handler [_]
  {:status 200 :body {:photos []}})
(defn create-product-photo-handler [_]
  {:status 201 :body {:photo {}}})
(defn delete-product-photo-handler [_]
  {:status 204})

;; Product <-> Colors
(defn list-product-colors-for-product-handler [_]
  {:status 200 :body {:colors []}})
(defn add-product-color-handler [_]
  {:status 201 :body {:result "added"}})
(defn remove-product-color-handler [_]
  {:status 204})

;; Product <-> Sizes
(defn list-product-sizes-for-product-handler [_]
  {:status 200 :body {:sizes []}})
(defn add-product-size-handler [_]
  {:status 201 :body {:result "added"}})
(defn remove-product-size-handler [_]
  {:status 204})

;; Product <-> Materials
(defn list-product-materials-for-product [{{:keys [product_id]} :path-params}]
  {:status 200
   :body (product-materials-repo/get-all-for-product (parse-long product_id))})

(defn add-product-material-for-product [{{:keys [product_id]} :path-params :keys [body-params]}]
  (let [{:keys [characteristic_id value]} body-params]
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

;; Product <-> Genders
(defn list-product-genders-for-product-handler [_]
  {:status 200 :body {:genders []}})
(defn add-product-gender-handler [_]
  {:status 201 :body {:result "added"}})
(defn remove-product-gender-handler [_]
  {:status 204})

;; Product <-> Clothing Sizes
(defn list-product-clothing-sizes-for-product-handler [_]
  {:status 200 :body {:clothing_sizes []}})
(defn add-product-clothing-size-handler [_]
  {:status 201 :body {:result "added"}})
(defn remove-product-clothing-size-handler [_]
  {:status 204})

;; Product <-> Categories
(defn list-product-categories-for-product-handler [_]
  {:status 200 :body {:categories []}})
(defn add-product-category-handler [_]
  {:status 201 :body {:result "added"}})
(defn remove-product-category-handler [_]
  {:status 204})

;; Payment Options Handlers
(defn get-all-payment-options [_]
  {:status 200
   :body (payment-option-repo/get-all)})

(defn get-payment-option [{{:keys [id]} :path-params}]
  (if-let [option (payment-option-repo/get-by-id (parse-long id))]
    {:status 200 :body option}
    {:status 404 :body {:error "Payment option not found"}}))

(defn create-payment-option [{:keys [body-params]}]
  (try
    (let [created (payment-option-repo/create! body-params)]
      {:status 201 :body created})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn update-payment-option [{{:keys [id]} :path-params :keys [body-params]}]
  (try
    (if-let [updated (payment-option-repo/update! (parse-long id) body-params)]
      {:status 200 :body updated}
      {:status 404 :body {:error "Payment option not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn delete-payment-option [{{:keys [id]} :path-params}]
  (try
    (if-let [deleted (payment-option-repo/delete! (parse-long id))]
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

(defn create-delivery-option [{:keys [body-params]}]
  (try
    (let [created (delivery-option-repo/create! body-params)]
      {:status 201 :body created})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn update-delivery-option [{{:keys [id]} :path-params :keys [body-params]}]
  (try
    (if-let [updated (delivery-option-repo/update! (parse-long id) body-params)]
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

(defn create-product [{:keys [body-params]}]
  (try
    (let [created (product-repo/create! body-params)]
      {:status 201 :body created})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn update-product [{{:keys [id]} :path-params :keys [body-params]}]
  (try
    (if-let [updated (product-repo/update! (parse-long id) body-params)]
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

(defn- s3-config []
  (aero/read-config (io/resource (str "config/" (or (System/getenv "APP_ENV") "dev") ".edn"))))

(defn list-product-photos [{{:keys [product_id]} :path-params}]
  {:status 200
   :body (product-photo-repo/get-all-for-product (parse-long product_id))})

(defn create-product-photo [{{:keys [product_id]} :path-params :keys [multipart-params]}]
  (let [file (get multipart-params "file")
        bucket (get-in (s3-config) [:s3 :bucket])
        key (str "products/" product_id "/" (System/currentTimeMillis) "-" (:filename file))
        file-bytes (slurp (:tempfile file) :encoding "ISO-8859-1")
        content-type (:content-type file)]
    (try
      (s3-service/upload-file! bucket key (.getBytes file-bytes) content-type)
      (let [url (s3-service/generate-url bucket key)
            photo (product-photo-repo/create! {:product_id (parse-long product_id)
                                               :url url
                                               :s3_key key})]
        {:status 201 :body photo})
      (catch Exception e
        {:status 400 :body {:error (.getMessage e)}}))))

(defn delete-product-photo [{{:keys [product_id photo_id]} :path-params}]
  (let [photo (product-photo-repo/delete! (parse-long photo_id))]
    (if photo
      (let [bucket (get-in (s3-config) [:s3 :bucket])
            key (:s3_key photo)]
        (try
          (s3-service/delete-file! bucket key)
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

(defn create-product-characteristic [{:keys [body-params]}]
  (try
    (let [created (product-characteristic-repo/create! body-params)]
      {:status 201 :body created})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(defn update-product-characteristic [{{:keys [id]} :path-params :keys [body-params]}]
  (try
    (if-let [updated (product-characteristic-repo/update! (parse-long id) body-params)]
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