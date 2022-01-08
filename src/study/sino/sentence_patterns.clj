(ns study.sino.sentence-patterns
  (:require [dk.simongray.datalinguist :as dl]
            [dk.simongray.datalinguist.tree :as tree]
            [dk.simongray.datalinguist.dependency :as dep]
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
    :expression {:semgrex "{value:看} >/compound:vc/ {tag:VV}=AA"}
    :definition "to give ~ a try"
    :examples   ["你有什么好建议，先说说看。"
                 "这些菜都是我做的，不知道合不合你的口味，你吃吃看。"
                 "甲：这电视机你能修好吗？ 乙：现在还不知道，试试看吧。"]}
   "爱~不~"
   {:reference  [common-330]
    :pinyin     "ài ~ bù ~"
    :expression {:tokens "\"爱\" ( []+ ) \"不\" \\1"}
    :definition "(it's up to smn) whether ~ or not"
    :note       "expresses dissatisfaction with the other person"
    :examples   ["道理我都讲清楚了，你爱听不听。"
                 "他爱生气不生气，反正我有意见就得提。"
                 "你爱复习不复习，不过考不好可不要怪别人。"
                 "你爱说不说，以后你想说我还不想听了呢。"]}

   "爱~就~"
   {:reference  [common-330]
    :pinyin     "ài ~ jiù ~"
    :expression {:tokens "\"爱\" ( []+ ) \"就\" \\1"}
    :definition "(do) ~ as one wishes"
    :note       "occasionally expressing slight dissatisfaction"
    :examples   ["一个人生活可自由啦，爱干什么就干什么。"
                 "放假了，我爱几点起就几点起,太舒服了。"
                 "我这几天就在家，你爱哪天来就哪天来吧。"
                 "父母都出差了，孩子在家爱打扑克就打扑克，爱看电视就看电视，没人管。"]}})

(defonce nlp
  (future
    (dl/->pipeline (:chinese configs))))

(defn dependency-graphs
  "Return all dependency graphs found in `sentences`."
  [& sentences]
  (let [dg (comp dl/dependency-graph @nlp)]
    (mapcat dg sentences)))

(defn constituency-trees
  "Return all constituency trees found in `sentences`."
  [& sentences]
  (let [ct (comp dl/constituency-tree @nlp)]
    (mapcat ct sentences)))

(defn tokens
  "Return all tokens found in `sentences`."
  [& sentences]
  (mapcat (comp dl/tokens @nlp) sentences))

(defn semgrex-matches
  "Return all matches of `semgrex` pattern found in `s`."
  [semgrex s]
  (when semgrex
    (let [p (dep/sem-pattern semgrex)]
      (not-empty (mapcat (partial dep/sem-seq p) (dependency-graphs s))))))

(defn tregex-matches
  "Return all matches of `tregex` pattern found in `s`."
  [tregex s]
  (when tregex
    (let [p (tree/tregex-pattern tregex)]
      (not-empty (mapcat (partial tree/tregex-seq p) (constituency-trees s))))))

(defn token-matches
  "Return all matches of `tokens-regex` pattern found in `s`."
  [tokens-regex s]
  (when tokens-regex
    (let [p (dl/token-pattern tokens-regex)]
      (not-empty (dl/token-seq p (tokens s))))))

(defn find-patterns
  "Return a mapping from pattern name to pattern instances found in `s`."
  [s]
  (->> (for [[k {:keys [expression]}] patterns]
         (when-let [{:keys [semgrex tregex tokens]} expression]
           (when-let [matches (or (semgrex-matches semgrex s)
                                  (tregex-matches tregex s)
                                  (token-matches tokens s))]
             {k matches})))
       (apply merge-with into)))

(comment
  (require '[study.sino.pinyin :refer [digits->diacritics]])
  (digits->diacritics "ai4 ~ jiu4 ~")

  (tokens "一个人生活可自由啦，爱干什么就干什么。")
  (token-matches "\"爱\" ([]+) \"就\" \\1"
                 "一个人生活可自由啦，爱干什么就干什么。")
  (constituency-trees "一个人生活可自由啦，爱干什么就干什么。")

  (semgrex-matches "{value:看} >/compound:vc/ {tag:VV}=AA"
                   "甲：这电视机你能修好吗？ 乙：现在还不知道，试试看吧。")
  (dependency-graphs "一个人生活可自由啦，爱干什么就干什么。"
                     "放假了，我爱几点起就几点起,太舒服了。"
                     "我这几天就在家，你爱哪天来就哪天来吧。"
                     "父母都出差了，孩子在家爱打扑克就打扑克，爱看电视就看电视，没人管。")
  (semgrex-matches "{value:看} >/compound:vc/ {tag:VV}=AA" "一个人生活可自由啦，爱干什么就干什么。")
  (find-patterns "一个人生活可自由啦，爱干什么就干什么。")
  #_.)
