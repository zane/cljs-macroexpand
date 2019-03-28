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
    (ana/macroexpand-1 env form))
")

(def macro-source "
  (ns cljs-macroexpand.macro
    (:require [cljs-macroexpand.fun :as fun]))

  (defmacro our-expand
    [form]
    (pr-str (fun/expand &env form)))
")

(def state (cljs.js/empty-state))

(defn load-library-analysis-cache! []
  (cljs/load-analysis-cache! state 'cljs.analyzer (state/analyzer-state 'cljs.analyzer))
  nil)

(defn set-loaded!
  [ns]
  (swap! cljs/*loaded* conj ns))

(defn deps
  [sym]
  (condp = sym
    'cljs-macroexpand.value       value-source
    'cljs-macroexpand.value-macro value-macro-source
    'cljs-macroexpand.fun         fun-source
    'cljs-macroexpand.macro       macro-source))

(def test-inc "(inc 0)")
(def test-value "(ns cljs-macroexpand.test-value
                   (:require [cljs-macroexpand.value :as value]))
                 (inc value/my-value)")
(def test-value-macro "(ns cljs-macroexpand.test-value-macro
                         (:require-macros [cljs-macroexpand.value-macro :as value]))
                       (println (value/value-macro))")
(def test-macro "(ns cljs-macroexpand.test-macro
                   (:require-macros [cljs-macroexpand.macro :as macro]))
                 (println (macro/our-expand (let [x inc] (x 0))))")

(defn load
  "https://cljs.github.io/api/cljs.js/STARload-fnSTAR"
  [{:keys [name macros path] :as opts} cb]
  (print "Loading dependency: ")
  (prn opts)
  (cb {:lang :clj, :source (deps name)}))


#_(load-library-analysis-cache!)
#_(set-loaded! 'cljs.analyzer)

(defn eval-str
  [s]

  (binding [cljs/*eval-fn* cljs/js-eval
            cljs/*load-fn* load]
    (cljs/eval-str #_(cljs/empty-state)
                   state
                   s
                   identity)))

(defn -main
  [& _]
  (set-loaded! 'cljs.analyzer)
  (eval-str test-macro))
