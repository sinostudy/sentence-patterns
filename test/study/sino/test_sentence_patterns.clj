(ns study.sino.test-sentence-patterns
  (:require [clojure.test :refer :all]
            [study.sino.sentence-patterns :refer [patterns
                                                  matches]]))

(defn test-pattern
  [k examples matches-fn expression]
  (let [matches (remove nil? (map (partial matches-fn expression) examples))]
    (is (= (count matches) (count examples))
        (str "'" k "' should match every example with: " expression ""))))

(deftest test-patterns
  (doseq [[k {:keys [expression examples]}] patterns]
    (if-let [p expression]
      (test-pattern k examples matches p)
      (println (str "Skipped '" k "' (no pattern expression to test).")))))
