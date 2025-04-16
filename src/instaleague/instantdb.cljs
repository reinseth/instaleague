(ns instaleague.instantdb
  (:require ["@instantdb/core" :as instantdb]))

(defn init-db [app-id]
  (instantdb/init (clj->js {:appId app-id})))

(defn new-id []
  (instantdb/id))

(defn subscribe-query [^js db q on-result]
  (.subscribeQuery
   db
   (clj->js q)
   (fn [res]
     (if-let [error (.-error res)]
       (on-result {:db/error error :db/q q})
       (on-result {:db/data (js->clj (.-data res) :keywordize-keys true) :db/q q})))))

(defn transact [^js db txs]
  (.transact
   db
   (clj->js
    (map (fn [[action entity id data]]
           (let [proxy (-> (.-tx db)
                           (aget (if (keyword? entity)
                                   (name entity)
                                   entity))
                           (aget id))
                 prepped-data (clj->js data)]
             (case action
               :update (.update proxy prepped-data)
               :delete (.delete proxy prepped-data))))
         txs))))
