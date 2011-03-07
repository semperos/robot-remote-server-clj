(ns robot-remote-server.keyword
  (:import javax.swing.JOptionPane))

(defn my_keyword
  "Documentation for myKeyword"
  [arg1 arg2]
  (println (str "My first keyword! Arg1: " arg1 ", Arg2: " arg2)))

(defn open_dialog
  "Open a JOptionPane, just testing things"
  []
  (JOptionPane/showMessageDialog
    nil "Hello, Clojure World!" "Greeting"
    JOptionPane/INFORMATION_MESSAGE))