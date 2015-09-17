(ns datomic-book.transactions
  (:require [datomic.api :as d]))

(defn read-file [s]
  (read-string (slurp s)))

(def uri "datomic:mem://books-transactions")

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
  (let [schema (read-file "resources/db/transactions/schema.edn")
        seed-data (read-file "resources/db/transactions/data.edn")]
    (init-db uri schema seed-data)))

app

(def q-title-by-genre
  (d/q '[:find ?title
         :where [?b :book/genre :book.genre/novel]
         [?b :book/title ?title]]
       (get-db uri)))

q-title-by-genre

; [:find ?e :in $ ?email :where [?e :person/email ?email]]

(defn find-author-entity-by-name [name]
  (d/q '[:find ?e
         :in $ ?name
         :where [?e :person/name ?name]]
       (get-db uri) name))

(find-author-entity-by-name "Joseph Conrad")

(defn find-book-by-author [author]
  (d/q '[:find ?b
         :in $ ?author-name
         :where [?a :person/name ?author-name]
         [?b :book/author ?a]]
       (get-db uri) author))

(find-book-by-author "Joseph Conrad")

(def find-title-by-entity-id
  (d/q '[:find ?title
         :in $ ?e
         :where [?b :book/title ?title]
         [?b :book/author ?e]]
       (get-db uri) (ffirst (find-author-entity-by-name "Joseph Conrad"))))

find-title-by-entity-id

; not working properly. throwing exception.
#_(def find-title-by-db-ident
  (d/q '[:find ?title
         :in $ ?e
         :where [?b :book/title ?title]
         [?b :book/author ?e]]
       (get-db uri) "Joseph Conrad"))

;find-title-by-db-ident


(def find-value-of-attribute
  (d/q '[:find ?value
         :where [?e :book/title "Lord Jim"]
         [?e ?a]
         [?a :db/ident ?value]]
       (get-db uri)))

find-value-of-attribute

; add data to new entity
(def joseph-conrad
  (ffirst (find-author-entity-by-name "Joseph Conrad")))

joseph-conrad

; adding using map - id coded:
@(d/transact
   (get-conn uri)
   [{:db/id       #db/id[:db.part/user]
     :book/title  "ttheee Heart of Darkness"
     :book/author joseph-conrad
     :book/genre  :book.genre/novel}])

; adding using map - programatical id:
@(d/transact
   (get-conn uri)
   [{:db/id       (d/tempid :db.part/user)
     :book/title  "The Arrow of Gold"
     :book/author joseph-conrad
     :book/genre  :book.genre/novel}])

; adding using list: -  either tempid or coded
(def nostromo-tempid (d/tempid :db.part/user))

@(d/transact
   (get-conn uri)
   [[:db/add nostromo-tempid
     ;(d/tempid :db.part/user)
     :book/title "Nostromo"]
    [:db/add nostromo-tempid
     :book/author joseph-conrad]
    [:db/add nostromo-tempid
     :book/genre :book.genre/novel]])

(defn find-book-titles-by-author-name
  [name]
  (d/q '[:find ?title
         :in $ ?name
         :where [?b :book/author ?a]
         [?a :person/name ?name]
         [?b :book/title ?title]]
       (get-db uri) name))

(find-book-titles-by-author-name "Joseph Conrad")

; works
@(d/transact
   (get-conn uri)
   [[:db/add
     (d/tempid :db.part/user)
     :db/doc
     "Add random doc"]])


;; query input and result are data
(def get-random-doc-entity (d/q '[:find ?e .
                                  :where [?e :db/doc "Add random doc"]]
                                (get-db uri)))

get-random-doc-entity

@(d/transact
   (get-conn uri)
   [[:db/add (d/tempid :db.part/user)
     :book/title "Typhoon"]
    [:db/add (d/tempid :db.part/user)
     :book/genre :book.genre/novel]])

(defn find-all-titles []
  (d/q '[:find ?title
         :where [_ :book/title ?title]]
       (get-db uri)))

(find-all-titles)

