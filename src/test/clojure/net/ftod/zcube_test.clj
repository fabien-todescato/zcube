(ns net.ftod.zcube-test
  ( :use clojure.test )
  ( :require [ net.ftod.zcube :as z ] )
)

( deftest test-sum-1 ; Linear trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ ( z/times  5 ( z/path "a" "b" "c" ) )
               , ( z/times 10 ( z/path "a" "b" "d" ) )
               ] ) ]
    ( and
      ( = 15 ( ( z/count-trees ( z/path "a" )         ) zn ) )
      ( = 15 ( ( z/count-trees ( z/path "a" "b" )     ) zn ) )
      ( =  5 ( ( z/count-trees ( z/path "a" "b" "c" ) ) zn ) )
      ( = 10 ( ( z/count-trees ( z/path "a" "b" "d" ) ) zn ) )
    ) ) ) )

( deftest test-p-sum-group-by-1 ; Linear trees example
  ( is
    ( =
      ( z/p-sum-group-by
        [ ( z/path "a" ) ;  15
        , ( z/path "a" "b" ) ; 15 
        , ( z/path "a" "b" "c" ) ; 5
        , ( z/path "a" "b" "d" ) ; 10
        ]
        [ ( z/times  5 ( z/path "a" "b" "c" ) )
        , ( z/times 10 ( z/path "a" "b" "d" ) )
        ]
      )
      [ 15 15 5 10 ]
    ) ) )

( deftest test-sum-2 ; Branching trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ ( z/times 1 ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ) 
               , ( z/times 1 ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) )
               ] ) ]
      ( and
        ( =  2 ( ( z/count-trees ( z/path "a" )                                    ) zn ) )
        ( =  2 ( ( z/count-trees ( z/path "a" "b" )                                ) zn ) )
        ( =  1 ( ( z/count-trees ( z/path "a" "c" )                                ) zn ) )
        ( =  1 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ) zn ) )
        ( =  1 ( ( z/count-trees ( z/path "a" "d" )                                ) zn ) )
        ( =  1 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ) zn ) )
      ) ) ) )

