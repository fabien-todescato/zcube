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
// TODO Standardize cache names.
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
        result = 31 * result + hash(x);
        result = 31 * result + b.h;
        result = 31 * result + t.h;
        return result;
    }

    private static int hash(final long l)
    {
        return (int) (l ^ l >>> 32);
    }

    private static ZDD zdd(final ZDDCacheN nod, final long x, final ZDD b, final ZDD t)
    {
        if (t == BOT) {
            return b;
        }

        final int h = hash(x, b, t);

        ZDD z = nod.get(h, x, b, t);

        if (z == null) {
            z = new ZDD(x, b, t, h);
            nod.put(h, x, b, t, z);
        }

        return z;
    }

    public static long size(final ZDD z)
    {
        return size(new ZDDCacheL(), z);
    }

    static long size(final ZDDCacheL _cl, final ZDD z)
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
        return singleton(new ZDDCacheN(), x);
    }

    static ZDD singleton(final ZDDCacheN nod, final long x)
    {
        return zdd(nod, x, BOT, TOP);
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
        return set(new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), new ZDDCacheO(), xs);
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
        return included(new ZDDCacheP(), new ZDDCacheP(), zdd1, zdd2);
    }

    static boolean included(final ZDDCacheP equ, final ZDDCacheP in, final ZDD zdd1, final ZDD zdd2)
    {
        if (equals(equ, zdd1, zdd2)) {
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
            included = included(equ, in, zdd1, zdd2.b);
        } else {
            included = included(equ, in, zdd1.b, zdd2.b) && included(equ, in, zdd1.t, zdd2.t);
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
        return union(new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), zdd1, zdd2);
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
        return union(new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), zdds);
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
        return equals(new ZDDCacheP(), zdd1, zdd2);
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

    static ZDD union(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO uni, final ZDD... zdds)
    {
        return union(nod, equ, uni, 0, zdds.length, zdds);
    }

    static ZDD union(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO uni, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return union(nod, equ, uni, union(nod, equ, uni, begin, middle, zdda), union(nod, equ, uni, middle, end, zdda));
        }
        if (length > 1) {
            return union(nod, equ, uni, zdda[begin], zdda[begin + 1]);
        }
        if (length > 0) {
            return zdda[begin];
        }

        return BOT;
    }

    static ZDD union(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO uni, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return zdd2;
        }

        if (zdd2 == BOT) {
            return zdd1;
        }

        if (equals(equ, zdd1, zdd2)) {
            return zdd1;
        }

        ZDD zdd = uni.get(zdd1, zdd2);

        if (zdd == null) {
            if (zdd1 == TOP) {
                zdd = unionTop(nod, uni, zdd2);
            } else if (zdd2 == TOP) {
                zdd = unionTop(nod, uni, zdd1);
            } else {

                final long x1 = zdd1.x;
                final long x2 = zdd2.x;

                if (x1 < x2) {
                    zdd = zdd(nod, x1, union(nod, equ, uni, zdd1.b, zdd2), zdd1.t);
                } else if (x1 > x2) {
                    zdd = zdd(nod, x2, union(nod, equ, uni, zdd1, zdd2.b), zdd2.t);
                } else {
                    zdd = zdd(nod, x1, union(nod, equ, uni, zdd1.b, zdd2.b), union(nod, equ, uni, zdd1.t, zdd2.t));
                }
            }

            uni.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD unionTop(final ZDDCacheN nod, final ZDDCacheO uni, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return TOP;
        }

        if (zdd1 == TOP) {
            return TOP;
        }

        ZDD zdd = uni.get(TOP, zdd1);

        if (zdd == null) {
            zdd = zdd(nod, zdd1.x, unionTop(nod, uni, zdd1.b), zdd1.t);
            uni.put(TOP, zdd1, zdd);
        }

        return zdd;
    }

    static ZDD intersection(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO in, final ZDD... zdds)
    {
        return intersection(nod, equ, in, 0, zdds.length, zdds);
    }

    static ZDD intersection(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO in, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return intersection(nod, equ, in, intersection(nod, equ, in, begin, middle, zdda), intersection(nod, equ, in, middle, end, zdda));
        } else if (length > 1) {
            return intersection(nod, equ, in, zdda[begin], zdda[begin + 1]);
        } else if (length > 0) {
            return zdda[begin];
        }

        return BOT;
    }

    static ZDD intersection(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO in, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd2 == BOT) {
            return BOT;
        }

        if (equals(equ, zdd1, zdd2)) {
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
                    zdd = intersection(nod, equ, in, zdd1.b, zdd2);
                } else if (x1 > x2) {
                    zdd = intersection(nod, equ, in, zdd1, zdd2.b);
                } else {
                    zdd = zdd(nod, x1, intersection(nod, equ, in, zdd1.b, zdd2.b), intersection(nod, equ, in, zdd1.t, zdd2.t));
                }
            }

            in.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD intersectionTop(final ZDDCacheO in, final ZDD zdd1)
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

    static ZDD difference(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO di, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd2 == BOT) {
            return zdd1;
        }

        if (equals(equ, zdd1, zdd2)) {
            return BOT;
        }

        ZDD zdd = di.get(zdd1, zdd2);

        if (zdd == null) {

            if (zdd1 == TOP) {
                zdd = topDifference(di, zdd2);
            } else if (zdd2 == TOP) {
                zdd = differenceTop(nod, di, zdd1);
            } else {

                final long x1 = zdd1.x;
                final long x2 = zdd2.x;

                if (x1 < x2) {
                    zdd = zdd(nod, x1, difference(nod, equ, di, zdd1.b, zdd2), zdd1.t);
                } else if (x1 > x2) {
                    zdd = difference(nod, equ, di, zdd1, zdd2.b);
                } else {
                    zdd = zdd(nod, x1, difference(nod, equ, di, zdd1.b, zdd2.b), difference(nod, equ, di, zdd1.t, zdd2.t));
                }
            }

            di.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD differenceTop(final ZDDCacheN nod, final ZDDCacheO di, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd1 == TOP) {
            return BOT;
        }

        ZDD zdd = di.get(zdd1, TOP);

        if (zdd == null) {
            zdd = zdd(nod, zdd1.x, differenceTop(nod, di, zdd1.b), zdd1.t);
            di.put(zdd1, TOP, zdd);
        }

        return zdd;
    }

    private static ZDD topDifference(final ZDDCacheO di, final ZDD zdd2)
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

    static ZDD crossUnion(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO cru, final ZDDCacheO uni, final ZDD... zdds)
    {
        return crossUnion(nod, equ, cru, uni, 0, zdds.length, zdds);
    }

    static ZDD crossUnion(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO cru, final ZDDCacheO uni, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return crossUnion(nod, equ, cru, uni, crossUnion(nod, equ, cru, uni, begin, middle, zdda), crossUnion(nod, equ, cru, uni, middle, end, zdda));
        } else if (length > 1) {
            return crossUnion(nod, equ, cru, uni, zdda[begin], zdda[begin + 1]);
        } else if (length > 0) {
            return zdda[begin];
        }

        return TOP;
    }

    static ZDD crossUnion(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO cru, final ZDDCacheO uni, final ZDD zdd1, final ZDD zdd2)
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

        ZDD zdd = cru.get(zdd1, zdd2);

        if (zdd == null) {

            final long x1 = zdd1.x;
            final long x2 = zdd2.x;

            if (x1 < x2) {
                zdd = zdd(nod, x1, crossUnion(nod, equ, cru, uni, zdd1.b, zdd2), crossUnion(nod, equ, cru, uni, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = zdd(nod, x2, crossUnion(nod, equ, cru, uni, zdd1, zdd2.b), crossUnion(nod, equ, cru, uni, zdd1, zdd2.t));
            } else {
                zdd = zdd(nod, x1, crossUnion(nod, equ, cru, uni, zdd1.b, zdd2.b), union(nod, equ, uni, crossUnion(nod, equ, cru, uni, zdd1.t, zdd2.t), union(nod, equ, uni, crossUnion(nod, equ, cru, uni, zdd1.t, zdd2.b), crossUnion(nod, equ, cru, uni,
                        zdd1.b, zdd2.t))));
            }

            cru.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD set(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO cru, final ZDDCacheO uni, final long... xs)
    {
        final ZDD[] zdd = new ZDD[xs.length];

        for (int i = 0; i < xs.length; ++i) {
            zdd[i] = singleton(nod, xs[i]);
        }

        return crossUnion(nod, equ, cru, uni, zdd);
    }

    static ZDD crossIntersection(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO cri, final ZDDCacheO uni, final ZDD... zdds)
    {
        return crossIntersection(nod, equ, cri, uni, 0, zdds.length, zdds);
    }

    static ZDD crossIntersection(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO cri, final ZDDCacheO uni, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return crossIntersection(nod, equ, cri, uni, crossIntersection(nod, equ, cri, uni, begin, middle, zdda), crossIntersection(nod, equ, cri, uni, middle, end, zdda));
        } else if (length > 1) {
            return crossIntersection(nod, equ, cri, uni, zdda[begin], zdda[begin + 1]);
        } else if (length > 0) {
            return zdda[begin];
        }

        return TOP;
    }

    static ZDD crossIntersection(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO cri, final ZDDCacheO uni, final ZDD zdd1, final ZDD zdd2)
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

        ZDD zdd = cri.get(zdd1, zdd2);

        if (zdd == null) {

            final long x1 = zdd1.x;
            final long x2 = zdd2.x;

            if (x1 < x2) {
                zdd = union(nod, equ, uni, crossIntersection(nod, equ, cri, uni, zdd1.b, zdd2), crossIntersection(nod, equ, cri, uni, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = union(nod, equ, uni, crossIntersection(nod, equ, cri, uni, zdd1, zdd2.b), crossIntersection(nod, equ, cri, uni, zdd1, zdd2.t));
            } else {
                zdd = zdd(nod, x1, union(nod, equ, uni, crossIntersection(nod, equ, cri, uni, zdd1.b, zdd2.b), union(nod, equ, uni, crossIntersection(nod, equ, cri, uni, zdd1.b, zdd2.t), crossIntersection(nod, equ, cri, uni, zdd1.t, zdd2.b))),
                        crossIntersection(nod, equ, cri, uni, zdd1.t, zdd2.t));
            }

            cri.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD crossDifference(final ZDDCacheN nod, final ZDDCacheP equ, final ZDDCacheO crd, final ZDDCacheO uni, final ZDD zdd1, final ZDD zdd2)
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

        ZDD zdd = crd.get(zdd1, zdd2);

        if (zdd == null) {

            final long x1 = zdd1.x;
            final long x2 = zdd2.x;

            if (x1 < x2) {
                zdd = zdd(nod, x1, crossDifference(nod, equ, crd, uni, zdd1.b, zdd2), crossDifference(nod, equ, crd, uni, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = union(nod, equ, uni, crossDifference(nod, equ, crd, uni, zdd1, zdd2.b), crossDifference(nod, equ, crd, uni, zdd1, zdd2.t));
            } else {
                zdd = zdd(nod, x1, union(nod, equ, uni, crossDifference(nod, equ, crd, uni, zdd1.b, zdd2.b), crossDifference(nod, equ, crd, uni, zdd1.b, zdd2.t), crossDifference(nod, equ, crd, uni, zdd1.t, zdd2.t)), crossDifference(nod, equ, crd, uni,
                        zdd1.t, zdd2.b));
            }

            crd.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static boolean equals(final ZDDCacheP equ, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == zdd2) {
            return true;
        }

        if (zdd1.h != zdd2.h) {
            return false;
        }

        final Boolean cached = equ.get(zdd1, zdd2);

        if (cached != null) {
            return cached.booleanValue();
        }

        boolean equal;

        if (zdd1.x != zdd2.x) {
            equal = false;
        } else if (!equals(equ, zdd1.b, zdd2.b)) {
            equal = false;
        } else if (!equals(equ, zdd1.t, zdd2.t)) {
            equal = false;
        } else {
            equal = true;
        }

        equ.put(zdd1, zdd2, equal);

        return equal;
    }

}
