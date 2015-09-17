(ns datomic-book.identity
  (:require [datomic.api :as d]))


(defn read-file [s]
  (read-string (slurp s)))

(def uri "datomic:mem://identity")

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

(let [schema (read-file "resources/db/identity/schema.edn")
      seed-data (read-file "resources/db/identity/data.edn")]
  (init-db uri schema seed-data))

; entities
(def victory-temp-id (d/tempid :db.part/user))
(def rescue-temp-id (d/tempid :library.books))

(def adding-books-trans
  @(d/transact
     (get-conn uri)
     [[:db/add victory-temp-id
       :book/title "Victory"]
      [:db/add rescue-temp-id
       :book/title "The Rescue"]]))

(:tempids adding-books-trans)

(def victory-ent-id
  (d/resolve-tempid
    (get-db uri)
    (:tempids adding-books-trans)
    victory-temp-id))

victory-ent-id

(d/part victory-ent-id)

(d/pull
  (get-db uri)
  '[*]
  (d/part victory-ent-id))


(def rescue-ent-id
  (d/resolve-tempid
    (get-db uri)
    (:tempids adding-books-trans)
    rescue-temp-id))

(d/pull
  (get-db uri)
  '[*]
  (d/part rescue-ent-id))

; idents

; [42 :book/genre :book.genre/novel]
; ! [42 11536 5677]

(def genre-data
  (d/pull
    (get-db uri)
    '[*]
    :book/genre))

(def book-genre-id
  (genre-data :db/id))

book-genre-id

(def novel-data
  (d/pull
    (get-db uri)
    '[*]
    :book.genre/novel))

(def novel-id
  (novel-data :db/id))

novel-id

(d/q
  '[:find ?title
    :in $ ?attr ?val
    :where [?b ?attr ?val]
    [?b :book/title ?title]]
  (get-db uri)
  book-genre-id novel-id)

; not working returns an empty set
(d/q
  '[:find ?title
    :in $ ?b ?attr ?val
    :where [?b ?attr ?val]
    [?b :book/title ?title]]
  (get-db uri)
  17592186045426 :book/genre :book.genre/novel)

; navigating a book returns ident for :book/genre not entity
(def lord-jim
  (d/entity
    (get-db uri)
    [:book/title "Lord Jim"]))

(d/touch lord-jim)

;return keyword associated with ent id
(d/ident
  (get-db uri)
  novel-id)

; return keyword itself
(d/ident
  (get-db uri)
  :book.genre/fiction)

(d/entid
  (get-db uri)
  :book/genre)

(d/entid
  (get-db uri)
  book-genre-id)

; even though :book/title and :book/alternative-title
; are unique the values between them can be the same
(d/pull
  (get-db uri)
  '[*]
  [:book/title "Animal Farm"])


; using ident in transaction for upsert
(defn find-author-entity-by-name [name]
  (d/q '[:find ?e
         :in $ ?name
         :where [?e :person/name ?name]]
       (get-db uri) name))

(def joseph-conrad
  (ffirst (find-author-entity-by-name "Joseph Conrad")))

@(d/transact
   (get-conn uri)
   [{:db/id       #db/id[:db.part/user]
     :book/title  "The Heart of Darkness"
     :book/author joseph-conrad
     :book/genre  :book.genre/novel}])

(d/pull
  (get-db uri)
  '[*]
  [:book/title "The Heart of Darkness"])

@(d/transact
   (get-conn uri)
   [{:db/id       #db/id[:db.part/user]
     :book/title  "The Heart of Darkness"
     :book/alternative-title "The Apocalypse"
     :book/author joseph-conrad
     :book/genre  :book.genre/novel}])

#_(def george-orwell
  (ffirst (find-author-entity-by-name "George Orwell")))

#_@(d/transact
   (get-conn uri)
   [{:db/id       #db/id[:db.part/user]
     :book/title  "The Heart of Darkness"
     :book/author george-orwell
     :book/genre  :book.genre/fiction}])

; squuids
(def my-squuid
  (d/squuid))

my-squuid

(d/squuid-time-millis my-squuid)

; lookup refs
(def lord-jim
  (d/entity
    (get-db uri)
    [:book/title "Lord Jim"]))

lord-jim

(def lord-jim-ent
  (d/touch lord-jim))

lord-jim-ent

@(d/transact
   (get-conn uri)
   [{:db/id       [:book/title "Lord Jim"]
     :book/alternative-title "Sea Voyage"}])

(d/pull
  (get-db uri)
  '[*]
  [:book/title "Lord Jim"])

(d/q
  '[:find ?alt-title
    :in $ ?attr ?val
    :where [?b ?attr ?val]
    [?b :book/alternative-title ?alt-title]]
  (get-db uri)
  :book/title
  "Lord Jim")
;