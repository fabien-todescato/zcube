package net.ftod.zcube.zdd;

/**
 * <h1>Zero-Suppressed Binary Decision Diagrams</h1>
 * 
 * <p>
 * Representing sets of sets of <code>long</code> with <em>ZDD</em>.
 * </p>
 * <p>
 * In that implementation, we have given up the standard canonicalizing map, a bottleneck for concurrency. The {@link ZDD} operations are implemented as static
 * methods taking as arguments the caches alleviating the occurrences of repeated computation on shared sub {@link ZDD}.
 * </p>
 * 
 * @author <a href="mailto:fabien.todescato@gmail.com">Fabien Todescato</a>
 */
public final class ZDD {

    static final int CACHE_POWER = 5;
    static final int CACHE_SIZE = 1 << CACHE_POWER;
    static final int CACHE_MAX = CACHE_SIZE - 1;

    /**
     * The empty set.
     */
    public static final ZDD BOT = new ZDD(0L, null, null, 1);
    /**
     * The singleton set holding the empty set.
     */
    public static final ZDD TOP = new ZDD(0L, null, null, 2);

    public final long x;
    public final ZDD b;
    public final ZDD t;
    public final int h;

    private ZDD(final long x, final ZDD b, final ZDD t, final int h) {
        super();
        this.x = x;
        this.b = b;
        this.t = t;
        this.h = h;
    }

    private ZDD(final long x, final ZDD b, final ZDD t) {
        this(x, b, t, hash(x, b, t));
    }

    private static int hash(final long x, final ZDD b, final ZDD t)
    {
        int result = 1;
        result = 31 * result + Long.valueOf(x).hashCode();
        result = 31 * result + b.h;
        result = 31 * result + t.h;
        return result;
    }

    private static ZDD zdd(final ZDDCacheNode nod, final long x, final ZDD b, final ZDD t)
    {
        if (t == BOT) {
            return b;
        }

        ZDD z = nod.get(x, b, t);

        if (z == null) {
            z = new ZDD(x, b, t);
            nod.put(x, b, t, z);
        }

        return z;
    }

    public static long size(final ZDD z)
    {
        return size(new ZDDCacheLong(), z);
    }

    static long size(final ZDDCacheLong _cl, final ZDD z)
    {
        if (z == BOT) {
            return 0L;
        }
        if (z == TOP) {
            return 1L;
        }

        final Long cached = _cl.get(z);

        if (cached != null) {
            return cached.longValue();
        }

        final long s = size(_cl, z.b) + size(_cl, z.t);

        _cl.put(z, s);

        return s;
    }

    /**
     * <h3>Build a singleton set</h3>
     * 
     * @param x
     *            the <code>long</code> element of the singleton set.
     * @return the {@link ZDD} representing the singleton {x}.
     */
    public static ZDD singleton(final long x)
    {
        return new ZDD(x, BOT, TOP);
    }

    /**
     * <h3>Build a set of elements</h3>
     * 
     * @param xs
     *            a sequence of <code>long</code>.
     * @return the {@link ZDD} representing the set of <code>long</code> in the sequence <code>xs</code>.
     */
    public static ZDD set(final long... xs)
    {
        return set(new ZDDCachePredicate(), new ZDDCacheOperation(), new ZDDCacheOperation(), xs);
    }

    /**
     * <h3>Set inclusion predicate</h3>
     * 
     * @param zdd1
     *            the {@link ZDD} included.
     * @param zdd2
     *            the {@link ZDD} including.
     * @return <code>true</code> iff <b>all</b> sets in <code>zdd1</code> are in <code>zdd2</code>.
     */
    public static boolean included(final ZDD zdd1, final ZDD zdd2)
    {
        return included(new ZDDCachePredicate(), new ZDDCachePredicate(), zdd1, zdd2);
    }

