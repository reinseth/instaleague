(ns instaleague.router
  (:require [clojure.string :as str]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend :as rf]))

(defn route-template [{:keys [route]}]
  (str "/" (str/join "/" route)))

(comment
  (route-template {:route ["foo" :bar "baz"]}))

(defn start [pages on-navigate]
  (rfe/start!
   (rf/router (map (juxt route-template identity) pages))
   (fn [{:keys [data path-params query-params]}]
     (on-navigate {:id (:id data :not-found)
                   :path-params path-params
                   :query-params query-params}))
   {:use-fragment false}))
