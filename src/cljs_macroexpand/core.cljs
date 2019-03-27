(ns cljs-macroexpand.core
  (:require-macros [cljs-macroexpand.state :as state])
  (:require [cljs.analyzer]
            [cljs.js :as cljs]))

(def value-source "
  (ns cljs-macroexpand.value)
  (def my-value 99)
")

(def value-macro-source "
  (ns cljs-macroexpand.value-macro)
  (defmacro value-macro
    [form]
    88)
")

(def fun-source "
  (ns cljs-macroexpand.fun
    (:require [cljs.analyzer :as ana]))

  (defn expand
    [env form]
    (println \"in function\")
    (let [expanded (ana/macroexpand-1 env form)]
      (println expanded)
      expanded))
")

(def macro-source "
  (ns cljs-macroexpand.macro
    (:require [cljs-macroexpand.fun :as fun]))

  (defmacro our-expand
    [form]
    (println \"in macro\")
    (fun/expand &env form))
")

(def state (cljs.js/empty-state))

(defn load-library-analysis-cache! []
  (cljs/load-analysis-cache! state 'cljs.analyzer (state/analyzer-state 'cljs.analyzer))
  nil)

(load-library-analysis-cache!)

(defn deps
  [sym]
  (condp = sym
    'cljs-macroexpand.value       value-source
    'cljs-macroexpand.value-macro value-macro-source
    'cljs-macroexpand.fun         fun-source
    'cljs-macroexpand.macro       macro-source))

(defn load
  "https://cljs.github.io/api/cljs.js/STARload-fnSTAR"
  [{:keys [name macros path] :as opts} cb]
  (print "Loading dependency: ")
  (prn opts)
  (cb {:lang :clj, :source (deps name)}))

(defn go
  []
  (binding [cljs/*eval-fn* cljs/js-eval
            cljs/*load-fn* load]
    (cljs/eval-str #_(cljs/empty-state)
                   state
                   #_
                   "(inc 0)"
                   #_
                   "(ns cljs-macroexpand.test-0
                      (:require [cljs-macroexpand.value :as value]))
                    (inc value/my-value)"
                   #_
                   "(ns cljs-macroexpand.test-1
                      (:require-macros [cljs-macroexpand.value-macro :as value]))
                    (println (value/value-macro))"
                   "(ns cljs-macroexpand.test-2
                      (:require-macros [cljs-macroexpand.macro :as macro]))"
                   println)))

#_(go)
