(ns study.sino.sentence-patterns
  (:require [dk.simongray.datalinguist :as dl]
            [dk.simongray.datalinguist.dependency :as dd]
            [dk.simongray.datalinguist.util :refer [configs]]))

(def spoken-chinese
  {:title     "Exemplification of Common Sentence Patterns in Spoken Chinese"
   :year      2004
   :publisher "Peking University Press"
   :isbn      "978-7-301-07540-1"})

(def common-330
  {:title     "Common Chinese Patterns 330"
   :year      2010
   :publisher "Sinolingua"
   :isbn      "978-7-80200-647-8"})

(def patterns
  {"AA看"
   {:reference  [spoken-chinese]
    :pinyin     "AA kàn"
    :semgrex    "{value:看} >/compound:vc/ {tag:VV}=AA"
    :names      #{:AA}
    :definition "to give ~ a try"
    :examples   ["你有什么好建议，先说说看。"
                 "这些菜都是我做的，不知道合不合你的口味，你吃吃看。"
                 "甲：这电视机你能修好吗？ 乙：现在还不知道，试试看吧。"]}
   "爱~不~"
   {:reference  [common-330]
    :pinyin     "ài A bù A"
    :semgrex    nil                                         ; TODO
    :names      #{:A}
    :definition "(it's up to smn) whether ~ or not"
    :note       "expresses dissatisfaction with the other person"
    :examples   ["道理我都讲清楚了，你爱听不听。"
                 "他爱生气不生气，反正我有意见就得提。"
                 "你爱复习不复习，不过考不好可不要怪别人。"
                 "你爱说不说，以后你想说我还不想听了呢。"]}})

(defonce nlp
  (future
    (dl/->pipeline (:chinese configs))))

(defn dependency-graphs
  "Return all dependency graphs found in `sentences`."
  [& sentences]
  (let [dg (comp dl/dependency-graph @nlp)]
    (mapcat dg sentences)))

(defn semgrex-matches
  "Return all matches of `semgrex` pattern found in `s`."
  [semgrex s]
  (let [p (dd/sem-pattern semgrex)]
    (not-empty (mapcat (partial dd/sem-seq p) (dependency-graphs s)))))

(defn find-patterns
  "Return a mapping from pattern name to pattern instances found in `s`."
  [s]
  (->> (for [[k {:keys [semgrex]}] patterns]
         (when semgrex
           (when-let [matches (semgrex-matches semgrex s)]
             {k matches})))
       (apply merge-with into)))

(comment
  (require '[study.sino.pinyin :refer [digits->diacritics]])
  (digits->diacritics "ai4 A bu4 A")

  (semgrex-matches "{value:看} >/compound:vc/ {tag:VV}=AA"
                   "甲：这电视机你能修好吗？ 乙：现在还不知道，试试看吧。")
  (dependency-graphs "道理我都讲清楚了，你爱听不听。"
                     "他爱生气不生气，反正我有意见就得提。"
                     "你爱复习不复习，不过考不好可不要怪别人。"
                     "你爱说不说，以后你想说我还不想听了呢。")

  (find-patterns "这些菜都是我做的，不知道合不合你的口味，你吃吃看。")
  #_.)
