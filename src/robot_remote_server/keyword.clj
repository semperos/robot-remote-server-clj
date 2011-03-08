(ns robot-remote-server.keyword
  (:use robot-remote-server.core)
  (:import javax.swing.JOptionPane))

(defn my-keyword
  "Documentation for myKeyword"
  [arg1 arg2]
  (println (str "My first keyword! Arg1: " arg1 ", Arg2: " arg2)))

(defn open-dialog
  "Open a JOptionPane, just testing things"
  []
  (JOptionPane/showMessageDialog
    nil "Hello, Clojure World!" "Greeting"
    JOptionPane/INFORMATION_MESSAGE))

(defn -main
  []
  (do (use 'robot-remote-server.core)
      (server-start! (init-handler))))