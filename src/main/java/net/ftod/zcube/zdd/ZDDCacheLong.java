package net.ftod.zcube.zdd;

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

    private static final int POWER = 5;
    private static final int SIZE = 1 << POWER;
    private static final int MAX = SIZE - 1;

    private long hit = 0;
    private long miss = 0;

    private final ZDD[] _z = new ZDD[SIZE];
    private final long[] _l = new long[SIZE];

    private static int index(final ZDD z)
    {
        return z.h & MAX;
    }

    ZDDCacheLong() {
        super();
    }

    Long get(final ZDD z)
    {
        final int index = index(z);

        if (z != _z[index]) {
            ++miss;
            return null;
        }

        ++hit;
        return Long.valueOf(_l[index]);
    }

    void put(final ZDD z, final long l)
    {
        final int index = index(z);

        _z[index] = z;
        _l[index] = l;
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