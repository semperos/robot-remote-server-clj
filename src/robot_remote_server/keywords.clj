(ns robot-remote-server.keywords
  (:require [clj-webdriver.core :as wd])
  (:use robot-remote-server.core)
  (:import javax.swing.JOptionPane)
  (:gen-class :main true))

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

(defn open-browser
  "Open a browser"
  [browser]
  (wd/new-driver (keyword browser)))

(defn -main
  []
  (do (use 'robot-remote-server.core)
      (server-start! (init-handler false))))