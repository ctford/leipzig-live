(ns leipzig-live.eval
  (:require
    [cljs.js :as cljs]
    [leipzig-live.music :as music]))

(defn build-namespace [expr-str]
  (str
    "(ns leipzig-live.playing)"
    music/leipzig
    expr-str))

(defn uate
  [expr-str]
  (cljs/eval-str
    (cljs/empty-state)
    (build-namespace expr-str)
    nil
    {:eval cljs/js-eval}
    identity))
