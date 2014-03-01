package net.ftod.zcube.zdd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

abstract class ZDDTreeL {

    static final ZDDTreeL bot()
    {
        return ZDDTreeLBOT.INSTANCE;
    }

    static final ZDDTreeL top()
    {
        return ZDDTreeLTOP.INSTANCE;
    }

    static final ZDDTreeL node(final long node, final ZDDTreeL tree)
    {
        return new ZDDTreeLNode(node, tree);
    }

    static final ZDDTreeL sum(final ZDDTreeL... trees)
    {
        return new ZDDTreeLSum(trees);
    }

    static final ZDDTreeL cross(final ZDDTreeL... trees)
    {
        return new ZDDTreeLCross(trees);
    }

    protected enum Type {

        BOT {
            @Override
            ZDDTreeL read(final DataInputStream dis)
            {
                return ZDDTreeLBOT._read(dis);
            }
        },
        TOP {
            @Override
            ZDDTreeL read(final DataInputStream dis)
            {
                return ZDDTreeLTOP._read(dis);
            }
        },
        NODE {
            @Override
            ZDDTreeL read(final DataInputStream dis) throws IOException
            {
                return ZDDTreeLNode._read(dis);
            }
        },
        CROSS {
            @Override
            ZDDTreeL read(final DataInputStream dis) throws IOException
            {
                return ZDDTreeLCross._read(dis);
            }
        },
        SUM {
            @Override
            ZDDTreeL read(final DataInputStream dis) throws IOException
            {
                return ZDDTreeLSum._read(dis);
            }
        };

        abstract ZDDTreeL read(DataInputStream dis) throws IOException;

    }

    protected abstract Type type();

    protected abstract void _write(DataOutputStream dos) throws IOException;

    public final void write(final DataOutputStream dos) throws IOException
    {
        dos.writeByte(type().ordinal());
        _write(dos);
    }

    public static final ZDDTreeL read(final DataInputStream dis) throws IOException
    {
        return Type.values()[dis.readByte()].read(dis);
    }

    protected static final void write(final ZDDTreeL[] ts, final DataOutputStream dos) throws IOException
    {
        final int n = ts.length;
        dos.writeInt(n);
        for (int i = 0; i < n; ++i) {
            ts[i].write(dos);
        }
    }

    protected static final ZDDTreeL[] readArray(final DataInputStream dis) throws IOException
    {
        final int n = dis.readInt();
        final ZDDTreeL[] ts = new ZDDTreeL[n];
        for (int i = 0; i < n; ++i) {
            ts[i] = read(dis);
        }
        return ts;
    }

    protected abstract ZDD trees(ZDDCacheN nod, ZDDCacheP eq, ZDDCacheO cu, ZDDCacheO un);

    protected abstract ZDD subtrees(ZDDCacheN nod, ZDDCacheP eq, ZDDCacheO cu, ZDDCacheO un);

    protected static final ZDD[] mapTrees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un, final ZDDTreeL[] ts)
    {
        final int n = ts.length;
        final ZDD[] zdds = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            zdds[i] = ts[i].trees(nod, eq, cu, un);
        }

        return zdds;
    }

    protected static final ZDD[] mapSubtrees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un, final ZDDTreeL[] ts)
    {
        final int n = ts.length;
        final ZDD[] zdds = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            zdds[i] = ts[i].subtrees(nod, eq, cu, un);
        }

        return zdds;
    }

}

final class ZDDTreeLBOT extends ZDDTreeL {

    static final ZDDTreeLBOT INSTANCE = new ZDDTreeLBOT();

    private ZDDTreeLBOT() {
        super();
    }

    @Override
    protected Type type()
    {
        return Type.BOT;
    }

    @Override
    protected void _write(final DataOutputStream dos)
    {
        // Nothing to write
    }

    static ZDDTreeLBOT _read(@SuppressWarnings("unused") final DataInputStream dis)
    {
        return INSTANCE;
    }

    @Override
    protected ZDD trees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.BOT;
    }

    @Override
    protected ZDD subtrees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.BOT;
    }
}

final class ZDDTreeLTOP extends ZDDTreeL {

    static final ZDDTreeLTOP INSTANCE = new ZDDTreeLTOP();

    private ZDDTreeLTOP() {
        super();
    }

    @Override
    protected Type type()
    {
        return Type.TOP;
    }

    @Override
    protected void _write(final DataOutputStream dos)
    {
        // Nothing to write
    }

    static ZDDTreeLTOP _read(@SuppressWarnings("unused") final DataInputStream dis)
    {
        return INSTANCE;
    }

    @Override
    protected ZDD trees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.TOP;
    }

    @Override
    protected ZDD subtrees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.TOP;
    }
}

final class ZDDTreeLNode extends ZDDTreeL {

    private final long h;
    private final ZDDTreeL t;

    ZDDTreeLNode(final long h, final ZDDTreeL t) {
        super();
        this.h = h;
        this.t = t;
    }

    @Override
    protected Type type()
    {
        return Type.NODE;
    }

    @Override
    protected void _write(final DataOutputStream dos) throws IOException
    {
        dos.writeLong(h);
        t.write(dos);
    }

    static ZDDTreeLNode _read(final DataInputStream dis) throws IOException
    {
        final long node = dis.readLong();
        final ZDDTreeL tree = read(dis);
        return new ZDDTreeLNode(node, tree);
    }

    @Override
    protected ZDD trees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.crossUnion(nod, eq, cu, un, ZDD.singleton(nod, h), t.trees(nod, eq, cu, un));
    }

    @Override
    protected ZDD subtrees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.union(nod, eq, un, ZDD.TOP, ZDD.crossUnion(nod, eq, cu, un, ZDD.singleton(nod, h), t.subtrees(nod, eq, cu, un)));
    }

}

final class ZDDTreeLCross extends ZDDTreeL {

    private final ZDDTreeL[] ts;

    ZDDTreeLCross(final ZDDTreeL[] ts) {
        super();
        this.ts = ts;
    }

    @Override
    protected Type type()
    {
        return Type.CROSS;
    }

    @Override
    protected void _write(final DataOutputStream dos) throws IOException
    {
        write(ts, dos);
    }

    static ZDDTreeLCross _read(final DataInputStream dis) throws IOException
    {
        return new ZDDTreeLCross(readArray(dis));
    }

    @Override
    protected ZDD trees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.crossUnion(nod, eq, cu, un, mapTrees(nod, eq, cu, un, ts));
    }

    @Override
    protected ZDD subtrees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.crossUnion(nod, eq, cu, un, mapSubtrees(nod, eq, cu, un, ts));
    }
}

final class ZDDTreeLSum extends ZDDTreeL {

    private final ZDDTreeL[] ts;

    ZDDTreeLSum(final ZDDTreeL[] ts) {
        super();
        this.ts = ts;
    }

    @Override
    protected Type type()
    {
        return Type.SUM;
    }

    @Override
    protected void _write(final DataOutputStream dos) throws IOException
    {
        write(ts, dos);
    }

    static ZDDTreeLSum _read(final DataInputStream dis) throws IOException
    {
        return new ZDDTreeLSum(readArray(dis));
    }

    @Override
    protected ZDD trees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.union(nod, eq, un, mapTrees(nod, eq, cu, un, ts));
    }

    @Override
    protected ZDD subtrees(final ZDDCacheN nod, final ZDDCacheP eq, final ZDDCacheO cu, final ZDDCacheO un)
    {
        return ZDD.union(nod, eq, un, mapSubtrees(nod, eq, cu, un, ts));
    }
}