( deftest test-sum-3 ; Branching trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ ( z/times 5 ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) )
               , ( z/times 3 ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) )
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
               [ ( z/times  5 ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ) 
               , ( z/times -8 ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) )
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
                 [ ( z/times 1 ( z/cross
                                 ( z/path "www.company.com" "page1" )
                                 ( z/path "gender" "male" )
                                 ( z/path "2014" "01" "01" "10" "32" ) ) )
                 , ( z/times 1 ( z/cross
                                 ( z/path "www.company.com" "page2" )
                                 ( z/path "gender" "female" )
                                 ( z/path "2014" "01" "02" "11" "35" ) ) )
                 , ( z/times 1 ( z/cross
                                 ( z/path "www.company.com" "page1" )
                                 ( z/path "gender" "female" )
                                 ( z/path "2014" "01" "03" "08" "15" ) ) )
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

( deftest p-test-analytics ; Analytics example, parallel
  ( is
    ( let [ n ( * 64 1024 )
          , zn ( z/p-sum-subtrees
                 ( flatten ( repeat n
                   [ ( z/times 1 ( z/cross
                                   ( z/path "www.company.com" "page1" )
                                   ( z/path "gender" "male" )
                                   ( z/path "2014" "01" "01" "10" "32" ) ) )
                   , ( z/times 1 ( z/cross
                                   ( z/path "www.company.com" "page2" )
                                   ( z/path "gender" "female" )
                                   ( z/path "2014" "01" "02" "11" "35" ) ) )
                   , ( z/times 1 ( z/cross
                                   ( z/path "www.company.com" "page1" )
                                   ( z/path "gender" "female" )
                                   ( z/path "2014" "01" "03" "08" "15" ) ) )
                   ] ) )
               ) ]
      ( and
        ( = ( * 3 n ) ( ( z/count-trees ( z/path "www.company.com" )                                         ) zn ) )
        ( = ( * 2 n ) ( ( z/count-trees ( z/path "www.company.com" "page1" )                                 ) zn ) )
        ( = ( * 3 n ) ( ( z/count-trees ( z/path "2014" "01" )                                               ) zn ) )
        ( = ( * 2 n ) ( ( z/count-trees ( z/path "gender" "female" )                                         ) zn ) )
        ( = ( * 2 n ) ( ( z/count-trees ( z/cross ( z/path "gender" "female" ) ( z/path "2014" "01" ) )      ) zn ) )
        ( = ( * 1 n ) ( ( z/count-trees ( z/cross ( z/path "gender" "female" ) ( z/path "2014" "01" "02" ) ) ) zn ) )
      ) ) ) )

( deftest test-analytics-sum-group-by ; Analytics example, sequential sum group by
  ( is
    ( let [ n ( * 64 1024 ) ]
      ( =
        ( z/sum-group-by
          [ ( z/path "www.company.com" ) ; 3*n
          , ( z/path "www.company.com" "page1" ) ; 2*n
          , ( z/path "2014" "01" ) ; 3*n
          , ( z/path "gender" "female" ) ; 2*n
          , ( z/cross ( z/path "gender" "female" ) ( z/path "2014" "01" ) ) ; 2*n
          , ( z/cross ( z/path "gender" "female" ) ( z/path "2014" "01" "02" ) ) ; 1*n
          ]
          ( flatten ( repeat n
            [ ( z/times 1 ( z/cross
                            ( z/path "www.company.com" "page1" )
                            ( z/path "gender" "male" )
                            ( z/path "2014" "01" "01" "10" "32" ) ) )
            , ( z/times 1 ( z/cross
                            ( z/path "www.company.com" "page2" )
                            ( z/path "gender" "female" )
                            ( z/path "2014" "01" "02" "11" "35" ) ) )
            , ( z/times 1 ( z/cross
                            ( z/path "www.company.com" "page1" )
                            ( z/path "gender" "female" )
                            ( z/path "2014" "01" "03" "08" "15" ) ) )
            ] ) )
        )
        [ ( * 3 n )
        , ( * 2 n )
        , ( * 3 n )
        , ( * 2 n )
        , ( * 2 n )
        , ( * 1 n )
        ]
      ) ) ) )

( deftest p-test-analytics-sum-group-by ; Analytics example, parallel sum group by
  ( is
    ( let [ n ( * 64 1024 ) ]
      ( =
        ( z/p-sum-group-by
          [ ( z/path "www.company.com" ) ; 3*n
          , ( z/path "www.company.com" "page1" ) ; 2*n
          , ( z/path "2014" "01" ) ; 3*n
          , ( z/path "gender" "female" ) ; 2*n
          , ( z/cross ( z/path "gender" "female" ) ( z/path "2014" "01" ) ) ; 2*n
          , ( z/cross ( z/path "gender" "female" ) ( z/path "2014" "01" "02" ) ) ; 1*n
          ]
          ( flatten ( repeat n
            [ ( z/times 1 ( z/cross
                            ( z/path "www.company.com" "page1" )
                            ( z/path "gender" "male" )
                            ( z/path "2014" "01" "01" "10" "32" ) ) )
            , ( z/times 1 ( z/cross
                            ( z/path "www.company.com" "page2" )
                            ( z/path "gender" "female" )
                            ( z/path "2014" "01" "02" "11" "35" ) ) )
            , ( z/times 1 ( z/cross
                            ( z/path "www.company.com" "page1" )
                            ( z/path "gender" "female" )
                            ( z/path "2014" "01" "03" "08" "15" ) ) )
            ] ) )
        )
        [ ( * 3 n )
        , ( * 2 n )
        , ( * 3 n )
        , ( * 2 n )
        , ( * 2 n )
        , ( * 1 n )
        ]
      ) ) ) )

