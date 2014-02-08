package net.ftod.zcube.zdd;

import static net.ftod.zcube.zdd.ZDD.included;

import java.util.Collection;

/**
 * <h1>Representing linear combination of sets over integers with {@link ZDD}</h1>
 * 
 * <p>
 * <em>Pr. Minato et Al</em> show how to represent linear combinations of sets with integer coefficients as a forest of shared {@link ZDD}. A <em>binary</em>
 * representation can be used for unsigned integers, whereas signed integers may be represented in <em>negabinary</em>. Both representations are offered here.
 * </p>
 * <p>
 * <em>Zero</em> is represented as <code>null</code>.
 * </p>
 * 
 * @author Fabien Todescato
 */
public final class ZDDNumber {

    /**
     * Lowest-order digit.
     */
    public final ZDD digit;
    /**
     * Higher-order digits.
     */
    public final ZDDNumber number;

    private ZDDNumber(final ZDD digit, final ZDDNumber number) {
        super();
        this.digit = digit;
        this.number = number;
    }

    private static ZDDNumber number(final ZDD digit, final ZDDNumber number)
    {
        if (digit == ZDD.BOT && number == null) {
            return null;
        }

        return new ZDDNumber(digit, number);
    }

    static ZDDNumber shift(final ZDDNumber n)
    {
        return number(ZDD.BOT, n);
    }

    /**
     * <h3>Compute the <em>binary</em> representation of a {@link ZDD} multiplied by a <em>positive</em> <code>long</code></h3>
     * 
     * @param l
     *            the <code>long</code> holding the <em>positive</em> coefficient.
     * @param zdd
     *            the {@link ZDD} multiplicand.
     * @return the {@link ZDDNumber} representing in binary <code>l</code> occurrences of <code>zdd</code>
     */
    public static ZDDNumber binary(final long l, final ZDD zdd)
    {
        return l == 0L ? null : number(l % 2L == 0 ? ZDD.BOT : zdd, binary(l >> 1, zdd));
    }

    /**
     * <h3>Counting the occurrences of a {@link ZDD} in an unsigned binary {@link ZDDNumber}</h3>
     * 
     * <p>
     * Return the number of occurrences of a set of sets represented as a {@link ZDD} within a {@link ZDDNumber}.
     * </p>
     * 
     * @param zddn
     *            the {@link ZDDNumber} to be projected over the set of sets.
     * @param zdd
     *            the {@link ZDD} representing the set of sets.
     * @return the <code>long</code> representing the number of occurrences of the set of sets within the {@link ZDDNumber}.
     */
    public static long binary(final ZDDNumber zddn, final ZDD zdd)
    {
        return binary(new ZDDPredicateCache(), new ZDDPredicateCache(), zddn, zdd);
    }

    static long binary(final ZDDPredicateCache eq, final ZDDPredicateCache in, final ZDDNumber zddn, final ZDD zdd)
    {
        return zddn == null ? 0L : (included(eq, in, zdd, zddn.digit) ? 1L : 0L) + (binary(eq, in, zddn.number, zdd) << 1);
    }

    /**
     * <h3>Addition of two unsigned binary {@link ZDDNumber}</h3>
     * 
     * @param zddn1
     *            left operand {@link ZDDNumber}.
     * @param zddn2
     *            right operand {@link ZDDNumber}.
     * @return the {@link ZDDNumber} sum of the above.
     */
    public static ZDDNumber binaryAdd(final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        return binaryAdd(new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache(), new ZDDOperationCache(), zddn1, zddn2);
    }

    static ZDDNumber binaryAdd(final ZDDPredicateCache eq, final ZDDOperationCache in, final ZDDOperationCache un, final ZDDOperationCache di, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        final ZDDNumber zddnc = intersection(eq, in, zddn1, zddn2);
        final ZDDNumber zddns = difference(eq, di, union(eq, un, zddn1, zddn2), zddnc);

        if (zddnc == null) {
            return zddns;
        }

        if (zddns == null) {
            return new ZDDNumber(ZDD.BOT, zddnc);
        }

        return number(zddns.digit, binaryAdd(eq, in, un, di, zddns.number, zddnc));
    }

    /**
     * <h3>Compute the <em>negabinary</em> representation of a {@link ZDD} multiplied by a <em>signed</em> <code>long</code></h3>
     * 
     * @param l
     *            the <code>long</code> holding the <em>signed</em> coefficient.
     * @param zdd
     *            the {@link ZDD} multiplicand.
     * @return the {@link ZDDNumber} representing in negabinary <code>l</code> occurrences of <code>zdd</code>
     */
    public static ZDDNumber negabinary(final long l, final ZDD zdd)
    {
        if (l == 0) {
            return null;
        }

        final long q = l / -2L;
        final long r = l + 2L * q;

        final long l1;
        ZDD digit;

        if (r > 0) {
            digit = zdd;
            l1 = q;
        } else if (r < 0) {
            digit = zdd;
            l1 = q + 1;
        } else {
            digit = ZDD.BOT;
            l1 = q;
        }

        return number(digit, negabinary(l1, zdd));
    }