    static boolean included(final ZDDCachePredicate eq, final ZDDCachePredicate in, final ZDD zdd1, final ZDD zdd2)
    {
        if (equals(eq, zdd1, zdd2)) {
            return true;
        }
        if (zdd1 == BOT) {
            return true;
        }
        if (zdd1 == TOP) {
            return topIncluded(zdd2);
        }
        if (zdd2 == BOT) {
            return false;
        }
        if (zdd2 == TOP) {
            return false;
        }

        final Boolean cached = in.get(zdd1, zdd2);

        if (cached != null) {
            return cached.booleanValue();
        }

        final boolean included;

        final long x1 = zdd1.x;
        final long x2 = zdd2.x;

        if (x1 < x2) {
            included = false;
        } else if (x1 > x2) {
            included = included(eq, in, zdd1, zdd2.b);
        } else {
            included = included(eq, in, zdd1.b, zdd2.b) && included(eq, in, zdd1.t, zdd2.t);
        }

        in.put(zdd1, zdd2, included);

        return included;
    }

    private static boolean topIncluded(final ZDD zdd)
    {
        if (zdd == BOT) {
            return false;
        }

        if (zdd == TOP) {
            return true;
        }

        return topIncluded(zdd.b);
    }

    /**
     * <h3>Set union</h3>
     * 
     * @param zdd1
     *            a {@link ZDD}
     * @param zdd2
     *            a {@link ZDD}
     * @return the {@link ZDD} union of zdd1 and zdd2 ie the set of sets that are in zdd1 or zdd2.
     */
    public static ZDD union(final ZDD zdd1, final ZDD zdd2)
    {
        return union(new ZDDCacheNode(), new ZDDCachePredicate(), new ZDDCacheOperation(), zdd1, zdd2);
    }

    /**
     * <h3>Set union</h3>
     * 
     * @param zdds
     *            a sequence of {@link ZDD}.
     * @return the {@link ZDD} union of the {@link ZDD} in the sequence.
     */
    public static ZDD union(final ZDD... zdds)
    {
        return union(new ZDDCacheNode(), new ZDDCachePredicate(), new ZDDCacheOperation(), zdds);
    }

    /**
     * <h3>Equality predicate</h3>
     * 
     * @param zdd1
     * @param zdd2
     * @return <code>true</code> if zdd1 and zdd2 represent the same set of sets.
     */
    public static boolean equals(final ZDD zdd1, final ZDD zdd2)
    {
        return equals(new ZDDCachePredicate(), zdd1, zdd2);
    }

    @Override
    public int hashCode()
    {
        return h;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj != null && obj instanceof ZDD && equals(this, (ZDD) obj);
    }

    @Override
    public String toString()
    {
        if (this == BOT) {
            return "BOT";
        }

        if (this == TOP) {
            return "TOP";
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("ZDD [x=");
        builder.append(x);
        builder.append(", ");
        if (b != null) {
            builder.append("b=");
            builder.append(b);
            builder.append(", ");
        }
        if (t != null) {
            builder.append("t=");
            builder.append(t);
            builder.append(", ");
        }
        builder.append("h=");
        builder.append(h);
        builder.append("]");
        return builder.toString();
    }

    static ZDD union(final ZDDCacheNode nod, final ZDDCachePredicate eq, final ZDDCacheOperation un, final ZDD... zdds)
    {
        return union(nod, eq, un, 0, zdds.length, zdds);
    }

    static ZDD union(final ZDDCacheNode nod, final ZDDCachePredicate eq, final ZDDCacheOperation un, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return union(nod, eq, un, union(nod, eq, un, begin, middle, zdda), union(nod, eq, un, middle, end, zdda));
        }
        if (length > 1) {
            return union(nod, eq, un, zdda[begin], zdda[begin + 1]);
        }
        if (length > 0) {
            return zdda[begin];
        }

        return BOT;
    }

    static ZDD union(final ZDDCacheNode nod, final ZDDCachePredicate eq, final ZDDCacheOperation un, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return zdd2;
        }

        if (zdd2 == BOT) {
            return zdd1;
        }

