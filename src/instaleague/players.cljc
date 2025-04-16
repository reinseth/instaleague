(ns instaleague.players
  (:require [clojure.string :as str]
            [instaleague.page :as page]))

(defn contains-name? [name players]
  (some #(= (str/lower-case (or name ""))
            (str/lower-case (:name %))) players))

(defn add-player [data state]
  (let [name (str/trim (or (:new-player/name state) ""))]
    (into [[:event/prevent-default]]
          (cond
            (str/blank? name)
            [[:assoc-in [:new-player/validation-error] "Required"]]

            (contains-name? name (:players data))
            [[:assoc-in [:new-player/validation-error] "Already exists"]]

            :else
            [[:db/transact [[:update :players :db/new-id {:name name}]]]
             [:assoc-in [:new-player/name] nil]
             [:assoc-in [:new-player/validation-error] nil]]))))

(defn delete-player [player]
  [[:db/transact [[:delete :players (:id player)]]]])

(def players-page
  (page/define
    {:id :pages/players
     :route ["players"]
     :query (fn [_route _state]
              {:players {}})
     :render
     (fn [data _route state]
       [:div.container.mx-auto.flex.flex-col.gap-4.mt-4.p-4
        [:h1.text-xl "Players"]
        [:form.flex.gap-2 {:on {:submit (add-player data state)}}
         [:input.input {:class (when (:new-player/validation-error state) "input-error")
                        :placeholder "Name"
                        :autofocus true
                        :value (:new-player/name state "")
                        :on {:input [[:assoc-in [:new-player/name] :event/target.value]]}}]
         [:button.btn.btn-primary {:type :submit} "Add"]]
        [:ul.list.rounded-box.shadow-md.bg-base-100
         (for [player (:players data)]
           [:li.list-row {:replicant/key (:id player)}
            [:div.list-col-grow.self-center (:name player)]
            [:button.btn.btn-ghost
             {:on {:click (delete-player player)}}
             "x"]])]
        ])}))
