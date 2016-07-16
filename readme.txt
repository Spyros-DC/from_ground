This is a simple program for computer vision. I have used this program for detection of faces, but with some modifications, it can work for any kind of well formed object.

The two basic functions are:

(train file1 file2 a-vector-of-zeros-and-ones a-number)
file1 and file2 are csv files. Every line of file1 corresponds to one black and white image of a face (i have used octave, a free alternative to matlab, for building those files). Every line of file2 corresponds to one black and white image that is either a face or not. If the image is a face the corresponding element of a-vector-of-zeros-and-ones is 1 else is 0. Finally a-number is the number of right singular vectors or … simply 10.
This train function builds a space from the file1. This space, and near this one, is where the images of faces lives. From the file2 my program deduces how close to that space is one face image and how far to that space is a non-face image. The return value of the train function is a map of values.

(predict values file)
This function takes as first argument a map of values and as second argument a csv file. Every line of that file is either a black and white image or not. The predict function well… predicts if this image is a face.

I have provided some sample csv files, so one usage might be:

(require '[from-ground.eigenfaces :as eigen])

(def values (eigen/train "reduced_faces.csv" "unclassified_reduced.csv" [0 1 1 1 1 1 0 0 0 0 0] 10))

(eigen/predict values "no_class.csv")

I have also provided three directories with the images i have used.

