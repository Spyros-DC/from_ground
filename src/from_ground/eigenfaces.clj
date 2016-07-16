(ns from-ground.eigenfaces
  (:refer-clojure :exclude [* - + == /])
  (:require [clojure.core.matrix :refer :all]
            [clojure.core.matrix.operators :refer :all]
            [clojure.core.matrix.linear :as linear]
            [from-ground.load-data :refer :all]
))

;;a function from core.matrix for setting the implementation
(set-current-implementation :vectorz)

(defn projection 
  "computes the projection of b to V. V has norm 1"
  [b V]
  (mmul (mmul b V) V))

(defn project-orthogonal 
  "computes the orthogonal projection b to the space spanned by M"
  [M b]
  (if (empty? M)
    b
    (project-orthogonal (rest M) (- b (projection b (first M))))))

(defn vector-decomposition 
  "decomposes the vector b to it's orthogonal and parallel projection to
  the vector space spanned by the rows of M, where the rows of M are the
  n right singular vectors of some other matrix"
  [M b]
  (let [b-orthogonal (project-orthogonal M b)
        b-parallel (- b b-orthogonal)]
    {:b-parallel b-parallel :b-orthogonal b-orthogonal})
)

(defn compute-centroid 
  "computes the centroid of the rows of a matrix M"
  [M]
  (let [s (apply + (slices M))]
    (* s (/ 1 (get (shape M) 0)))))

(defn normalize-matrix
  "normalize a matix M by subtracting a centroid from each row"
  ([M]
   (let [centroid (compute-centroid M)]
    (-> (slices M) (- centroid)  ))
   )
  ([M centroid]
   (-> (slices M) (- centroid))
   )
)

(defn squared-decomposed
  "computes a scalar, the square of the norm of the decomposed vectors b-paralel and b-orthogonal"
  [M b]
  (let [decom-map (vector-decomposition M b)]
    {:b-parallel (mmul (decom-map :b-parallel) (decom-map :b-parallel))
     :b-orthogonal (mmul (decom-map :b-orthogonal) (decom-map :b-orthogonal))})
)

(defn mat-decomposition
  "decomposes each row of the matrix"
  [eigens B]
  (loop [vals [] consumed-mat B]
    (if (empty? consumed-mat)
      (reverse vals)
      (recur
       (cons (squared-decomposed eigens (first consumed-mat)) vals)
       (rest consumed-mat)
       ))
    )
)

(defn find-n-right-singular-vectors
    "returns n right singular vectors of matrix M"
    [M n]
  (let [right-singular-vectors-transpose (get (linear/svd M {:return [:V*]}) :V*)
        m (get (shape right-singular-vectors-transpose) 1)]
    (reshape right-singular-vectors-transpose [n m]))
)

(defn eigenfaces
     "loads the data from file and produces n eigenfaces and centroid from that"
     [file n]
     (let [
           M (load-data file)
           centroid (compute-centroid M)
           m-normalized (normalize-matrix M)
           eigens (find-n-right-singular-vectors m-normalized n)
           ]
       {:centroid centroid :eigens eigens}
       )
     )

(defn two-vecs
  "construct two vecs for objects and non-objects"
  [map-values zeros-and-ones]
  (loop [
         mp {:0 [] :1 []} vals map-values z-o zeros-and-ones
         ]
    (if (empty? z-o)
      mp
      (recur
       (if (= 0 (first z-o))
         {:0 (cons ((first vals) :b-orthogonal) (mp :0)) :1 (mp :1)}
         {:0 (mp :0) :1 (cons ((first vals) :b-orthogonal) (mp :1))}
         )
       (rest vals)
       (rest z-o)
       ))
    )
)

(defn train
  "returns a scalar the threshold between objects and non-objects"
  [eigen-file train-file zeros-and-ones n]
  (let [ 
        eigens (eigenfaces eigen-file n)
        centroid (eigens :centroid)
        n-eigens (eigens :eigens)
        train-matrix (load-data train-file)
        train-centered (normalize-matrix train-matrix centroid)
        map-values (mat-decomposition n-eigens train-centered)
        results (two-vecs map-values zeros-and-ones)
        ]
    {:threshold     (/ (+ (maximum (results :1)) (minimum (results :0))) 1) 
     :eigens n-eigens
     :centroid centroid
     }

)
)

(defn predict-helper
  "just a helper function"
  [distances threshold]
  (loop [dist distances vals [] i 1]
    (if (empty? dist)
      (reverse vals)
      (recur
       (rest dist)
       (if (<= (mget ((first dist) :b-orthogonal)) threshold)
         (do (println "number " i "photo is a face") (cons 1 vals))
         (do (println "number " i "photo is not a face") (cons 0 vals))
         )
       (inc i)
       )
      )
    )
  )

(defn predict
  "finally the prediction!"
  [values file]
  (let [
        M (load-data file)
        m-normalized (normalize-matrix M (values :centroid))
        distances (mat-decomposition (values :eigens) m-normalized)
        ]
    (predict-helper distances (values :threshold))
    )
)



