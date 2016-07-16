(ns from-ground.load-data
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.core.matrix :refer :all]
 ))


;;Sriphani's function
(defn load-data
  [csv-file]
  (let [data (with-open [in-file (io/reader csv-file)]
               (doall
                (csv/read-csv in-file)))]
    (matrix
     (map
      (fn [row]
        (map #(Double/parseDouble %) row))
      data))))
