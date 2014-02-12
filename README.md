_net.ftod/zcube_
==============================================
Counting Trees for Fun and Profit

_zcube_ is about counting trees, and aggregating the counts of the _subtrees_ of these trees. The intent is to provide an analytical tool to compute aggregate sums over multiple hierarchical dimensions.

In a nutshell :

      5*a      5*a     5*a   5*a     5*a                           
       / \  =      +    /  +    \  +  / \                          
      b   c            b         c   b   c                         
            +                                                      
      3*a      3*a     3*a                   3*a       a
       / \  =      +    /                  +    \  +  / \
      b   d            b                         d   b   d
     ------------------------------------------------------
               8*a     8*a   5*a     5*a     3*a     3*a
            =      +    /  +    \  +  / \  +    \  +  / \
                       b         c   b   c       d   b   d

```clojure
(ns net.ftod.zcube-test
  ( :use clojure.test )
  ( :require [ net.ftod.zcube :as z ] )
)

( deftest test-sum-3 ; Branching trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ [ 5 ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ] 
               , [ 3 ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ]
               ] ) ]
      ( and
        ( =  8 ( ( z/count-trees ( z/path "a" )                                    ) zn ) )
        ( =  8 ( ( z/count-trees ( z/path "a" "b" )                                ) zn ) )
        ( =  5 ( ( z/count-trees ( z/path "a" "c" )                                ) zn ) )
        ( =  5 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ) zn ) )
        ( =  3 ( ( z/count-trees ( z/path "a" "d" )                                ) zn ) )
        ( =  3 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ) zn ) )
      ) ) ) )
```

Releases and Dependency Information
========================================

Releases published on [Clojars].

Latest stable is **0.0.1**


[Leiningen] dependency information:
```clojure
[ net.ftod/zcube "0.0.1" ]
```

[Maven] dependency information:
```xml
<dependency>
  <groupId>net.ftod</groupId>
  <artifactId>zcube</artifactId>
  <version>0.0.1</version>
</dependency>
```

[Maven] repository information:
```xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```
    
[Clojars]: http://clojars.org/
[Leiningen]: http://leiningen.org/
[Maven]: http://maven.apache.org/

The API
=======

The rather simple API provides two styles of computing aggregate counts of subtrees :

* An _accumulative style_ whereby :
  * Given a tree and a integer coefficient, occurrences of its subtrees are accumulated into an immutable _ZDDNumber_.
* A _commutative associative_ style whereby :
  * A tree and an integer coefficient yield a _ZDDNumber_
  * Sequences of _ZDDNumber_ may be added.

See [Add ALL The Things][1] for a good introduction to the power of associativity and commutativity.

## Example 1 : About counting subtrees

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
  ( :use clojure.test )
  ( :require [ net.ftod.zcube :as z ] )
)

( deftest test-sum-2 ; Branching trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ [ 1 ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ] 
               , [ 1 ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ]
               ] ) ]
      ( and
        ( =  2 ( ( z/count-trees ( z/path "a" ) ) zn ) )
        ( =  2 ( ( z/count-trees ( z/path "a" "b" ) ) zn ) )
        ( =  1 ( ( z/count-trees ( z/path "a" "c" ) ) zn ) )
        ( =  1 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ) zn ) )
        ( =  1 ( ( z/count-trees ( z/path "a" "d" ) ) zn ) )
        ( =  1 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ) zn ) )
      ) ) ) )
```
ie we add the subtrees generated by 1 occurrence of each tree, and count the occurrences of the individual trees in the result.

This generalizes easily to multiple occurrences of trees, using again a _multiplicative_ notation to suggest multiple occurrences of trees, as follows :

      5*a      5*a     5*a   5*a     5*a                           
       / \  =      +    /  +    \  +  / \                          
      b   c            b         c   b   c                         
            +                                                      
      3*a      3*a     3*a                   3*a     3*a
       / \  =      +    /                  +    \  +  / \
      b   d            b                         d   b   d
     ------------------------------------------------------
               8*a     8*a   5*a     5*a     3*a     3*a
            =      +    /  +    \  +  / \  +    \  +  / \
                       b         c   b   c       d   b   d

Nothing really new there :


```clojure
(ns net.ftod.zcube-test
  ( :use clojure.test )
  ( :require [ net.ftod.zcube :as z ] )
)

( deftest test-sum-3 ; Branching trees example
  ( is
    ( let [ zn ( z/sum-subtrees
               [ [ 5 ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ] 
               , [ 3 ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ]
               ] ) ]
      ( and
        ( =  8 ( ( z/count-trees ( z/path "a" )                                    ) zn ) )
        ( =  8 ( ( z/count-trees ( z/path "a" "b" )                                ) zn ) )
        ( =  5 ( ( z/count-trees ( z/path "a" "c" )                                ) zn ) )
        ( =  5 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "c" ) ) ) zn ) )
        ( =  3 ( ( z/count-trees ( z/path "a" "d" )                                ) zn ) )
        ( =  3 ( ( z/count-trees ( z/cross ( z/path "a" "b" ) ( z/path "a" "d" ) ) ) zn ) )
      ) ) ) )
