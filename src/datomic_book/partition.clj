(ns datomic-book.parition
  (:require [datomic.api :as d]))

(slurp "resources/db/partition/data.edn")

(slurp "resources/db/partition/schema.edn")

(defn read-file [s]
  (read-string (slurp s)))

(def uri "datomic:mem://books-partition")

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

(def app
  (let [schema (read-file "resources/db/partition/schema.edn")
        seed-data (read-file "resources/db/partition/data.edn")]
    (init-db uri schema seed-data)))

app


(def q-title (d/q '[:find ?title
                    :where [?m :book/title ?title]]
                  (get-db uri)))

q-title

(def q-genre (d/q '[:find ?genre
                  :where [_ :book/genre ?genre]]
                (get-db uri)))

q-genre

(def q-title-by-genre
  (d/q '[:find ?title
         :where [?b :book/genre :book.genre/novel]
                [?b :book/title ?title]]
       (get-db uri)))

q-title-by-genre

(def get-partitions
  (d/q '[:find ?ident :where [:db.part/db :db.install/partition ?p]
         [?p :db/ident ?ident]]
       (get-db uri)))

get-partitions