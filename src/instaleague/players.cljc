(ns instaleague.players)

(def players-page
  {:id :pages/players
   :route ["players"]
   :query (fn [_route _state]
            {:players {}})
   :render (fn [data _route state]
             [:div.container.mx-auto
              [:h1 "Players"]
              [:ul
               (for [player (:players data)]
                 [:li (:name player)])]
              
              [:form.flex.gap-2 {:on {:submit [[:event/prevent-default]
                                               [:db/transact [[:players :db/new-id {:name (:new-player/name state)}]]]]}}
               [:input.input {:placeholder "Name"
                              :autofocus true
                              :on {:input [[:assoc-in [:new-player/name] :event/target.value]]}}]
               [:button.btn.btn-primary {:type :submit} "Add"]]])})