```

## Example 2 : What for ? Analytics !

Now, why in the world would you want to do such a thing, decomposing trees into subtrees, and counting their occurrences ?

Well, suppose you want to perform some _analytics_ on a clickstream, where each event in the stream, besides the url, gives you data about the demographics of the user, and the time of click.

You can model such events as trees, for example, using an _informal_ algebraic notation to denote trees :

      male   user on page1 the 1st of january 2014 at 1OH32 ~ www.company.com/page1+gender/male+2014/01/01/10/32
      female user on page2 the 2nd of january 2014 at 11H15 ~ www.company.com/page2+gender/female+2014/01/02/11/15
      female user on page1 the 3rd of january 2014 at 08H15 ~ www.company.com/page1+gender/female+2014/01/03/08/15

Now, computing the subtrees generated by these, and summing, you get the following terms :

      2*(www.company.com+2014/01) ~ 2 clicks on the domain www.company.com in January 2014
      2*(www.company.com+2014+gender/female) ~ 2 clicks on the domain www.company.com in January 2014 by female users
      
ie computing the subtree decomposition is tantamount to performing _multidimensional aggregate sums_.

This translates as follows using the zcube API :

```clojure
(ns net.ftod.zcube-test
  ( :use net.ftod.zcube clojure.test )
)

