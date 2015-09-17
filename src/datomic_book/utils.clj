(ns datomic-book.utils
  (:require [datomic.api :as d]))

(defn read-file [s]
  (read-string (slurp s)))

(defn get-conn [uri]
  (d/connect uri))

(defn get-db [uri]
  (d/db (get-conn uri)))

(defn init-db [uri schema seed-data]
  (let [conn (do (d/delete-database uri)
                 (d/create-database uri)
                 (get-conn uri))]
    @(d/transact conn schema)
    @(d/transact conn seed-data)
    (d/db conn)))

(defn format-k [value]
  (format "%d K" value))