(ns net.ftod.zcube-test
  ( :use net.ftod.zcube clojure.test )
)

( deftest test-sum-1
  ( is
    ( let [ zn ( sum-subtrees [ [ 5 ( path "a" "b" "c" ) ]
                              , [ 10 ( path "a" "b" "d" ) ]
                              ] ) ]
      ( = ( map #( ( count-trees % ) zn )
            [ ( path "a" )
            , ( path "a" "b" )
            , ( path "a" "b" "c" )
            , ( path "a" "b" "d" )
            ]
          )
          [ 15 15 5 10 ]
      )
    )
  )
)

( deftest test-sum-2
  ( is ( let [ zn ( sum-subtrees
                      [ [ 1 ( product ( path "www.company.com" "page1" ) ( path "gender" "male"   ) ( path "2014" "01" "01" "10" "32" ) ) ]
                      , [ 1 ( product ( path "www.company.com" "page2" ) ( path "gender" "female" ) ( path "2014" "01" "02" "11" "35" ) ) ]
                      , [ 1 ( product ( path "www.company.com" "page1" ) ( path "gender" "female" ) ( path "2014" "01" "03" "08" "15" ) ) ]
                      ]
                    )
             ]
      ( = ( map #( ( count-trees % ) zn )
            [ ( path "www.company.com" )
            , ( path "www.company.com" "page1" )
            , ( path "2014" "01" )
            , ( path "gender" "female" )
            ]
          )
          [ 3
          , 2
          , 3
          , 2
          ]
      ) ) ) )