        if (equals(eq, zdd1, zdd2)) {
            return zdd1;
        }

        ZDD zdd = un.get(zdd1, zdd2);

        if (zdd == null) {
            if (zdd1 == TOP) {
                zdd = unionTop(un, zdd2);
            } else if (zdd2 == TOP) {
                zdd = unionTop(un, zdd1);
            } else {

                final long x1 = zdd1.x;
                final long x2 = zdd2.x;

                if (x1 < x2) {
                    zdd = zdd(nod, x1, union(nod, eq, un, zdd1.b, zdd2), zdd1.t);
                } else if (x1 > x2) {
                    zdd = zdd(nod, x2, union(nod, eq, un, zdd1, zdd2.b), zdd2.t);
                } else {
                    zdd = zdd(nod, x1, union(nod, eq, un, zdd1.b, zdd2.b), union(nod, eq, un, zdd1.t, zdd2.t));
                }
            }

            un.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD unionTop(final ZDDCacheOperation un, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return TOP;
        }

        if (zdd1 == TOP) {
            return TOP;
        }

        ZDD zdd = un.get(TOP, zdd1);

        if (zdd == null) {
            zdd = zdd(new ZDDCacheNode(), zdd1.x, unionTop(un, zdd1.b), zdd1.t);
            un.put(TOP, zdd1, zdd);
        }

        return zdd;
    }

    static ZDD intersection(final ZDDCacheNode nod, final ZDDCachePredicate eq, final ZDDCacheOperation in, final ZDD... zdds)
    {
        return intersection(nod, eq, in, 0, zdds.length, zdds);
    }

    static ZDD intersection(final ZDDCacheNode nod, final ZDDCachePredicate eq, final ZDDCacheOperation in, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return intersection(nod, eq, in, intersection(nod, eq, in, begin, middle, zdda), intersection(nod, eq, in, middle, end, zdda));
        } else if (length > 1) {
            return intersection(nod, eq, in, zdda[begin], zdda[begin + 1]);
        } else if (length > 0) {
            return zdda[begin];
        }

