package net.ftod.zcube.zdd;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ZDDLongPSum {

    public static ZDDNumber sumSubtrees(final Iterable<ZDDLong> i)
    {
        return sumSubtrees(i.iterator());
    }

    public static ZDDNumber sumSubtrees(final Iterator<ZDDLong> i)
    {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int sums = 2 * processors;

        final ExecutorService threadPool = Executors.newFixedThreadPool(processors);
        final BlockingQueue<ZDDLongPSum> pSumQueue = new ArrayBlockingQueue<ZDDLongPSum>(sums);

        for (int j = 0; j < sums; ++j) {
            pSumQueue.offer(new ZDDLongPSum());
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

    private static Runnable sumTask(final BlockingQueue<ZDDLongPSum> pSumQueue, final ZDDLong zl)
    {
        final ZDDLongPSum pSum;

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

    public static ZDDNumber sumSubtrees(final ZDD filter, final Iterable<ZDDLong> i)
    {
        return sumSubtrees(filter, i.iterator());
    }

    public static ZDDNumber sumSubtrees(final ZDD filter, final Iterator<ZDDLong> i)
    {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int sums = 2 * processors;

        final ExecutorService threadPool = Executors.newFixedThreadPool(processors);
        final BlockingQueue<ZDDLongPSum> pSumQueue = new ArrayBlockingQueue<ZDDLongPSum>(sums);

        for (int j = 0; j < sums; ++j) {
            pSumQueue.offer(new ZDDLongPSum());
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

    private static Runnable sumTask(final ZDD filter, final BlockingQueue<ZDDLongPSum> pSumQueue, final ZDDLong zl)
    {
        final ZDDLongPSum pSum;

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

    private static ZDDNumber sum(final BlockingQueue<ZDDLongPSum> pSumQueue)
    {
        final Iterator<ZDDLongPSum> i = pSumQueue.iterator();

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

    private final ZDDCachePredicate _equ = new ZDDCachePredicate();
    private final ZDDCacheOperation _cru = new ZDDCacheOperation();
    private final ZDDCacheOperation _uni = new ZDDCacheOperation();
    private final ZDDCacheOperation _int = new ZDDCacheOperation();
    private final ZDDCacheOperation _dif = new ZDDCacheOperation();

    private ZDDNumber zn = null;

    private ZDDLongPSum() {
        super();
    }

    private void addSubtrees(final ZDDLong zl)
    {
        zn = ZDDNumber.addSubtrees(zl.l, zl.t, zn, _equ, _cru, _uni, _int, _dif);
    }

    private void addSubtrees(final ZDD filter, final ZDDLong zl)
    {
        zn = ZDDNumber.addSubtrees(zl.l, zl.t, filter, zn, _equ, _cru, _uni, _int, _dif);
    }

}
