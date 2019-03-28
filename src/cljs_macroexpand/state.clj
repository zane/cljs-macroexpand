(ns cljs-macroexpand.state
  (:require [cljs.analyzer :as ana]
            [cljs.env :as env]))

(defmacro analyzer-state [[_ ns-sym]]
  `'~(get-in @env/*compiler* [:ana/namespaces ns-sym]))

(defmacro compiler-namespaces []
  `(quote ~(get-in @env/*compiler* [::ana/namespaces])))
