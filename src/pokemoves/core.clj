(ns pokemoves.core
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [instaparse.core :as ip]))

(def grammar
  "defs = def (<sep> def)* <sep*>
  def = move <sep> pokemons*
  move = <'['> #'[A-Z]+' <']'>
  pokemons = pokemon (<sep>+ pokemon)*
  pokemon = #'[A-Z0-9a-z]+'
  sep = ','+ | #'\r\n'+")

(def parse*
  (ip/parser grammar))

(defmulti -parse first)

(defmethod -parse :default
  [x]
  x)

(defmethod -parse :pokemon
  [[_ nme]]
  (-> nme s/lower-case keyword))

(defmethod -parse :pokemons
  [[_ & pokes]]
  (mapv -parse pokes))

(defmethod -parse :move
  [[_ nme]]
  (-> nme s/lower-case keyword))

(defmethod -parse :def
  [[_ m pokes]]
  [(-parse m) (-parse pokes)])

(defmethod -parse :defs
  [[_ & defs]]
  (map -parse defs))

(defn usage
  []
  (println "lein convert <FILE NAME>")
  (System/exit 1))

(defn -main
  [& args]
  (let [[file-name] args]
    (if-let [s (some-> file-name io/resource slurp parse*)]
      (do
        (->> s
             -parse
             (into {})
             pr-str
             println)
        (System/exit 0))
      (usage))))
