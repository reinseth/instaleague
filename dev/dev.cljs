(ns dev
  (:require [instaleague.main :as main]))

(defn ^:dev/after-load re-render []
  (main/render @main/store))
