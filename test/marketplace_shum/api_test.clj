(ns marketplace-shum.api-test
  (:require [clojure.test :refer [deftest testing is]]))

(deftest smoke-test
  (testing "Smoke test"
    (is (= 1 1))))