(ns datomic-book.excision
  (:require [datomic.api :as d]
            [datomic-book.utils :refer [get-conn get-db init-db read-file]]))

; !!! Excision has no effect on the memory database, as the memory database has no storage. !!!

(def uri "datomic:mem://excision")

(let [schema (read-file "resources/db/db_functions/schema.edn")
      seed-data (read-file "resources/db/db_functions/data.edn")]
  (init-db uri schema seed-data))

(defn find-all-books []
  (d/q '[:find ?b ?title ?genre ?name
         :where [?b :book/title ?title]
         [?b :book/genre ?g]
         [?g :db/ident ?genre]
         [?b :book/author ?a]
         [?a :person/name ?name]]
       (get-db uri)))

(find-all-books)

(def the-heart-of-darkness
  (ffirst
    (d/q
      '[:find ?e
        :where [?e :book/title "The Heart of Darkness"]]
      (get-db uri))))

the-heart-of-darkness

; excising specific entity
#_@(d/transact
  (get-conn uri)
  [{:db/id     #db/id[:db.part/user],
    :db/excise the-heart-of-darkness}])

(find-all-books)

(d/pull
  (get-db uri)
  '[*]
  the-heart-of-darkness)

; excising specific attributes of an entity
@(d/transact
  (get-conn uri)
  [{:db/id           #db/id[:db.part/user],
    :db/excise       the-heart-of-darkness
    :db.excise/attrs [:book/author]}])


(d/request-index (get-conn uri))

(def joseph-conrad
  (ffirst
    (d/q '[:find ?e
           :where [?e :person/name "Joseph Conrad"]]
         (get-db uri))))

(def the-arrow
  @(d/transact
     (get-conn uri)
     [{:db/id       (d/tempid :db.part/user)
       :book/title  "The Arrow of Gold"
       :book/author joseph-conrad
       :book/genre  :book.genre/novel}]))

(def the-arrow-trans-date
  (:v (first (:tx-data the-arrow))))

the-arrow-trans-date

; java.util.Date
(class the-arrow-trans-date)

(def the-arrow-t
  (d/basis-t (get-db uri)))

; store the t value for transaction adding The Arrow of Gold
the-arrow-t

#_@(d/transact
   (get-conn uri)
   [{:db/id       (d/tempid :db.part/user)
     :book/title  "Nostromo"
     :book/author joseph-conrad
     :book/genre  :book.genre/novel}])

; Excising Old Values of an Attribute

; use before
@(d/transact
   (get-conn uri)
   [{:db/id (d/tempid :db.part/user)
     :db/excise :book/title
     :db.excise/before the-arrow-trans-date}])

; use beforeT
@(d/transact
   (get-conn uri)
   [{:db/id (d/tempid :db.part/user)
     :db/excise :book/author
     :db.excise/before the-arrow-t}])


; find all excised entities
(d/q
  '[:find ?e :where [?e :db/excise]]
  (get-db uri))

; find excised attributes for The Heart of Darkness:
(d/q
  '[:find ?e :where [?e :db/excise the-heart-of-darkness]]
  (get-db uri))


