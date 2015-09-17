(ns datomic-book.filters
  (:require [datomic.api :as d]
            [datomic-book.utils :refer [get-conn get-db init-db read-file]])
  (:import (datomic Datom)))

(def uri "datomic:mem://filters")

(let [schema (read-file "resources/db/filters/schema.edn")
      seed-data (read-file "resources/db/filters/data.edn")]
  (init-db uri schema seed-data))

(def warsaw
  (ffirst
    (d/q
      '[:find ?e
        :where [?e :city/name "Warszawa"]]
      (get-db uri))))

(defn find-population [db city]
  (d/q
    '[:find ?pop
      :in $ ?city
      :where [?city :city/population ?pop]]
    db
    city))

(find-population (get-db uri) warsaw)

(def warsaw-1800
  @(d/transact
     (get-conn uri)
     [{:db/id           warsaw
       :city/population 63400}]))

(find-population (warsaw-1800 :db-after) warsaw)

(def warsaw-1830
  @(d/transact
     (get-conn uri)
     [{:db/id           warsaw
       :city/population 139700}]))

(find-population (warsaw-1830 :db-after) warsaw)

(def warsaw-1850
  @(d/transact
     (get-conn uri)
     [{:db/id           warsaw
       :city/population 163600}]))

(find-population (warsaw-1850 :db-after) warsaw)

(def warsaw-1882
  @(d/transact
     (get-conn uri)
     [{:db/id           warsaw
       :city/population 383000}]))

(find-population (warsaw-1882 :db-after) warsaw)


; as-of by t:
(def warsaw-1830-as-of-db-after-by-t-after
  (d/as-of
    (warsaw-1830 :db-after)
    (d/basis-t (warsaw-1830 :db-after))))

; expected 139700
(find-population warsaw-1830-as-of-db-after-by-t-after warsaw)

(def warsaw-1830-as-of-db-after-by-t-before
  (d/as-of
    (warsaw-1830 :db-after)
    (d/basis-t (warsaw-1830 :db-before))))

; expected 63400
(find-population warsaw-1830-as-of-db-after-by-t-before warsaw)


; as-of by transaction id:
(def warsaw-1830-tx-id
  (d/t->tx (d/basis-t (warsaw-1830 :db-after))))

(def warsaw-1830-as-of
  (d/as-of (warsaw-1830 :db-after) warsaw-1830-tx-id))

(find-population warsaw-1830-as-of warsaw)


; as-of by date
(def warsaw-1850-tx-date
  (ffirst
    (d/q '[:find ?t
           :in $ ?tx
           :where [?tx :db/txInstant ?t]]
         (warsaw-1850 :db-after)
         (d/t->tx (d/basis-t (warsaw-1850 :db-after))))))

; => java.util.Date
(class warsaw-1850-tx-date)

; 163600
(find-population
  (d/as-of (warsaw-1850 :db-after) warsaw-1850-tx-date)
  warsaw)

;; since
; => #{[163600]}
(find-population
  (d/since (warsaw-1850 :db-after) warsaw-1830-tx-id)
  warsaw)

(defn since-pop [db-since]
  (d/q '[:find ?count
         :in $ $since ?name
         :where [$ ?e :city/name ?name]
         [$since ?e :city/population ?count]]
       (get-db uri)
       db-since
       "Warszawa"))

