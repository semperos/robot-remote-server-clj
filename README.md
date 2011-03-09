# RobotFramework Remote Server in Clojure

This is a remote server implementation written in Clojure, to be used with the RobotFramework automated testing framework.

## Usage

### Running the Remote Server

If you're using Leiningen, you can simply do `lein run` at the command-line to start the RobotFramework remote server.

Otherwise, in your Clojure code, you need the following:

    (use 'robot-remote-server.core)
    (server-start! (init-handler))

This code should be placed inside the namespace that contains all your RobotFramework keywords; the `init-handler` macro generates a Ring handler that uses the current namespace to find RobotFramework keywords. Be not afraid to spread you namespace over multiple files using `(in-ns 'name-of-namescape)` and `(load file_name)` to make your keyword code more manageable. The function `(server-start!)` also accepts a map of options to pass to jetty, defaulting to `{:port 8270, :join false?}`.

You can start and stop the XML-RPC remote server by using the `(server-start!)` and `(server-stop!)` functions, or by calling `stop_remote_server` directly via RPC (refer to [brehaut's necessary-evil project][ne] for help with XML-RPC in Clojure).

### Writing RobotFramework Keywords in Clojure

Keywords can be word-separated using dashes or underscores, e.g. a RobotFramework keyword "Open Dialog" can be implemented as either the function "open-dialog" or "open_dialog". Avoid naming conflicts and stick to one method (dashes are more conventional for Clojure/Lisp; underscores are supported here for consistency with existing RobotFramework keyword libraries in other languages).

Any function with an asterisk `*` or exclamation-point/bang `!` will not be included as a RobotFramework keyword, so if you absolutely need to put non-RobotFramework-keyword functions in your keyword namespace, include one of those symbols in the name. I highly recommend using an alternative namespace and requiring it separately.

### Running RobotFramework Tests

If you're writing Clojure, you obviously have Java installed, so I recommend that you run your RobotFramework tests using the standalone jar that ships with RobotFramework. Check the [downloads page for RobotFramework][rf-dl] to find the latest standalone jar.

In addition, you need to add a `Library` statement to your RobotFramework tests `*** Settings ***` section to use this your Clojure keywords via the remote server:

    Library         Remote  http://localhost:8270

After updating your test scripts with that setting, place the standalone RobotFramework jar in the same directory as your test scripts and run them as follows:

    java -jar robotframework-x.x.x.jar my_robotframework_test.txt

There are a number of options you can pass to the jar; for more details, see the [RobotFramework documentation][rf-java-integration-docs] on the subject. For a bare-bones example of a RobotFramework test, see the file `resources/test.txt` in this code base.

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.

[ne]: https://github.com/brehaut/necessary-evil
[rf-dl]: http://code.google.com/p/robotframework/downloads/list
[rf-java-integration-docs]: http://code.google.com/p/robotframework/wiki/JavaIntegration
