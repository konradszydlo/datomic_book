(ns datomic-book.core
  (:require [datomic.api :as d]))



(slurp "resources/db/data.edn")

(slurp "resources/db/schema.edn")

(slurp "resources/db/test.edn")


(defn read-file [s]
  (read-string (slurp s)))

(read-file "resources/db/test.edn")

(def uri "datomic:mem://book")

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
  (let [schema (read-file "resources/db/schema.edn")
        seed-data (read-file "resources/db/data.edn")]
    (init-db uri schema seed-data)))

app


(def q-title (d/q '[:find ?title
                    :where [?m :book/title ?title]]
                  (get-db uri)))

q-title


(def q-author (d/q '[:find ?name
                    :where [?book :book/title "1984"]
                     [?book :book/author ?author]
                     [?author :person/name ?name]]
                  (get-db uri)))

q-author
