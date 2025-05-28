(ns instaleague.forms)

;;;
;;; Declarative forms concept taken from
;;; https://replicant.fun/tutorials/declarative-forms/
;;;

(defn keyword->s [k]
  (if-let [ns (namespace k)]
    (str ns "/" (name k))
    (name k)))

(defn text-input
  ([state form-id field-id]
   (text-input state form-id field-id {}))
  ([state form-id field-id attrs]
   (let [field (get-in state [form-id field-id])
         error (:error field)]
     [:div.flex.flex-col.grow
      [:input.input.w-full
       (into
        {:type "text"
         :name (keyword->s field-id)
         :value (:value field)
         :on {:input [[:assoc-in [form-id field-id :value] :event/target.value]
                      (when error
                        [:form/validate form-id field-id])]}}
        (cond-> attrs
          error
          (update :class conj "input-error")))]
      (when error
        [:div.validator-hint.text-error error])])))

(defn validate [state forms form-id field-name]
  (let [form (first (filter #(= form-id (:form/id %)) forms))
        field (first (filter #(= field-name (:field/name %)) (:form/fields form)))
        form-data (get state form-id)
        field-data (get form-data field-name)
        error ((:field/validate field) state form-data field-data)]
    [[:assoc-in [form-id field-name :error] error]]))

(defn submit [state forms form-id]
  (let [form (first (filter #(= form-id (:form/id %)) forms))
        fields (:form/fields form)
        form-data (get state form-id)
        validation-results (into
                            {}
                            (map (juxt
                                  :field/name
                                  (fn [field]
                                    ((:field/validate field) state form-data (get form-data (:field/name field))))))
                            fields)
        has-error? (some second validation-results)
        store-validations (mapv (fn [field]
                                  [:assoc-in [form-id (:field/name field) :error] (validation-results (:field/name field))])
                                fields)]
    (into
     store-validations
     (when-not has-error?
       ((:form/submit form) state form-data)))))

(defn reset [form-id]
  [[:assoc-in [form-id] nil]])

