(ns instaleague.icons
  (:require [phosphor.icons :as i]))

(def icons
  {:check (i/icon :phosphor.regular/check)
   :x (i/icon :phosphor.regular/x)
   :trash (i/icon :phosphor.regular/trash-simple)})

(defn size-to-css-value [size]
  (case (or size :md)
    :context "1em"
    :sm 16
    :md 20
    :lg 24
    :xl 32
    :xxl 50
    :mega 104
    :jumbo 160))

(defn render
  ([name] (render name nil))
  ([name attrs]
   (let [{:keys [size style]} attrs]
     (if-let [svg (@i/icons (icons name))]
       (-> svg
           (assoc-in [1 :fill] "currentColor")
           (assoc-in [1 :style] (cond-> {:display "inline-block"
                                         :line-height "1"
                                         :width (size-to-css-value size)
                                         :height (size-to-css-value size)}
                                  style (into style)))
           (update 1 merge (dissoc attrs :size :style)))
       (throw (ex-info "Invalid icon" {:name name}))))))
