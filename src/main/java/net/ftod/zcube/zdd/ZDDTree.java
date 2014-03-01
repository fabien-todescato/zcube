package net.ftod.zcube.zdd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * <h1>Algebra of sets of trees</h1>
 * 
 * <p>
 * Representation of sets of trees that support binary serialization, and functions to compute set of trees and set of subtrees as {@link ZDD}.
 * </p>
 * 
 * @author <a href="mailto:fabien.todescato@gmail.com">Fabien Todescato</a>
 */
public abstract class ZDDTree {

    /**
     * Empty set of trees.
     */
    public static final ZDDTree BOT = new ZDDTree() {

        @Override
        protected ZDDTreeL treeL(final long h)
        {
            return ZDDTreeL.top();
        }

        @Override
        protected ZDD trees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
        {
            return ZDD.BOT;
        }

        @Override
        protected ZDD subtrees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
        {
            return ZDD.BOT;
        }

        @Override
        protected Type type()
        {
            return Type.BOT;
        }

        @Override
        protected void _write(final DataOutputStream dos) throws IOException
        {
            // Nothing to write
        }

        @Override
        public String toString()
        {
            return "bot";
        }
    };

    /**
     * Singleton containing the empty tree.
     */
    public static final ZDDTree TOP = new ZDDTree() {

        @Override
        protected ZDDTreeL treeL(final long h)
        {
            return ZDDTreeL.bot();
        }

        @Override
        protected ZDD trees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
        {
            return ZDD.TOP;
        }

        @Override
        protected ZDD subtrees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
        {
            return ZDD.TOP;
        }

        @Override
        protected Type type()
        {
            return Type.TOP;
        }

        @Override
        protected void _write(final DataOutputStream dos) throws IOException
        {
            // Nothing to write
        }

        @Override
        public String toString()
        {
            return "top";
        }
    };

    /**
     * Prefix a {@link ZDDTree} with a symbol.
     */
    public static ZDDTree prefix(final String p, final ZDDTree t)
    {
        return new ZDDTreePrefix(p, t);
    }

    /**
     * Prefix a {@link ZDDTree} with a path represented as a sequence of symbols.
     */
    public static ZDDTree prefix(final ZDDTree t, final String... p)
    {
        return prefix(Arrays.asList(p), t);
    }

    /**
     * Prefix a {@link ZDDTree} with a path represented as a sequence of symbols.
     */
    public static ZDDTree prefix(final Iterable<String> p, final ZDDTree t)
    {
        return prefix(p.iterator(), t);
    }

    /**
     * Prefix a {@link ZDDTree} with a path represented as a sequence of symbols.
     */
    public static ZDDTree prefix(final Iterator<String> p, final ZDDTree t)
    {
        if (p.hasNext()) {
            return new ZDDTreePrefix(p.next(), prefix(p, t));
        }

        return t;
    }

    /**
     * Build a path, a linear tree, from a sequence of strings.
     */
    public static ZDDTree path(final String... p)
    {
        return path(Arrays.asList(p));
    }

    /**
     * Build a path, a linear tree, from a sequence of strings.
     */
    public static ZDDTree path(final Iterable<String> p)
    {
        return path(p.iterator());
    }

    /**
     * Build a path, a linear tree, from a sequence of strings.
     */
    public static ZDDTree path(final Iterator<String> p)
    {
        return prefix(p, TOP);
    }

    /**
     * <h3>Cross-product of a sequence of {@link ZDDTree}</h3>
     */
    public static ZDDTree cross(final ZDDTree... ts)
    {
        return new ZDDTreeCross(ts);
    }

    /**
     * <h3>Cross-product of a {@link Collection} of {@link ZDDTree}</h3>
     */
    public static ZDDTree cross(final Collection<ZDDTree> ts)
    {
        return cross(array(ts));
    }

    /**
     * <h3>Sum of a sequence of {@link ZDDTree}</h3>
     */
    public static ZDDTree sum(final ZDDTree... ts)
    {
        return new ZDDTreeSum(ts);
    }

    /**
     * <h3>Product of a {@link Collection} of {@link ZDDTree}</h3>
     */
    public static ZDDTree sum(final Collection<ZDDTree> ts)
    {
        return sum(array(ts));
    }