; => 139700
(since-pop
  (d/since (warsaw-1830 :db-after) #inst "2015-01-01"))

; => 63400
(since-pop
  (d/since (warsaw-1830 :db-before) #inst "2015-01-01"))

; => {}
(since-pop
  (d/since (warsaw-1850 :db-before) warsaw-1830-tx-id))

; => 163600
(since-pop
  (d/since (warsaw-1850 :db-after) warsaw-1830-tx-id))

; => 163600
(since-pop
  (d/since (warsaw-1882 :db-before) warsaw-1830-tx-id))

; => 383000
(since-pop
  (d/since (warsaw-1882 :db-after) warsaw-1830-tx-id))

; => {}
(since-pop
  (d/since (warsaw-1882 :db-before) warsaw-1850-tx-date))

; => 383000
(since-pop
  (d/since (warsaw-1882 :db-after) warsaw-1850-tx-date))

; eoy - end of year
(def as-of-eoy-2015 (d/as-of (warsaw-1850 :db-after) #inst "2015-01-01"))

(d/pull
  (get-db uri)
  '[*]
  (:db/id (d/entity as-of-eoy-2015 warsaw)))

(def since-1830 (d/since (warsaw-1830 :db-after) warsaw-1830-tx-id))

(d/pull
  (warsaw-1850 :db-after)
  '[*]
  (:db/id (d/entity since-1830 warsaw)))

; returns only => {:db/id 17592186045421}
; needs investigating why
(d/pull
  since-1830
  '[*]
   warsaw)

(d/pull
  (get-db uri)
  '[*]
  warsaw)

;; history

(defn warsaw-history []
  (d/history (get-db uri)))

; filtering result through filter fn:
(->> (d/q '[:find ?aname ?v ?inst
            :in $ ?e
            :where [?e ?a ?v ?tx true]
            [?tx :db/txInstant ?inst]
            [?a :db/ident ?aname]]
          (warsaw-history)
          warsaw)
     (filter #(some #{:city/population} %))
     (sort-by #(nth % 2)))

; filtering in query:
(->> (d/q '[:find ?aname ?v ?inst
            :in $ ?e
            :where [?e ?a ?v ?tx true]
            [?tx :db/txInstant ?inst]
            [?a :db/ident ?aname]
            [?a :db/ident :city/population]]
          (warsaw-history) warsaw)
     (sort-by #(nth % 2)))

(defn get-population-history [db]
  (d/q
    '[:find ?e ?pop ?tx
      :where [?e :city/name "Warszawa"]
      [?e :city/population ?pop ?tx true]]
    db))

; returns set with a single vector
(get-population-history (get-db uri))

; returns a set with many vectors
(get-population-history (warsaw-history))

; filtering errors

(def warsaw-error
  @(d/transact
     (get-conn uri)
     [{:db/id           warsaw
       :city/population 999999}]))

(find-population (warsaw-error :db-after) warsaw)

(def warsaw-1900
  @(d/transact
     (get-conn uri)
     [{:db/id           warsaw
       :city/population 686000}
      [:db/add (d/tempid :db.part/tx) :source/confidence 95 ]]))

(find-population (warsaw-1900 :db-after) warsaw)

; what keys are available for datomic.db.Datum
(
  ;:e
  ;:a
  ;:v
  :tx
  ;:added
  (first (warsaw-error :tx-data)))

(def error-tx-id
  (:tx (first (warsaw-error :tx-data))))

(d/pull
  (get-db uri)
  '[*]
  error-tx-id)

(def error-txes
  "Known bad transactions"
  #{error-tx-id})

(defn correct?
  [_ datom]
  (not (contains? error-txes (:tx datom))))

(def corrected (d/filter (warsaw-history) correct?))

(get-population-history corrected)

; filtering for security

(defn get-all-city-data [db city-ent-id]
  (d/pull
    db
    '[*]
    city-ent-id))

(get-all-city-data (get-db uri) warsaw)

(def city-hash-id (d/entid (get-db uri) :city/hashed-name))

(def city-hash-filter (fn [_ ^Datom datom] (not= city-hash-id (.a datom))))

(def filtered-db (d/filter (get-db uri) city-hash-filter))

(get-all-city-data filtered-db warsaw)

(defn city-filter
  [_ datom]
  (not= city-hash-id (:a datom)))

(get-all-city-data
  (d/filter (get-db uri) city-filter)
  warsaw)

; joining differnt filters on same db:
(d/q
  '[:find ?a ?ident
    :in $plain $filtered ?name
    :where
    [$plain ?e :city/name ?name]
    [$filtered ?e ?a]
    [$plain ?a :db/ident ?ident]]
  (get-db uri)
  filtered-db
  "Warszawa")


; filtering on transaction attributes:
; get source confidence marked for the latest transaction:

; won't include :source/confidence
(get-all-city-data (get-db uri) warsaw)

; will get :source/confidence
(d/pull
  (get-db uri)
  [:source/confidence]
  (:e (first (:tx-data warsaw-1900))))

(defn get-source-conf-by-tx-id [db tx-id]
  (d/pull
    db
    [:source/confidence]
    tx-id))

(defn confidence-filter
  [min-conf]
  (fn [db datom]
    (let [conf (:source/confidence (get-source-conf-by-tx-id db (:tx datom)))]
      (or (nil? conf) (> conf min-conf))

      ))
  #_(do
    (println (:source/confidence (get-source-conf-by-tx-id db (:tx datom))))
    true
    )
  )

(defn get-all-cities [db]
  (d/q
    '[:find ?name ?pop
      :where [?e :city/name ?name]
      [?e :city/population ?pop]]
    db))

(def krakow
  (ffirst
    (d/q
      '[:find ?e
        :where [?e :city/name "Krakow"]]
      (get-db uri))))

(def krakow-1791
  @(d/transact
     (get-conn uri)
     [{:db/id           krakow
       :city/population 23591}
      [:db/add (d/tempid :db.part/tx) :source/confidence 70 ]]))


(get-all-city-data
   (get-db uri)
  krakow)

(d/pull
  (get-db uri)
  [:source/confidence]
  (:e (first (:tx-data krakow-1791))))

; :source/confidence is 70 so we will get :city/population
(get-all-city-data
  (d/filter (get-db uri) (confidence-filter 60))
  krakow)

; :source/confidence is 70 so we will NOT get :city/population
(get-all-city-data
  (d/filter (get-db uri) (confidence-filter 80))
  krakow)

(get-all-cities (get-db uri))

; interesting we seem to skip the most recent 686000
; and get 999999 as it doesn't have :source/confidence set
; and passes filter
(get-all-city-data
  (d/filter (get-db uri) (confidence-filter 99))
  warsaw)

(get-all-city-data
  (d/filter (d/filter (get-db uri) (confidence-filter 99)) correct?)
  warsaw)

