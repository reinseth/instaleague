(ns dev)

(defn ^:dev/after-load re-render []
  (js/console.log "re-render"))
