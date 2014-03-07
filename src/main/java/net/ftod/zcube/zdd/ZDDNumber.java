package net.ftod.zcube.zdd;

import static net.ftod.zcube.zdd.ZDD.included;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * <h1>Representing linear combination of sets over integers with {@link ZDD}</h1>
 * 
 * <p>
 * <em>Pr. Minato et Al</em> show how to represent linear combinations of sets with integer coefficients as a forest of shared {@link ZDD}. A <em>binary</em>
 * representation can be used for unsigned integers, whereas signed integers may be represented in <em>negabinary</em>. Both representations are offered here.
 * </p>
 * 
 * @author Fabien Todescato
 */
public final class ZDDNumber {

    private static final int PROCESSOR_SPREAD = 8;
    /**
     * Lowest-order digit.
     */
    public final ZDD digit;
    /**
     * Higher-order digits.
     */
    public final ZDDNumber number;
    /**
     * The <em>zero</em> {@link ZDDNumber}.
     */
    public static final ZDDNumber ZERO = new ZDDNumber(null, null);

    private ZDDNumber(final ZDD digit, final ZDDNumber number) {
        super();
        this.digit = digit;
        this.number = number;
    }

    private static ZDDNumber number(final ZDD digit, final ZDDNumber number)
    {
        if (digit == ZDD.BOT && number == ZERO) {
            return ZERO;
        }

        return new ZDDNumber(digit, number);
    }

    static ZDDNumber shift(final ZDDNumber n)
    {
        return number(ZDD.BOT, n);
    }

    /**
     * <h3>Compute the <em>binary</em> representation of a {@link ZDD} multiplied by a <em>positive</em> <code>long</code></h3>
     * 
     * @param l
     *            the <code>long</code> holding the <em>positive</em> coefficient.
     * @param zdd
     *            the {@link ZDD} multiplicand.
     * @return the {@link ZDDNumber} representing in binary <code>l</code> occurrences of <code>zdd</code>
     */
    public static ZDDNumber binary(final long l, final ZDD zdd)
    {
        return l == 0L ? ZERO : number(l % 2L == 0 ? ZDD.BOT : zdd, binary(l >> 1, zdd));
    }

    /**
     * <h3>Counting the occurrences of a {@link ZDD} in an unsigned binary {@link ZDDNumber}</h3>
     * 
     * <p>
     * Return the number of occurrences of a set of sets represented as a {@link ZDD} within a {@link ZDDNumber}.
     * </p>
     * 
     * @param zddn
     *            the {@link ZDDNumber} to be projected over the set of sets.
     * @param zdd
     *            the {@link ZDD} representing the set of sets.
     * @return the <code>long</code> representing the number of occurrences of the set of sets within the {@link ZDDNumber}.
     */
    public static long binary(final ZDDNumber zddn, final ZDD zdd)
    {
        return binary(new ZDDCacheP(), new ZDDCacheP(), zddn, zdd);
    }

    static long binary(final ZDDCacheP eq, final ZDDCacheP in, final ZDDNumber zddn, final ZDD zdd)
    {
        return zddn == ZERO ? 0L : (included(eq, in, zdd, zddn.digit) ? 1L : 0L) + (binary(eq, in, zddn.number, zdd) << 1);
    }

