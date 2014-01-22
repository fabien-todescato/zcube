zcube - Counting trees for fun and profit
=========================================

_zcube_ is about counting trees, and aggregating the counts over the _subtrees_ of these trees.


# About counting subtrees

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

# Counting subtrees, what for ?

Now, why in the world would you want to do such a thing, decomposing trees into subtrees, and counting their occurrences ?
