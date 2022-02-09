(ns study.sino.sentence-patterns
  (:require [dk.simongray.datalinguist :as dl]
            [dk.simongray.datalinguist.tree :as tree]
            [dk.simongray.datalinguist.dependency :as dep]
            [dk.simongray.datalinguist.util :refer [configs]]
            [study.sino.sentence-patterns.tokens-regex :refer [tr zi+]])
  (:import [edu.stanford.nlp.semgraph.semgrex SemgrexPattern]
           [edu.stanford.nlp.trees.tregex TregexPattern]
           [edu.stanford.nlp.ling.tokensregex TokenSequencePattern]
           [java.util.regex Pattern]))

(def sem (comp dep/sem-pattern str))
(def tre (comp tree/tregex-pattern str))
(def tok (comp dl/token-pattern tr))

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
    :expression (sem "{value:看} >/compound:vc/ {tag:VV}=AA")
    :definition "to give ~ a try"
    :examples   ["你有什么好建议，先说说看。"
                 "这些菜都是我做的，不知道合不合你的口味，你吃吃看。"
                 "甲：这电视机你能修好吗？ 乙：现在还不知道，试试看吧。"]}
   "爱～不～"
   {:reference  [common-330]
    :pinyin     "ài ~ bù ~"
    :expression (tok "\"爱\" ( []+ ) \"不\" \\1")
    :definition "(it's up to smn) whether ~ or not"
    :note       "expresses dissatisfaction with the other person"
    :examples   ["道理我都讲清楚了，你爱听不听。"
                 "他爱生气不生气，反正我有意见就得提。"
                 "你爱复习不复习，不过考不好可不要怪别人。"
                 "你爱说不说，以后你想说我还不想听了呢。"]}

   "爱～就～"
   {:reference  [common-330]
    :pinyin     "ài ~ jiù ~"
    :expression (tok "\"爱\" ( []+ ) \"就\" \\1")
    :definition "(do) ~ as one wishes"
    :note       "occasionally expressing slight dissatisfaction"
    :examples   ["一个人生活可自由啦，爱干什么就干什么。"
                 "放假了，我爱几点起就几点起,太舒服了。"
                 "我这几天就在家，你爱哪天来就哪天来吧。"
                 "父母都出差了，孩子在家爱打扑克就打扑克，爱看电视就看电视，没人管。"]}

   "把A～成/做B"
   {:reference  [common-330]
    :pinyin     "bǎ A ~ chéng/zuò B"
    :expression (tok "'把'" zi+ "([{tag:VV;word:/.+(成|做)/}]+)" zi+)
    :definition "to ~ A as (or into) B"
    :note       "成/做 is always prepended by ~ (a verb indicating manner)"
    :examples   ["看来政府要把社会医疗保险当成大量来抓。"
                 "中国人把长城看做中国的象征。"
                 "我说怎么听不懂这个句子呢，原来我把“中国文字”听成“中国蚊子”了。"
                 "做这个菜要费些工夫，首先得把所有的用料都切成细丝儿。"]}

   #_.})

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

(defn matches
  "Return all matches of pattern `p` found in `s`."
  [p s]
  (condp instance? p
    TokenSequencePattern
    (not-empty (dl/token-seq p (tokens s)))

    SemgrexPattern
    (not-empty (mapcat (partial dep/sem-seq p) (dependency-graphs s)))

    TregexPattern
    (not-empty (mapcat (partial tree/tregex-seq p) (constituency-trees s)))

    ;; TODO: return token results instead of strings
    Pattern
    (re-seq p s)))

(defn find-patterns
  "Return a mapping from pattern name to pattern instances found in `s`."
  [s]
  (->> (for [[k v] patterns]
         (let [p (:expression v)]
           (when-let [res (matches p s)]
             {k res})))
       (apply merge-with into)))

(comment
  (require '[study.sino.pinyin :refer [digits->diacritics]])
  (digits->diacritics "ai4 ~ jiu4 ~")

  (tokens "一个人生活可自由啦，爱干什么就干什么。")
  (matches (tok "\"爱\" ([]+) \"就\" \\1")
           "一个人生活可自由啦，爱干什么就干什么。")
  (constituency-trees "一个人生活可自由啦，爱干什么就干什么。")

  (matches (sem "{value:看} >/compound:vc/ {tag:VV}=AA")
           "甲：这电视机你能修好吗？ 乙：现在还不知道，试试看吧。")
  (dependency-graphs "一个人生活可自由啦，爱干什么就干什么。"
                     "放假了，我爱几点起就几点起,太舒服了。"
                     "我这几天就在家，你爱哪天来就哪天来吧。"
                     "父母都出差了，孩子在家爱打扑克就打扑克，爱看电视就看电视，没人管。")
  (find-patterns "看来政府要把社会医疗保险当成大量来抓。")
  #_.)
