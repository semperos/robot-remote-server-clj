# RobotFramework Remote Server in Clojure

This is a remote server implementation written in Clojure, to be used with the RobotFramework automated testing framework.

## Usage

### Running the Remote Server

In your Clojure code, you need the following:

    (use 'robot-remote-server.core)
    (reset! rf-ns 'your-keyword-namespace)
    (server-start!)

The atom `rf-ns` should be set to the namespace containing your RobotFramework keywords. By default it's set to an example namespace `robot-remote-server.keyword` with a couple of keywords already defined.

You can start and stop the XML-RPC remote server by using the `(server-start!)` and `(server-stop!)` functions, or by calling `:stop_remote_server` directly via RPC (refer to [brehaut's necessary-evil project][ne] for help with XML-RPC in Clojure).

Keywords can be word-separated using dashes or underscores, e.g. "open-dialog" and "open_dialog" are the same thing. Avoid naming conflicts and stick to one method (dashes are more conventinal for Clojure/Lisp; underscores are supported here for consistency with existing RobotFramework keyword libraries in other languages).

### Running RobotFramework Tests

If you're writing Clojure, you obviously have Java installed, so I recommend that you run your RobotFramework tests using the standalone jar that ships with RobotFramework. Check the [downloads page for RobotFramework][rf-dl] to find the latest standalone jar.

Once acquired, you can simply put the `robotframework-x.x.x.jar` file in the same directory as your RobotFramework tests and run it:

    java -jar robotframework-x.x.x.jar my_robotframework_test.txt

There are a number of options you can pass to the jar; for more details, see the [RobotFramework documentation][rf-java-integration-docs] on the subject. For a bare-bones example of a RobotFramework test, see the file `resources/test.txt` in this code base.

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.

[ne]: https://github.com/brehaut/necessary-evil
[rf-dl]: http://code.google.com/p/robotframework/downloads/list
[rf-java-integrations-docs]: http://code.google.com/p/robotframework/wiki/JavaIntegration
