package net.ftod.zcube.zdd;

import static net.ftod.zcube.zdd.ZDD.CACHE_MAX;
import static net.ftod.zcube.zdd.ZDD.CACHE_SIZE;

final class ZDDCacheN {

    private final long[] _x = new long[CACHE_SIZE];
    private final ZDD[] _b = new ZDD[CACHE_SIZE];
    private final ZDD[] _t = new ZDD[CACHE_SIZE];
    private final ZDD[] _z = new ZDD[CACHE_SIZE];

    ZDDCacheN() {
        super();
    }

    ZDD get(final int h, final long x, final ZDD b, final ZDD t)
    {
        final int index = h & CACHE_MAX;

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

    void put(final int h, final long x, final ZDD b, final ZDD t, final ZDD z)
    {
        final int index = h & CACHE_MAX;

        _x[index] = x;
        _b[index] = b;
        _t[index] = t;
        _z[index] = z;
    }
}
