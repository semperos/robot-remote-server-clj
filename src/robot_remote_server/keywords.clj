(ns robot-remote-server.keywords
  (:use robot-remote-server.core :reload)
  (:import javax.swing.JOptionPane)
  (:gen-class :main true))

;; (defn my-keyword
;;   "Documentation for myKeyword"
;;   [arg1 arg2 & others]
;;   (println (str "My first keyword! Arg1: " arg1 ", Arg2: " arg2 ", Others: " others))

(defn sample-keyword
  "Documentation for my Sample Keyword"
  [arg1 arg2 & other-ones]
  (println (str "My sample keyword! Arg1: " arg1 ", and Arg2: " arg2 ", and The Rest: " (apply str other-ones))))

(defn open-dialog
  "Open a JOptionPane, just testing things"
  []
  (JOptionPane/showMessageDialog
    nil "Hello, Clojure World!" "Greeting"
    JOptionPane/INFORMATION_MESSAGE))

(defn -main
  []
  (do (use 'robot-remote-server.core)
      (server-start! (init-handler) false)))