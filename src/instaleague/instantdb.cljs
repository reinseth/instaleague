(ns instaleague.instantdb
  (:require ["@instantdb/core" :as instantdb]))

(defn init-db [app-id]
  (instantdb/init (clj->js {:appId app-id})))

(defn new-id []
  (instantdb/id))

(defn subscribe-query [^js db store q]
  (prn "Subscribing to query" q)
  (when-let [unsubscribe (:sub/unsubscribe @store)] (unsubscribe))
  (swap! store assoc
         :sub/query q
         :sub/data :loading
         :sub/unsubscribe
         (.subscribeQuery
          db
          (clj->js q)
          (fn [res]
            (when (= q (:sub/query @store))
              (if (.-error res)
                (do (js/console.error (.-error res) res)
                    (swap! store assoc :sub/data :error))
                (swap! store assoc :sub/data (js->clj (.-data res) :keywordize-keys true))))))))

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
