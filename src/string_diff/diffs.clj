(ns string-diff.diffs)

(defn add-entry
  "Add a key + total count to a map"
  [hmap k]
  (let [cnt (or (get hmap k) 0)]
    (assoc hmap k (inc cnt))))

(defn count-uniq
  "Create a map that represents with each entry how may times an element occurs in a given
  sequence (key->elem & value->count)"
  [seq]
  (loop [uniq {}
         [x & rest] seq]
    (if (nil? x)
      uniq
      (recur (add-entry uniq x) rest))))

(defn select-vals-where
  "Select hash-map entries where value matches a predicate"
  [pred hmap]
  (->> hmap
       (filter (fn [[k v]] (pred v)))
       (into {})))

(def lowercase-chars (->> (range (int \a) (inc (int \z)))
                          (map char) set))

(defn unique-lowercase-chars
  "Creates a map from a string with the unique elements as keys and totals as values
  Only returns unique lowercase characters from the string"
  [str]
  (let [present-lowercase-chars (-> str set (clojure.set/intersection lowercase-chars))]
    (select-keys (count-uniq str) present-lowercase-chars)))

(defn frequent
  "Select hash-map entries where the value is greater than 1"
  [hmap]
  (select-vals-where #(> % 1) hmap))

(def unique-and-frequent (comp frequent unique-lowercase-chars))

(defn diffs [seq-diffs]
  (loop [diffs (list)
         [k & ks] (-> (map keys seq-diffs) flatten distinct)]
    (if-not k
      diffs
      (let [vals (mapv #(get % k) seq-diffs)
            indexes-of (fn [e coll] (keep-indexed #(if (= e %2) %1) coll))
            diff-idxs (->> vals (indexes-of (reduce (fnil max 0 0) vals)))
            diff-symbol (if (apply = vals)
                          "="
                          (clojure.string/join "," (map inc diff-idxs)))
            diff-val (nth vals (first diff-idxs))]
        (recur (conj diffs [diff-symbol k diff-val]) ks)))))

(defn prioritized-diff?
  "Returns whether diff1 should be printed before diff2"
  [[sym1 ch1 times1] [sym2 ch2 times2]]
  (let [equal (= sym1 "=")
        both-equal (and equal (= sym2 "="))
        same-times (= times1 times2)
        time-priority (> times1 times2)
        alphabetic-order (< (int ch1) (int ch2))]
    (if same-times
      (if (and equal (not both-equal)) ;;if only sym1 is "=", diff2 is prioritized
        false
        alphabetic-order)
      time-priority)))

(defn emit
  [diff-vec]
  (->> (loop [visual-diffs []
              [[str-key char times] & diffs] (sort prioritized-diff? diff-vec)]
         (if-not char
           visual-diffs
           (let [repeated-char (->> (repeat times char) (apply str))]
             (recur (conj visual-diffs (str str-key ":" repeated-char)) diffs))))
       (clojure.string/join "/")))

(defn mix
  "Given two strings, yield a single string that represents visual differences"
  [& strings]
  (when strings
    (->> (map unique-and-frequent strings)
         diffs
         emit)))
