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

    static long size(final ZDDCacheL _clo, final ZDD z)
    {
        if (z == BOT) {
            return 0L;
        }
        if (z == TOP) {
            return 1L;
        }

        final Long cached = _clo.get(z);

        if (cached != null) {
            return cached.longValue();
        }

        final long s = size(_clo, z.b) + size(_clo, z.t);

        _clo.put(z, s);

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

    static ZDD singleton(final ZDDCacheN _nod, final long x)
    {
        return zdd(_nod, x, BOT, TOP);
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

    static boolean included(final ZDDCacheP _equ, final ZDDCacheP _inc, final ZDD zdd1, final ZDD zdd2)
    {
        if (equals(_equ, zdd1, zdd2)) {
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

        final Boolean cached = _inc.get(zdd1, zdd2);

        if (cached != null) {
            return cached.booleanValue();
        }

        final boolean included;

        final long x1 = zdd1.x;
        final long x2 = zdd2.x;

        if (x1 < x2) {
            included = false;
        } else if (x1 > x2) {
            included = included(_equ, _inc, zdd1, zdd2.b);
        } else {
            included = included(_equ, _inc, zdd1.b, zdd2.b) && included(_equ, _inc, zdd1.t, zdd2.t);
        }

        _inc.put(zdd1, zdd2, included);

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

    static ZDD union(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _uni, final ZDD... zdds)
    {
        return union(_nod, _equ, _uni, 0, zdds.length, zdds);
    }

    static ZDD union(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _uni, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return union(_nod, _equ, _uni, union(_nod, _equ, _uni, begin, middle, zdda), union(_nod, _equ, _uni, middle, end, zdda));
        }
        if (length > 1) {
            return union(_nod, _equ, _uni, zdda[begin], zdda[begin + 1]);
        }
        if (length > 0) {
            return zdda[begin];
        }

        return BOT;
    }

    static ZDD union(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _uni, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return zdd2;
        }

        if (zdd2 == BOT) {
            return zdd1;
        }

        if (equals(_equ, zdd1, zdd2)) {
            return zdd1;
        }

        ZDD zdd = _uni.get(zdd1, zdd2);

        if (zdd == null) {
            if (zdd1 == TOP) {
                zdd = unionTop(_nod, _uni, zdd2);
            } else if (zdd2 == TOP) {
                zdd = unionTop(_nod, _uni, zdd1);
            } else {

                final long x1 = zdd1.x;
                final long x2 = zdd2.x;

                if (x1 < x2) {
                    zdd = zdd(_nod, x1, union(_nod, _equ, _uni, zdd1.b, zdd2), zdd1.t);
                } else if (x1 > x2) {
                    zdd = zdd(_nod, x2, union(_nod, _equ, _uni, zdd1, zdd2.b), zdd2.t);
                } else {
                    zdd = zdd(_nod, x1, union(_nod, _equ, _uni, zdd1.b, zdd2.b), union(_nod, _equ, _uni, zdd1.t, zdd2.t));
                }
            }

            _uni.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD unionTop(final ZDDCacheN _nod, final ZDDCacheO _uni, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return TOP;
        }

        if (zdd1 == TOP) {
            return TOP;
        }

        ZDD zdd = _uni.get(TOP, zdd1);

        if (zdd == null) {
            zdd = zdd(_nod, zdd1.x, unionTop(_nod, _uni, zdd1.b), zdd1.t);
            _uni.put(TOP, zdd1, zdd);
        }

        return zdd;
    }

    static ZDD intersection(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _int, final ZDD... zdds)
    {
        return intersection(_nod, _equ, _int, 0, zdds.length, zdds);
    }

    static ZDD intersection(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _int, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return intersection(_nod, _equ, _int, intersection(_nod, _equ, _int, begin, middle, zdda), intersection(_nod, _equ, _int, middle, end, zdda));
        } else if (length > 1) {
            return intersection(_nod, _equ, _int, zdda[begin], zdda[begin + 1]);
        } else if (length > 0) {
            return zdda[begin];
        }

        return BOT;
    }

    static ZDD intersection(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _int, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd2 == BOT) {
            return BOT;
        }

        if (equals(_equ, zdd1, zdd2)) {
            return zdd1;
        }

        ZDD zdd = _int.get(zdd1, zdd2);

        if (zdd == null) {
            if (zdd1 == TOP) {
                zdd = intersectionTop(_int, zdd2);
            } else if (zdd2 == TOP) {
                zdd = intersectionTop(_int, zdd1);
            } else {

                final long x1 = zdd1.x;
                final long x2 = zdd2.x;

                if (x1 < x2) {
                    zdd = intersection(_nod, _equ, _int, zdd1.b, zdd2);
                } else if (x1 > x2) {
                    zdd = intersection(_nod, _equ, _int, zdd1, zdd2.b);
                } else {
                    zdd = zdd(_nod, x1, intersection(_nod, _equ, _int, zdd1.b, zdd2.b), intersection(_nod, _equ, _int, zdd1.t, zdd2.t));
                }
            }

            _int.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD intersectionTop(final ZDDCacheO _int, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd1 == TOP) {
            return TOP;
        }

        ZDD zdd = _int.get(TOP, zdd1);

        if (zdd == null) {
            zdd = intersectionTop(_int, zdd1.b);
            _int.put(TOP, zdd1, zdd);
        }

        return zdd;
    }

    static ZDD difference(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _dif, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd2 == BOT) {
            return zdd1;
        }

        if (equals(_equ, zdd1, zdd2)) {
            return BOT;
        }

        ZDD zdd = _dif.get(zdd1, zdd2);

        if (zdd == null) {

            if (zdd1 == TOP) {
                zdd = topDifference(_dif, zdd2);
            } else if (zdd2 == TOP) {
                zdd = differenceTop(_nod, _dif, zdd1);
            } else {

                final long x1 = zdd1.x;
                final long x2 = zdd2.x;

                if (x1 < x2) {
                    zdd = zdd(_nod, x1, difference(_nod, _equ, _dif, zdd1.b, zdd2), zdd1.t);
                } else if (x1 > x2) {
                    zdd = difference(_nod, _equ, _dif, zdd1, zdd2.b);
                } else {
                    zdd = zdd(_nod, x1, difference(_nod, _equ, _dif, zdd1.b, zdd2.b), difference(_nod, _equ, _dif, zdd1.t, zdd2.t));
                }
            }

            _dif.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD differenceTop(final ZDDCacheN _nod, final ZDDCacheO _dif, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd1 == TOP) {
            return BOT;
        }

        ZDD zdd = _dif.get(zdd1, TOP);

        if (zdd == null) {
            zdd = zdd(_nod, zdd1.x, differenceTop(_nod, _dif, zdd1.b), zdd1.t);
            _dif.put(zdd1, TOP, zdd);
        }

        return zdd;
    }

    private static ZDD topDifference(final ZDDCacheO _dif, final ZDD zdd2)
    {
        if (zdd2 == BOT) {
            return TOP;
        }

        if (zdd2 == TOP) {
            return BOT;
        }

        ZDD zdd = _dif.get(TOP, zdd2);

        if (zdd == null) {
            zdd = topDifference(_dif, zdd2.b);
            _dif.put(TOP, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD crossUnion(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final ZDD... zdds)
    {
        return crossUnion(_nod, _equ, _cru, _uni, 0, zdds.length, zdds);
    }

    static ZDD crossUnion(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return crossUnion(_nod, _equ, _cru, _uni, crossUnion(_nod, _equ, _cru, _uni, begin, middle, zdda), crossUnion(_nod, _equ, _cru, _uni, middle, end, zdda));
        } else if (length > 1) {
            return crossUnion(_nod, _equ, _cru, _uni, zdda[begin], zdda[begin + 1]);
        } else if (length > 0) {
            return zdda[begin];
        }

        return TOP;
    }

    static ZDD crossUnion(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final ZDD zdd1, final ZDD zdd2)
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

        ZDD zdd = _cru.get(zdd1, zdd2);

        if (zdd == null) {

            final long x1 = zdd1.x;
            final long x2 = zdd2.x;

            if (x1 < x2) {
                zdd = zdd(_nod, x1, crossUnion(_nod, _equ, _cru, _uni, zdd1.b, zdd2), crossUnion(_nod, _equ, _cru, _uni, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = zdd(_nod, x2, crossUnion(_nod, _equ, _cru, _uni, zdd1, zdd2.b), crossUnion(_nod, _equ, _cru, _uni, zdd1, zdd2.t));
            } else {
                zdd = zdd(_nod, x1, crossUnion(_nod, _equ, _cru, _uni, zdd1.b, zdd2.b), union(_nod, _equ, _uni, crossUnion(_nod, _equ, _cru, _uni, zdd1.t, zdd2.t), union(_nod, _equ, _uni, crossUnion(_nod, _equ, _cru, _uni, zdd1.t, zdd2.b), crossUnion(
                        _nod, _equ, _cru, _uni, zdd1.b, zdd2.t))));
            }

            _cru.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD set(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long[] xs)
    {
        final ZDD[] zdd = new ZDD[xs.length];

        for (int i = 0; i < xs.length; ++i) {
            zdd[i] = singleton(_nod, xs[i]);
        }

        return crossUnion(_nod, _equ, _cru, _uni, zdd);
    }

    static ZDD crossIntersection(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cri, final ZDDCacheO _uni, final ZDD[] zdds)
    {
        return crossIntersection(_nod, _equ, _cri, _uni, 0, zdds.length, zdds);
    }

    static ZDD crossIntersection(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cri, final ZDDCacheO _uni, final int begin, final int end, final ZDD[] zdda)
    {
        final int length = end - begin;

        if (length > 2) {
            final int middle = begin + (length >> 1);
            return crossIntersection(_nod, _equ, _cri, _uni, crossIntersection(_nod, _equ, _cri, _uni, begin, middle, zdda), crossIntersection(_nod, _equ, _cri, _uni, middle, end, zdda));
        } else if (length > 1) {
            return crossIntersection(_nod, _equ, _cri, _uni, zdda[begin], zdda[begin + 1]);
        } else if (length > 0) {
            return zdda[begin];
        }

        return TOP;
    }

    static ZDD crossIntersection(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cri, final ZDDCacheO _uni, final ZDD zdd1, final ZDD zdd2)
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

        ZDD zdd = _cri.get(zdd1, zdd2);

        if (zdd == null) {

            final long x1 = zdd1.x;
            final long x2 = zdd2.x;

            if (x1 < x2) {
                zdd = union(_nod, _equ, _uni, crossIntersection(_nod, _equ, _cri, _uni, zdd1.b, zdd2), crossIntersection(_nod, _equ, _cri, _uni, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = union(_nod, _equ, _uni, crossIntersection(_nod, _equ, _cri, _uni, zdd1, zdd2.b), crossIntersection(_nod, _equ, _cri, _uni, zdd1, zdd2.t));
            } else {
                zdd = zdd(_nod, x1, union(_nod, _equ, _uni, crossIntersection(_nod, _equ, _cri, _uni, zdd1.b, zdd2.b), union(_nod, _equ, _uni, crossIntersection(_nod, _equ, _cri, _uni, zdd1.b, zdd2.t), crossIntersection(_nod, _equ, _cri, _uni, zdd1.t,
                        zdd2.b))), crossIntersection(_nod, _equ, _cri, _uni, zdd1.t, zdd2.t));
            }

            _cri.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD crossDifference(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _crd, final ZDDCacheO _uni, final ZDD zdd1, final ZDD zdd2)
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

        ZDD zdd = _crd.get(zdd1, zdd2);

        if (zdd == null) {

            final long x1 = zdd1.x;
            final long x2 = zdd2.x;

            if (x1 < x2) {
                zdd = zdd(_nod, x1, crossDifference(_nod, _equ, _crd, _uni, zdd1.b, zdd2), crossDifference(_nod, _equ, _crd, _uni, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = union(_nod, _equ, _uni, crossDifference(_nod, _equ, _crd, _uni, zdd1, zdd2.b), crossDifference(_nod, _equ, _crd, _uni, zdd1, zdd2.t));
            } else {
                zdd = zdd(_nod, x1, union(_nod, _equ, _uni, crossDifference(_nod, _equ, _crd, _uni, zdd1.b, zdd2.b), crossDifference(_nod, _equ, _crd, _uni, zdd1.b, zdd2.t), crossDifference(_nod, _equ, _crd, _uni, zdd1.t, zdd2.t)), crossDifference(_nod,
                        _equ, _crd, _uni, zdd1.t, zdd2.b));
            }

            _crd.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static boolean equals(final ZDDCacheP _equ, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == zdd2) {
            return true;
        }

        if (zdd1.h != zdd2.h) {
            return false;
        }

        final Boolean cached = _equ.get(zdd1, zdd2);

        if (cached != null) {
            return cached.booleanValue();
        }

        boolean equal;

        if (zdd1.x != zdd2.x) {
            equal = false;
        } else if (!equals(_equ, zdd1.b, zdd2.b)) {
            equal = false;
        } else if (!equals(_equ, zdd1.t, zdd2.t)) {
            equal = false;
        } else {
            equal = true;
        }

        _equ.put(zdd1, zdd2, equal);

        return equal;
    }

}
