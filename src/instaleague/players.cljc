(ns instaleague.players
  (:require [clojure.string :as str]
            [instaleague.forms :refer [text-input]]
            [instaleague.page :as page]
            [instaleague.icons :as icons]))

(defn contains-name? [players name]
  (some #(= (str/lower-case (or name ""))
            (str/lower-case (:name %))) players))

(defn get-name [form-data]
  (str/trim (or (get-in form-data [:name :value]) "")))

(defn validate-name [state form-data _field-data]
  (let [players (filter #(not= (:id %) (:id form-data))
                        (get-in state [:db/data :players]))
        name (get-name form-data)]
    (cond
      (str/blank? name)
      "Required"

      (contains-name? players name)
      "Already exists"

      :else
      nil)))

(defn save-player [form-id form-data]
  (let [id (or (:id form-data) :db/new-id)]
    [[:db/transact [[:update :players id {:name (get-name form-data)}]]]
     [:form/reset form-id]]))

(defn delete-player [player]
  [[:event/stop-propagation]
   [:db/transact [[:delete :players (:id player)]]]])

(defn edit-player [player]
  [[:assoc-in [:edit-player :id] (:id player)]
   [:assoc-in [:edit-player :name :value] (:name player)]])

(defn render-new-player-form [state]
  [:form.flex.gap-2 {:on {:submit [[:form/submit :new-player]]}}
   (text-input state :new-player :name {:placeholder "Name", :autofocus true})
   [:button.btn.btn-primary {:type :submit} "Add"]])

(defn render-players [{:db/keys [data] :as state}]
  [:div.list.rounded-box.shadow-md.bg-base-100
   (for [player (:players data)]
     (if (= (:id player) (get-in state [:edit-player :id]))
       [:form.list-row {:replicant/key (:id player)
                        :on {:submit [[:form/submit :edit-player]]}}
        (text-input state :edit-player :name {:placeholder "Name"
                                              :autofocus true
                                              :class ["list-col-grow"]})
        [:div.flex.gap-1
         [:button.btn.btn-primary
          {:type :submit}
          (icons/render :check)]
         [:button.btn.btn-ghost
          {:type :button :on {:click [[:form/reset :edit-player]]}}
          (icons/render :x)]]]
       [:div.list-row {:replicant/key (:id player)
                       :role "button"
                       :tabindex 0
                       :on {:click (edit-player player)}}
        [:div.list-col-grow.self-center (:name player)]
        [:button.btn.btn-ghost
         {:on {:click (delete-player player)}}
         (icons/render :trash)]]))])

(def players-page
  (page/define
    {:page/id :players
     :page/route ["players"]
     :page/query (fn [_state] {:players {}})
     :page/forms [{:form/id :new-player
                   :form/fields [{:field/name :name
                                  :field/validate validate-name}]
                   :form/submit (fn [_state form-data] (save-player :new-player form-data))}
                  {:form/id :edit-player
                   :form/fields [{:field/name :name
                                  :field/validate validate-name}]
                   :form/submit (fn [_state form-data] (save-player :edit-player form-data))}]
     :page/render
     (fn [state]
       [:div.container.mx-auto.flex.flex-col.gap-4.mt-4.p-4
        [:h1.text-xl "Players"]
        (render-new-player-form state)
        (render-players state)])}))