( deftest test-analytics ; Analytics example
  ( is
    ( let [ zn ( z/sum-subtrees
                 [ [ 1 ( z/cross
                         ( z/path "www.company.com" "page1" )
                         ( z/path "gender" "male" )
                         ( z/path "2014" "01" "01" "10" "32" ) ) ]
                 , [ 1 ( z/cross
                         ( z/path "www.company.com" "page2" )
                         ( z/path "gender" "female" )
                         ( z/path "2014" "01" "02" "11" "35" ) ) ]
                 , [ 1 ( z/cross
                         ( z/path "www.company.com" "page1" )
                         ( z/path "gender" "female" )
                         ( z/path "2014" "01" "03" "08" "15" ) ) ]
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
```

## The Tree API

The _tree API_ handles the construction of __sets__ of trees. In the previous sections, we have glossed over this subtlety, happlily confusing trees with singleton sets of trees.  

|Name|Description|
|----|-----------|
|top   |The _singleton_ set containing only the _empty_ tree.|
|bot   |The _empty_ set of trees.|
|path  |The _singleton_ set containing a _path_, ie a linear tree.|
|prefix|Prepending a path segment to all trees in a set, yielding a new set.|
|cross |Cross-product of two sets of trees, taking the union of both trees in each product pair.|
|sum   |Union of two sets of trees.|

The usual pattern for constructing (sets of) trees is as follows, combining _paths_ with the _cross_ operator :

```clojure
( cross
  ( path "www.company.com" "page1" )
  ( path "gender" "male" )
  ( path "2014" "01" "01" "10" "32" )
)
```

The _sum_ construct is useful to model overlapping hierarchical dimensions.
For example, representing dates both as year/month/day-of-month, and year/week/day-of-week :
 
```clojure
( cross
  ( path "www.company.com" "page1" )
  ( path "gender" "male" )
  ( sum ( path "ymd" "2014" "01" "01" "10" "32" )
        ( path "ywd" "2014" "01" "1" )
  )
)
```

This trick relies on the distributivity of _cross_ over _sum_...  

### The Tree Algebra

A few algebraic identities hold :

    path(a,b,c,...) = prefix(a,prefix(b,prefix(c,... top)))
    sum(a,bot) = a
    sum(a,b) = sum(b,a)
    sum(a,sum(b,c)) = sum(sum(a,b),c)
    cross(a,top) = a
    cross(a,b) = cross(b,a)
    cross(a,cross(b,c)) = cross(cross(a,b),c)
    cross(sum(a,b),c) = sum(cross(a,c),cross(b,c))
    prefix(x,cross(a,b,c,...)) = cross(prefix(x,a),prefix(x,b),prefix(x,c),...)
    prefix(x,sum(a,b,c,...)) = sum(prefix(x,a),prefix(x,b),prefix(x,c),...)

## The Associative/Commutative API

### Basic API 

|Expression      |Description|
|----------------|-----------|
|nil             |The _ZDDNumber_ zero.|
|( subtrees l t )|Linear combination of _l_ times the subtrees of the tree _t_.|
|( add z1 z2 )   |Sum of _ZDDNumbers_ _z1_, _z2_.                              |
|( sub z1 z2 )   |Difference of _ZDDNumbers_ _z1_, _z2_.                       |

_add_ is _associative_ and _commutative_, and thus lends itself well to the concurrent execution of aggregation operations.   

### Filtering

The expansion of trees into their subtrees entails exponential complexity.
When counting subtrees for trees with numerous or deep branches, one may want to restrict the set of subtrees before aggregating.
The arity 1 variant of the _subtrees_ function is an higher-order function that takes as parameter a set of trees acting as a filter.

Thus :

```clojure
( ( subtrees filter ) l tree ) 
```

Will generate _l_ occurrences of all the subtrees of _tree_, that are also in the set of trees expressed by _filter_. 

## The Accumulative API

The accumulative API conflates into a single operation the computation of the subtrees of a tree, and adding them into a _ZDDNumber_.
This allows these otherwise separate computations to share internal caches.
The caches are allocated less often, and the sharing hopefully results in more cache hits.

### Basic API

|Expression      |Description                                              |
|----------------|---------------------------------------------------------|
|nil             |The _ZDDNumber_ zero.                                    |
|( add-subtrees l t z )|Add _l_ occurrences of the subtrees of the tree _t_ to the _ZDDNumber_ _z_.|
|( sum-subtrees lts )|Reduce a sequence of pairs of longs and trees, adding up the occurrences of their subtrees into a _ZDDNumber_.[

_sum-subtrees_ is a simple convenience function :

```clojure
( defn sum-subtrees [ long-trees ]
  ( reduce
    ( fn [ zn [ long tree ] ] ( add-subtrees long tree zn ) )
    nil 
    long-trees
  )
)
```


### Filtering

Again, filtering against a set of trees is taken care of by the following higher-order function :

```clojure
( ( add-subtrees filter ) l tree znumber ) 
```

Will add to _znumber_ _l_ occurrences of the subtrees of _tree_, that are also in _filter_.

## Counting subtrees

Eventually, counting occurrences of trees in a _ZDDNumber_ is done with the _count-trees_ higher-order function :

```clojure
( ( count-trees tree ) znumber ) 
```

When _tree_ is a singleton holding one tree, the result is the number of occurrences of that tree in _znumber_.

# Design and Implementation

The data structures are immutable variants of _ZDD_ (zero-suppressed binary decision diagrams) and numerical representations based on on _ZDD_, taken from the work of pr. _Shin-Ichi Minato_. _ZDD_ offer a compressed representation of sets of sets as found in combinatorial problems, that usually suffer from exponential size explosion.

In the [VSOP Calculator][2] paper, _Minato et al_ explain how _ZDD_, and forests of shared _ZDD_ arranged in lists provide for an efficient representation of linear combinations of sets. We have adapted their representational trick to an _immutable_ settings.

The bulk of the library is written in Java, around hopefully efficient _immutable_ data structures. A thin Clojure layer provides for the public API of the library.

* A **ZDDNumber** represents a linear combination of sets of trees with integer coefficients.
* A **ZDDTree** is a symbolic expression that represents a sets of trees.
* A **ZDD** is a symbolic decision-diagram based representation of a set of trees.

The _ZDD_ type is not exposed by the public Clojure API.

The overall algorithmic organization is as follows :

* A _ZDDTree_ represents a set of trees the branch of which are labelled by strings.
* A hashing scheme based on the _djb2_ hash functions transforms these labelled trees into trees with 64 bits integer nodes.
* A tree can be represented as the set of its integer nodes.
* The subtrees of a tree can be represented as a set of sets of integers, ie a _ZDD_.
* A list of shared _ZDD_ then represents occurrences of trees.        

# Future Work

* Use multiple hash functions to reduce collisions probability. Again, [Add ALL The Things][1] explains the idea neatly.   [Bloom Filters][4] are based on that technique, too.
* More operations...
  * Max and min.
  * Multiplication and division.

# Resources

* [Add ALL the Things: Abstract Algebra Meets Analytics][1]
* [VSOP Calculator based on Zero-Suppressed Binary Decision Diagrams][2]
* [Fun with ZDDs: Notes from Knuth's 14th Annual Christmas Tree Lecture][3]
* [Bloom Filters][4]
* [Which hashing algorithm is best for uniqueness and speed][5]

[1]: http://www.infoq.com/presentations/abstract-algebra-analytics
[2]: https://github.com/ftod/zcube/blob/master/papers/VSOP%20(Valued-Sum-Of-Products)%20Calculator%20Based%20on%20Zero-Suppressed%20BDDs.pdf?raw=true
[3]: http://ashutoshmehra.net/blog/2008/12/notes-on-zdds/
[4]: http://en.wikipedia.org/wiki/Bloom_filters
[5]: http://programmers.stackexchange.com/questions/49550/which-hashing-algorithm-is-best-for-uniqueness-and-speed

License
=======

The use and distribution terms for this software are covered by the Eclipse Public
License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.  You must not remove this notice, or any
other, from this software.
