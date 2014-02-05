(ns net.ftod.zcube
  ( :import net.ftod.zcube.zdd.ZDD
            net.ftod.zcube.zdd.ZDDNumber
            net.ftod.zcube.zdd.ZDDTree
            java.lang.Iterable
            java.util.Collection
  )
)

;
; The algebra of ZDD trees.
;

( def ^ZDDTree top ZDDTree/TOP ) ; The singleton set containing the empty tree. 
( def ^ZDDTree bot ZDDTree/BOT ) ; The empty set of trees.

( defn ^ZDDTree prefix [ ^Iterable strings  ^ZDDTree tree ]
  "Prefix a set of trees with a segment."
  ( ZDDTree/prefix strings tree )
)

( defn ^ZDDTree path [ & strings ]
  "Build a singleton containing a linear tree."
  ( ZDDTree/path ^Iterable strings )
)

( defn ^ZDDTree cross [ & trees ]
  "Generate new trees as the cross union of the trees in the treesets."
  ( ZDDTree/cross ^Collection trees )
)

( defn ^ZDDTree sum [ & trees ]
  "Take union of tree sets."
  ( ZDDTree/sum ^Collection trees )
) 

;
; The algebra of ZDD numbers.
;

( defn ^ZDDNumber add
  "Add two signed ZDD numbers. Associative and commutative, nil is the zero element."
  [ ^ZDDNumber zn1 ^ZDDNumber zn2 ]
  ( ZDDNumber/negabinaryAdd zn1 zn2 )
)

( defn ^ZDDNumber sub
  "Subtract two signed ZDD numbers."
  [ ^ZDDNumber zn1 ^ZDDNumber zn2 ]
  ( ZDDNumber/negabinarySub zn1 zn2 )
)

;
; Constructing ZDD numbers from ZDD trees.
;

( defn ^ZDDNumber subtrees
  "Construct ZDD number counting the occurrences of subtrees of a tree.
   The higher-order one-argument version takes a filter expressed a sequence of trees,
   and yields the corresponding constructor function.
  "
  ( [ trees ]
    ( let [ ^ZDD z ( ZDDTree/unionTrees trees ) ]
      ( fn [ ^long l ^ZDDTree t ] ( ZDDNumber/negabinary l ( ZDDTree/subtrees z t ) ) )
    )
  )
  ( [ ^long l ^ZDDTree t] ( ZDDNumber/negabinary l ( ZDDTree/subtrees t ) )
  )
)

( defn ^ZDDNumber add-subtrees
  "Add occurrences of subtrees to a ZDD number.
   The higher-order one-argument version takes a filter expressed a sequence of trees,
   and yields the corresponding adder function.
  "
  ( [ trees ]
    ( let [ ^ZDD z ( ZDDTree/unionTrees trees ) ] ; Pay the ZDD computation once...
      ( fn [ ^long l ^ZDDTree trees ^ZDD filter ^ZDDNumber zn ] ( ZDDNumber/addSubtrees l trees filter zn ) ) ; ...possibly apply multiple times over ZDD numbers.
    )
  )
  ( [ ^long l ^ZDDTree trees ^ZDDNumber zn ] ( ZDDNumber/addSubtrees l trees zn )
  )
)

( defn ^ZDDNumber sum-subtrees
  "Sum a sequence of pairs of longs and trees."
  [ long-trees ]
  ( reduce
    ( fn [ zn [ long tree ] ] ( add-subtrees long tree zn ) )
    nil ; Yes, this is the zero ZDD number. 
    long-trees
  )
)

;
; Retrieving counts from ZDD numbers.
;

( defn ^long count-trees
  "Count occurrences of trees in a ZDD number.
   This is an higher-order function that yields a proper counting function.
  "
  [ ^ZDDTree tree ]
  ( let [ ^ZDD z ( ZDDTree/trees tree ) ] ; Pay the ZDD computation once...
    ( fn [ ^ZDDNumber n ] ( ZDDNumber/negabinary n z ) ) ; ...possibly apply multiple times over ZDD numbers.
  )
)

