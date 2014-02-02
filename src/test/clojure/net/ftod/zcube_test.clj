(ns net.ftod.zcube-test
  ( :use net.ftod.zcube clojure.test )
)

( deftest test-sum-1
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
      )
    )
  )
)

( deftest test-sum-2
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
        ( = 2 ( ( count-trees  ( product ( path "gender" "female" ) ( path "2014" "01" ) ) ) zn ) )
        ( = 1 ( ( count-trees  ( product ( path "gender" "female" ) ( path "2014" "01" "02" ) ) ) zn ) )
      ) ) ) )
