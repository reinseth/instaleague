(ns instaleague.players
  (:require [clojure.string :as str]
            [instaleague.forms :refer [text-input]]
            [instaleague.page :as page]))

(defn contains-name? [players name]
  (some #(= (str/lower-case (or name ""))
            (str/lower-case (:name %))) players))

(defn get-name [form-data]
  (str/trim (or (get-in form-data [:name :value]) "")))

(defn validate-name [state form-data _field-data]
  (tap> form-data)
  (let [name (get-name form-data)]
    (cond
      (str/blank? name)
      "Required"

      (contains-name? (-> state :db/data :players) name)
      "Already exists"

      :else
      nil)))

(defn save-player [_state form-data]
  [[:db/transact [[:update :players :db/new-id {:name (get-name form-data)}]]]
   [:form/reset :new-player]])

(defn delete-player [player]
  [[:db/transact [[:delete :players (:id player)]]]])

(defn render-new-player-form [state]
  [:form.flex.gap-2 {:on {:submit [[:form/submit :new-player]]}}
   (text-input state :new-player :name {:placeholder "Name", :autofocus true})
   [:button.btn.btn-primary {:type :submit} "Add"]])

(defn render-players [{:db/keys [data]}]
  [:ul.list.rounded-box.shadow-md.bg-base-100
   (for [player (:players data)]
     [:li.list-row {:replicant/key (:çid player)}
      [:div.list-col-grow.self-center (:name player)]
      [:button.btn.btn-ghost
       {:on {:click (delete-player player)}}
       "x"]])])

(def players-page
  (page/define
    {:id :players
     :route ["players"]
     :query (fn [_state] {:players {}})
     :forms [{:form/id :new-player
              :form/fields [{:field/name :name
                             :field/validate validate-name}]
              :form/submit save-player}]     
     :render
     (fn [state]
       [:div.container.mx-auto.flex.flex-col.gap-4.mt-4.p-4
        [:h1.text-xl "Players"]
        (render-new-player-form state)
        (render-players state)])}))
