(ns net.ftod.zcube
  ( :import net.ftod.zcube.zdd.ZDD
            net.ftod.zcube.zdd.ZDDNumber
            net.ftod.zcube.zdd.ZDDTree
  )
  ( :require [ clojure [ string :as s ] ]
  )
)

( def ^ZDDTree top ZDDTree/TOP ) 
( def ^ZDDTree bot ZDDTree/BOT ) 

( defn ^ZDDTree prefix  [ strings  ^ZDDTree treeSet ] ( ZDDTree/prefix strings treeSet ) )
( defn ^ZDDTree path    [ & strings  ] ( ZDDTree/path strings ) )
( defn ^ZDDTree product [ & treeSets ] ( ZDDTree/product treeSets ) ) 
( defn ^ZDDTree sum     [ & treeSets ] ( ZDDTree/sum     treeSets ) ) 

( defn ^ZDD trees    [ ^ZDDTree t ] ( ZDDTree/trees    t ) )
( defn ^ZDD subtrees [ ^ZDDTree t ] ( ZDDTree/subtrees t ) )

( defn ^ZDDNumber add
  "Add two signed ZDD numbers"
  [ ^ZDDNumber zn1 ^ZDDNumber zn2 ]
  ( ZDDNumber/negabinaryAdd zn1 zn2 )
)

( defn ^ZDDNumber sub
  "Subtract two signed ZDD numbers"
  [ ^ZDDNumber zn1 ^ZDDNumber zn2 ]
  ( ZDDNumber/negabinarySub zn1 zn2 )
)

( defn ^ZDDNumber cube
  ( [ ^long l ^ZDDTree trees ^ZDDNumber zn ] ( ZDDNumber/cube l trees zn ) )
  ( [ ^long l ^ZDDTree trees ^ZDD filter ^ZDDNumber zn ] ( ZDDNumber/cube l trees filter zn ) )
)

( defn ^long measure
  [ ^ZDDNumber cube ^ZDD z ] ( ZDDNumber/negabinary cube z )
)

( defn ^ZDDNumber make-cube
  [ long-trees ]
  ( reduce ( fn [ zn [ l trees] ] ( cube l trees zn ) ) nil long-trees )
)
