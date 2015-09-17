(require '[datomic.api :as d])
(def uri "datomic:mem://test")
(d/create-database uri)
(def conn (d/connect uri))

(defn get-conn [uri]
  (d/connect uri))

(defn get-db [uri]
  (d/db (get-conn uri)))

(defn read-file [s]
  (read-string (slurp s)))

(def schema
  (read-file "resources/db/pull/schema.edn"))
(def seed-data
  (read-file "resources/db/pull/data.edn"))

; :db.part/db
(d/pull
  (d/db conn)
  '[*]
  0)

; :db.install/attribute
(d/pull
  (d/db conn)
  '[*]
  13)

; :book/genre
(d/pull
  (d/db conn)
  '[*]
  67)

; tx
(d/pull
  (d/db conn)
  '[*]
  13194139534312)

; :db/ident
(d/pull
  (d/db conn)
  '[*]
  10)

; :db/valueType
(d/pull
  (d/db conn)
  '[*]
  40  )

; :db.type/ref
(d/pull
  (d/db conn)
  '[*]
  20  )

; :db/cardinality
(d/pull
  (d/db conn)
  '[*]
  41  )

; :db.cardinality/one
(d/pull
  (d/db conn)
  '[*]
  35  )

; :db/doc
(d/pull
  (d/db conn)
  '[*]
  62  )

; E A V Tx added

; info about :book/genre
(d/pull
  (d/db conn)
  '[*]
   :book/genre)



@(d/transact conn schema)

{:db-before datomic.db.Db@bc953f62,
 :db-after datomic.db.Db@6fb24e5c,
 :tx-data [#datom[13194139534312 50 #inst "2014-12-28T06:10:55.005-00:00" 13194139534312 true]
           #datom[63 10 :library.books 13194139534312 true]
           #datom[63 62 "The partition of DB for books" 13194139534312 true]
           #datom[0 11 63 13194139534312 true]
           #datom[64 10 :users 13194139534312 true]
           #datom[64 62 "The partition of DB for users" 13194139534312 true]
           #datom[0 11 64 13194139534312 true]
           #datom[65 10 :book/title 13194139534312 true]
           #datom[65 40 23 13194139534312 true]
           #datom[65 41 35 13194139534312 true]
           #datom[65 42 38 13194139534312 true]
           #datom[65 62 "Book's title" 13194139534312 true]
           #datom[0 13 65 13194139534312 true]
           #datom[66 10 :book/author 13194139534312 true]
           #datom[66 40 20 13194139534312 true]
           #datom[66 41 35 13194139534312 true]
           #datom[66 62 "Books author" 13194139534312 true]
           #datom[0 13 66 13194139534312 true]

           #datom[67 10 :book/genre 13194139534312 true]
           #datom[67 40 20 13194139534312 true]
           #datom[67 41 35 13194139534312 true]
           #datom[67 62 "Books's genre" 13194139534312 true]
           #datom[0 13 67 13194139534312 true]

           #datom[68 10 :book/publications 13194139534312 true]
           #datom[68 43 true 13194139534312 true]
           #datom[68 40 20 13194139534312 true]
           #datom[68 41 36 13194139534312 true]
           #datom[0 13 68 13194139534312 true]
           #datom[69 10 :person/name 13194139534312 true]
           #datom[69 40 23 13194139534312 true]
           #datom[69 41 35 13194139534312 true]
           #datom[69 42 38 13194139534312 true]
           #datom[69 51 true 13194139534312 true]
           #datom[69 62 "Person's name" 13194139534312 true]
           #datom[0 13 69 13194139534312 true]
           #datom[70 10 :person/place-of-birth 13194139534312 true]
           #datom[70 40 23 13194139534312 true]
           #datom[70 41 35 13194139534312 true]
           #datom[70 62 "Person's place of birth" 13194139534312 true]
           #datom[0 13 70 13194139534312 true]
           #datom[71 10 :person/email 13194139534312 true]
           #datom[71 40 23 13194139534312 true]
           #datom[71 41 36 13194139534312 true]
           #datom[71 62 "Person's email address" 13194139534312 true]
           #datom[0 13 71 13194139534312 true]
           #datom[72 10 :person/influenced-by 13194139534312 true]
           #datom[72 40 20 13194139534312 true]
           #datom[72 41 35 13194139534312 true]
           #datom[0 13 72 13194139534312 true]
           #datom[73 10 :city/name 13194139534312 true]
           #datom[73 40 23 13194139534312 true]
           #datom[73 51 true 13194139534312 true]
           #datom[73 41 35 13194139534312 true]
           #datom[0 13 73 13194139534312 true]
           #datom[74 10 :country/name 13194139534312 true]
           #datom[74 40 23 13194139534312 true]
           #datom[74 51 true 13194139534312 true]
           #datom[74 41 35 13194139534312 true]
           #datom[0 13 74 13194139534312 true]
           #datom[75 10 :country/city 13194139534312 true]
           #datom[75 40 20 13194139534312 true]
           #datom[75 41 36 13194139534312 true]
           #datom[0 13 75 13194139534312 true]
           #datom[76 10 :publication/city 13194139534312 true]
           #datom[76 40 20 13194139534312 true]
           #datom[76 41 35 13194139534312 true]
           #datom[0 13 76 13194139534312 true]
           #datom[77 10 :publication/year 13194139534312 true]
           #datom[77 40 23 13194139534312 true]
           #datom[77 41 35 13194139534312 true]
           #datom[0 13 77 13194139534312 true]],
 :tempids {-9223367638809264713 72, -9223367638809264706 65, -9223367638809264705 64, -9223367638809264712 71, -9223367638809264708 67, -9223367638809264707 66, -9223367638809264711 70, -9223367638809264704 63, -9223367638809264709 68, -9223367638809264715 74, -9223367638809264710 69, -9223367638809264717 76, -9223367638809264714 73, -9223367638809264718 77, -9223367638809264716 75}}


(d/pull
  (d/db conn)
  '[*]
  :book.genre/novel)



@(d/transact conn seed-data)

{:db-before datomic.db.Db@6fb24e5c,
 :db-after datomic.db.Db@9351bdfa,
 :tx-data [#datom[13194139534313 50 #inst "2014-12-28T06:39:09.001-00:00" 13194139534313 true]
           #datom[281474976711658 69 "George Orwell" 13194139534313 true]
           #datom[281474976711659 69 "Joseph Conrad" 13194139534313 true]
           #datom[281474976711660 69 "Adam Mickiewicz" 13194139534313 true]
           #datom[277076930200557 65 "Animal Farm" 13194139534313 true]
           #datom[277076930200557 66 281474976711658 13194139534313 true]
           #datom[277076930200557 67 17592186045447 13194139534313 true]
           #datom[17592186045422 65 "Lord Jim" 13194139534313 true]
           #datom[17592186045422 66 281474976711659 13194139534313 true]

           #datom[17592186045422 67 17592186045448 13194139534313 true]

           #datom[17592186045422 68 17592186045423 13194139534313 true]
           #datom[17592186045423 76 17592186045428 13194139534313 true]
           #datom[17592186045423 77 "1900" 13194139534313 true]
           #datom[17592186045422 68 17592186045424 13194139534313 true]
           #datom[17592186045424 76 17592186045429 13194139534313 true]
           #datom[17592186045424 77 "1900" 13194139534313 true]
           #datom[17592186045422 68 17592186045425 13194139534313 true]
           #datom[17592186045425 76 17592186045430 13194139534313 true]
           #datom[17592186045425 77 "1901" 13194139534313 true]
           #datom[17592186045426 65 "The Heart of Darkness" 13194139534313 true]
           #datom[17592186045426 66 281474976711659 13194139534313 true]

           #datom[17592186045426 67 17592186045448 13194139534313 true]

           #datom[277076930200563 65 "1984" 13194139534313 true]
           #datom[277076930200563 66 281474976711658 13194139534313 true]

           #datom[277076930200563 67 17592186045447 13194139534313 true]

           #datom[17592186045428 73 "Tokyo" 13194139534313 true]
           #datom[17592186045429 73 "Osaka" 13194139534313 true]
           #datom[17592186045430 73 "London" 13194139534313 true]
           #datom[17592186045431 73 "Warszawa" 13194139534313 true]
           #datom[17592186045432 73 "Krakow" 13194139534313 true]
           #datom[17592186045433 73 "Gdansk" 13194139534313 true]
           #datom[17592186045434 73 "Poznan" 13194139534313 true]
           #datom[17592186045435 73 "Wroclaw" 13194139534313 true]
           #datom[17592186045436 73 "Lodz" 13194139534313 true]
           #datom[17592186045437 73 "Szczecin" 13194139534313 true]
           #datom[17592186045438 74 "Poland" 13194139534313 true]
           #datom[17592186045438 75 17592186045431 13194139534313 true]
           #datom[17592186045438 75 17592186045432 13194139534313 true]
           #datom[17592186045438 75 17592186045433 13194139534313 true]
           #datom[17592186045438 75 17592186045434 13194139534313 true]
           #datom[17592186045438 75 17592186045435 13194139534313 true]
           #datom[17592186045438 75 17592186045436 13194139534313 true]
           #datom[17592186045438 75 17592186045437 13194139534313 true]
           #datom[17592186045439 69 "Geoffrey Chaucer" 13194139534313 true]
           #datom[17592186045440 69 "William Shakespeare" 13194139534313 true]
           #datom[17592186045440 72 17592186045439 13194139534313 true]
           #datom[17592186045441 69 "Alexander Pope" 13194139534313 true]
           #datom[17592186045441 72 17592186045440 13194139534313 true]
           #datom[17592186045442 69 "William Wordsworth" 13194139534313 true]
           #datom[17592186045442 72 17592186045441 13194139534313 true]
           #datom[17592186045443 69 "Charles Dickens" 13194139534313 true]
           #datom[17592186045443 72 17592186045442 13194139534313 true]
           #datom[17592186045444 69 "Thomas Hardy" 13194139534313 true]
           #datom[17592186045444 72 17592186045443 13194139534313 true]
           #datom[17592186045445 69 "J.R.R. Tolkien" 13194139534313 true]
           #datom[17592186045445 72 17592186045444 13194139534313 true]
           #datom[281474976711658 72 17592186045445 13194139534313 true]
           #datom[17592186045446 69 "Anthony Burgess" 13194139534313 true]
           #datom[17592186045446 72 281474976711658 13194139534313 true]
           #datom[17592186045447 10 :book.genre/fiction 13194139534313 true]
           #datom[17592186045448 10 :book.genre/novel 13194139534313 true]],

 :tempids {-9223350046622220689 17592186045429, -9223086163831554149 281474976711659, -9223086163831554148 281474976711658, -9223350046622220789 17592186045448, -9223350046622220697 17592186045437, -9223350046622220593 17592186045444, -9223350046622220690 17592186045430, -9223354444667731321 17592186045424, -9223090561878065352 277076930200557, -9223350046622220688 17592186045428, -9223350046622220489 17592186045422, -9223350046622220695 17592186045435, -9223350046622220595 281474976711658, -9223350046622220490 17592186045426, -9223090561878065354 277076930200563, -9223350046622220692 17592186045432, -9223350046622220590 17592186045441, -9223350046622220788 17592186045447, -9223350046622220694 17592186045434, -9223354444667731322 17592186045423, -9223350046622220696 17592186045436, -9223350046622220592 17592186045443, -9223086163831554150 281474976711660, -9223350046622220691 17592186045431, -9223350046622220591 17592186045442, -9223350046622220693 17592186045433, -9223350046622220594 17592186045445, -9223350046622220596 17592186045446, -9223350046622220588 17592186045439, -9223350046622220589 17592186045440, -9223350046623220331 17592186045438, -9223354444667731320 17592186045425}}

; works
(d/q
  '[:find ?title
    :in $ ?b ?val
    :where [?b :book/genre ?val]
    [?b :book/title ?title]]
  ;(get-db uri)
  (get-db uri)
  17592186045426 :book.genre/novel  )

(d/q
  '[:find ?title
    :in $ ?b ?attr ?val
    :where [?b ?attr]
    [?attr :db/id ?val]
    [?b :book/title ?title]]
  ;(get-db uri)
  (get-db uri)
  17592186045426 :book/genre :book.genre/novel  )

