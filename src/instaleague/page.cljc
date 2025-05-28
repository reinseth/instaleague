(ns instaleague.page)

(defn define [{:page/keys [id route query render] :as attrs}]
  (when-not id
    (throw (ex-info "Pages are expected to have a unique :page/id" attrs)))
  (when-not route
    (throw (ex-info (str "Pages are expected to define a :page/route in "
                         "the form of a vector of path components and variables, "
                         "e.g. [\"todos\" :id]")
                    attrs)))
  (when-not query
    (throw (ex-info "Pages are expected to define a :page/query function" attrs)))
  (when-not render
    (throw (ex-info (str "Pages are expected to define a :page/render function "
                         "that takes the query data, route params and state as params")
                    attrs)))
  attrs)
