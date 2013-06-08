(ns wigwam-clj.request
  (:refer-clojure :exclude [defn]))

(alias 'core 'clojure.core)

(def ^:dynamic *request* (atom nil))
(def ^:dynamic *session* (atom nil))

(defmacro when-http
  [forms]
  `(try
     (if @wigwam-clj.request/*request* (and ~@forms) true)
     (finally
       (prn ["session" @wigwam-clj.request/*request*])
       (reset! wigwam-clj.request/*request* nil))))

(core/defn make-asserts [forms]
  (when forms `[(assert (wigwam-clj.request/when-http ~forms))]))

(defmacro defn [name & fdecl]
  (let [doc?  (string? (first fdecl))
        doc   (if doc? [(first fdecl)] [])
        [args & forms] (if doc? (rest fdecl) fdecl)
        pre?  (and (< 1 (count forms)) (map? (first forms))) 
        rpc   (when pre? (make-asserts (:rpc (first forms))))
        head  (->> [(if pre? (dissoc (first forms) :rpc) (first forms))]
                (remove #(or (nil? %) (empty? %)))) 
        tail  (rest forms)
        name  (if rpc (with-meta name (assoc (meta name) :rpc true)) name)]
    `(clojure.core/defn ~name ~@doc ~args ~@head ~@rpc ~@tail)))