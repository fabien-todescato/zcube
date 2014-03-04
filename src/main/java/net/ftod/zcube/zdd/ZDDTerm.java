package net.ftod.zcube.zdd;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public final class ZDDTerm {

    public final long l;
    public final ZDDTreeL t;

    private ZDDTerm(final long l, final ZDDTreeL t) {
        super();
        this.l = l;
        this.t = t;
    }

    public static ZDDTerm times(final long l, final ZDDTree t)
    {
        return new ZDDTerm(l, ZDDTree.treeL(t));
    }

    ZDDNumber trees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni)
    {
        return ZDDNumber.negabinary(l, ZDDTreeL.trees(t, _nod, _equ, _cru, _uni));
    }

    public static ZDDNumber subtrees(final ZDDTerm zt)
    {
        return zt.subtrees(new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), new ZDDCacheO());
    }

    ZDDNumber subtrees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni)
    {
        return ZDDNumber.negabinary(l, ZDDTreeL.subtrees(t, _nod, _equ, _cru, _uni));
    }

    public static ZDDNumber subtrees(final ZDD z, final ZDDTerm zt)
    {
        return zt.subtrees(new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), new ZDDCacheO(), new ZDDCacheO(), z);
    }

    ZDDNumber subtrees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final ZDDCacheO _int, final ZDD filter)
    {
        return ZDDNumber.negabinary(l, ZDD.intersection(_nod, _equ, _int, filter, ZDDTreeL.subtrees(t, _nod, _equ, _cru, _uni)));
    }

    public void write(final DataOutputStream dos) throws IOException
    {
        dos.writeLong(l);
        t.write(dos);
    }

    public static void write(final Iterable<ZDDTerm> i, final File file) throws IOException
    {
        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        try {
            write(i, dos);
        } finally {
            dos.close();
        }
    }

    public static void write(final Iterable<ZDDTerm> i, final DataOutputStream dos) throws IOException
    {
        write(i.iterator(), dos);
    }

    public static void write(final Iterator<ZDDTerm> i, final DataOutputStream dos) throws IOException
    {
        while (i.hasNext()) {
            i.next().write(dos);
        }
    }

    public static ZDDTerm read(final DataInputStream dis) throws IOException
    {
        final long l = dis.readLong();
        final ZDDTreeL t = ZDDTreeL.read(dis);

        return new ZDDTerm(l, t);
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append("( times ").append(l).append(' ').append(t).append(" )").toString();
    }
}
