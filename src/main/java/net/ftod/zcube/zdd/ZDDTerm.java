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
    public final ZDDTree t;

    public ZDDTerm(final long l, final ZDDTree t) {
        super();
        this.l = l;
        this.t = t;
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
        final ZDDTree t = ZDDTree.read(dis);

        return new ZDDTerm(l, t);
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append("( times ").append(l).append(' ').append(t).append(" )").toString();
    }
}
