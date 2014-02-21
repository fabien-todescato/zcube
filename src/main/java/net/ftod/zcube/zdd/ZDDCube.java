package net.ftod.zcube.zdd;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ZDDCube {

    public static long[] pSumGroupBy(final ZDDTree[] ts, final Iterable<ZDDLong> i)
    {
        final int n = ts.length;
        final ZDD[] zs = new ZDD[n];
        final ZDD u;
        {
            final ZDDCachePredicate eq = new ZDDCachePredicate();
            final ZDDCacheOperation cu = new ZDDCacheOperation();
            final ZDDCacheOperation un = new ZDDCacheOperation();

            for (int j = 0; j < n; ++j) {
                zs[j] = ZDDTree.trees(ts[j], eq, cu, un);
            }

            u = ZDD.union(eq, un, zs);
        }

        final ZDDNumber zn = pSumSubtrees(u, i);

        final long[] ls = new long[n];

        for (int j = 0; j < n; ++j) {
            ls[j] = ZDDNumber.negabinary(zn, zs[j]);
        }

        return ls;
    }

    public static ZDDNumber pSumSubtrees(final Iterable<ZDDLong> i)
    {
        return pSumSubtrees(i.iterator());
    }

    public static ZDDNumber pSumSubtrees(final Iterator<ZDDLong> i)
    {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int sums = 2 * processors;

        final ExecutorService threadPool = Executors.newFixedThreadPool(processors);
        final BlockingQueue<ZDDCube> pSumQueue = new ArrayBlockingQueue<ZDDCube>(sums);

        for (int j = 0; j < sums; ++j) {
            pSumQueue.offer(new ZDDCube());
        }

        try {

            while (i.hasNext()) {
                threadPool.submit(sumTask(pSumQueue, i.next()));
            }

            threadPool.shutdown();
            threadPool.awaitTermination(100L, TimeUnit.MILLISECONDS);

            return sum(pSumQueue);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Runnable sumTask(final BlockingQueue<ZDDCube> pSumQueue, final ZDDLong zl)
    {
        final ZDDCube pSum;

        try {
            pSum = pSumQueue.take();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new Runnable() {
            @Override
            public void run()
            {
                pSum.addSubtrees(zl);
                try {
                    pSumQueue.put(pSum);
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static ZDDNumber pSumSubtrees(final ZDD filter, final Iterable<ZDDLong> i)
    {
        return pSumSubtrees(filter, i.iterator());
    }

    public static ZDDNumber pSumSubtrees(final ZDD filter, final Iterator<ZDDLong> i)
    {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int sums = 2 * processors;

        final ExecutorService threadPool = Executors.newFixedThreadPool(processors);
        final BlockingQueue<ZDDCube> pSumQueue = new ArrayBlockingQueue<ZDDCube>(sums);

        for (int j = 0; j < sums; ++j) {
            pSumQueue.offer(new ZDDCube());
        }

        try {

            while (i.hasNext()) {
                threadPool.submit(sumTask(filter, pSumQueue, i.next()));
            }

            threadPool.shutdown();
            threadPool.awaitTermination(100L, TimeUnit.MILLISECONDS);

            return sum(pSumQueue);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Runnable sumTask(final ZDD filter, final BlockingQueue<ZDDCube> pSumQueue, final ZDDLong zl)
    {
        final ZDDCube pSum;

        try {
            pSum = pSumQueue.take();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new Runnable() {
            @Override
            public void run()
            {
                pSum.addSubtrees(filter, zl);
                try {
                    pSumQueue.put(pSum);
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static ZDDNumber sum(final BlockingQueue<ZDDCube> pSumQueue)
    {
        final Iterator<ZDDCube> i = pSumQueue.iterator();

        return ZDDNumber.negabinaryAdd(new Iterator<ZDDNumber>() {

            @Override
            public boolean hasNext()
            {
                return i.hasNext();
            }

            @Override
            public ZDDNumber next()
            {
                return i.next().zn;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        });
    }

    public static ZDDNumber sumSubtrees(final Iterable<ZDDLong> i)
    {
        return sumSubtrees(i.iterator());
    }

    public static ZDDNumber sumSubtrees(final Iterator<ZDDLong> i)
    {
        return new ZDDCube()._sumSubtrees(i);
    }

    private ZDDNumber _sumSubtrees(final Iterator<ZDDLong> i)
    {
        final ZDDCachePredicate _equ = new ZDDCachePredicate();
        final ZDDCacheOperation _cru = new ZDDCacheOperation();
        final ZDDCacheOperation _uni = new ZDDCacheOperation();
        final ZDDCacheOperation _int = new ZDDCacheOperation();
        final ZDDCacheOperation _dif = new ZDDCacheOperation();

        ZDDNumber zn = ZDDNumber.ZERO;

        while (i.hasNext()) {
            final ZDDLong zl = i.next();
            zn = ZDDNumber.addSubtrees(zl.l, zl.t, zn, _equ, _cru, _uni, _int, _dif);
        }

        return zn;
    }

    public static ZDDNumber sumSubtrees(final ZDD filter, final Iterable<ZDDLong> i)
    {
        return sumSubtrees(filter, i.iterator());
    }

    public static ZDDNumber sumSubtrees(final ZDD filter, final Iterator<ZDDLong> i)
    {
        return new ZDDCube()._sumSubtrees(filter, i);
    }

    private ZDDNumber _sumSubtrees(final ZDD filter, final Iterator<ZDDLong> i)
    {
        final ZDDCachePredicate _equ = new ZDDCachePredicate();
        final ZDDCacheOperation _cru = new ZDDCacheOperation();
        final ZDDCacheOperation _uni = new ZDDCacheOperation();
        final ZDDCacheOperation _int = new ZDDCacheOperation();
        final ZDDCacheOperation _dif = new ZDDCacheOperation();

        ZDDNumber zn = ZDDNumber.ZERO;

        while (i.hasNext()) {
            final ZDDLong zl = i.next();
            zn = ZDDNumber.addSubtrees(zl.l, zl.t, filter, zn, _equ, _cru, _uni, _int, _dif);
        }

        return zn;
    }

    private ZDDNumber zn = ZDDNumber.ZERO;

    private ZDDCube() {
        super();
    }

    private void addSubtrees(final ZDDLong zl)
    {
        final ZDDCachePredicate _equ = new ZDDCachePredicate();
        final ZDDCacheOperation _cru = new ZDDCacheOperation();
        final ZDDCacheOperation _uni = new ZDDCacheOperation();
        final ZDDCacheOperation _int = new ZDDCacheOperation();
        final ZDDCacheOperation _dif = new ZDDCacheOperation();

        zn = ZDDNumber.addSubtrees(zl.l, zl.t, zn, _equ, _cru, _uni, _int, _dif);
    }

    private void addSubtrees(final ZDD filter, final ZDDLong zl)
    {
        final ZDDCachePredicate _equ = new ZDDCachePredicate();
        final ZDDCacheOperation _cru = new ZDDCacheOperation();
        final ZDDCacheOperation _uni = new ZDDCacheOperation();
        final ZDDCacheOperation _int = new ZDDCacheOperation();
        final ZDDCacheOperation _dif = new ZDDCacheOperation();

        zn = ZDDNumber.addSubtrees(zl.l, zl.t, filter, zn, _equ, _cru, _uni, _int, _dif);
    }

}