    /**
     * <h3>Counting the occurrences of a {@link ZDD} in a signed negabinary {@link ZDDNumber}</h3>
     * 
     * <p>
     * Return the number of occurrences of a set of sets represented as a {@link ZDD} within a {@link ZDDNumber}.
     * </p>
     * 
     * @param zddn
     *            the {@link ZDDNumber} to be projected over the set of sets.
     * @param zdd
     *            the {@link ZDD} representing the set of sets.
     * @return the <code>long</code> representing the number of occurrences of the set of sets within the {@link ZDDNumber}.
     */
    public static long negabinary(final ZDDNumber zddn, final ZDD zdd)
    {
        return negabinary(new ZDDPredicateCache(), new ZDDPredicateCache(), zddn, zdd);
    }

    static long negabinary(final ZDDPredicateCache eq, final ZDDPredicateCache in, final ZDDNumber zddn, final ZDD zdd)
    {
        if (zddn == null) {
            return 0L;
        }

        return (included(eq, in, zdd, zddn.digit) ? 1L : 0L) + negabinary(eq, in, zddn.number, zdd) * -2L;
    }

    /**
     * <h3>Addition of two signed negabinary {@link ZDDNumber}</h3>
     * 
     * @param zddn1
     *            left operand {@link ZDDNumber}.
     * @param zddn2
     *            right operand {@link ZDDNumber}.
     * @return the {@link ZDDNumber} sum of the above.
     */
    public static ZDDNumber negabinaryAdd(final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        return negabinaryAdd(new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache(), new ZDDOperationCache(), zddn1, zddn2);
    }

    static ZDDNumber negabinaryAdd(final ZDDPredicateCache eq, final ZDDOperationCache in, final ZDDOperationCache un, final ZDDOperationCache di, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        final ZDDNumber zddnc = intersection(eq, in, zddn1, zddn2);
        final ZDDNumber zddns = difference(eq, di, union(eq, un, zddn1, zddn2), zddnc);

        if (zddnc == null) {
            return zddns;
        }

        return negabinarySub(eq, in, un, di, zddns, shift(zddnc));
    }

    /**
     * <h3>Subtraction of two signed negabinary {@link ZDDNumber}</h3>
     * 
     * @param zddn1
     *            left operand {@link ZDDNumber}.
     * @param zddn2
     *            right operand {@link ZDDNumber}.
     * @return the {@link ZDDNumber} difference of the above.
     */
    public static ZDDNumber negabinarySub(final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        return negabinarySub(new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache(), new ZDDOperationCache(), zddn1, zddn2);
    }

    static ZDDNumber negabinarySub(final ZDDPredicateCache eq, final ZDDOperationCache in, final ZDDOperationCache un, final ZDDOperationCache di, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        final ZDDNumber zddnb = difference(eq, di, zddn2, zddn1);
        final ZDDNumber zddnd = union(eq, un, difference(eq, di, zddn1, zddn2), zddnb);

        if (zddnb == null) {
            return zddnd;
        }

        return negabinaryAdd(eq, in, un, di, zddnd, shift(zddnb));
    }

    private static ZDDNumber intersection(final ZDDPredicateCache eq, final ZDDOperationCache in, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        if (zddn1 == null) {
            return null;
        }
        if (zddn2 == null) {
            return null;
        }
        return number(ZDD.intersection(eq, in, zddn1.digit, zddn2.digit), intersection(eq, in, zddn1.number, zddn2.number));
    }

    private static ZDDNumber union(final ZDDPredicateCache eq, final ZDDOperationCache un, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        if (zddn1 == null) {
            return zddn2;
        }
        if (zddn2 == null) {
            return zddn1;
        }
        return number(ZDD.union(eq, un, zddn1.digit, zddn2.digit), union(eq, un, zddn1.number, zddn2.number));
    }

    private static ZDDNumber difference(final ZDDPredicateCache eq, final ZDDOperationCache di, final ZDDNumber zddn1, final ZDDNumber zddn2)
    {
        if (zddn1 == null) {
            return null;
        }
        if (zddn2 == null) {
            return zddn1;
        }
        return number(ZDD.difference(eq, di, zddn1.digit, zddn2.digit), difference(eq, di, zddn1.number, zddn2.number));
    }

