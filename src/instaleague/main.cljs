(ns instaleague.main
  (:require ["@instantdb/core" :as instantdb]
            [replicant.dom :as replicant]))

(goog-define app-id "<APP_ID env variable>")

(defonce db (instantdb/init (clj->js {:appId app-id})))

(defonce store (atom nil))

(defn subscribe-query [q]
  (when-let [unsubscribe (:sub/unsubscribe @store)] (unsubscribe))
  (swap! store assoc :sub/unsubscribe
         (.subscribeQuery
          db
          (clj->js q)
          (fn [res]
            (if (.-error res)
              (prn (.-error res))
              (swap! store assoc :sub/data (js->clj (.-data res) :keywordize-keys true)))))))

(defn render [state]
  (prn "render" state)
  (replicant/render (js/document.getElementById "root") [:h1 "Instaleage"]))

(defn ^:export init []
  (subscribe-query {:players {}})
  (add-watch store ::me (fn [_ _ _ new-val] (render new-val)))
  (swap! store assoc :initialized (js/Date.now)))
