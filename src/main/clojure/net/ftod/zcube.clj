(ns net.ftod.zcube
  ( :import net.ftod.zcube.zdd.ZDD
            net.ftod.zcube.zdd.ZDDNumber
            net.ftod.zcube.zdd.ZDDTree
            java.lang.Iterable
            java.util.Collection
  )
)

( def ^ZDDTree top ZDDTree/TOP ) 
( def ^ZDDTree bot ZDDTree/BOT ) 

( defn ^ZDDTree prefix  [  ^Iterable strings  ^ZDDTree tree ] ( ZDDTree/prefix strings tree ) )
( defn ^ZDDTree path    [ & strings  ] ( ZDDTree/path ^Iterable strings ) )
( defn ^ZDDTree product [ & tree ] ( ZDDTree/product ^Collection tree ) ) 
( defn ^ZDDTree sum     [ & tree ] ( ZDDTree/sum     ^Collection tree ) ) 

( defn ^ZDDNumber subtrees
  ( [ trees ]
    ( let [ ^ZDD z ( ZDDTree/unionTrees trees ) ]
      ( fn [ ^long l ^ZDDTree t ] ( ZDDNumber/negabinary l ( ZDDTree/subtrees z t ) ) )
    )
  )
  ( [ ^long l ^ZDDTree t] ( ZDDNumber/negabinary l ( ZDDTree/subtrees t ) )
  )
)

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

( defn ^ZDDNumber add-subtrees
  ( [ trees ]
    ( let [ ^ZDD z ( ZDDTree/unionTrees trees ) ]
      ( fn [ ^long l ^ZDDTree trees ^ZDD filter ^ZDDNumber zn ] ( ZDDNumber/addSubtrees l trees filter zn ) )
    )
  )
  ( [ ^long l ^ZDDTree trees ^ZDDNumber zn ] ( ZDDNumber/addSubtrees l trees zn )
  )
)

( defn ^long count-trees [ ^ZDDTree tree ]
  ( let [ ^ZDD z ( ZDDTree/trees tree ) ]
    ( fn [ ^ZDDNumber n ] ( ZDDNumber/negabinary n z ) )
  )
)

( defn ^ZDDNumber sum-subtrees
  [ long-trees ]
  ( reduce ( fn [ zn [ long tree ] ] ( add-subtrees long tree zn ) ) nil long-trees )
)
