(ns net.ftod.zcube-test
  ( :use net.ftod.zcube clojure.test )
)

( deftest test-cube-1
  ( is
    ( let [ cube ( make-cube [ [ 5 ( path "a" "b" "c" ) ] [ 10 ( path "a" "b" "d" ) ] ] ) ]
      ( = ( map #( measure cube ( trees % ) ) [ ( path "a" ) ( path "a" "b" ) ( path "a" "b" "c" ) ( path "a" "b" "d" ) ] )
          [ 15 15 5 10 ]
      )
    )
  )
)

( deftest test-cube-2
  ( is ( let [ cube ( make-cube
                      [ [ 1 ( product ( path "www.company.com" "page1" ) ( path "gender" "male"   ) ( path "2014" "01" "01" "10" "32" ) ) ]
                      , [ 1 ( product ( path "www.company.com" "page2" ) ( path "gender" "female" ) ( path "2014" "01" "02" "11" "35" ) ) ]
                      , [ 1 ( product ( path "www.company.com" "page1" ) ( path "gender" "female" ) ( path "2014" "01" "03" "08" "15" ) ) ]
                      ]
                    )
             ]
      ( = ( map #( measure cube ( trees % ) )
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

( deftest test-subtrees-1 ( is ( = ( subtrees ( prefix [ "a" ] ( product ( path "b" ) ( path "c" ) ) ) ) ( subtrees ( product ( path "a" "b" ) ( path "a" "c" ) ) ) ) ) )
( deftest test-subtrees-2 ( is ( = ( trees ( prefix [ "a" ] ( product ( path "b" ) ( path "c" ) ) ) ) ( trees ( product ( path "a" "b" ) ( path "a" "c" ) ) ) ) ) )
( deftest test-subtrees-3 ( is ( = ( trees ( prefix [ "a" ] ( product ( path "b" ) ( path "d" ) ) ) ) ( trees ( product ( path "a" "b" ) ( path "a" "d" ) ) ) ) ) )



;( def big-cube
;  ( make-cube
;    ( for
;      [ year [ 2011 2012 2013 ]
;      , month ( range 12 )
;      , day ( range 31 ) ; FIXME Function of month...
;      , hour (  range 24 )
;      , minute ( range 60 )
;      , second ( range 60 )
;      ]
;      [ 1 ( apply path ( map str [
;                                    year
;                                    month
;                                    day
;;                                    hour
;;                                    minute
;;                                    second
;                                    ] ) ) ]
;    )
;  )
;)

; ( measure big-cube ( trees bot ) )
; ( measure big-cube ( trees top ) )
; ( measure big-cube ( trees ( path "2011" ) ) )
