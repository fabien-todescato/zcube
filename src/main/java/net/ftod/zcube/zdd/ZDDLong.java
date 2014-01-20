package net.ftod.zcube.zdd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    public static ZDDLong read(final DataInputStream dis) throws IOException
    {
        final long l = dis.readLong();
        final ZDDTree t = ZDDTree.read(dis);

        return new ZDDLong(l, t);
    }
}
