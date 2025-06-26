#!/bin/sh
clojure -M -m nrepl.cmdline --port 7888 &
clojure -M -m migrate
exec java $JAVA_OPTS -jar app.jar 