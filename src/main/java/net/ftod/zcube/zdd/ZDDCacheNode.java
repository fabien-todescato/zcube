package net.ftod.zcube.zdd;

import static net.ftod.zcube.zdd.ZDD.CACHE_MAX;
import static net.ftod.zcube.zdd.ZDD.CACHE_SIZE;

final class ZDDCacheNode {

    private final long[] _x = new long[CACHE_SIZE];
    private final ZDD[] _b = new ZDD[CACHE_SIZE];
    private final ZDD[] _t = new ZDD[CACHE_SIZE];
    private final ZDD[] _z = new ZDD[CACHE_SIZE];

    private static int index(final long x, final ZDD b, final ZDD t)
    {
        return 1 + 31 * (Long.valueOf(x).hashCode() + 31 * (b.h + 31 * t.h)) & CACHE_MAX;
    }

    ZDDCacheNode() {
        super();
    }

    ZDD get(final long x, final ZDD b, final ZDD t)
    {
        final int index = index(x, b, t);

        if (x != _x[index]) {
            return null;
        }
        if (b != _b[index]) {
            return null;
        }
        if (t != _t[index]) {
            return null;
        }
        return _z[index];
    }

    void put(final long x, final ZDD b, final ZDD t, final ZDD z)
    {
        final int index = index(x, b, t);

        _x[index] = x;
        _b[index] = b;
        _t[index] = t;
        _z[index] = z;
    }
}
