package net.ftod.zcube.zdd;

import static net.ftod.zcube.zdd.ZDD.CACHE_MAX;
import static net.ftod.zcube.zdd.ZDD.CACHE_SIZE;

/**
 * <h3>Caching <code>ZDD->long</code> functions</h3>
 * 
 * <p>
 * Small <b>mutable</b> cache for <code>long</long> functions on {@link ZDD}. Used to speed up recursive operations.
 * </p>
 * 
 * @author <a href="mailto:fabien.todescato@gmail.com">Fabien Todescato</a>
 */
final class ZDDCacheLong {

    private final ZDD[] _z = new ZDD[CACHE_SIZE];
    private final long[] _l = new long[CACHE_SIZE];

    private static int index(final ZDD z)
    {
        return z.h & CACHE_MAX;
    }

    ZDDCacheLong() {
        super();
    }

    Long get(final ZDD z)
    {
        final int index = index(z);

        if (z != _z[index]) {
            return null;
        }

        return Long.valueOf(_l[index]);
    }

    void put(final ZDD z, final long l)
    {
        final int index = index(z);

        _z[index] = z;
        _l[index] = l;
    }

}