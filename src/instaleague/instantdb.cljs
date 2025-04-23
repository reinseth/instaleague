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

(defn transact
  "Transact a list of steps/ops onto the database. A step is a vector in the form of 
  `[action entity-type entity-id data]`.

  - `action` - on of :update, :merge, :delete, :link, :unlink
  - `entity-type` - the name of the entity/table (can be given as a keyword)
  - `entity-id` - the unique id of the entity
  - `data` - a map of key/vals

  These step shapes are what the tx proxy in the underlying javascript library creates, e.g:

  ```js
  db.transact(db.tx.players[playerId].update({name: \"Marvin\"}));
  ```

  which is the same as:

  ```js
  db.transact({__ops: [[\"update\", \"players\", playerId, {name: \"Marvin\"}]]});
  ```

  Read more here: https://www.instantdb.com/docs/instaml"
  [^js db tx-steps]
  (.transact db (clj->js {:__ops tx-steps})))

