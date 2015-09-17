(ns datomic-book.pull
  (:require [datomic.api :as d]))


(defn read-file [s]
  (read-string (slurp s)))

(def uri "datomic:mem://pull")

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

(let [schema (read-file "resources/db/pull/schema.edn")
      seed-data (read-file "resources/db/pull/data.edn")]
  (init-db uri schema seed-data))

(defn find-author-entity-by-name [name]
  (d/q '[:find ?e
         :in $ ?name
         :where [?e :person/name ?name]]
       (get-db uri) name))

(def joseph-conrad
  (ffirst (find-author-entity-by-name "Joseph Conrad")))

(d/pull
  (get-db uri)
  '[*]
  joseph-conrad)

(defn find-book-entityid-by-title [title]
  (d/q '[:find ?b
         :in $ ?title
         :where [?b :book/title ?title]]
       (get-db uri)
       title))

(def lord-jim-id
  (ffirst (find-book-entityid-by-title "Lord Jim")))

(d/pull
  (get-db uri)
  '[:book/title :book/genre]
  lord-jim-id)

; in data.edn delared enum as :book.genre/fiction
; whereas it should be genre.type/fiction
(d/pull
  (get-db uri)
  '[:book/_genre]
  :book.genre/fiction)

(d/pull
  (get-db uri)
  '[:book/publications]
  lord-jim-id)

; reverese lookup
(d/pull
  (get-db uri)
  '[:book/_genre]
  :book.genre/fiction)

; multiple results. not returning results
(d/pull
  (get-db uri)
  '[:book/_publications]
  lord-jim-id)

; map specifications

; getting just author
(d/pull
  (get-db uri)
  [ {:book/author [:db/id :person/name]}]
  lord-jim-id)

; getting all attrs and overriding handling of author
(d/pull
  (get-db uri)
  '[* {:book/author [:db/id :person/name]}]
  ;[:publication/year {:publication/city [:db/id :city/name]}]
  lord-jim-id)

; nesting
(d/pull
  (get-db uri)
  [{:book/publications
    [:publication/year {:publication/city [:city/name]}]}]
  lord-jim-id)

; wild card
(d/pull
  (get-db uri)
  '[*]
  lord-jim-id)

; default expressions
(def adam-mickiewicz
  (ffirst (find-author-entity-by-name "Adam Mickiewicz")))

(d/pull
  (get-db uri)
  '[:person/name (default :person/email "N/A")]
  adam-mickiewicz)

(d/pull
  (get-db uri)
  '[:person/name (default :person/email 0)]
  adam-mickiewicz)

; missing attributes
(d/pull
  (get-db uri)
  [:person/name :person/nothing]
  joseph-conrad)

; limit expression
(def poland
     (ffirst (d/q
               '[:find ?e
                 :where [?e :country/name "Poland"]]
               (get-db uri))))

(d/pull
  (get-db uri)
  '[:country/name (limit :country/city 5)]
  poland)

(d/pull
  (get-db uri)
  '[:country/name {(limit :country/city 5) [:city/name]}]
  poland)

(d/pull
  (get-db uri)
  '[:country/name {(limit :country/city nil) [:city/name]}]
  poland)

; recursive specifications

(defn get-person-entity-id-by-name [name]
  (ffirst
    (d/q
      '[:find ?e
        :in $ ?name
        :where [?e :person/name ?name]]
      (get-db uri)
      name)))

(def dickens
  (get-person-entity-id-by-name "Charles Dickens"))

(d/pull
  (get-db uri)
  '[:person/name {:person/_influenced-by 3}]
  dickens)

(d/pull
  (get-db uri)
  '[:person/name {:person/_influenced-by ...}]
  dickens)

; v1
(d/pull
  (get-db uri)
  [{:person/influenced-by 5 } :person/name ]
  dickens)

; v2 - reversing :person/name and recursive influence
; gives results in different format:
(d/pull
  (get-db uri)
  [:person/name {:person/influenced-by 5 }]
  dickens)

; empty results

(d/pull
  (get-db uri)
  [:person/nothing]
  dickens)

(d/pull
  (get-db uri)
  [{:country/city [:city/name :ufo-site]} :country/name]
  poland)

; entity
; entity is a navigable view over data
(def ent
  (d/entity
    (get-db uri)
    poland))

ent

; entities are lazy, so...
(def pol-ent
  (d/touch ent))

(:country/name pol-ent)

(:country/city pol-ent)

(:country/_city pol-ent)

; use lookup ref to get entity
(def lord-jim
  (d/entity
    (get-db uri)
    [:book/title "Lord Jim"]))

lord-jim

(def lord-jim-ent
  (d/touch lord-jim))

lord-jim-ent

(def author (:book/author lord-jim-ent))

(def conrad
  (d/touch author))

conrad

(:book/_author lord-jim-ent)

(keys pol-ent)

; entities and time
(def pol-ent-db
  (d/entity-db pol-ent))

(d/q
  '[:find ?name
    :where [?b :book/title "Lord Jim"]
    [?b :book/author ?p]
    [?p :person/name ?name]]
  pol-ent-db)

; get basisT
(d/basis-t pol-ent-db)

