(require '[datomic.api :as d])
(def uri "datomic:mem://test")
(d/create-database uri)
(def conn (d/connect uri))

(d/transact conn [;; Article
                  {:db/id #db/id [:db.part/db]
                   :db/ident :article/title
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/unique :db.unique/identity
                   :db/doc "This article's title"
                   :db.install/_attribute :db.part/db}
                  {:db/id #db/id [:db.part/db]
                   :db/ident :article/comments
                   :db/valueType :db.type/ref
                   :db/cardinality :db.cardinality/many
                   :db/isComponent true
                   :db/doc "This article's comments"
                   :db.install/_attribute :db.part/db}
                  ;; Comment
                  {:db/id #db/id [:db.part/db]
                   :db/ident :comment/body
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/doc "This comment's body"
                   :db.install/_attribute :db.part/db}])

(d/transact conn
            [;; A user writing an article
             {:db/id #db/id [:db.part/user -100]
              :article/title "Monads in Pictures"}
              {:db/id #db/id [:db.part/user]
               :comment/body "Great article!"
               :article/_comments #db/id [:db.part/user -100]}
              {:db/id #db/id [:db.part/user]
               :comment/body "I like the pictures"
               :article/_comments #db/id [:db.part/user -100]}])

;; this works as well:
#_(d/transact conn
            [;; A user writing an article
             {:db/id #db/id [:db.part/user -100]
              :article/title "Monads in Pictures"
              :article/comments
              [{ :db/id #db/id [:db.part/user] :comment/body  "first comment"}
               { :db/id #db/id [:db.part/user] :comment/body "second comment"}]}])

;; Find article's comments
(defn find-comments [db article-title]
  (d/q '[:find ?c
         :in $ ?a
         :where
         [?aid :article/title ?a]
         [?aid :article/comments ?cid]
         [?cid :comment/body ?c]]
       db article-title))

(defn get-eid [db attr-name attr-value]
  (ffirst (d/q '[:find ?e
                 :in $ ?an ?av
                 :where
                 [?e ?an ?av]]
               db attr-name attr-value)))

(def article-title "Monads in Pictures")

(find-comments (d/db conn) article-title)
;; > #{["I like the pictures"] ["Great article!"]}

#_(let [article-id (get-eid (d/db conn) :article/title article-title)]
  (d/transact conn [[:db.fn/retractEntity article-id]]))

;; NO comments are left after deleting the article because the
;; :article/comments attribute is defined as a containment
;; relationship i.e. db/isComponent true
(find-comments (d/db conn) article-title)
;; > #{}



(d/pull
  (d/db conn)
  '[*]
  [:article/title "Monads in Pictures"])
;; {:db/id 17592186045418, :article/title "Monads in Pictures",
;; :article/comments [{:db/id 17592186045419, :comment/body "Great article!"}
;;                    {:db/id 17592186045420, :comment/body "I like the pictures"}]}

;; {:db/id 277076930200557, :book/title "Lord Jim", :book/author {:db/id 281474976711659}, :book/genre {:db/id 17592186045429},
;; :book/publications [{:db/id 277076930200558, :publication/year "1900"}
;;                     {:db/id 277076930200559, :publication/year "1900"}
;;                     {:db/id 277076930200560, :publication/year "1901"}]}