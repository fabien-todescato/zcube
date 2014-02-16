package net.ftod.zcube.zdd;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ZDDLongPSum {

    public static ZDDNumber reduce(final Iterable<ZDDLong> i)
    {
        return reduce(i.iterator());
    }

    public static ZDDNumber reduce(final Iterator<ZDDLong> i)
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
                threadPool.submit(new Runnable() {

                    private final ZDDLongPSum pSum = pSumQueue.take();
                    private final ZDDLong zl = i.next();

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
                });
            }

            threadPool.shutdown();
            threadPool.awaitTermination(100L, TimeUnit.MILLISECONDS);

            return ZDDNumber.negabinaryAdd(new Iterator<ZDDNumber>() {

                private final Iterator<ZDDLongPSum> i = pSumQueue.iterator();

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
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
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

}
