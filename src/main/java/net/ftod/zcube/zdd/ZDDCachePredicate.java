package net.ftod.zcube.zdd;

import static net.ftod.zcube.zdd.ZDD.CACHE_SIZE;

/**
 * <h3>Caching binary predicates</h3>
 * 
 * <p>
 * Small <b>mutable</b> cache for binary predicates on pairs of {@link ZDD}. Used to speed up recursive operations that require equality checks on {@link ZDD}.
 * </p>
 * 
 * @author <a href="mailto:fabien.todescato@gmail.com">Fabien Todescato</a>
 */
final class ZDDCachePredicate {

    private long hit = 0;
    private long miss = 0;

    private final ZDD[] _zdd1 = new ZDD[CACHE_SIZE];
    private final ZDD[] _zdd2 = new ZDD[CACHE_SIZE];
    private final boolean[] _bool = new boolean[CACHE_SIZE];

    private static int index(final ZDD zdd1, final ZDD zdd2)
    {
        return 1 + 31 * (zdd1.h + 31 * zdd2.h) & ZDD.CACHE_MAX;
    }

    ZDDCachePredicate() {
        super();
    }

    Boolean get(final ZDD zdd1, final ZDD zdd2)
    {
        final int index = index(zdd1, zdd2);

        if (zdd1 != _zdd1[index] || zdd2 != _zdd2[index]) {
            ++miss;
            return null;
        }

        ++hit;
        return Boolean.valueOf(_bool[index]);
    }

    void put(final ZDD zdd1, final ZDD zdd2, final boolean zdd3)
    {
        final int index = index(zdd1, zdd2);

        _zdd1[index] = zdd1;
        _zdd2[index] = zdd2;
        _bool[index] = zdd3;
    }

    long hit()
    {
        return hit;
    }

    long miss()
    {
        return miss;
    }
}