@(d/transact
   (get-conn uri)
   [[:db/add (d/tempid :db.part/user)
     :book/title "Victory"]
    [:db/add (d/tempid :db.part/user)
     :book/title "The Rescue"]])


; not working as expected - not adding additional entities because more than one attribute specified:
; it will add a title but no other entities though no error is present.
@(d/transact
   (get-conn uri)
   [[:db/add
     (d/tempid :db.part/user)
     :book/title "Nostromo-test"
     :book/genre :book.genre/novel]])

(defn find-titles-with-genre []
  (d/q '[:find ?title ?genre
         :where [?b :book/title ?title]
         [?b :book/genre ?genre]]
       (get-db uri)))

(find-titles-with-genre)

(def find-novels
  (d/q '[:find ?title
         :where [?b :book/genre :book.genre/novel]
         [?b :book/title ?title]]
       (get-db uri)))

find-novels


; find book's entity based on it's title
(defn find-book-entityid-by-title [title]
  (d/q '[:find ?b
         :in $ ?title
         :where [?b :book/title ?title]]
       (get-db uri)
       title))

(def heart-of-darkness-entity-id
  (ffirst (find-book-entityid-by-title "ttheee Heart of Darkness")))

; update the title to correct one:
@(d/transact
   (get-conn uri)
   [{:db/id      heart-of-darkness-entity-id
     :book/title "The Heart of Darkness"}])

(find-book-titles-by-author-name "Joseph Conrad")

@(d/transact
   (get-conn uri)
   [{:db/id       #db/id[:db.part/user -1]
     :person/name "Samuel Beckett"}
    {:db/id       #db/id[:db.part/user]
     :book/title  "Watt"
     :book/author #db/id[:db.part/user -1]}])

(find-book-titles-by-author-name "Samuel Beckett")

; retracting data
(def adding-authors-transaction
  @(d/transact
     (get-conn uri)
     [{:db/id                 (d/tempid :db.part/user)
       :person/name           "Henrik Ibsen"
       :person/place-of-birth "Skien"
       :person/email          "henrik@theatre.com"
       }
      {:db/id                 (d/tempid :db.part/user)
       :person/name           "Slawomir Mrozek"
       :person/place-of-birth "Borzecin"}
      {:db/id                 joseph-conrad
       :person/place-of-birth "Berdyczow"}]))

; get the value of future with :tempids, :db-after etc
adding-authors-transaction

(adding-authors-transaction :tempids)

(class (adding-authors-transaction :tempids))

(first (vals (adding-authors-transaction :tempids)))

(defn get-entity-details-by-id [id]
  (d/q '[:find ?val
         :in $ ?id
         :where [?id ?a ?val]]
       (get-db uri)
       id))

(get-entity-details-by-id (first (vals (adding-authors-transaction :tempids))))

(get-entity-details-by-id (second (vals (adding-authors-transaction :tempids))))

(def db-before-authors (adding-authors-transaction :db-before))

(def db-after-authors (adding-authors-transaction :db-after))

(defn find-person-details-by-name [name db]
  (d/q '[:find ?val
         :in $ ?name
         :where [?p :person/name ?name]
         [?p ?a ?val]]
       db
       name))

(find-person-details-by-name "Henrik Ibsen" (get-db uri))

(find-person-details-by-name "Slawomir Mrozek" (get-db uri))

(find-person-details-by-name "Joseph Conrad" (get-db uri))

(find-person-details-by-name "Joseph Conrad" db-before-authors)

(find-person-details-by-name "Joseph Conrad" db-after-authors)

; :tx-data part

(d/q '[:find ?e ?aname ?v ?added
       :in $ [[?e ?a ?v _ ?added]]
       :where
       [?e ?a ?v _ ?added]
       [?a :db/ident ?aname]]
     db-after-authors
     (adding-authors-transaction :tx-data))

(def henrik-ibsen
  (ffirst (d/q '[:find ?p
                 :where [?p :person/name "Henrik Ibsen"]]
               (get-db uri))))

henrik-ibsen

