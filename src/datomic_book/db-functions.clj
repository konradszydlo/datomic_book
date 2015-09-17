(ns datomic-book.indices
  (:require [datomic.api :as d]
            [datomic-book.utils :refer [get-conn get-db init-db read-file]]))

(def uri "datomic:mem://db-functions")

(let [schema (read-file "resources/db/db_functions/schema.edn")
      seed-data (read-file "resources/db/db_functions/data.edn")]
  (init-db uri schema seed-data))

@(d/transact
   (get-conn uri)
   (read-file "resources/db/db_functions/functions.edn"))

; create a function
; function that requires project file:
(d/invoke
  (get-db uri)
  :calc-k-volume
  4000)

; function that requires clojure lib:
(d/invoke
  (get-db uri)
  :join-strs
  "aaaa" "bbb")

;; create a function programmatically
(def greeting
  (d/function '{:lang :clojure
                :params [name]
                :requires [[clojure.string :as str]]
                :code (str "Hello, " (str/upper-case name))}))

(greeting "Arwen")

;; install the function in a database, under the name :hello
@(d/transact
   (get-conn uri)
   [{:db/id (d/tempid :db.part/user)
     :db/doc "Example function returning a greeting with name upper-cased"
     :db/ident :greeting-upper
     :db/fn greeting}])


;; retrieve function from db and call it
(def greeting-from-db (d/entity (get-db uri) :greeting-upper))

(:db/doc greeting-from-db)
((:db/fn greeting-from-db) "Legolas")

; using ident in transaction for upsert
(defn find-author-entity-by-name [name]
  (d/q '[:find ?e
         :in $ ?name
         :where [?e :person/name ?name]]
       (get-db uri) name))

(def joseph-conrad
  (ffirst (find-author-entity-by-name "Joseph Conrad")))

;; get a validation function from the database
(def validate-book
  (-> (d/entity (get-db uri) :validate-book) :db/fn))

;; validate an invalid book - throws an exception:
#_(-> (validate-book {:book/author "Nostromo"}))

;; validate a valid book
(validate-book {:book/title "Nostromo"
                :book/author joseph-conrad})

;; get a constructor function from the database
(def construct-book
  (-> (d/entity (get-db uri) :construct-book) :db/fn))

;; test constructing an invalid book locally - throws an exception
#_(-> (construct-book db {}))


;; test constructing a valid book locally
(construct-book
  (get-db uri)
  {:book/title "Victory"
   :book/author joseph-conrad})

;; create a book in the database!
@(d/transact
   (get-conn uri)
   [[:construct-book
     {:book/title "Victory"
      :book/author joseph-conrad}]])

(d/q
  '[:find ?title
    :in $ ?author
    :where [?b :book/author ?author]
    [?b :book/title ?title]]
  (get-db uri)
  joseph-conrad)

; note that :construct-book is called only with book.
; db is passed implicitly:
(defn create-book [ db conn book note]
  (let [txid (datomic.api/tempid :db.part/tx)]
    (d/transact conn [[:construct-book book]
                      {:db/id txid, :db/doc note }])))

(create-book
  (get-db uri)
  (get-conn uri)
  {:book/title "The Secret Agent"
   :book/author joseph-conrad}
  "adding The Secret Agent")



