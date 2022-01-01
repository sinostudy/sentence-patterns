(ns study.sino.test-sentence-patterns
  (:require [clojure.test :refer :all]
            [study.sino.sentence-patterns :refer [patterns
                                                  semgrex-matches]]))

(deftest test-patterns
  (doseq [[k {:keys [semgrex names examples]}] patterns]
    (if semgrex
      (let [matches (map (partial semgrex-matches semgrex) examples)]
        (is (= (count matches) (count examples))
            (str "Pattern '" k "' should match every example."))
        (is (every? (partial = names) (->> (apply concat matches)
                                           (map (comp set keys second))))
            (str "Matches for '" k "' should contain the required names.")))
      (println (str "No semgrex provided for '" k "' - skipping test.")))))
