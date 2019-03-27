(ns cljs-macroexpand.core
  (:require [cljs.js :as cljs]))

(def fun-source "
(ns cljs-macroexpand.fun
  (:require [cljs.analyzer :as ana]))

(defn hello
  []
  \"hello\")

(defn expand
  [&env form]
  (let [expanded (ana/macroexpand-1 &env form)]
    (println expanded)
    expanded))
")

(def macro-source "
  (ns cljs-macroexpand.macro
    (:require [cljs-macroexpand.fun :as fun]))

  (defmacro test
    [form]
    (fun/expand &env form)
    :done)
")

(defn load-source
  [name]
  (case 'cljs-macroexpand.fun fun-source
        'cljs-macroexpand.macro macro-source))

(defn load
  "https://cljs.github.io/api/cljs.js/STARload-fnSTAR"
  [{:keys [name macros path] :as opts} cb]
  (print "Loading dependency: ")
  (prn opts)
  (cb {:lang :clj
       :source (load-source name)}))

(defn go
  []
  (binding [cljs/*eval-fn* cljs/js-eval
            cljs/*load-fn* load]
    (cljs/eval-str (cljs/empty-state)
                   #_
                   "(println (inc 0))"
                   "(ns cljs-macroexpand.test-1
                      (:require [cljs-macroexpand.fun :as fun]))
                    (println (hello))"
                   #_
                   "(ns cljs-macroexpand.test-2
                      (:require-macros [cljs-macroexpand.macro :as macro]))
                    (println (macro/test (let [x inc] (x 0))))"
                   identity)))

#_(go)
