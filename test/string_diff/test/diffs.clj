(ns test.string-diff.diffs
  (:use [midje.sweet]
        [string-diff.diffs]))

(def examples [{:s1 "my&friend&Paul has heavy hats! &!"
                :s2 "my friend John has many many friends &"}
               {:s1 "mmmmm m nnnnn y&friend&Paul has heavy hats! &"
                :s2 "my frie n d Joh n has ma n y ma n y frie n ds n&"}
               {:s1 "Are the kids at home? aaaaa fffff"
                :s2 "Yes they are here! aaaaa fffff"}])


(fact "can identify in a map the ocurrences of unique lowercase characters in a string that appear more than once"
      (unique-and-frequent (-> examples first :s1)) => {\a 4 \h 3
                                                        \e 2 \s 2
                                                        \y 2}
      (unique-and-frequent (-> examples first :s2)) => {\m 3 \a 3 \d 2
                                                        \e 2 \f 2 \h 2
                                                        \i 2 \n 5 \r 2
                                                        \s 2 \y 3})

(fact "given two strings, diffs yields a vector representation of differences"
      (diffs (map unique-and-frequent (-> examples first vals))) =>
      (just '(["2" \r 2] ["2" \n 5] ["2" \m 3]
              ["2" \i 2] ["2" \f 2] ["2" \d 2]
              ["2" \y 3] ["=" \s 2] ["1" \h 3]
              ["=" \e 2] ["1" \a 4]) :in-any-order))

(fact "can mix two or more strings, and can emit a representation string of differences"
      (apply mix (-> examples (nth 0) vals)) =>
      "2:nnnnn/1:aaaa/1:hhh/2:mmm/2:yyy/2:dd/2:ff/2:ii/2:rr/=:ee/=:ss"
      (apply mix (-> examples (nth 1) vals)) =>
      "1:mmmmmm/=:nnnnnn/1:aaaa/1:hhh/2:yyy/2:dd/2:ff/2:ii/2:rr/=:ee/=:ss"
      (apply mix (-> examples (nth 2) vals)) =>
      "=:aaaaaa/2:eeeee/=:fffff/2:rr/1:tt/=:hh"
      (mix "Liffe is good" "Liffe is very good" "Life is so good") =>
      "3:ooo/2:ee/1,2:ff/3:ss/=:ii")
