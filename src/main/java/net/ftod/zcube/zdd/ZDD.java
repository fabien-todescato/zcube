package net.ftod.zcube.zdd;

import java.util.Collection;
import java.util.Iterator;

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

    /**
     * The empty set.
     */
    public static final ZDD BOT = new ZDD(0L, null, null, 1, 0L);
    /**
     * The singleton set holding the empty set.
     */
    public static final ZDD TOP = new ZDD(0L, null, null, 2, 1L);

    public final long x;
    public final ZDD b;
    public final ZDD t;

    public final int h;
    public final long s;

    private ZDD(final long x, final ZDD b, final ZDD t, final int h, final long s) {
        super();
        this.x = x;
        this.b = b;
        this.t = t;
        this.h = h;
        this.s = s;
    }

    private ZDD(final long x, final ZDD b, final ZDD t) {
        this(x, b, t, hash(x, b, t), b.s + t.s);
    }

    private static int hash(final long x, final ZDD b, final ZDD t)
    {
        int result = 1;
        result = 31 * result + Long.valueOf(x).hashCode();
        result = 31 * result + b.h;
        result = 31 * result + t.h;
        return result;
    }

    private static ZDD zdd(final long x, final ZDD b, final ZDD t)
    {
        if (t == BOT) {
            return b;
        }

        return new ZDD(x, b, t);
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
        return set(new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache(), xs);
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
        return included(new ZDDPredicateCache(), new ZDDPredicateCache(), zdd1, zdd2);
    }

    static boolean included(final ZDDPredicateCache eq, final ZDDPredicateCache in, final ZDD zdd1, final ZDD zdd2)
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
        return union(new ZDDPredicateCache(), new ZDDOperationCache(), zdd1, zdd2);
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
        return union(new ZDDPredicateCache(), new ZDDOperationCache(), zdds);
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
        return equals(new ZDDPredicateCache(), zdd1, zdd2);
    }

    public static ZDD trees(final String[][]... a)
    {
        return trees(new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache(), a);
    }

    static ZDD trees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final String[][]... a)
    {
        final int n = a.length;
        final ZDD[] trees = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            trees[i] = trees(eq, cu, un, a[i]);
        }

        return union(eq, un, trees);
    }

    public static ZDD trees(final String[]... a)
    {
        return trees(new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache(), a);
    }

    static ZDD trees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final String[]... a)
    {
        final int n = a.length;
        final ZDD[] trees = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            trees[i] = trees(eq, cu, un, a[i]);
        }

        return crossUnion(eq, cu, un, trees);
    }

    public static ZDD trees(final String... a)
    {
        return trees(new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache(), a);
    }

    static ZDD trees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final String... a)
    {
        return trees(eq, cu, un, 1L, 0, a);
    }

    private static ZDD trees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h, final int i, final String[] a)
    {
        if (i == a.length) {
            return TOP;
        }

        final long h1 = djb2(h, a[i]);

        return union(eq, un, TOP, crossUnion(eq, cu, un, singleton(h1), trees(eq, cu, un, h1, i + 1, a)));
    }

    public static ZDD trees(final Collection<Collection<Collection<String>>> i3)
    {
        final ZDDPredicateCache eq = new ZDDPredicateCache();
        final ZDDOperationCache cu = new ZDDOperationCache();
        final ZDDOperationCache un = new ZDDOperationCache();

        return trees3(eq, cu, un, i3);
    }

    static ZDD trees3(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final Collection<Collection<Collection<String>>> i3)
    {
        final ZDD[] zs = new ZDD[i3.size()];

        int j = 0;

        for (final Collection<Collection<String>> i2 : i3) {
            zs[j++] = trees2(eq, cu, un, i2);
        }

        return union(eq, un, zs);
    }

    private static ZDD trees2(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final Collection<Collection<String>> i2)
    {
        final ZDD[] zs = new ZDD[i2.size()];

        int j = 0;

        for (final Collection<String> i : i2) {
            zs[j++] = trees1(eq, cu, un, i);
        }

        return crossUnion(eq, cu, un, zs);
    }

    private static ZDD trees1(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final Collection<String> i)
    {
        return trees1(eq, cu, un, 1L, i.iterator());
    }

    private static ZDD trees1(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h, final Iterator<String> i)
    {
        if (i.hasNext()) {
            final long h1 = djb2(h, i.next());
            return union(eq, un, TOP, crossUnion(eq, cu, un, singleton(h1), trees1(eq, cu, un, h1, i)));
        }

        return TOP;
    }

    public static ZDD tree(final Collection<Collection<Collection<String>>> i3)
    {
        final ZDDPredicateCache eq = new ZDDPredicateCache();
        final ZDDOperationCache cu = new ZDDOperationCache();
        final ZDDOperationCache un = new ZDDOperationCache();

        return tree3(eq, cu, un, i3);
    }

    public static ZDD tree2(final Collection<Collection<String>> i2)
    {
        final ZDDPredicateCache eq = new ZDDPredicateCache();
        final ZDDOperationCache cu = new ZDDOperationCache();
        final ZDDOperationCache un = new ZDDOperationCache();

        return tree2(eq, cu, un, i2);
    }

    public static ZDD tree1(final Collection<String> i)
    {
        final ZDDPredicateCache eq = new ZDDPredicateCache();
        final ZDDOperationCache cu = new ZDDOperationCache();
        final ZDDOperationCache un = new ZDDOperationCache();

        return tree1(eq, cu, un, i);
    }

    private static ZDD tree3(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final Collection<Collection<Collection<String>>> i3)
    {
        final ZDD[] zs = new ZDD[i3.size()];

        int j = 0;

        for (final Collection<Collection<String>> i2 : i3) {
            zs[j++] = tree2(eq, cu, un, i2);
        }

        return union(eq, un, zs);
    }

    private static ZDD tree2(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final Collection<Collection<String>> i2)
    {
        final ZDD[] zs = new ZDD[i2.size()];

        int j = 0;

        for (final Collection<String> i : i2) {
            zs[j++] = tree1(eq, cu, un, i);
        }

        return crossUnion(eq, cu, un, zs);
    }

    private static ZDD tree1(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final Collection<String> i)
    {
        final ZDD[] zs = new ZDD[i.size()];

        long h = 1L;
        int j = 0;

        for (final String s : i) {
            zs[j++] = singleton(h = djb2(h, s));
        }

        return crossUnion(eq, cu, un, zs);
    }

    public static ZDD tree(final String[]... a)
    {
        return tree(new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache(), a);
    }

    public static ZDD tree(final String... a)
    {
        return tree(new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache(), a);
    }

    static ZDD tree(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final String[]... a)
    {
        final int n = a.length;
        final ZDD[] trees = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            trees[i] = tree(eq, cu, un, a[i]);
        }

        return crossUnion(eq, cu, un, trees);
    }

    static ZDD tree(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final String... a)
    {
        long h = 1L;
        ZDD p = TOP;

        for (final String s : a) {
            h = djb2(h, s);
            p = crossUnion(eq, cu, un, p, singleton(h));
        }

        return p;
    }

    /**
     * <h3>Hashing a {@link String}, starting from a <code>long</code> seed</h3>
     * 
     * <p>
     * This hashing scheme is used to generate <code>long</code> identifiers for the nodes of the trees. The hash for a child node is computed by taking the
     * hash of the father node and combining it with the label of the branch from the father node to the child node.
     * </p>
     * <p>
     * See <a href="http://programmers.stackexchange.com/questions/49550/which-hashing-algorithm-is-best-for-uniqueness-and-speed">Which hashing algorithm is
     * best for uniqueness and speed</a> for an overview of hashing, and pointers on the <code>djb2</code> hash function.
     * </p>
     */
    static long djb2(final long seed, final String string)
    {
        long hash = 5381L;

        hash = 33L * hash ^ seed >>> 56 & 0xFF;
        hash = 33L * hash ^ seed >>> 48 & 0xFF;
        hash = 33L * hash ^ seed >>> 40 & 0xFF;
        hash = 33L * hash ^ seed >>> 32 & 0xFF;
        hash = 33L * hash ^ seed >>> 24 & 0xFF;
        hash = 33L * hash ^ seed >>> 16 & 0xFF;
        hash = 33L * hash ^ seed >>> 8 & 0xFF;
        hash = 33L * hash ^ seed & 0xFF;

        for (int i = 0; i < string.length(); ++i) {
            hash = 33L * hash ^ string.charAt(i);
        }

        return hash;
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
        builder.append(", s=");
        builder.append(s);
        builder.append("]");
        return builder.toString();
    }

    static ZDD union(final ZDDPredicateCache eq, final ZDDOperationCache un, final ZDD... zdds)
    {
        final int l = zdds.length;

        if (l > 2) {

            final int l1 = l >> 1;
            final int l2 = l - l1;

            final ZDD[] zdds1 = new ZDD[l1];
            final ZDD[] zdds2 = new ZDD[l2];

            System.arraycopy(zdds, 0, zdds1, 0, l1);
            System.arraycopy(zdds, l1, zdds2, 0, l2);

            return union(eq, un, union(eq, un, zdds1), union(eq, un, zdds2));
        }
        if (l > 1) {
            return union(eq, un, zdds[0], zdds[1]);
        }
        if (l > 0) {
            return zdds[0];
        }

        return BOT;
    }

    static ZDD union(final ZDDPredicateCache eq, final ZDDOperationCache un, final ZDD zdd1, final ZDD zdd2)
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
                    zdd = zdd(x1, union(eq, un, zdd1.b, zdd2), zdd1.t);
                } else if (x1 > x2) {
                    zdd = zdd(x2, union(eq, un, zdd1, zdd2.b), zdd2.t);
                } else {
                    zdd = zdd(x1, union(eq, un, zdd1.b, zdd2.b), union(eq, un, zdd1.t, zdd2.t));
                }
            }

            un.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD unionTop(final ZDDOperationCache un, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return TOP;
        }

        if (zdd1 == TOP) {
            return TOP;
        }

        ZDD zdd = un.get(TOP, zdd1);

        if (zdd == null) {
            zdd = zdd(zdd1.x, unionTop(un, zdd1.b), zdd1.t);
            un.put(TOP, zdd1, zdd);
        }

        return zdd;
    }

    static ZDD intersection(final ZDDPredicateCache eq, final ZDDOperationCache in, final ZDD... zdds)
    {
        final int l = zdds.length;

        if (l > 2) {

            final int l1 = l >> 1;
            final int l2 = l - l1;

            final ZDD[] zdds1 = new ZDD[l1];
            final ZDD[] zdds2 = new ZDD[l2];

            System.arraycopy(zdds, 0, zdds1, 0, l1);
            System.arraycopy(zdds, l1, zdds2, 0, l2);

            return intersection(eq, in, intersection(eq, in, zdds1), intersection(eq, in, zdds2));
        } else if (l > 1) {
            return intersection(eq, in, zdds[0], zdds[1]);
        } else if (l > 0) {
            return zdds[0];
        }

        return BOT;
    }

    static ZDD intersection(final ZDDPredicateCache eq, final ZDDOperationCache in, final ZDD zdd1, final ZDD zdd2)
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
                    zdd = intersection(eq, in, zdd1.b, zdd2);
                } else if (x1 > x2) {
                    zdd = intersection(eq, in, zdd1, zdd2.b);
                } else {
                    zdd = zdd(x1, intersection(eq, in, zdd1.b, zdd2.b), intersection(eq, in, zdd1.t, zdd2.t));
                }
            }

            in.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD intersectionTop(final ZDDOperationCache in, final ZDD zdd1)
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

    static ZDD difference(final ZDDPredicateCache eq, final ZDDOperationCache di, final ZDD zdd1, final ZDD zdd2)
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
                    zdd = zdd(x1, difference(eq, di, zdd1.b, zdd2), zdd1.t);
                } else if (x1 > x2) {
                    zdd = difference(eq, di, zdd1, zdd2.b);
                } else {
                    zdd = zdd(x1, difference(eq, di, zdd1.b, zdd2.b), difference(eq, di, zdd1.t, zdd2.t));
                }
            }

            di.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    private static ZDD differenceTop(final ZDDOperationCache di, final ZDD zdd1)
    {
        if (zdd1 == BOT) {
            return BOT;
        }

        if (zdd1 == TOP) {
            return BOT;
        }

        ZDD zdd = di.get(zdd1, TOP);

        if (zdd == null) {
            zdd = zdd(zdd1.x, differenceTop(di, zdd1.b), zdd1.t);
            di.put(zdd1, TOP, zdd);
        }

        return zdd;
    }

    private static ZDD topDifference(final ZDDOperationCache di, final ZDD zdd2)
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

    static ZDD crossUnion(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final ZDD... zdds)
    {
        final int l = zdds.length;

        if (l > 2) {

            final int l1 = l >> 1;
            final int l2 = l - l1;

            final ZDD[] zdds1 = new ZDD[l1];
            final ZDD[] zdds2 = new ZDD[l2];

            System.arraycopy(zdds, 0, zdds1, 0, l1);
            System.arraycopy(zdds, l1, zdds2, 0, l2);

            return crossUnion(eq, cu, un, crossUnion(eq, cu, un, zdds1), crossUnion(eq, cu, un, zdds2));
        } else if (l > 1) {
            return crossUnion(eq, cu, un, zdds[0], zdds[1]);
        } else if (l > 0) {
            return zdds[0];
        }

        return TOP;
    }

    static ZDD crossUnion(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final ZDD zdd1, final ZDD zdd2)
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
                zdd = zdd(x1, crossUnion(eq, cu, un, zdd1.b, zdd2), crossUnion(eq, cu, un, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = zdd(x2, crossUnion(eq, cu, un, zdd1, zdd2.b), crossUnion(eq, cu, un, zdd1, zdd2.t));
            } else {
                zdd = zdd(x1, crossUnion(eq, cu, un, zdd1.b, zdd2.b), union(eq, un, crossUnion(eq, cu, un, zdd1.t, zdd2.t), union(eq, un, crossUnion(eq, cu, un, zdd1.t, zdd2.b), crossUnion(eq, cu, un, zdd1.b, zdd2.t))));
            }

            cu.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD set(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long... xs)
    {
        final ZDD[] zdd = new ZDD[xs.length];

        for (int i = 0; i < xs.length; ++i) {
            zdd[i] = singleton(xs[i]);
        }

        return crossUnion(eq, cu, un, zdd);
    }

    static ZDD crossIntersection(final ZDDPredicateCache eq, final ZDDOperationCache ci, final ZDDOperationCache un, final ZDD... zdds)
    {
        final int l = zdds.length;

        if (l > 2) {

            final int l1 = l >> 1;
            final int l2 = l - l1;

            final ZDD[] zdds1 = new ZDD[l1];
            final ZDD[] zdds2 = new ZDD[l2];

            System.arraycopy(zdds, 0, zdds1, 0, l1);
            System.arraycopy(zdds, l1, zdds2, 0, l2);

            return crossIntersection(eq, ci, un, crossIntersection(eq, ci, un, zdds1), crossIntersection(eq, ci, un, zdds2));
        } else if (l > 1) {
            return crossIntersection(eq, ci, un, zdds[0], zdds[1]);
        } else if (l > 0) {
            return zdds[0];
        }

        return TOP;
    }

    static ZDD crossIntersection(final ZDDPredicateCache eq, final ZDDOperationCache ci, final ZDDOperationCache un, final ZDD zdd1, final ZDD zdd2)
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
                zdd = union(eq, un, crossIntersection(eq, ci, un, zdd1.b, zdd2), crossIntersection(eq, ci, un, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = union(eq, un, crossIntersection(eq, ci, un, zdd1, zdd2.b), crossIntersection(eq, ci, un, zdd1, zdd2.t));
            } else {
                zdd = zdd(x1, union(eq, un, crossIntersection(eq, ci, un, zdd1.b, zdd2.b), union(eq, un, crossIntersection(eq, ci, un, zdd1.b, zdd2.t), crossIntersection(eq, ci, un, zdd1.t, zdd2.b))), crossIntersection(eq, ci, un, zdd1.t, zdd2.t));
            }

            ci.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static ZDD crossDifference(final ZDDPredicateCache eq, final ZDDOperationCache cd, final ZDDOperationCache un, final ZDD zdd1, final ZDD zdd2)
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
                zdd = zdd(x1, crossDifference(eq, cd, un, zdd1.b, zdd2), crossDifference(eq, cd, un, zdd1.t, zdd2));
            } else if (x1 > x2) {
                zdd = union(eq, un, crossDifference(eq, cd, un, zdd1, zdd2.b), crossDifference(eq, cd, un, zdd1, zdd2.t));
            } else {
                zdd = zdd(x1, union(eq, un, crossDifference(eq, cd, un, zdd1.b, zdd2.b), crossDifference(eq, cd, un, zdd1.b, zdd2.t), crossDifference(eq, cd, un, zdd1.t, zdd2.t)), crossDifference(eq, cd, un, zdd1.t, zdd2.b));
            }

            cd.put(zdd1, zdd2, zdd);
        }

        return zdd;
    }

    static boolean equals(final ZDDPredicateCache eq, final ZDD zdd1, final ZDD zdd2)
    {
        if (zdd1 == zdd2) {
            return true;
        }
        if (zdd1 == BOT) {
            return zdd2 == BOT;
        }
        if (zdd1 == TOP) {
            return zdd2 == TOP;
        }
        if (zdd2 == BOT) {
            return zdd1 == BOT;
        }
        if (zdd2 == TOP) {
            return zdd1 == TOP;
        }

        final Boolean cached = eq.get(zdd1, zdd2);

        if (cached != null) {
            return cached.booleanValue();
        }

        boolean equal;

        if (zdd1.h != zdd2.h) {
            equal = false;
        } else if (zdd1.x != zdd2.x) {
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