#_@(d/transact
   (get-conn uri)
   [[:db/retract henrik-ibsen
     :person/place-of-birth "Skien"]])

@(d/transact
   (get-conn uri)
   [[:db/retract henrik-ibsen
     :person/place-of-birth "Skien"]
    [:db/retract henrik-ibsen
     :person/email "henrik@theatre.com"]])

(find-person-details-by-name "Henrik Ibsen" (get-db uri))

; retract entity
(def mrozek-entity-id
  (ffirst (find-author-entity-by-name "Slawomir Mrozek")))

mrozek-entity-id

(defn find-all-people []
  (d/q '[:find ?name
         :where [_ :person/name ?name]]
       (get-db uri)))

(find-all-people)

@(d/transact
   (get-conn uri)
   [[:db.fn/retractEntity mrozek-entity-id]
    [:db.fn/retractEntity [:person/name "Henrik Ibsen"]]
    ])

; component it

(defn find-book-details-by-title [title]
  (d/q '[:find ?val
         :in $ ?title
         :where [?b :book/title ?title]
         [?b ?a]
         [?a :db/ident ?val]]
       (get-db uri)
       title))

(find-book-details-by-title "Lord Jim")



(-> (get-db uri) (d/entity (ffirst (find-book-entityid-by-title "Lord Jim"))) d/touch)

(d/pull
  (get-db uri)
  '[*]
  [:book/title "Lord Jim"])



@(d/transact
   (get-conn uri)
   [[:db.fn/retractEntity (ffirst (find-book-entityid-by-title "Lord Jim"))]])

(-> (get-db uri) (d/entity 13194139534351) d/touch)

(d/q '[:find ?name
       :where [_ :city/name ?name]]
     (get-db uri))

; :db.fn/cas example:
; [[:db.fn/cas 42 :account/balance 100 110]]

(def animal-farm-book (ffirst (find-book-entityid-by-title "Animal Farm")))

;(find-book-entityid-by-title "1984")
(find-all-titles)

(d/pull
  (get-db uri)
  '[:book/title]
  animal-farm-book)

@(d/transact
   (get-conn uri)
   [[:db.fn/cas animal-farm-book :book/title "Animal Farm" "1984"]])

; Processig transactions:

; submitting transactions:

(def transact-async-trans
  @(d/transact-async
     (get-conn uri)
     [{:db/id                 (d/tempid :db.part/user)
       :person/name           "Stanislaw Lem"
       :person/place-of-birth "Lwow"}]))

(transact-async-trans :db-after)

(find-person-details-by-name "Stanislaw Lem" (transact-async-trans :db-after))

; monitoring transactions:

(def report-queue-1 (d/tx-report-queue (get-conn uri)))

@(d/transact-async
   (get-conn uri)
   [{:db/id                 (d/tempid :db.part/user)
     :person/name           "Henryk Sienkiewicz"
     :person/place-of-birth "Wola Okrzejska"}])


(.poll report-queue-1)

(d/remove-tx-report-queue (get-conn uri))

(def report-queue-2 (d/tx-report-queue (get-conn uri)))

@(d/transact-async
   (get-conn uri)
   [{:db/id                 (d/tempid :db.part/user)
     :person/name           "Czeslaw Milosz"
     :person/place-of-birth "Szetejnie"}])

(.poll report-queue-1)
(.poll report-queue-2)

; reified transactions:

(def tx-temp-id (d/tempid :db.part/tx))

@(d/transact-async
   (get-conn uri)
   [{:db/id (d/tempid :db.part/user)
     :person/name "Zbigniew Herbert"
     :person/place-of-birth "Lwow"}
    {:db/id tx-temp-id
     :db/doc "Wikipedia"}])

(d/pull
  (get-db uri)
  '[*]
  [:person/name "Zbigniew Herbert"])

(def herbert-transaction-datom-id
  (d/t->tx (d/basis-t (get-db uri))))

herbert-transaction-datom-id

(d/pull
  (get-db uri)
  '[*]
  herbert-transaction-datom-id)




