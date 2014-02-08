(ns net.ftod.zcube-test
  ( :use clojure.test )
  ( :require [ net.ftod.zcube :as z ] )
)

( deftest test-sum-1 ; Linear trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ [  5 ( z/path "a" "b" "c" ) ]
                 [ 10 ( z/path "a" "b" "d" ) ]
               ] ) ]
    ( and
      ( = 15 ( ( z/count-trees ( z/path "a" )         ) zn ) )
      ( = 15 ( ( z/count-trees ( z/path "a" "b" )     ) zn ) )
      ( =  5 ( ( z/count-trees ( z/path "a" "b" "c" ) ) zn ) )
      ( = 10 ( ( z/count-trees ( z/path "a" "b" "d" ) ) zn ) )
    ) ) ) )

( deftest test-sum-2 ; Branching trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ [ 1 ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ] 
               , [ 1 ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ]
               ] ) ]
      ( and
        ( =  2 ( ( z/count-trees ( z/path "a" ) ) zn ) )
        ( =  2 ( ( z/count-trees ( z/path "a" "b" ) ) zn ) )
        ( =  1 ( ( z/count-trees ( z/path "a" "c" ) ) zn ) )
        ( =  1 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ) zn ) )
        ( =  1 ( ( z/count-trees ( z/path "a" "d" ) ) zn ) )
        ( =  1 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ) zn ) )
      ) ) ) )

( deftest test-sum-3 ; Branching trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ [ 5 ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ] 
               , [ 3 ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ]
               ] ) ]
      ( and
        ( =  8 ( ( z/count-trees ( z/path "a" )                                    ) zn ) )
        ( =  8 ( ( z/count-trees ( z/path "a" "b" )                                ) zn ) )
        ( =  5 ( ( z/count-trees ( z/path "a" "c" )                                ) zn ) )
        ( =  5 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ) zn ) )
        ( =  3 ( ( z/count-trees ( z/path "a" "d" )                                ) zn ) )
        ( =  3 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ) zn ) )
      ) ) ) )

( deftest test-sum-4 ; Branching trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ [  5 ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ] 
               , [ -8 ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ]
               ] ) ]
      ( and
        ( = -3 ( ( z/count-trees ( z/path "a" )                                    ) zn ) )
        ( = -3 ( ( z/count-trees ( z/path "a" "b" )                                ) zn ) )
        ( =  5 ( ( z/count-trees ( z/path "a" "c" )                                ) zn ) )
        ( =  5 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ) zn ) )
        ( = -8 ( ( z/count-trees ( z/path "a" "d" )                                ) zn ) )
        ( = -8 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ) zn ) )
      ) ) ) )

( deftest test-analytics ; Analytics example
  ( is
    ( let [ zn ( z/sum-subtrees
                 [ [ 1 ( z/cross
                         ( z/path "www.company.com" "page1" )
                         ( z/path "gender" "male" )
                         ( z/path "2014" "01" "01" "10" "32" ) ) ]
                 , [ 1 ( z/cross
                         ( z/path "www.company.com" "page2" )
                         ( z/path "gender" "female" )
                         ( z/path "2014" "01" "02" "11" "35" ) ) ]
                 , [ 1 ( z/cross
                         ( z/path "www.company.com" "page1" )
                         ( z/path "gender" "female" )
                         ( z/path "2014" "01" "03" "08" "15" ) ) ]
                 ]
               ) ]
      ( and
        ( = 3 ( ( z/count-trees ( z/path "www.company.com" )                                         ) zn ) )
        ( = 2 ( ( z/count-trees ( z/path "www.company.com" "page1" )                                 ) zn ) )
        ( = 3 ( ( z/count-trees ( z/path "2014" "01" )                                               ) zn ) )
        ( = 2 ( ( z/count-trees ( z/path "gender" "female" )                                         ) zn ) )
        ( = 2 ( ( z/count-trees ( z/cross ( z/path "gender" "female" ) ( z/path "2014" "01" ) )      ) zn ) )
        ( = 1 ( ( z/count-trees ( z/cross ( z/path "gender" "female" ) ( z/path "2014" "01" "02" ) ) ) zn ) )
      ) ) ) )
