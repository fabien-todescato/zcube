package net.ftod.zcube.zdd;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ZDDLong {

    public final long l;
    public final ZDDTree t;

    public ZDDLong(final long l, final ZDDTree t) {
        super();
        this.l = l;
        this.t = t;
    }

    public void write(final DataOutputStream dos) throws IOException
    {
        dos.writeLong(l);
        t.write(dos);
    }

    public static void write(final Iterable<ZDDLong> i, final File file) throws IOException
    {
        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        try {
            write(i, dos);
        } finally {
            dos.close();
        }
    }

    public static void write(final Iterable<ZDDLong> i, final DataOutputStream dos) throws IOException
    {
        write(i.iterator(), dos);
    }

    public static void write(final Iterator<ZDDLong> i, final DataOutputStream dos) throws IOException
    {
        while (i.hasNext()) {
            i.next().write(dos);
        }
    }

    public static ZDDLong read(final DataInputStream dis) throws IOException
    {
        final long l = dis.readLong();
        final ZDDTree t = ZDDTree.read(dis);

        return new ZDDLong(l, t);
    }

    public static ZDDNumber sumSubtrees(final Iterable<ZDDLong> i)
    {
        return sumSubtrees(i.iterator());
    }

    public static ZDDNumber sumSubtrees(final Iterator<ZDDLong> i)
    {
        final ZDDCachePredicate _equ = new ZDDCachePredicate();
        final ZDDCacheOperation _cru = new ZDDCacheOperation();
        final ZDDCacheOperation _uni = new ZDDCacheOperation();
        final ZDDCacheOperation _int = new ZDDCacheOperation();
        final ZDDCacheOperation _dif = new ZDDCacheOperation();

        ZDDNumber zn = null;

        while (i.hasNext()) {
            final ZDDLong zl = i.next();
            zn = ZDDNumber.addSubtrees(zl.l, zl.t, zn, _equ, _cru, _uni, _int, _dif);
        }

        return zn;
    }

    public static ZDDNumber sumSubtrees(final ZDD filter, final Iterator<ZDDLong> i)
    {
        final ZDDCachePredicate _equ = new ZDDCachePredicate();
        final ZDDCacheOperation _cru = new ZDDCacheOperation();
        final ZDDCacheOperation _uni = new ZDDCacheOperation();
        final ZDDCacheOperation _int = new ZDDCacheOperation();
        final ZDDCacheOperation _dif = new ZDDCacheOperation();

        ZDDNumber zn = null;

        while (i.hasNext()) {
            final ZDDLong zl = i.next();
            zn = ZDDNumber.addSubtrees(zl.l, zl.t, filter, zn, _equ, _cru, _uni, _int, _dif);
        }

        return zn;
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
        final BlockingQueue<PSum> pSumQueue = new ArrayBlockingQueue<PSum>(sums);

        for (int j = 0; j < sums; ++j) {
            pSumQueue.offer(new PSum());
        }

        try {

            while (i.hasNext()) {
                threadPool.submit(new PSumTask(pSumQueue.take(), pSumQueue, i.next()));
            }

            threadPool.shutdown();
            threadPool.awaitTermination(100L, TimeUnit.MILLISECONDS);

            return ZDDNumber.negabinaryAdd(new Iterator<ZDDNumber>() {

                private final Iterator<PSum> i = pSumQueue.iterator();

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

}

final class PSum {

    private final ZDDCachePredicate _equ = new ZDDCachePredicate();
    private final ZDDCacheOperation _cru = new ZDDCacheOperation();
    private final ZDDCacheOperation _uni = new ZDDCacheOperation();
    private final ZDDCacheOperation _int = new ZDDCacheOperation();
    private final ZDDCacheOperation _dif = new ZDDCacheOperation();

    ZDDNumber zn = null;

    PSum() {
        super();
    }

    void addSubtrees(final ZDDLong zl)
    {
        zn = ZDDNumber.addSubtrees(zl.l, zl.t, zn, _equ, _cru, _uni, _int, _dif);
    }
}

final class PSumTask implements Runnable {

    private final PSum pSum;
    private final BlockingQueue<PSum> sumQueue;
    private final ZDDLong zl;

    PSumTask(final PSum pSum, final BlockingQueue<PSum> sumQueue, final ZDDLong zl) {
        super();
        this.pSum = pSum;
        this.sumQueue = sumQueue;
        this.zl = zl;
    }

    @Override
    public void run()
    {
        try {
            pSum.addSubtrees(zl);
            sumQueue.put(pSum);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
