(ns instaleague.main
  (:require ["@instantdb/core" :as instantdb]
            [clojure.walk :as walk]
            [instaleague.players :refer [players-page]]
            [instaleague.router :as router]
            [replicant.dom :as replicant]))

(goog-define app-id "<APP_ID env variable>")

(defonce db (instantdb/init (clj->js {:appId app-id})))

(defonce store (atom nil))

(def pages [players-page])

(def pages-map (into {} (map (juxt :id identity)) pages))

(defn subscribe-query [q]
  (prn "subscribing to query" q)
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

(defn transact [txs]
  (.transact db (clj->js
                 (map (fn [[entity id data]]
                        (-> (.-tx db)
                            (aget (if (keyword? entity)
                                    (name entity)
                                    entity))
                            (aget id)
                            (.update (clj->js data))))
                      txs))))

(defn render [state]
  (let [route (:route state)
        page (pages-map (:id route))
        rendered (if page
                   ((:render page) (:sub/data state) route state)
                   [:div "Not found"])]
    (replicant/render (js/document.getElementById "root") rendered)))

(defn interpolate-event-data [event actions]
  (walk/postwalk
   (fn [x]
     (case x
       :db/new-id (instantdb/id)
       :event/target.value (.. event -target -value)
       x))
   actions))

(defn execute-actions [event actions]
  (doseq [[action & args] (remove nil? actions)]
    (apply prn action args)
    (case action
      :assoc-in (apply swap! store assoc-in args)
      :db/transact (apply transact args)
      :event/prevent-default (.preventDefault event)
      (js/console.error "Unknown action" action))))

(defn ^:export init []
  (replicant/set-dispatch!
   (fn [{:keys [replicant/dom-event]} actions]
     (->> actions
          (interpolate-event-data dom-event)
          (execute-actions dom-event))))
  
  (add-watch
   store ::me
   (fn [_ _ _ new-val] (render new-val)))

  (router/start
   pages
   (fn [match]
     (when-let [page (pages-map (:id match))]
       (subscribe-query (apply (:query page) match @store)))
     (swap! store assoc :route match)))
  
  (swap! store assoc :initialized (js/Date.now)))
