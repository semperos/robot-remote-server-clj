(ns robot-remote-server.util)

(defn handle-return-val
    "Convert everything to RobotFramework-acceptable types. See implementations in other languages for examples"
    [ret]
    (cond
      (= (class ret) java.lang.String)              ret
      (contains?
       (supers (class ret)) java.lang.Number)       ret
      (map? ret)                                    (into {}
                                                          (for [[k v] ret]
                                                            [(.toString k) (handle-return-val v)]))
      (contains?
       (supers (class ret)) java.util.Collection)   (map handle-return-val ret)
      (nil? ret)                                    ""
      :else ret))