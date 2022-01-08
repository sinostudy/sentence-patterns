(ns study.sino.test-sentence-patterns
  (:require [clojure.test :refer :all]
            [study.sino.sentence-patterns :refer [patterns
                                                  token-matches
                                                  tregex-matches
                                                  semgrex-matches]]))

(defn test-pattern
  [k examples matches-fn expression]
  (let [matches (remove nil? (map (partial matches-fn expression) examples))]
    (is (= (count matches) (count examples))
        (str "'" k "' should match every example with: " expression ""))))

(deftest test-patterns
  (doseq [[k {:keys [expression examples]}] patterns]
    (let [{:keys [semgrex tregex tokens]} expression]
      (if (or semgrex tregex tokens)
        (do
          (when semgrex (test-pattern k examples semgrex-matches semgrex))
          (when tregex (test-pattern k examples tregex-matches tregex))
          (when tokens (test-pattern k examples token-matches tokens)))
        (println (str "Skipped '" k "' (no expressions to test)."))))))
