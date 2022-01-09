(ns study.sino.sentence-patterns.tokens-regex
  (:require [clojure.string :as str]))

;; https://en.wikipedia.org/wiki/Chinese_punctuation
(def zi
  "Matches a token that does not contain any sentence-breaking punctuation."
  "[!{word:/[，！？；：。]/}]")

(def zi+
  (str "(" zi "+)"))

(defn tr
  "A helper function for writing TokensRegex patterns from `parts`."
  [& parts]
  (-> (str/join " " parts)
      (str/replace "'" "\"")))
