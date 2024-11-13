(ns app
  (:require ["@hono/node-server" :as node-server]
            ["@hono/node-server/serve-static" :refer [serveStatic]]
            ["hono/etag" :refer [etag]]
            ["hono/logger" :refer [logger]]
            ["hono/powered-by" :refer [poweredBy]]
            ["hono/pretty-json" :refer [prettyJSON]]            
            ["hono" :as hono]))

(def book
  (hono/Hono.))

(doto book
  (.get
   "/" (^async fn [c] (.text c "List books")))
  (.get
   "/:id" (^async fn [c] (.text c (str "Get book: " (.req.param c :id)))))
  (.post
   "/" (^async fn [c] (.text c "Create book"))))

(def app
  (hono/Hono. {:port 3300}))

(defn- ^:async not-found [c]
  (.text c "Sorry, not found" 404))

(defn- ^:async error-handler [_err c]
  (.text c "Custom error message" 500))

(defn- ^:async hello [c]
  (.text c "Hello Hono!"))

(doto app
  (.use (prettyJSON {:space 4}))
  (.use "*" (logger))
  (.use "*" (poweredBy))
  (.use "/static/*" (etag))
  (.use "/static/*" (serveStatic {:path "static"}))
  (.notFound not-found)
  (.onError error-handler)
  (.get "/auth/page" (fn [c] (.text c "This is behind closed door")))
  (.get "/" hello)
  (.get "/bye" (^async fn [c] (.json c {:message "Bye!" :mood "positive"})))
  (.route "/book" book))

(node-server/serve
 app (fn [info]
       (js/console.log (str "Server is running on http://localhost:" info.port))))
