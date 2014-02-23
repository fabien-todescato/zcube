package net.ftod.zcube.zdd;

import static net.ftod.zcube.zdd.ZDD.CACHE_MAX;
import static net.ftod.zcube.zdd.ZDD.CACHE_SIZE;

/**
 * <h3>Caching binary operations</h3>
 * 
 * <p>
 * Small <b>mutable</b> cache for binary operations on {@link ZDD}. Used to speed up recursive operations.
 * </p>
 * 
 * @author <a href="mailto:fabien.todescato@gmail.com">Fabien Todescato</a>
 */
final class ZDDCacheOperation {

    private final ZDD[] _zdd1 = new ZDD[CACHE_SIZE];
    private final ZDD[] _zdd2 = new ZDD[CACHE_SIZE];
    private final ZDD[] _zdd3 = new ZDD[CACHE_SIZE];

    private static int index(final ZDD zdd1, final ZDD zdd2)
    {
        return 1 + 31 * (zdd1.h + 31 * zdd2.h) & CACHE_MAX;
    }

    ZDDCacheOperation() {
        super();
    }

    ZDD get(final ZDD zdd1, final ZDD zdd2)
    {
        final int index = index(zdd1, zdd2);

        if (zdd1 != _zdd1[index] || zdd2 != _zdd2[index]) {
            return null;
        }

        return _zdd3[index];
    }

    void put(final ZDD zdd1, final ZDD zdd2, final ZDD zdd3)
    {
        final int index = index(zdd1, zdd2);

        _zdd1[index] = zdd1;
        _zdd2[index] = zdd2;
        _zdd3[index] = zdd3;
    }

}