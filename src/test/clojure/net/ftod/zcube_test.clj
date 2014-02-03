(ns net.ftod.zcube-test
  ( :use net.ftod.zcube clojure.test )
)

( deftest test-sum-1 ; Linear trees example
  ( is
    ( let [ zn ( sum-subtrees
               [ [  5 ( path "a" "b" "c" ) ]
                 [ 10 ( path "a" "b" "d" ) ]
               ] ) ]
    ( and
      ( = 15 ( ( count-trees ( path "a" )         ) zn ) )
      ( = 15 ( ( count-trees ( path "a" "b" )     ) zn ) )
      ( =  5 ( ( count-trees ( path "a" "b" "c" ) ) zn ) )
      ( = 10 ( ( count-trees ( path "a" "b" "d" ) ) zn ) )
    ) ) ) )

( deftest test-sum-2 ; Branching trees example
  ( is
    ( let [ zn ( sum-subtrees
               [ [ 1 ( product ( path "a" "b" ) ( path "a" "c" ) ) ] 
               , [ 1 ( product ( path "a" "b" ) ( path "a" "d" ) ) ]
               ] ) ]
      ( and
        ( =  2 ( ( count-trees ( path "a" ) ) zn ) )
        ( =  2 ( ( count-trees ( path "a" "b" ) ) zn ) )
        ( =  1 ( ( count-trees ( path "a" "c" ) ) zn ) )
        ( =  1 ( ( count-trees ( product ( path "a" "b" ) ( path "a" "c" ) ) ) zn ) )
        ( =  1 ( ( count-trees ( path "a" "d" ) ) zn ) )
        ( =  1 ( ( count-trees ( product ( path "a" "b" ) ( path "a" "d" ) ) ) zn ) )
      ) ) ) )

( deftest test-sum-3 ; Branching trees example
  ( is
    ( let [ zn ( sum-subtrees
               [ [ 5 ( product ( path "a" "b" ) ( path "a" "c" ) ) ] 
               , [ 3 ( product ( path "a" "b" ) ( path "a" "d" ) ) ]
               ] ) ]
      ( and
        ( =  8 ( ( count-trees ( path "a" ) ) zn ) )
        ( =  8 ( ( count-trees ( path "a" "b" ) ) zn ) )
        ( =  5 ( ( count-trees ( path "a" "c" ) ) zn ) )
        ( =  5 ( ( count-trees ( product ( path "a" "b" ) ( path "a" "c" ) ) ) zn ) )
        ( =  3 ( ( count-trees ( path "a" "d" ) ) zn ) )
        ( =  3 ( ( count-trees ( product ( path "a" "b" ) ( path "a" "d" ) ) ) zn ) )
      ) ) ) )

( deftest test-sum-4 ; Analytics example
  ( is
    ( let [ zn ( sum-subtrees
                 [ [ 1 ( product
                      ( path "www.company.com" "page1" )
                      ( path "gender" "male" )
                      ( path "2014" "01" "01" "10" "32" ) ) ]
                    , [ 1 ( product
                      ( path "www.company.com" "page2" )
                      ( path "gender" "female" )
                      ( path "2014" "01" "02" "11" "35" ) ) ]
                    , [ 1 ( product
                      ( path "www.company.com" "page1" )
                      ( path "gender" "female" )
                      ( path "2014" "01" "03" "08" "15" ) ) ]
                    ]
               ) ]
      ( and
        ( = 3 ( ( count-trees ( path "www.company.com" )         ) zn ) )
        ( = 2 ( ( count-trees ( path "www.company.com" "page1" ) ) zn ) )
        ( = 3 ( ( count-trees ( path "2014" "01" )               ) zn ) )
        ( = 2 ( ( count-trees ( path "gender" "female" )         ) zn ) )
        ( = 2 ( ( count-trees ( product ( path "gender" "female" ) ( path "2014" "01" ) ) ) zn ) )
        ( = 1 ( ( count-trees ( product ( path "gender" "female" ) ( path "2014" "01" "02" ) ) ) zn ) )
      ) ) ) )