    public static ZDDTreeL treeL(final ZDDTree t)
    {
        return t.treeL(0L);
    }

    protected static final ZDDTreeL[] treeL(final long h, final ZDDTree[] ts)
    {
        final int n = ts.length;
        final ZDDTreeL[] tsl = new ZDDTreeL[n];

        for (int i = 0; i < n; ++i) {
            tsl[i] = ts[i].treeL(h);
        }

        return tsl;
    }

    protected abstract ZDDTreeL treeL(long h);

    /**
     * <h3>Set of trees generated by a {@link ZDDTree}</h3>
     * 
     * @param t
     *            the {@link ZDDTree}
     * @return the set of trees represented as a {@link ZDD}
     */
    public static ZDD trees(final ZDDTree t)
    {
        return trees(t, new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), new ZDDCacheO());
    }

    static ZDD trees(final ZDDTree t, final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni)
    {
        return t.trees(_nod, _equ, _cru, _uni, 1L);
    }

    /**
     * <h3>Set of the trees generated by a {@link Collection} of {@link ZDDTree}</h3>
     * 
     * <p>
     * This function is useful to compute the {@link ZDD} used in filtering a set of trees before aggregation.
     * </p>
     * 
     * @param ts
     *            the {@link Collection} of {@link ZDDTree}
     * @return the set of the trees represented as a {@link ZDD}
     */
    public static ZDD unionTrees(final ZDDTree... ts)
    {
        return unionTrees(ts, new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), new ZDDCacheO());
    }

    static ZDD unionTrees(final ZDDTree[] ts, final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni)
    {
        final int n = ts.length;
        final ZDD[] zs = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            zs[i] = trees(ts[i], _nod, _equ, _cru, _uni);
        }

        return ZDD.union(_nod, _equ, _uni, zs);
    }

    /**
     * <h3>Set of subtrees generated by a {@link ZDDTree}</h3>
     * 
     * @param t
     *            the {@link ZDDTree}
     * @return the set of subtrees represented as a {@link ZDD}
     */
    public static ZDD subtrees(final ZDDTree t)
    {
        return subtrees(t, new ZDDCacheN(), new ZDDCacheP(), new ZDDCacheO(), new ZDDCacheO());
    }

    static ZDD subtrees(final ZDDTree t, final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni)
    {
        return t.subtrees(_nod, _equ, _cru, _uni, 1L);
    }

    /**
     * <h3>Set of subtrees generated by a {@link ZDDTree}</h3>
     * 
     * @param z
     *            a {@link ZDD} representing a set of trees, acting as a filter against the set of subtrees generated by the tree.
     * @param t
     *            the {@link ZDDTree} the subtrees of which are computed and filtered.
     * @return the {@link ZDD} for the set of subtrees generated by the tree, and filtered by the set of trees.
     */
    public static ZDD subtrees(final ZDD z, final ZDDTree t)
    {
        final ZDDCacheN _nod = new ZDDCacheN();
        final ZDDCacheP _equ = new ZDDCacheP();
        final ZDDCacheO _cru = new ZDDCacheO();
        final ZDDCacheO _uni = new ZDDCacheO();
        final ZDDCacheO _int = new ZDDCacheO();

        return ZDD.intersection(_nod, _equ, _int, z, subtrees(t, _nod, _equ, _cru, _uni));
    }

    protected abstract ZDD trees(ZDDCacheN _nod, ZDDCacheP _equ, ZDDCacheO _cru, ZDDCacheO _uni, long h);

    protected abstract ZDD subtrees(ZDDCacheN _nod, ZDDCacheP _equ, ZDDCacheO _cru, ZDDCacheO _uni, long h);

    protected static final ZDD[] mapTrees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h, final ZDDTree[] ts)
    {
        final int n = ts.length;
        final ZDD[] zdds = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            zdds[i] = ts[i].trees(_nod, _equ, _cru, _uni, h);
        }

        return zdds;
    }

    protected static final ZDD[] mapSubtrees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h, final ZDDTree[] ts)
    {
        final int n = ts.length;
        final ZDD[] zdds = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            zdds[i] = ts[i].subtrees(_nod, _equ, _cru, _uni, h);
        }

        return zdds;
    }

    private static ZDDTree[] array(final Collection<ZDDTree> c)
    {
        final ZDDTree[] a = new ZDDTree[c.size()];
        c.toArray(a);
        return a;
    }

    protected enum Type {
        @SuppressWarnings("hiding")
        BOT {
            @Override
            protected ZDDTree read(final DataInputStream dis)
            {
                return ZDDTree.BOT;
            }
        },
        @SuppressWarnings("hiding")
        TOP {
            @Override
            protected ZDDTree read(final DataInputStream dis)
            {
                return ZDDTree.TOP;
            }
        },
        PREFIX {
            @Override
            protected ZDDTree read(final DataInputStream dis) throws IOException
            {
                return ZDDTreePrefix._read(dis);
            }
        },
        CROSS {
            @Override
            protected ZDDTree read(final DataInputStream dis) throws IOException
            {
                return ZDDTreeCross._read(dis);
            }
        },
        SUM {
            @Override
            protected ZDDTree read(final DataInputStream dis) throws IOException
            {
                return ZDDTreeSum._read(dis);
            }
        };

        protected abstract ZDDTree read(DataInputStream dis) throws IOException;
    }

    protected abstract Type type();

    /**
     * <h3>Read back {@link ZDDTree} from {@link DataInputStream}</h3>
     */
    public static ZDDTree read(final DataInputStream dis) throws IOException
    {
        return Type.values()[dis.readByte()].read(dis);
    }

    /**
     * <h3>Write a {@link ZDDTree} to a {@link DataOutputStream}</h3>
     */
    public void write(final DataOutputStream dos) throws IOException
    {
        dos.writeByte(type().ordinal());
        _write(dos);
    }

    protected abstract void _write(DataOutputStream dos) throws IOException;

    /**
     * <h3>Read back an array of {@link ZDDTree} from a {@link DataInputStream}</h3>
     */
    public static ZDDTree[] readArray(final DataInputStream dis) throws IOException
    {
        final int length = dis.readInt();
        final ZDDTree[] array = new ZDDTree[length];

        for (int i = 0; i < length; ++i) {
            array[i] = read(dis);
        }

        return array;
    }

    /**
     * <h3>Write an array of {@link ZDDTree} to a {@link DataOutputStream}</h3>
     */
    public static void writeArray(final ZDDTree[] ts, final DataOutputStream dos) throws IOException
    {
        final int length = ts.length;

        dos.writeInt(length);

        for (int i = 0; i < length; ++i) {
            ts[i].write(dos);
        }
    }

}

