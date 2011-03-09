# RobotFramework Remote Server in Clojure

This is a remote server implementation written in Clojure, to be used with the RobotFramework automated testing framework.

## Usage

### Quick Start

If you're using Leiningen and you just want to run the server using the small keyword library that comes with this code, run `lein run` at the command-line.

Once you're ready to run your own keyword library, read on.

### Running the Remote Server

In the namespace you've used to write your RobotFramework keyword library, include the following:

    (use 'robot-remote-server.core)
    (server-start! (init-handler false))

This code should be placed inside the namespace that contains all your RobotFramework keywords; the `init-handler` macro generates a Ring handler that uses the current namespace to find RobotFramework keywords. Be not afraid to spread you namespace over multiple files using `(in-ns 'name-of-namescape)` and `(load file_name)` to make your keyword codebase more manageable. The function `(server-start!)` accepts a second argument as a map of options to pass to jetty, defaulting to `{:port 8270, :join false?}`. To stop the server, use the `(server-stop!)` function.

By default, this remote server does not implement the `stop_remote_server` command as suggested by the RobotFramework spec. You can enable it by starting the server as follows:

    (server-start! (init-handler true))

You can then stop the server simply by calling the `stop_remote_server` command over RPC, in addition to using the `(server-stop!)` function. Refer to [brehaut's necessary-evil project][ne] for help using XML-RPC in Clojure.

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

## Acknowledgements

Thanks to [Mark McGranaghan][mmcgrana-github] for his library [ring][ring-github], which makes defining the application handler and running it with Jetty a simple affair.

Thanks to [Andrew Brehaut][brehaut-site] for his library [necessary-evil][ne] and his help over IRC, which has made writing this XML-RPC server extremely straightforward.

Thanks to [Michael Fogus][fogus-github] for his library [marginalia][marg-github], which has been used to generate the beautifully-rendered source documentation available [here][rrs-marg].

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.

[ne]: https://github.com/brehaut/necessary-evil
[rf-dl]: http://code.google.com/p/robotframework/downloads/list
[rf-java-integration-docs]: http://code.google.com/p/robotframework/wiki/JavaIntegration
[mmcgrana-github]: https://github.com/mmcgrana
[ring-github]: https://github.com/mmcgrana/ring
[brehaut-site]: http://brehaut.net/
[fogus-site]: http://fogus.me/
[marg-github]: https://github.com/fogus/marginalia
[rrs-marg]: http://semperos.github.com/robot-remote-server-clj/uberdoc.html
