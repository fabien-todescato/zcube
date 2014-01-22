(ns net.ftod.zcube-test
  ( :use net.ftod.zcube clojure.test )
)

( deftest test-cube-1
  ( is
    ( let [ cube ( make-cube [ [ 5 ( branch "a" "b" "c" ) ] [ 10 ( branch "a" "b" "d" ) ] ] ) ]
      ( = ( map #( measure cube ( trees % ) ) [ ( branch "a" ) ( branch "a" "b" ) ( branch "a" "b" "c" ) ( branch "a" "b" "d" ) ] )
          [ 15 15 5 10 ]
      )
    )
  )
)

( deftest test-subtrees-1 ( is ( = ( subtrees ( prefix [ "a" ] ( product ( branch "b" ) ( branch "c" ) ) ) ) ( subtrees ( product ( branch "a" "b" ) ( branch "a" "c" ) ) ) ) ) )
( deftest test-subtrees-2 ( is ( = ( trees ( prefix [ "a" ] ( product ( branch "b" ) ( branch "c" ) ) ) ) ( trees ( product ( branch "a" "b" ) ( branch "a" "c" ) ) ) ) ) )
( deftest test-subtrees-3 ( is ( = ( trees ( prefix [ "a" ] ( product ( branch "b" ) ( branch "d" ) ) ) ) ( trees ( product ( branch "a" "b" ) ( branch "a" "d" ) ) ) ) ) )



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
;      [ 1 ( apply branch ( map str [
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
; ( measure big-cube ( trees ( branch "2011" ) ) )
