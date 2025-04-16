(ns instaleague.page)

(defn define [{:keys [id route query render] :as attrs}]
  (when-not id
    (throw (js/Error. "Pages are expected to have a unique :id")))
  (when-not route
    (throw (js/Error. "Pages are expected to define a :route in the form of a vector of path components and variables, e.g. [\"todos\" :id]")))
  (when-not query
    (throw (js/Error. "Pages are expected to define a :query function")))
  (when-not render
    (throw (js/Error. "Pages are expected to define a :render function that takes the query data, route params and state as params")))
  attrs)
