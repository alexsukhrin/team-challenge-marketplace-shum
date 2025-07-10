(ns marketplace-shum.ads.domain
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.set :as set]))

(defn uuid-string? [s]
  (and (string? s)
       (try
         (uuid? (java.util.UUID/fromString s))
         (catch Exception _ false))))

(defn non-blank-string? [s]
  (and (string? s) (not (str/blank? s))))

(s/def ::title non-blank-string?)
(s/def ::description non-blank-string?)
(s/def ::price (s/and number? #(<= 0 %)))
(s/def ::user uuid-string?)
(s/def ::quantity (s/and int? #(<= 1 %)))
(s/def ::categories (s/coll-of string? :kind vector?))
(s/def ::photos (s/coll-of string? :kind vector?))
(s/def ::shoe-material (s/coll-of string? :kind vector?))
(s/def ::clothing-material (s/coll-of string? :kind vector?))
(s/def ::home-type (s/coll-of string? :kind vector?))
(s/def ::home-material (s/coll-of string? :kind vector?))
(s/def ::book-genre (s/coll-of string? :kind vector?))
(s/def ::book-cover string?)
(s/def ::book-language string?)
(s/def ::garden-type (s/coll-of string? :kind vector?))
(s/def ::electronics-type (s/coll-of string? :kind vector?))
(s/def ::auto-type (s/coll-of string? :kind vector?))
(s/def ::stationery-type (s/coll-of string? :kind vector?))
(s/def ::activity-type (s/coll-of string? :kind vector?))
(s/def ::children-type (s/coll-of string? :kind vector?))
(s/def ::color (s/coll-of string? :kind vector?))
(s/def ::gender string?)

(s/def ::ad
  (s/keys :req-un [::title ::description ::price ::user ::quantity ::categories ::photos]
          :opt-un [::shoe-material ::clothing-material ::home-type ::home-material
                   ::book-genre ::book-cover ::book-language ::garden-type ::electronics-type ::auto-type
                   ::stationery-type ::activity-type ::children-type ::color ::gender]))

(def allowed-attributes
  {"sport"        #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/activity-type :ad/color :ad/condition :ad/categories}
   "transport"    #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/color :ad/condition :ad/categories}
   "books"        #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/book-genre :ad/book-cover :ad/book-language :ad/categories}
   "home"         #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/home-type :ad/home-material :ad/color :ad/categories}
   "electronics"  #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/electronics-type :ad/color :ad/categories}
   "auto"         #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/auto-type :ad/color :ad/categories}
   "stationery"   #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/stationery-type :ad/color :ad/categories}
   "activity"     #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/activity-type :ad/color :ad/categories}
   "children"     #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/children-type :ad/color :ad/categories}
   "garden"       #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/garden-type :ad/color :ad/categories}
   "clothing"     #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/clothing-material :ad/color :ad/gender :ad/categories}
   "shoes"        #{:ad/title :ad/description :ad/price :ad/user :ad/quantity :ad/photos :ad/shoe-material :ad/color :ad/gender :ad/categories}})

(defn allowed-keys-for-categories [categories]
  (let [sets (keep allowed-attributes categories)]
    (if (seq sets)
      (apply clojure.set/union sets)
      #{})))
