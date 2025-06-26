#!/bin/sh
clojure -M -m nrepl.cmdline --port 7888 --host 0.0.0.0 &
clojure -M -m team_challenge.marketplace_shum
