(ns dataworks.db.user-db
  (:require
   [clojure.java.io :as io]
   [dataworks.utils.common :refer [read-string]]
   [crux.api :as crux]
   [mount.core :refer [defstate]]
   [tick.alpha.api :as time]))

(defstate kafka-settings
  :start
   (try
     (-> "config.edn"
         slurp
         read-string
         :kafka-settings)
     (catch Exception _ {})))

(defstate user-db
  :start
  (let [db (crux/start-node kafka-settings)]
    (println "synchronizing user-db")
    (crux/sync db (time/new-duration 3 :seconds))
    db)

  :stop
  (.close user-db))

(defn submit-tx
  "Shorthand for crux/submit-tx, using user-db"
  [transactions]
  (crux/submit-tx user-db transactions))

(defn query
  "Shorthand for crux/q using user-db"
  ([query]
   (crux/q (crux/db user-db) query))
  ([valid-time query]
   (crux/q (crux/db user-db valid-time) query))
  ([valid-time transaction-time query]
   (crux/q (crux/db user-db
                    valid-time
                    transaction-time)
           query)))

(defn entity
  "Shorthand for crux/entity using user-db"
  [entity-id]
  (crux/entity (crux/db user-db) entity-id))
