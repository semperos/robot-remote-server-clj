(ns robot-remote-server.util)

;;; TODO: Extend ResponseElements protocol to deal with nil

(comment
  
  (defn- handle-return-val
    "Convert everything to RobotFramework-acceptable types. See implementations in other languages for examples"
    [ret]
    (condp class ret
      java.lang.String                           ret
      java.util.concurrent.atomic.AtomicInteger  ret
      java.util.concurrent.atomic.AtomicLong     ret
      java.math.BigDecimal                       ret
      java.math.BigInteger                       ret
      java.lang.Byte                             ret
      java.lang.Double                           ret
      java.lang.Float                            ret
      java.lang.Integer                          ret
      java.lang.Long                             ret
      java.lang.Short                            ret
      clojure.lang.PersistentVector              (map handle-return-val ret)
      clojure.lang.PersistentArrayMap            (into {}
                                                       (for [[k v] ret]
                                                         [(.toString k) (handle-return-val v)]))
      clojure.lang.PersistentTreeMap             (into {}
                                                       (for [[k v] ret]
                                                         [(.toString k) (handle-return-val v)]))
      :else ret))

  (defonce a-server (run-jetty #'app-handler {:port 8271 :join? false}))
  (doto (Thread. #(run-jetty #'app-handler {:port 8271})) .start)
)