    public static ZDDNumber addSubtrees(final ZDDLong zl, final ZDDNumber zn)
    {
        return addSubtrees(zl.l, zl.t, zn);
    }

    public static ZDDNumber addSubtrees(final long l, final ZDDTree trees, final ZDDNumber zn)
    {
        final ZDDPredicateCache _equ = new ZDDPredicateCache();
        final ZDDOperationCache _cru = new ZDDOperationCache();
        final ZDDOperationCache _uni = new ZDDOperationCache();
        final ZDDOperationCache _int = new ZDDOperationCache();
        final ZDDOperationCache _dif = new ZDDOperationCache();

        return negabinaryAdd(_equ, _int, _uni, _dif, negabinary(l, ZDDTree.subtrees(trees, _equ, _cru, _uni)), zn);
    }

    public static ZDDNumber addSubtrees(final ZDDLong zl, final ZDD filter, final ZDDNumber zn)
    {
        return addSubtrees(zl.l, zl.t, filter, zn);
    }

    public static ZDDNumber addSubtrees(final long l, final ZDDTree trees, final ZDD filter, final ZDDNumber zn)
    {
        final ZDDPredicateCache _equ = new ZDDPredicateCache();
        final ZDDOperationCache _cru = new ZDDOperationCache();
        final ZDDOperationCache _uni = new ZDDOperationCache();
        final ZDDOperationCache _int = new ZDDOperationCache();
        final ZDDOperationCache _dif = new ZDDOperationCache();

        return negabinaryAdd(_equ, _int, _uni, _dif, negabinary(l, ZDD.intersection(_equ, _int, filter, ZDDTree.trees(trees, _equ, _cru, _uni))), zn);
    }

    public static ZDDNumber addSubtrees(final long l, final Collection<Collection<Collection<String>>> trees, final ZDDNumber zn)
    {
        final ZDDPredicateCache _equ = new ZDDPredicateCache();
        final ZDDOperationCache _cru = new ZDDOperationCache();
        final ZDDOperationCache _uni = new ZDDOperationCache();
        final ZDDOperationCache _int = new ZDDOperationCache();
        final ZDDOperationCache _dif = new ZDDOperationCache();

        return negabinaryAdd(_equ, _int, _uni, _dif, negabinary(l, ZDD.trees3(_equ, _cru, _uni, trees)), zn);
    }

    public static ZDDNumber addSubtrees(final long l, final Collection<Collection<Collection<String>>> trees, final ZDD filter, final ZDDNumber zn)
    {
        final ZDDPredicateCache _equ = new ZDDPredicateCache();
        final ZDDOperationCache _cru = new ZDDOperationCache();
        final ZDDOperationCache _uni = new ZDDOperationCache();
        final ZDDOperationCache _int = new ZDDOperationCache();
        final ZDDOperationCache _dif = new ZDDOperationCache();

        return negabinaryAdd(_equ, _int, _uni, _dif, negabinary(l, ZDD.intersection(_equ, _int, filter, ZDD.trees3(_equ, _cru, _uni, trees))), zn);
    }

    public static ZDDNumber addSubtrees(final long l, final String[][][] trees, final ZDDNumber zn)
    {
        final ZDDPredicateCache _equ = new ZDDPredicateCache();
        final ZDDOperationCache _cru = new ZDDOperationCache();
        final ZDDOperationCache _uni = new ZDDOperationCache();
        final ZDDOperationCache _int = new ZDDOperationCache();
        final ZDDOperationCache _dif = new ZDDOperationCache();

        return negabinaryAdd(_equ, _int, _uni, _dif, negabinary(l, ZDD.trees(_equ, _cru, _uni, trees)), zn);
    }

    public static ZDDNumber addSubtrees(final long l, final String[][][] trees, final ZDD filter, final ZDDNumber zn)
    {
        final ZDDPredicateCache _equ = new ZDDPredicateCache();
        final ZDDOperationCache _cru = new ZDDOperationCache();
        final ZDDOperationCache _uni = new ZDDOperationCache();
        final ZDDOperationCache _int = new ZDDOperationCache();
        final ZDDOperationCache _dif = new ZDDOperationCache();

        return negabinaryAdd(_equ, _int, _uni, _dif, negabinary(l, ZDD.intersection(_equ, _int, filter, ZDD.trees(_equ, _cru, _uni, trees))), zn);
    }

    public static String sizes(final ZDDNumber n)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        if (n != null) {
            sb.append(n.digit.s);
            for (ZDDNumber n1 = n.number; n1 != null; n1 = n1.number) {
                sb.append(',');
                sb.append(n1.digit.s);
            }
        }
        sb.append(']');
        return sb.toString();
    }
}
