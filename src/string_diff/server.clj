(ns string-diff.server
  (:require [string-diff.diffs :refer [unique-and-frequent diffs]]
            [bidi.ring :refer (make-handler)]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [liberator.core :refer [defresource]]
            [liberator.representation :refer [as-response]]
            [ring.adapter.jetty :refer (run-jetty)]))

(declare mix)

(defresource mix
  {:allowed-methods [:post]
   :available-media-types ["application/json"]
   :malformed? (fn [ctx]
                 (let [strings (get-in ctx [:request :body])]
                   (some (complement string?) strings)))
   :post! (fn [ctx]
            (let [strings (get-in ctx [:request :body])]
              (assoc ctx :diffs (-> (map unique-and-frequent strings) diffs))))
   ;; doesn't mean we are creating a resource,
   ;; in this context it means handle 201 response (a correct post)
   :handle-created (fn [ctx]
                     ;;convert characters in diffs to strings
                     ;;to generate a JSON response without problems
                     {:diffs (map (fn [[k c v]] [k (str c) v]) (:diffs ctx))})})

(def routes
  ["/" {"mix" mix}])

(def app
  (-> routes
      make-handler
      wrap-json-body
      wrap-json-response))

(defn -main [& args]
  (run-jetty #'app {:port 3000 :join? false}))
