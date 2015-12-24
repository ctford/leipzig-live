(ns leipzig-live.processing
  (:require
    [leipzig-live.music :as music]
    [leipzig-live.instruments :as instrument]
    [leipzig-live.actions :as action]
    [leipzig-live.framework :as framework]
    [cljs.js :as cljs]))

(defn add-namespace [expr-str]
  (str
    "(ns leipzig-live.playing
      (:require [leipzig-live.music :as music]))"
    expr-str))

(defn evaluate
  [expr-str]
  (cljs/eval-str
    (cljs/empty-state)
    (add-namespace expr-str)
    nil
    {:eval cljs/js-eval
     :load (fn [_ cb] (cb {:lang :clj :source ""}))}
    #(:value %)))

(extend-protocol framework/Action
  action/Refresh
  (process [{expr-str :text} _ {original-music :music :as state}]
    (let [new-music (evaluate expr-str)
          music (or new-music original-music)]
      (-> state
          (assoc :compiling? (not (not new-music)))
          (assoc :text expr-str)
          (assoc :music music))))

  action/Stop
  (process [_ handle! state]
    (assoc state :looping? false))

  action/Play
  (process [this handle! state]
    (framework/process (action/->Loop) handle! (assoc state :looping? true)))

  action/Loop
  (process [this handle! {notes :music :as state}]
    (when (:looping? state)
      (music/play-on! instrument/beep! notes)
      (let [duration (->> notes (map :duration) (reduce +) (* 1000))]
        (js/setTimeout #(handle! this) duration)))
    state))