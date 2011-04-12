(ns robot-remote-server.util
  (:require [clojure.string :as str]))

(defn handle-return-val
    "Convert everything to RobotFramework-acceptable types. See implementations in other languages for examples"
    [ret]
    (cond
      (string? ret)  ret
      (number? ret)  ret
      (map? ret)     (into {}
                           (for [[k v] ret]
                             [(str k) (handle-return-val v)]))
      (coll? ret)    (map handle-return-val ret)
      (nil? ret)     ""
      :else (str ret)))