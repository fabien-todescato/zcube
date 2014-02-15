package net.ftod.zcube.zdd;

/**
 * <h3>Caching binary predicates</h3>
 * 
 * <p>
 * Small <b>mutable</b> cache for binary predicates on pairs of {@link ZDD}. Used to speed up recursive operations that require equality checks on {@link ZDD}.
 * </p>
 * 
 * @author <a href="mailto:fabien.todescato@gmail.com">Fabien Todescato</a>
 */
final class ZDDPredicateCache {

    private static final int POWER = 5;
    private static final int SIZE = 1 << POWER;
    private static final int MAX = SIZE - 1;

    private long hit = 0;
    private long miss = 0;

    private final ZDD[] _zdd1 = new ZDD[SIZE];
    private final ZDD[] _zdd2 = new ZDD[SIZE];
    private final Boolean[] _bool = new Boolean[SIZE];

    private static int index(final ZDD zdd1, final ZDD zdd2)
    {
        return 1 + 31 * (zdd1.h + 31 * zdd2.h) & MAX;
    }

    ZDDPredicateCache() {
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
        return _bool[index];
    }

    void put(final ZDD zdd1, final ZDD zdd2, final boolean zdd3)
    {
        final int index = index(zdd1, zdd2);

        _zdd1[index] = zdd1;
        _zdd2[index] = zdd2;
        _bool[index] = Boolean.valueOf(zdd3);
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