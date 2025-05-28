(ns instaleague.main
  (:require [clojure.walk :as walk]
            [instaleague.forms :as forms]
            [instaleague.instantdb :as instantdb]
            [instaleague.players :refer [players-page]]
            [instaleague.router :as router]
            [replicant.dom :as replicant]))

(goog-define app-id "<APP_ID env variable>")

(defonce db (instantdb/init-db app-id))

(defonce store (atom nil))

(def pages [players-page])

(def pages-map (into {} (map (juxt :page/id identity)) pages))

(defn current-page [state]
  (pages-map (-> state :route :page/id)))

(defn render [state]
  (let [page (current-page state)
        rendered (if page
                   ((:page/render page) state)
                   [:div "Not found"])]
    (replicant/render (js/document.getElementById "root") rendered)))

(defn interpolate [event actions]
  (walk/postwalk
   (fn [x]
     (case x
       :db/new-id (instantdb/new-id)
       :event/target.value (.. event -target -value)
       x))
   actions))

(defn execute-actions [{:replicant/keys [dom-event node] :as event-data} actions]
  (doseq [[action & args] (interpolate dom-event (remove nil? actions))]
    (let [state @store
          forms (:page/forms (current-page state))]
      (apply prn action args)
      (case action
        :assoc-in (apply swap! store assoc-in args)
        :db/transact (apply instantdb/transact db args)
        :event/prevent-default (.preventDefault dom-event)
        :event/stop-propagation (.stopPropagation dom-event)
        :form/validate (execute-actions event-data (apply forms/validate state forms args))
        :form/submit (do
                       (.preventDefault dom-event)
                       (execute-actions event-data (apply forms/submit state forms args)))
        :form/reset (execute-actions event-data (apply forms/reset args))
        :node/focus (when node
                      (.focus node))
        (js/console.error "Unknown action" action)))))

(defn handle-db-result [{:db/keys [q error data] :as res}]
  (when (= q (:db/q @store))
    (if error
      (do (js/console.error error res)
          (swap! store assoc :db/data :error))
      (swap! store assoc :db/data data))))

(defn handle-route-change [match]
  (let [state @store
        page (pages-map (:page/id match))
        q (when page ((:page/query page) state))]
    (when-let [unsubscribe (:db/unsubscribe state)] (unsubscribe))
    (if q
      (swap! store assoc
             :route match
             :db/data :loading
             :db/q q
             :db/unsubscribe (instantdb/subscribe-query db q handle-db-result))
      (swap! store assoc
             :route match
             :db/data nil
             :db/q nil
             :db/unsubscribe nil))))

(defn ^:export init []
  (replicant/set-dispatch! #'execute-actions)
  (add-watch store ::me (fn [_ _ _ new-val] (render new-val)))
  (router/start pages handle-route-change)
  (swap! store assoc :initialized (js/Date.now)))
