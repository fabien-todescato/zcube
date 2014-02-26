package net.ftod.zcube.zdd;

/**
 * <h1>Context of a computation over {@link ZDD}</h1>
 * 
 * <p>
 * Embed the internal operations caches into a stateful object, so as to provide a fluent functional interface to express computations on {@link ZDD}, sharing
 * the internal caches from end to end.
 * </p>
 * 
 * @author Fabien Todescato
 */
public abstract class ZDDContext {

    private final ZDDCachePredicate _equ = new ZDDCachePredicate();
    private final ZDDCachePredicate _inc = new ZDDCachePredicate();
    private final ZDDCacheOperation _uni = new ZDDCacheOperation();
    private final ZDDCacheOperation _int = new ZDDCacheOperation();
    private final ZDDCacheOperation _dif = new ZDDCacheOperation();
    private final ZDDCacheOperation _cru = new ZDDCacheOperation();
    private final ZDDCacheOperation _crd = new ZDDCacheOperation();
    private final ZDDCacheOperation _cri = new ZDDCacheOperation();

    protected ZDDContext() {
        super();
    }

    final public ZDD set(final long... xs)
    {
        return ZDD.set(_equ, _cru, _uni, xs);
    }

    final public ZDD union(final ZDD... zdds)
    {
        return ZDD.union(_equ, _uni, zdds);
    }

    final public ZDD union(final ZDD zdd1, final ZDD zdd2)
    {
        return ZDD.union(new ZDDCacheNode(), _equ, _uni, zdd1, zdd2);
    }

    final public ZDD intersection(final ZDD... zdds)
    {
        return ZDD.intersection(_equ, _int, zdds);
    }

    final public ZDD intersection(final ZDD zdd1, final ZDD zdd2)
    {
        return ZDD.intersection(_equ, _int, zdd1, zdd2);
    }

    final public ZDD difference(final ZDD zdd1, final ZDD zdd2)
    {
        return ZDD.difference(_equ, _dif, zdd1, zdd2);
    }

    final public ZDD crossDifference(final ZDD zdd1, final ZDD zdd2)
    {
        return ZDD.crossDifference(_equ, _crd, _uni, zdd1, zdd2);
    }

    final public ZDD crossUnion(final ZDD... zdds)
    {
        return ZDD.crossUnion(_equ, _cru, _uni, zdds);
    }

    final public ZDD crossUnion(final ZDD zdd1, final ZDD zdd2)
    {
        return ZDD.crossUnion(_equ, _cru, _uni, zdd1, zdd2);
    }

    final public ZDD crossIntersection(final ZDD... zdds)
    {
        return ZDD.crossIntersection(_equ, _cri, _uni, zdds);
    }

    final public ZDD crossIntersection(final ZDD zdd1, final ZDD zdd2)
    {
        return ZDD.crossIntersection(_equ, _cri, _uni, zdd1, zdd2);
    }

    final public boolean equals(final ZDD zdd1, final ZDD zdd2)
    {
        return ZDD.equals(_equ, zdd1, zdd2);
    }

    final public boolean included(final ZDD zdd1, final ZDD zdd2)
    {
        return ZDD.included(_equ, _inc, zdd1, zdd2);
    }

    final public long binary(final ZDDNumber zddn, final ZDD zdd)
    {
        return ZDDNumber.binary(_equ, _inc, zddn, zdd);
    }

    final public static ZDDNumber binary(final long l, final ZDD zdd)
    {
        return ZDDNumber.binary(l, zdd);
    }

    final public static ZDDNumber shift(final ZDDNumber zddn)
    {
        return ZDDNumber.shift(zddn);
    }

    final public ZDDNumber binaryAdd(final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        return ZDDNumber.binaryAdd(_equ, _int, _uni, _dif, zddn1, zddn2);
    }

    final public long negabinary(final ZDDNumber zddn, final ZDD zdd)
    {
        return ZDDNumber.negabinary(_equ, _inc, zddn, zdd);
    }

    final public static ZDDNumber negabinary(final long l, final ZDD zdd)
    {
        return ZDDNumber.negabinary(l, zdd);
    }

    final public ZDDNumber negabinaryAdd(final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        return ZDDNumber.negabinaryAdd(_equ, _int, _uni, _dif, zddn1, zddn2);
    }

    final public ZDDNumber negabinarySub(final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        return ZDDNumber.negabinarySub(_equ, _int, _uni, _dif, zddn1, zddn2);
    }

    final public ZDD trees(final ZDDTree t)
    {
        return ZDDTree.trees(t, _equ, _cru, _uni);
    }

    final public ZDD subtrees(final ZDDTree t)
    {
        return ZDDTree.subtrees(t, _equ, _cru, _uni);
    }

    protected abstract <T> T expression();

    final public <T> T eval()
    {
        return expression();
    }
}
