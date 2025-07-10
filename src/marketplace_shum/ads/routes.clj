(ns marketplace-shum.ads.routes
  (:require 
   [marketplace-shum.ads.handler :as ads-handler]))

(def routes
  ["/ads"
   {:tags ["ads"]}
   
   ["/categories"
    {:get {:summary "get all categories"
           :responses {200 {:body
                            (vector
                              {:ad-category/name string?
                               :ad-category/photo string?})}}
           :handler #'ads-handler/get-categories}}]
   
   ["/attributes"
    {:get {:summary "get all attributes for ad creation"
           :responses {200 {:body map?}}
           :handler #'ads-handler/get-attributes}}]
   
   ["/post/:id"
    {:get {:summary "Get ad by id"
           :parameters {:path {:id string?}}
           :handler #'ads-handler/get-ad-by-id}
     :put {:summary "Update ad"
           :parameters {:path {:id string?} :body map?}
           :handler #'ads-handler/update-ad}
     :delete {:summary "Delete ad"
              :parameters {:path {:id string?}}
              :handler #'ads-handler/delete-ad}}]
   
   [""
    {:get {:summary "Get all ads"
           :handler #'ads-handler/get-all-ads}
     :post {:summary "Create new ad"
            :parameters {:multipart map?}
            :responses {201 {:body map?}}
            :handler #'ads-handler/create-ad}}]
   
  ])