final class ZDDTreePrefix extends ZDDTree {

    private final String prefix;
    private final ZDDTree treeSet;

    ZDDTreePrefix(final String prefix, final ZDDTree treeSet) {
        super();
        this.treeSet = treeSet;
        this.prefix = prefix;
    }

    static ZDDTree _read(final DataInputStream dis) throws IOException
    {
        final String prefix = dis.readUTF();
        final ZDDTree treeSet = read(dis);

        return new ZDDTreePrefix(prefix, treeSet);
    }

    @Override
    protected void _write(final DataOutputStream dos) throws IOException
    {
        dos.writeUTF(prefix);
        treeSet.write(dos);
    }

    @Override
    protected ZDD trees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
    {
        final long h1 = ZDDTreePrefix.djb2(h, prefix);

        return ZDD.crossUnion(_nod, _equ, _cru, _uni, ZDD.singleton(_nod, h1), treeSet.trees(_nod, _equ, _cru, _uni, h1));
    }

    @Override
    protected ZDD subtrees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
    {
        final long h1 = ZDDTreePrefix.djb2(h, prefix);

        return ZDD.union(_nod, _equ, _uni, ZDD.TOP, ZDD.crossUnion(_nod, _equ, _cru, _uni, ZDD.singleton(_nod, h1), treeSet.subtrees(_nod, _equ, _cru, _uni, h1)));
    }

    @Override
    protected Type type()
    {
        return Type.PREFIX;
    }