        return BOT;
    }

    static ZDD intersection(final ZDDCacheNode nod, final ZDDCachePredicate eq, final ZDDCacheOperation in, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd2 == BOT) {
            return BOT;
        }

        if (equals(eq, zdd1, zdd2)) {
            return zdd1;
        }

        ZDD zdd = in.get(zdd1, zdd2);

        if (zdd == null) {
            if (zdd1 == TOP) {
                zdd = intersectionTop(in, zdd2);
            } else if (zdd2 == TOP) {
                zdd = intersectionTop(in, zdd1);
            } else {

                final long x1 = zdd1.x;
                final long x2 = zdd2.x;

                if (x1 < x2) {
                    zdd = intersection(nod, eq, in, zdd1.b, zdd2);
                } else if (x1 > x2) {
                    zdd = intersection(nod, eq, in, zdd1, zdd2.b);
                } else {
                    zdd = zdd(nod, x1, intersection(nod, eq, in, zdd1.b, zdd2.b), intersection(nod, eq, in, zdd1.t, zdd2.t));
                }
            }

            in.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD intersectionTop(final ZDDCacheOperation in, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd1 == TOP) {
            return TOP;
        }

        ZDD zdd = in.get(TOP, zdd1);

        if (zdd == null) {
            zdd = intersectionTop(in, zdd1.b);
            in.put(TOP, zdd1, zdd);
        }

        return zdd;
    }

    static ZDD difference(final ZDDCachePredicate eq, final ZDDCacheOperation di, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd2 == BOT) {
            return zdd1;
        }

        if (equals(eq, zdd1, zdd2)) {
            return BOT;
        }

        ZDD zdd = di.get(zdd1, zdd2);

        if (zdd == null) {

            if (zdd1 == TOP) {
                zdd = topDifference(di, zdd2);
            } else if (zdd2 == TOP) {
                zdd = differenceTop(di, zdd1);
            } else {

                final long x1 = zdd1.x;
                final long x2 = zdd2.x;

                if (x1 < x2) {
                    zdd = zdd(new ZDDCacheNode(), x1, difference(eq, di, zdd1.b, zdd2), zdd1.t);
                } else if (x1 > x2) {
                    zdd = difference(eq, di, zdd1, zdd2.b);
                } else {
                    zdd = zdd(new ZDDCacheNode(), x1, difference(eq, di, zdd1.b, zdd2.b), difference(eq, di, zdd1.t, zdd2.t));
                }
            }

            di.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD differenceTop(final ZDDCacheOperation di, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd1 == TOP) {
            return BOT;
        }

        ZDD zdd = di.get(zdd1, TOP);

        if (zdd == null) {
            zdd = zdd(new ZDDCacheNode(), zdd1.x, differenceTop(di, zdd1.b), zdd1.t);
            di.put(zdd1, TOP, zdd);
        }

        return zdd;
    }

    private static ZDD topDifference(final ZDDCacheOperation di, final ZDD zdd2)
    {
        if (zdd2 == BOT) {
            return TOP;
        }

        if (zdd2 == TOP) {
            return BOT;
        }

        ZDD zdd = di.get(TOP, zdd2);

        if (zdd == null) {
            zdd = topDifference(di, zdd2.b);
            di.put(TOP, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD crossUnion(final ZDDCachePredicate eq, final ZDDCacheOperation cu, final ZDDCacheOperation un, final ZDD... zdds)
    {
        return crossUnion(eq, cu, un, 0, zdds.length, zdds);
    }

    static ZDD crossUnion(final ZDDCachePredicate eq, final ZDDCacheOperation cu, final ZDDCacheOperation un, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return crossUnion(eq, cu, un, crossUnion(eq, cu, un, begin, middle, zdda), crossUnion(eq, cu, un, middle, end, zdda));
        } else if (length > 1) {
            return crossUnion(eq, cu, un, zdda[begin], zdda[begin + 1]);
        } else if (length > 0) {
            return zdda[begin];
        }

        return TOP;
    }

    static ZDD crossUnion(final ZDDCachePredicate eq, final ZDDCacheOperation cu, final ZDDCacheOperation un, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd2 == BOT) {
            return BOT;
        }

        if (zdd1 == TOP) {
            return zdd2;
        }

        if (zdd2 == TOP) {
            return zdd1;
        }

        ZDD zdd = cu.get(zdd1, zdd2);

        if (zdd == null) {

            final long x1 = zdd1.x;
            final long x2 = zdd2.x;

            if (x1 < x2) {
                zdd = zdd(new ZDDCacheNode(), x1, crossUnion(eq, cu, un, zdd1.b, zdd2), crossUnion(eq, cu, un, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = zdd(new ZDDCacheNode(), x2, crossUnion(eq, cu, un, zdd1, zdd2.b), crossUnion(eq, cu, un, zdd1, zdd2.t));
            } else {
                zdd = zdd(new ZDDCacheNode(), x1, crossUnion(eq, cu, un, zdd1.b, zdd2.b), union(new ZDDCacheNode(), eq, un, crossUnion(eq, cu, un, zdd1.t, zdd2.t), union(new ZDDCacheNode(), eq, un, crossUnion(eq, cu, un, zdd1.t, zdd2.b), crossUnion(eq,
                        cu, un, zdd1.b, zdd2.t))));
            }

            cu.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD set(final ZDDCachePredicate eq, final ZDDCacheOperation cu, final ZDDCacheOperation un, final long... xs)
    {
        final ZDD[] zdd = new ZDD[xs.length];

        for (int i = 0; i < xs.length; ++i) {
            zdd[i] = singleton(xs[i]);
        }

        return crossUnion(eq, cu, un, zdd);
    }

    static ZDD crossIntersection(final ZDDCachePredicate eq, final ZDDCacheOperation ci, final ZDDCacheOperation un, final ZDD... zdds)
    {
        return crossIntersection(eq, ci, un, 0, zdds.length, zdds);
    }

    static ZDD crossIntersection(final ZDDCachePredicate eq, final ZDDCacheOperation ci, final ZDDCacheOperation un, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return crossIntersection(eq, ci, un, crossIntersection(eq, ci, un, begin, middle, zdda), crossIntersection(eq, ci, un, middle, end, zdda));
        } else if (length > 1) {
            return crossIntersection(eq, ci, un, zdda[begin], zdda[begin + 1]);
        } else if (length > 0) {
            return zdda[begin];
        }

        return TOP;
    }

    static ZDD crossIntersection(final ZDDCachePredicate eq, final ZDDCacheOperation ci, final ZDDCacheOperation un, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd2 == BOT) {
            return BOT;
        }

        if (zdd1 == TOP) {
            return TOP;
        }

        if (zdd2 == TOP) {
            return TOP;
        }

        ZDD zdd = ci.get(zdd1, zdd2);

        if (zdd == null) {

            final long x1 = zdd1.x;
            final long x2 = zdd2.x;

            if (x1 < x2) {
                zdd = union(new ZDDCacheNode(), eq, un, crossIntersection(eq, ci, un, zdd1.b, zdd2), crossIntersection(eq, ci, un, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = union(new ZDDCacheNode(), eq, un, crossIntersection(eq, ci, un, zdd1, zdd2.b), crossIntersection(eq, ci, un, zdd1, zdd2.t));
            } else {
                zdd = zdd(new ZDDCacheNode(), x1, union(new ZDDCacheNode(), eq, un, crossIntersection(eq, ci, un, zdd1.b, zdd2.b), union(new ZDDCacheNode(), eq, un, crossIntersection(eq, ci, un, zdd1.b, zdd2.t), crossIntersection(eq, ci, un, zdd1.t,
                        zdd2.b))), crossIntersection(eq, ci, un, zdd1.t, zdd2.t));
            }

            ci.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD crossDifference(final ZDDCachePredicate eq, final ZDDCacheOperation cd, final ZDDCacheOperation un, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd2 == BOT) {
            return BOT;
        }

        if (zdd1 == TOP) {
            return TOP;
        }

        if (zdd2 == TOP) {
            return zdd1;
        }

        ZDD zdd = cd.get(zdd1, zdd2);

        if (zdd == null) {

            final long x1 = zdd1.x;
            final long x2 = zdd2.x;

            if (x1 < x2) {
                zdd = zdd(new ZDDCacheNode(), x1, crossDifference(eq, cd, un, zdd1.b, zdd2), crossDifference(eq, cd, un, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = union(new ZDDCacheNode(), eq, un, crossDifference(eq, cd, un, zdd1, zdd2.b), crossDifference(eq, cd, un, zdd1, zdd2.t));
            } else {
                zdd = zdd(new ZDDCacheNode(), x1, union(new ZDDCacheNode(), eq, un, crossDifference(eq, cd, un, zdd1.b, zdd2.b), crossDifference(eq, cd, un, zdd1.b, zdd2.t), crossDifference(eq, cd, un, zdd1.t, zdd2.t)), crossDifference(eq, cd, un, zdd1.t,
                        zdd2.b));
            }

            cd.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static boolean equals(final ZDDCachePredicate eq, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == zdd2) {
            return true;
        }

        if (zdd1.h != zdd2.h) {
            return false;
        }

        final Boolean cached = eq.get(zdd1, zdd2);

        if (cached != null) {
            return cached.booleanValue();
        }

        boolean equal;

        if (zdd1.x != zdd2.x) {
            equal = false;
        } else if (!equals(eq, zdd1.b, zdd2.b)) {
            equal = false;
        } else if (!equals(eq, zdd1.t, zdd2.t)) {
            equal = false;
        } else {
            equal = true;
        }

        eq.put(zdd1, zdd2, equal);

        return equal;
    }

}
