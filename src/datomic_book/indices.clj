(ns datomic-book.indices
  (:require [datomic.api :as d]
            [datomic-book.utils :refer [get-conn get-db init-db read-file]]))

(def uri "datomic:mem://indices")

(let [schema (read-file "resources/db/identity/schema.edn")
      seed-data (read-file "resources/db/identity/data.edn")]
  (init-db uri schema seed-data))

(d/pull
  (get-db uri)
  '[*]
  [:book/title "Lord Jim"])

; datoms
;get all datoms:
(map
  #(identity %)
  (d/datoms
    (get-db uri)
    :avet
    :book/title))

(class
  (d/datoms
    (get-db uri)
    :avet
    :book/title))

; get all ent ids:
(map
  :e
  (d/datoms
    (get-db uri)
    :avet
    :book/title))

; pull data for first entity from index
(d/pull
  (get-db uri)
  '[*]
  (first
    (map
      :e
      (d/datoms
        (get-db uri)
        :avet
        :book/title))))

; narow the scope by proiding components
(map
  #(identity %)
      (d/datoms
        (get-db uri)
        :avet
        :book/title "Lord Jim"))


; throws exception - :person/email is not an indexed attr
#_(map
  #(identity %)
  (d/datoms
    (get-db uri)
    :avet
    :person/email))

; no component specified - will return all datoms in DB!!!
(map
  #(identity %)
  (d/datoms
    (get-db uri)
    :eavt))

; paritions - new entity scans:
(defn find-author-entity-by-name [name]
  (d/q '[:find ?e
         :in $ ?name
         :where [?e :person/name ?name]]
       (get-db uri) name))

(def joseph-conrad
  (ffirst (find-author-entity-by-name "Joseph Conrad")))

@(d/transact
   (get-conn uri)
   [{:db/id       (d/tempid :library.books)
     :book/title  "The Arrow of Gold"
     :book/author joseph-conrad
     :book/genre  :book.genre/novel}])

@(d/transact
   (get-conn uri)
   [{:db/id       (d/tempid :library.books)
     :book/title  "Nostromo"
     :book/author joseph-conrad
     :book/genre  :book.genre/novel}])

; find all the books by Joseph Conrad:
(d/q
  '[:find ?title
    :in $ ?author
    :where [?b :book/author ?author]
    [?b :book/title ?title]]
  (get-db uri)
  joseph-conrad)

(d/basis-t (get-db uri))

; (entid-at db part t-or-date)
(def ent-at
  (d/entid-at
    (get-db uri)
    :library.books
    ;:users
    ;:db.part/user
    (d/basis-t (get-db uri))
    ))

ent-at

(def ent
  (d/pull
    (get-db uri)
    '[*]
    ;277076930200589
    ;17592186045453
    277076930200591
    ))

(ent :db/id)

(d/entity
  (get-db uri)
  (ent :db/id))

(d/touch (d/entity
           (get-db uri)
           (ent :db/id)))

(map
  ;#(identity %)
  :v
     (d/seek-datoms
       (get-db uri)
       :eavt
       ent-at
       ))

; interestingly entity WILL return and entity for non-existing ent
; and not nil
(def non-existing-id 123123)
(d/entity (get-db uri) non-existing-id)

;