    /**
     * <h3>Hashing a {@link String}, starting from a <code>long</code> seed</h3>
     * 
     * <p>
     * This hashing scheme is used to generate <code>long</code> identifiers for the nodes of the trees. The hash for a child node is computed by taking the
     * hash of the father node and combining it with the label of the branch from the father node to the child node.
     * </p>
     * <p>
     * See <a href="http://programmers.stackexchange.com/questions/49550/which-hashing-algorithm-is-best-for-uniqueness-and-speed">Which hashing algorithm is
     * best for uniqueness and speed</a> for an overview of hashing, and pointers on the <code>djb2</code> hash function.
     * </p>
     */
    private static long djb2(final long seed, final String string)
    {
        long hash = 5381L;

        hash = 33L * hash ^ seed >>> 56 & 0xFF;
        hash = 33L * hash ^ seed >>> 48 & 0xFF;
        hash = 33L * hash ^ seed >>> 40 & 0xFF;
        hash = 33L * hash ^ seed >>> 32 & 0xFF;
        hash = 33L * hash ^ seed >>> 24 & 0xFF;
        hash = 33L * hash ^ seed >>> 16 & 0xFF;
        hash = 33L * hash ^ seed >>> 8 & 0xFF;
        hash = 33L * hash ^ seed & 0xFF;

        for (int i = 0; i < string.length(); ++i) {
            hash = 33L * hash ^ string.charAt(i);
        }

        return hash;
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append("( prefix \"").append(prefix).append("\" ").append(treeSet).append(" )").toString();
    }

    @Override
    protected ZDDTreeL treeL(final long h)
    {
        final long h1 = djb2(h, prefix);
        return ZDDTreeL.node(h1, treeSet.treeL(h1));
    }

}

final class ZDDTreeCross extends ZDDTree {

    private final ZDDTree[] ts;

    ZDDTreeCross(final ZDDTree[] ts) {
        super();
        this.ts = ts;
    }

    static ZDDTreeCross _read(final DataInputStream dis) throws IOException
    {
        return new ZDDTreeCross(readArray(dis));
    }

    @Override
    protected void _write(final DataOutputStream dos) throws IOException
    {
        writeArray(ts, dos);
    }

    @Override
    protected ZDD trees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
    {
        return ZDD.crossUnion(_nod, _equ, _cru, _uni, mapTrees(_nod, _equ, _cru, _uni, h, ts));
    }

    @Override
    protected ZDD subtrees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
    {
        return ZDD.crossUnion(_nod, _equ, _cru, _uni, mapSubtrees(_nod, _equ, _cru, _uni, h, ts));
    }

    @Override
    protected Type type()
    {
        return Type.CROSS;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("( cross");
        for (final ZDDTree element : ts) {
            sb.append(' ').append(element);
        }
        sb.append(" )");

        return sb.toString();
    }

    @Override
    protected ZDDTreeL treeL(final long h)
    {
        return ZDDTreeL.cross(treeL(h, ts));
    }

}

final class ZDDTreeSum extends ZDDTree {

    private final ZDDTree[] ts;

    ZDDTreeSum(final ZDDTree[] ts) {
        super();
        this.ts = ts;
    }

    static ZDDTreeSum _read(final DataInputStream dis) throws IOException
    {
        return new ZDDTreeSum(readArray(dis));
    }

    @Override
    protected void _write(final DataOutputStream dos) throws IOException
    {
        writeArray(ts, dos);
    }

    @Override
    protected ZDD trees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
    {
        return ZDD.union(_nod, _equ, _uni, mapTrees(_nod, _equ, _cru, _uni, h, ts));
    }

    @Override
    protected ZDD subtrees(final ZDDCacheN _nod, final ZDDCacheP _equ, final ZDDCacheO _cru, final ZDDCacheO _uni, final long h)
    {
        return ZDD.union(_nod, _equ, _uni, mapSubtrees(_nod, _equ, _cru, _uni, h, ts));
    }

    @Override
    protected Type type()
    {
        return Type.SUM;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("( sum");
        for (final ZDDTree element : ts) {
            sb.append(' ').append(element);
        }
        sb.append(" )");

        return sb.toString();
    }

    @Override
    protected ZDDTreeL treeL(final long h)
    {
        return ZDDTreeL.sum(treeL(h, ts));
    }

}