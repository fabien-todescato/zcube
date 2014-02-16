package net.ftod.zcube.zdd;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

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

}
