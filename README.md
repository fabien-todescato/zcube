zcube - Counting trees for fun and profit
=========================================

_zcube_ is about counting trees, and aggregating the counts of the _subtrees_ of these trees. The intent is to provide an analytical tool to compute aggregate sums over multiple hierarchical dimensions.

The bulk of the library is written in Java, around hopefully efficient _immutable_ data structures. A thin Clojure layer provides for the public API of the library.

The rather simple API provides two styles of computing aggregate counts of subtrees :

* An _accumulative style_ whereby, given a tree and a coefficient, occurrences of its subtrees are accumulated into an immutable _ZNumber_.
* A _commutative associative_ style whereby a tree and a coefficient yield a _ZNumber_, and _ZNumber_ may be added.

See [Add ALL the Things: Abstract Algebra Meets Analytics](http://www.infoq.com/presentations/abstract-algebra-analytics) for a good introduction to the power of associativity and commutativity.

# Example 1

As an example, consider the following pair of trees, and their respective decompositions into subtrees :

      a      a       a      a         a
     / \  =     +   /   +    \   +   / \
    b   c          b          c     b   c

      a      a       a      a         a
     / \  =     +   /   +    \   +   / \
    b   d          b          d     b   d

We can _symbolically_ sum the above decompositions as follows :

        a        a       a   a       a                           
       / \  =      +    /  +  \  +  / \                          
      b   c            b       c   b   c                         
            +                                                    
        a        a       a                 a       a
       / \  =      +    /                +  \  +  / \
      b   d            b                     d   b   d
     --------------------------------------------------
               2*a     2*a   a       a     a       a
            =      +    /  +  \  +  / \  +  \  +  / \
                       b       c   b   c     d   b   d

Using the _zcube_ Clojure API, this can be written :

```clojure
(ns net.ftod.zcube-test
  ( :use net.ftod.zcube clojure.test )
)


( deftest test-sum-1
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

```

This generalizes easily to multiple occurrences of trees, again, using a _multiplicative_ notation to suggest multiple occurrences, as follows :

      5*a      5*a     5*a   5*a     5*a                           
       / \  =      +    /  +    \  +  / \                          
      b   c            b         c   b   c                         
            +                                                      
      3*a      3*a     3*a                    3*       a
       / \  =      +    /                  +    \  +  / \
      b   d            b                         d   b   d
     ------------------------------------------------------
               8*a     8*a   5*a     5*a     3*a     3*a
            =      +    /  +    \  +  / \  +    \  +  / \
                       b         c   b   c       d   b   d

Again, in Clojure :


```clojure
(ns net.ftod.zcube-test
  ( :use net.ftod.zcube clojure.test )
)


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
```

# Example 2 : Some Analytics

Now, why in the world would you want to do such a thing, decomposing trees into subtrees, and counting their occurrences ?

Well, suppose you want to perform some analytics on a clickstream, where each event in the stream, besides the url, gives you data about the demographics of the user, and the time of click.

You can model such events as trees, for example, using ou _informal_ algebraic notation :

      male   user on page1 the 1st of january 2014 at 1OH32 ~ www.company.com/page1+gender/male+2014/01/01/10/32
      female user on page2 the 2nd of january 2014 at 11H15 ~ www.company.com/page2+gender/female+2014/01/02/11/15
      female user on page1 the 3rd of january 2014 at 08H15 ~ www.company.com/page1+gender/female+2014/01/03/08/15

Now, computing the subtrees generated by these, and summing, you get the following terms :

      2*(www.company.com+2014/01) ~ 2 clicks on the domain www.company.com in January 2014
      2*(www.company.com+2014+gender/female) ~ 2 clicks on the domain www.company.com in January 2014 by female users
      
ie computing the subtree decomposition is tantamount to performing multidimensional aggregate sums.

## In Clojure, please ?

**TODO** Clojure API examples.