    /**
     * <h3>Addition of two unsigned binary {@link ZDDNumber}</h3>
     * 
     * @param zddn1
     *            left operand {@link ZDDNumber}.
     * @param zddn2
     *            right operand {@link ZDDNumber}.
     * @return the {@link ZDDNumber} sum of the above.
     */
    public static ZDDNumber binaryAdd(final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        return binaryAdd(new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), new ZDDCacheO(), new ZDDCacheO(), zddn1, zddn2);
    }

    static ZDDNumber binaryAdd(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO in, final ZDDCacheO un, final ZDDCacheO di, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        final ZDDNumber zddnc = intersection(nod, eq, in, zddn1, zddn2);
        final ZDDNumber zddns = difference(nod, eq, di, union(nod, eq, un, zddn1, zddn2), zddnc);

        if (zddnc == ZERO) {
            return zddns;
        }

        if (zddns == ZERO) {
            return new ZDDNumber(ZDD.BOT, zddnc);
        }

        return number(zddns.digit, binaryAdd(nod, eq, in, un, di, zddns.number, zddnc));
    }

    /**
     * <h3>Compute the <em>negabinary</em> representation of a {@link ZDD} multiplied by a <em>signed</em> <code>long</code></h3>
     * 
     * @param l
     *            the <code>long</code> holding the <em>signed</em> coefficient.
     * @param zdd
     *            the {@link ZDD} multiplicand.
     * @return the {@link ZDDNumber} representing in negabinary <code>l</code> occurrences of <code>zdd</code>
     */
    public static ZDDNumber negabinary(final long l, final ZDD zdd)
    {
        if (l == 0) {
            return ZERO;
        }

        final long q = l / -2L;
        final long r = l + 2L * q;

        final long l1;
        ZDD digit;

        if (r > 0) {
            digit = zdd;
            l1 = q;
        } else if (r < 0) {
            digit = zdd;
            l1 = q + 1;
        } else {
            digit = ZDD.BOT;
            l1 = q;
        }

        return number(digit, negabinary(l1, zdd));
    }

    /**
     * <h3>Counting the occurrences of a {@link ZDD} in a signed negabinary {@link ZDDNumber}</h3>
     * 
     * <p>
     * Return the number of occurrences of a set of sets represented as a {@link ZDD} within a {@link ZDDNumber}.
     * </p>
     * 
     * @param zddn
     *            the {@link ZDDNumber} to be projected over the set of sets.
     * @param zdd
     *            the {@link ZDD} representing the set of sets.
     * @return the <code>long</code> representing the number of occurrences of the set of sets within the {@link ZDDNumber}.
     */
    public static long negabinary(final ZDDNumber zddn, final ZDD zdd)
    {
        return negabinary(new ZDDCacheP(), new ZDDCacheP(), zddn, zdd);
    }

    static long negabinary(final ZDDCacheP eq, final ZDDCacheP in, final ZDDNumber zddn, final ZDD zdd)
    {
        if (zddn == ZERO) {
            return 0L;
        }

        return (included(eq, in, zdd, zddn.digit) ? 1L : 0L) + negabinary(eq, in, zddn.number, zdd) * -2L;
    }

    /**
     * <h3>Addition of two signed negabinary {@link ZDDNumber}</h3>
     * 
     * @param zddn1
     *            left operand {@link ZDDNumber}.
     * @param zddn2
     *            right operand {@link ZDDNumber}.
     * @return the {@link ZDDNumber} sum of the above.
     */
    public static ZDDNumber negabinaryAdd(final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        return negabinaryAdd(new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), new ZDDCacheO(), new ZDDCacheO(), zddn1, zddn2);
    }

    public static ZDDNumber negabinaryAdd(final Iterable<ZDDNumber> i)
    {
        return negabinaryAdd(i.iterator());
    }

    public static ZDDNumber negabinaryAdd(final Iterator<ZDDNumber> i)
    {
        final ZDDCacheN _nod = new ZDDCacheN();
        final ZDDCacheP _equ = new ZDDCacheP();
        final ZDDCacheO _int = new ZDDCacheO();
        final ZDDCacheO _uni = new ZDDCacheO();
        final ZDDCacheO _dif = new ZDDCacheO();

        ZDDNumber zn = ZERO;

        while (i.hasNext()) {
            zn = negabinaryAdd(_nod, _equ, _int, _uni, _dif, zn, i.next());
        }

        return zn;
    }

    static ZDDNumber negabinaryAdd(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _int, final ZDDCacheO _uni, final ZDDCacheO _dif, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        final ZDDNumber zddnc = intersection(_nod, _equ, _int, zddn1, zddn2);
        final ZDDNumber zddns = difference(_nod, _equ, _dif, union(_nod, _equ, _uni, zddn1, zddn2), zddnc);

        if (zddnc == ZERO) {
            return zddns;
        }

        return negabinarySub(_nod, _equ, _int, _uni, _dif, zddns, shift(zddnc));
    }

    /**
     * <h3>Subtraction of two signed negabinary {@link ZDDNumber}</h3>
     * 
     * @param zddn1
     *            left operand {@link ZDDNumber}.
     * @param zddn2
     *            right operand {@link ZDDNumber}.
     * @return the {@link ZDDNumber} difference of the above.
     */
    public static ZDDNumber negabinarySub(final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        return negabinarySub(new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), new ZDDCacheO(), new ZDDCacheO(), zddn1, zddn2);
    }

    static ZDDNumber negabinarySub(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _int, final ZDDCacheO _uni, final ZDDCacheO _dif, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        final ZDDNumber zddnb = difference(_nod, _equ, _dif, zddn2, zddn1);
        final ZDDNumber zddnd = union(_nod, _equ, _uni, difference(_nod, _equ, _dif, zddn1, zddn2), zddnb);

        if (zddnb == ZERO) {
            return zddnd;
        }

        return negabinaryAdd(_nod, _equ, _int, _uni, _dif, zddnd, shift(zddnb));
    }

    private static ZDDNumber intersection(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _int, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        if (zddn1 == ZERO) {
            return ZERO;
        }
        if (zddn2 == ZERO) {
            return ZERO;
        }
        return number(ZDD.intersection(_nod, _equ, _int, zddn1.digit, zddn2.digit), intersection(_nod, _equ, _int, zddn1.number, zddn2.number));
    }

    private static ZDDNumber union(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _uni, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        if (zddn1 == ZERO) {
            return zddn2;
        }
        if (zddn2 == ZERO) {
            return zddn1;
        }
        return number(ZDD.union(_nod, _equ, _uni, zddn1.digit, zddn2.digit), union(_nod, _equ, _uni, zddn1.number, zddn2.number));
    }

    private static ZDDNumber difference(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _dif, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        if (zddn1 == ZERO) {
            return ZERO;
        }
        if (zddn2 == ZERO) {
            return zddn1;
        }
        return number(ZDD.difference(_nod, _equ, _dif, zddn1.digit, zddn2.digit), difference(_nod, _equ, _dif, zddn1.number, zddn2.number));
    }

    public static ZDDNumber addSubtrees(final ZDDTerm zt, final ZDDNumber zn)
    {
        final ZDDCacheN _nod = new ZDDCacheN();
        final ZDDCacheP _equ = new ZDDCacheP();
        final ZDDCacheO _cru = new ZDDCacheO();
        final ZDDCacheO _uni = new ZDDCacheO();
        final ZDDCacheO _int = new ZDDCacheO();
        final ZDDCacheO _dif = new ZDDCacheO();

        return addSubtrees(zt, zn, _nod, _equ, _cru, _uni, _int, _dif);
    }

    static ZDDNumber addSubtrees(final ZDDTerm zt, final ZDDNumber zn, final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final ZDDCacheO _int, final ZDDCacheO _dif)
    {
        return negabinaryAdd(_nod, _equ, _int, _uni, _dif, zt.subtrees(_nod, _equ, _cru, _uni), zn);
    }

    public static ZDDNumber addSubtrees(final ZDD filter, final ZDDTerm zt, final ZDDNumber zn)
    {
        final ZDDCacheN _nod = new ZDDCacheN();
        final ZDDCacheP _equ = new ZDDCacheP();
        final ZDDCacheO _cru = new ZDDCacheO();
        final ZDDCacheO _uni = new ZDDCacheO();
        final ZDDCacheO _int = new ZDDCacheO();
        final ZDDCacheO _dif = new ZDDCacheO();

        return addSubtrees(filter, zt, zn, _nod, _equ, _cru, _uni, _int, _dif);
    }

    static ZDDNumber addSubtrees(final ZDD filter, final ZDDTerm zt, final ZDDNumber zn, final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final ZDDCacheO _int, final ZDDCacheO _dif)
    {
        return negabinaryAdd(_nod, _equ, _int, _uni, _dif, zt.subtrees(_nod, _equ, _cru, _uni, _int, filter), zn);
    }

    public static ZDDNumber addSubtrees(final ZDDTerm zt, final ZDD filter, final ZDDNumber zn)
    {
        final ZDDCacheN _nod = new ZDDCacheN();
        final ZDDCacheP _equ = new ZDDCacheP();
        final ZDDCacheO _cru = new ZDDCacheO();
        final ZDDCacheO _uni = new ZDDCacheO();
        final ZDDCacheO _int = new ZDDCacheO();
        final ZDDCacheO _dif = new ZDDCacheO();

        return addSubtrees(filter, zt, zn, _nod, _equ, _cru, _uni, _int, _dif);
    }

    public static long[] sumGroupBy(final ZDDTree[] ts, final Iterable<ZDDTerm> i)
    {
        final int n = ts.length;
        final ZDD[] zs = new ZDD[n];
        final ZDD u;
        {
            final ZDDCacheN _nod = new ZDDCacheN();
            final ZDDCacheP _equ = new ZDDCacheP();
            final ZDDCacheO _cru = new ZDDCacheO();
            final ZDDCacheO _uni = new ZDDCacheO();

            for (int j = 0; j < n; ++j) {
                zs[j] = ZDDTree.trees(ts[j], _nod, _equ, _cru, _uni);
            }

            u = ZDD.union(_nod, _equ, _uni, zs);
        }

        final ZDDNumber zn = sumSubtrees(u, i);

        final long[] ls = new long[n];

        for (int j = 0; j < n; ++j) {
            ls[j] = negabinary(zn, zs[j]);
        }

        return ls;
    }

    public static long[] pSumGroupBy(final ZDDTree[] ts, final Iterable<ZDDTerm> i)
    {
        final int n = ts.length;
        final ZDD[] zs = new ZDD[n];
        final ZDD u;
        {
            final ZDDCacheN _nod = new ZDDCacheN();
            final ZDDCacheP _equ = new ZDDCacheP();
            final ZDDCacheO _cru = new ZDDCacheO();
            final ZDDCacheO _uni = new ZDDCacheO();

            for (int j = 0; j < n; ++j) {
                zs[j] = ZDDTree.trees(ts[j], _nod, _equ, _cru, _uni);
            }

            u = ZDD.union(_nod, _equ, _uni, zs);
        }

        final ZDDNumber zn = pSumSubtrees(u, i);

        final long[] ls = new long[n];

        for (int j = 0; j < n; ++j) {
            ls[j] = negabinary(zn, zs[j]);
        }

        return ls;
    }

    public static ZDDNumber pSumSubtrees(final Iterable<ZDDTerm> i)
    {
        return pSumSubtrees(i.iterator());
    }

    public static ZDDNumber pSumSubtrees(final Iterator<ZDDTerm> i)
    {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int sums = PROCESSOR_SPREAD * processors;

        final ExecutorService threads = Executors.newFixedThreadPool(processors);
        final BlockingQueue<ZDDNumber> zns = new ArrayBlockingQueue<ZDDNumber>(sums);

        for (int j = 0; j < sums; ++j) {
            zns.offer(ZERO);
        }

        while (i.hasNext()) {
            threads.submit(sumTask(zns, i.next()));
        }

        awaitTermination(threads);

        return pSum(processors, zns);
    }

    private static Runnable sumTask(final BlockingQueue<ZDDNumber> zns, final ZDDTerm zt)
    {
        final ZDDNumber zn;

        try {
            zn = zns.take();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new Runnable() {
            @Override
            public void run()
            {
                try {
                    zns.put(addSubtrees(zt, zn));
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static ZDDNumber pSumSubtrees(final ZDD filter, final Iterable<ZDDTerm> i)
    {
        return pSumSubtrees(filter, i.iterator());
    }

    public static ZDDNumber pSumSubtrees(final ZDD filter, final Iterator<ZDDTerm> i)
    {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int sums = PROCESSOR_SPREAD * processors;

        final ExecutorService threads = Executors.newFixedThreadPool(processors);
        final BlockingQueue<ZDDNumber> zns = new ArrayBlockingQueue<ZDDNumber>(sums);

        for (int j = 0; j < sums; ++j) {
            zns.offer(ZERO);
        }

        while (i.hasNext()) {
            threads.submit(sumTask(filter, zns, i.next()));
        }

        awaitTermination(threads);

        return pSum(processors, zns);
    }

    public static ZDDNumber pSum(final Collection<ZDDNumber> zns)
    {
        return pSum(Runtime.getRuntime().availableProcessors(), zns);
    }

    private static ZDDNumber pSum(final int processors, final Collection<ZDDNumber> zns)
    {
        final ZDDNumber[] zna = new ZDDNumber[zns.size()];
        zns.toArray(zna);
        return pSum(processors, zna);
    }

    private static ZDDNumber pSum(final int processors, final ZDDNumber[] zna)
    {
        final ForkJoinPool forkJoinPool = new ForkJoinPool(processors);

        try {
            return forkJoinPool.invoke(recursiveSumTask(zna, 0, zna.length));
        } finally {
            awaitTermination(forkJoinPool);
        }
    }

    private static RecursiveTask<ZDDNumber> recursiveSumTask(final ZDDNumber[] zns, final int begin, final int end)
    {
        return new RecursiveTask<ZDDNumber>() {

            private static final long serialVersionUID = 9099502138482957070L;

            @Override
            protected ZDDNumber compute()
            {
                final int length = end - begin;

                if (length > 2) {

                    final int middle = begin + (length >> 1);

                    final RecursiveTask<ZDDNumber> t1 = recursiveSumTask(zns, begin, middle);
                    final RecursiveTask<ZDDNumber> t2 = recursiveSumTask(zns, middle, end);

                    invokeAll(t1, t2);

                    return ZDDNumber.negabinaryAdd(t1.join(), t2.join());
                }

                if (length > 1) {
                    return ZDDNumber.negabinaryAdd(zns[begin], zns[begin + 1]);
                }

                if (length > 0) {
                    return zns[begin];
                }

                return ZDDNumber.ZERO;
            }
        };
    }

    private static Runnable sumTask(final ZDD filter, final BlockingQueue<ZDDNumber> zns, final ZDDTerm zt)
    {
        final ZDDNumber zn;

        try {
            zn = zns.take();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new Runnable() {
            @Override
            public void run()
            {
                try {
                    zns.put(addSubtrees(zt, filter, zn));
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static void awaitTermination(final ExecutorService es)
    {
        es.shutdown();
        try {
            es.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static ZDDNumber sumSubtrees(final Iterable<ZDDTerm> i)
    {
        return sumSubtrees(i.iterator());
    }

    public static ZDDNumber sumSubtrees(final Iterator<ZDDTerm> i)
    {
        final ZDDCacheN _nod = new ZDDCacheN();
        final ZDDCacheP _equ = new ZDDCacheP();
        final ZDDCacheO _cru = new ZDDCacheO();
        final ZDDCacheO _uni = new ZDDCacheO();
        final ZDDCacheO _int = new ZDDCacheO();
        final ZDDCacheO _dif = new ZDDCacheO();

        ZDDNumber zn = ZERO;

        while (i.hasNext()) {
            zn = addSubtrees(i.next(), zn, _nod, _equ, _cru, _uni, _int, _dif);
        }

        return zn;
    }

    public static ZDDNumber sumSubtrees(final ZDD filter, final Iterable<ZDDTerm> i)
    {
        return sumSubtrees(filter, i.iterator());
    }

    public static ZDDNumber sumSubtrees(final ZDD filter, final Iterator<ZDDTerm> i)
    {
        final ZDDCacheN _nod = new ZDDCacheN();
        final ZDDCacheP _equ = new ZDDCacheP();
        final ZDDCacheO _cru = new ZDDCacheO();
        final ZDDCacheO _uni = new ZDDCacheO();
        final ZDDCacheO _int = new ZDDCacheO();
        final ZDDCacheO _dif = new ZDDCacheO();

        ZDDNumber zn = ZERO;

        while (i.hasNext()) {
            zn = addSubtrees(filter, i.next(), zn, _nod, _equ, _cru, _uni, _int, _dif);
        }

        return zn;
    }

}
