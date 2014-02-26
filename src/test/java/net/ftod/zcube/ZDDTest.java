package net.ftod.zcube;

import static net.ftod.zcube.zdd.ZDD.BOT;
import static net.ftod.zcube.zdd.ZDD.TOP;
import static net.ftod.zcube.zdd.ZDDTree.cross;
import static net.ftod.zcube.zdd.ZDDTree.path;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import net.ftod.zcube.zdd.ZDD;
import net.ftod.zcube.zdd.ZDDContext;
import net.ftod.zcube.zdd.ZDDNumber;
import net.ftod.zcube.zdd.ZDDTree;

import org.junit.Test;

/**
 * <h1>Unit test on {@link ZDD} operations</h1>
 * 
 * @author Fabien Todescato
 */
public class ZDDTest {

    private static final long _N = 128L;

    private abstract class ZDDContextTest extends ZDDContext {

        protected final void assertEqual(final String s, final ZDD zdd1, final ZDD zdd2)
        {
            assertTrue(s, equals(zdd1, zdd2));
        }

        protected final void assertNotEqual(final String s, final ZDD zdd1, final ZDD zdd2)
        {
            assertFalse(s, equals(zdd1, zdd2));
        }
    }

    @Test
    public void equal()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertEqual("Reflexive", singleton(1L), singleton(1L));
                assertNotEqual("Not equal", singleton(1L), singleton(2L));

                return null;
            }
        }.eval();
    }

    @Test
    public void included()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertTrue(included(set(1, 2), union(set(1, 2), set(1, 3))));
                assertTrue(included(set(1, 3), union(set(1, 2), set(1, 3))));

                assertFalse(included(union(set(1, 2), set(1, 3)), set(1, 2)));
                assertFalse(included(union(set(1, 2), set(1, 3)), set(1, 3)));

                return null;
            }

        }.eval();
    }

    @Test
    public void set()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertEqual("Idempotent", set(1L, 1L, 1L), singleton(1L));
                assertEqual("Commutative", set(1L, 2L, 3L), set(3L, 2L, 1L));

                return null;
            }

        }.eval();
    }

    @Test
    public void union()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertEqual("Empty", BOT, union());
                assertEqual("Idempotent", union(singleton(1L), singleton(1L)), singleton(1L));
                assertEqual("Commutative", union(TOP, set(1L)), union(set(1L), TOP));
                assertEqual("Commutative", union(singleton(1L), singleton(2L)), union(singleton(2L), singleton(1L)));
                assertEqual("Associative", union(union(singleton(1L), singleton(2L)), singleton(3L)), union(singleton(1L), union(singleton(2L), singleton(3L))));
                assertEqual("Associative", union(union(singleton(2L), singleton(3L)), singleton(1L)), union(singleton(2L), union(singleton(3L), singleton(1L))));
                assertEqual("Associative", union(union(singleton(3L), singleton(1L)), singleton(2L)), union(singleton(3L), union(singleton(1L), singleton(2L))));

                return null;
            }

        }.eval();
    }

    @Test
    public void difference()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertEqual("TB", difference(union(TOP, set(1, 2)), TOP), set(1, 2));
                assertEqual("TB", difference(TOP, BOT), TOP);
                assertEqual("TB", difference(TOP, TOP), BOT);
                assertEqual("T", difference(TOP, set(1, 2)), TOP);
                assertEqual("B", difference(BOT, set(1, 2)), BOT);
                assertEqual("ST", difference(set(1, 2), TOP), set(1, 2));
                assertEqual("Def", difference(set(2), set(1)), set(2));
                assertEqual("Def", difference(set(1, 2), set(1, 3)), set(1, 2));
                assertEqual("Def", difference(set(1, 2), set(1)), set(1, 2));
                assertEqual("Def", difference(set(1), set(1, 2)), set(1));

                return null;
            }

        }.eval();
    }

    @Test
    public void crossDifference()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertEqual("A", crossDifference(set(1), set(2)), set(1));
                assertEqual("B", crossDifference(set(2), set(1)), set(2));
                assertEqual("C", crossDifference(BOT, BOT), BOT);
                assertEqual("D", crossDifference(TOP, TOP), TOP);
                assertEqual("E", crossDifference(set(1), TOP), set(1));
                assertEqual("F", crossDifference(set(1), set(1)), TOP);
                assertEqual("G", crossDifference(set(1, 2), set(1)), set(2));
                assertEqual("H", crossDifference(set(1, 2), set(2)), set(1));
                assertEqual("I", crossDifference(set(2), set(2, 3)), TOP);
                assertEqual("J", crossDifference(set(1, 2), set(2, 3)), set(1));
                assertEqual("K", crossDifference(set(2, 3), set(1, 3)), set(2));
                assertEqual("L", crossDifference(union(set(1, 2), set(2, 3)), set(2)), union(set(1), set(3)));
                assertEqual("M", crossDifference(union(set(1, 2, 3), set(2, 3, 4)), set(2, 3)), union(set(1), set(4)));
                assertEqual("N", crossDifference(union(set(1, 2, 3), set(2, 3, 4)), union(set(2), set(3))), union(set(1, 3), set(2, 4), set(1, 2), set(3, 4)));
                assertEqual("O", crossDifference(set(1, 2, 3, 4), union(set(2), set(3))), union(set(1, 3, 4), set(1, 2, 4)));

                return null;
            }

        }.eval();
    }

    @Test
    public void intersection()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertEqual("BOT", BOT, intersection(set(1L), TOP));
                assertEqual("BOT", BOT, intersection(set(1L), set(2L)));

                assertEqual("Idempotent", set(1L), intersection(set(1L), set(1L)));
                assertEqual("Idempotent", set(1L, 2L), intersection(set(1L, 2L), set(1L, 2L)));
                assertEqual("Idempotent", set(1L, 2L, 3L), intersection(set(1L, 2L, 3L), set(1L, 2L, 3L)));

                assertEqual("Absorption", union(TOP, set(1L)), intersection(union(set(1L), set(2L), TOP), union(set(1L), set(3L), TOP)));
                assertEqual("Absorption", set(1L), intersection(union(set(1L), set(2L)), union(set(1L), set(3L))));
                assertEqual("Absorption", set(2L), intersection(union(set(1L), set(2L)), union(set(2L), set(3L))));
                assertEqual("Absorption", set(1L, 2L), intersection(union(set(1L, 2L), set(2L, 3L)), union(set(1L, 2L), set(3L, 4L))));

                assertEqual("Disjoint", BOT, intersection(union(set(1L, 2L), set(2L, 3L)), union(set(3L, 4L), set(5L, 6L))));

                for (long l = 0L; l < _N; ++l) {
                    assertEqual(
                    //
                            String.format("Commutativity %s", Long.toString(l)) //
                            , intersection(union(set(l, l + 2L), set(l, l + 3L)), union(set(l, l + 1L), set(l, l + 2L))) //
                            , intersection(union(set(l, l + 1L), set(l, l + 2L)), union(set(l, l + 2L), set(l, l + 3L))) //
                    //
                    );
                }

                for (long l = 0L; l < _N; ++l) {

                    final ZDD z1 = union(set(l, l + 1L), set(l, l + 2L), set(l, l + 3L));
                    final ZDD z2 = union(set(l, l + 2L), set(l, l + 3L), set(l, l + 4L));
                    final ZDD z3 = union(set(l, l + 3L), set(l, l + 4L), set(l, l + 5L));

                    assertEqual(
                    //
                            String.format("Associativity %s", Long.toString(l)) //
                            , intersection(intersection(z1, z2), z3) //
                            , intersection(z1, intersection(z2, z3)) //
                    //
                    );
                }

                return null;
            }

        }.eval();
    }

    @Test
    public void crossUnion()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertEqual("Def", union(set(1L, 2L, 3L, 4L)), crossUnion(set(1L, 3L), set(2L, 4L)));
                assertEqual("Def", union(set(1L, 3L), set(1L, 4L), set(2L, 3L), set(2L, 4L)), crossUnion(union(set(1L), set(2L)), union(set(3L), set(4L))));
                assertEqual("Def", union(set(1L, 3L), set(1L, 4L)), difference(union(set(1L, 3L), crossUnion(singleton(2L), singleton(5L)), set(1L, 4L)), set(2L, 5L)));

                return null;
            }

        }.eval();
    }

    @Test
    public void crossIntersection()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertEqual("Def", TOP, crossIntersection());
                assertEqual("Def", TOP, crossIntersection(set(1), set(2)));
                assertEqual("Def", set(2), crossIntersection(set(1, 2), set(2, 3)));
                assertEqual("Def", union(set(2), set(3)), crossIntersection(union(set(1, 2), set(3, 4)), union(set(2, 3))));
                assertEqual("Def", union(TOP, set(2), set(3), set(3, 4), set(5)), crossIntersection(union(set(1, 2), set(3, 4), set(5, 7)), union(set(2, 3), set(3, 4, 5))));
                assertEqual("Def", set(0), crossIntersection(set(0), set(0, 1)));
                assertEqual("Def", union(set(0), set(1)), union(crossIntersection(set(0), set(0, 1)), crossIntersection(set(1), set(0, 1))));

                return null;
            }

        }.eval();
    }

    @Test
    public void trees()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                assertEquals(1L, ZDD.size(subtrees(ZDDTree.TOP)));
                assertEquals(2L, ZDD.size(subtrees(path("a"))));
                assertEquals(3L, ZDD.size(subtrees(path("a", "b"))));
                assertEquals(4L, ZDD.size(subtrees(path("a", "b", "c"))));
                assertEquals(4L, ZDD.size(subtrees(cross(path("a", "b", "c"), path("a", "b", "c")))));
                assertEquals(6L, ZDD.size(subtrees(cross(path("a", "b", "c"), path("a", "b", "d")))));
                assertEquals(10L, ZDD.size(subtrees(cross(path("a", "b", "c"), path("a", "b", "d"), path("a", "b", "e")))));

                return null;
            }

        }.eval();
    }

    @Test
    public void treesBinary()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                {
                    final ZDD z0 = subtrees(cross(path("a", "b"), path("a", "c")));
                    final ZDD z1 = trees(path("a"));
                    final ZDD z2 = trees(path("a", "b"));
                    final ZDD z3 = trees(path("a", "c"));

                    ZDDNumber zn = ZDDNumber.ZERO;
                    long n = 0L;

                    for (int i = 0; i < _N; ++i) {
                        n += i;
                        zn = binaryAdd(zn, binary(i, z0));
                    }

                    assertEquals(n, binary(zn, z0));
                    assertEquals(n, binary(zn, z1));
                    assertEquals(n, binary(zn, z2));
                    assertEquals(n, binary(zn, z3));
                }

                {
                    final ZDD zab = subtrees(path("a", "b"));
                    final ZDD zac = subtrees(path("a", "c"));
                    final ZDD zad = subtrees(path("a", "d"));
                    final ZDD zae = subtrees(path("a", "e"));
                    final ZDD zaf = subtrees(path("a", "f"));

                    ZDDNumber zn = ZDDNumber.ZERO;
                    long n = 0L;

                    for (int i = 0; i < _N; ++i) {
                        n += i;
                        zn = binaryAdd(zn, binary(i, zab));
                        zn = binaryAdd(zn, binary(i, zac));
                        zn = binaryAdd(zn, binary(i, zad));
                        zn = binaryAdd(zn, binary(i, zae));
                        zn = binaryAdd(zn, binary(i, zaf));
                    }

                    assertEquals(5L * n, binary(zn, trees(path("a"))));
                    assertEquals(n, binary(zn, trees(path("a", "b"))));
                    assertEquals(n, binary(zn, trees(path("a", "c"))));
                    assertEquals(n, binary(zn, trees(path("a", "d"))));
                    assertEquals(n, binary(zn, trees(path("a", "e"))));
                    assertEquals(n, binary(zn, trees(path("a", "f"))));
                }

                return null;
            }

        }.eval();
    }

    @Test
    public void treesNegabinary()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                {
                    final ZDD z0 = subtrees(cross(path("a", "b"), path("a", "c")));
                    final ZDD z1 = subtrees(path("a"));
                    final ZDD z2 = subtrees(path("a", "b"));
                    final ZDD z3 = subtrees(path("a", "c"));

                    ZDDNumber zn = ZDDNumber.ZERO;
                    long n = 0L;

                    for (int i = 0; i < _N; ++i) {
                        n += i;
                        zn = negabinaryAdd(zn, negabinary(i, z0));
                    }

                    assertEquals(n, negabinary(zn, z0));
                    assertEquals(n, negabinary(zn, z1));
                    assertEquals(n, negabinary(zn, z2));
                    assertEquals(n, negabinary(zn, z3));
                }

                {
                    final ZDD zab = subtrees(path("a", "b"));
                    final ZDD zac = subtrees(path("a", "c"));
                    final ZDD zad = subtrees(path("a", "d"));
                    final ZDD zae = subtrees(path("a", "e"));
                    final ZDD zaf = subtrees(path("a", "f"));

                    ZDDNumber zn = ZDDNumber.ZERO;
                    long n = 0L;

                    for (int i = 0; i < _N; ++i) {
                        n += i;
                        zn = negabinaryAdd(zn, negabinary(i, zab));
                        zn = negabinaryAdd(zn, negabinary(i, zac));
                        zn = negabinaryAdd(zn, negabinary(i, zad));
                        zn = negabinaryAdd(zn, negabinary(i, zae));
                        zn = negabinaryAdd(zn, negabinary(i, zaf));
                    }

                    assertEquals(5L * n, negabinary(zn, trees(path("a"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "b"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "c"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "d"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "e"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "f"))));
                }

                return null;
            }

        }.eval();
    }

    @Test
    public void treesNegabinaryParallel()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                {
                    final ZDD zab = subtrees(path("a", "b"));
                    final ZDD zac = subtrees(path("a", "c"));
                    final ZDD zad = subtrees(path("a", "d"));
                    final ZDD zae = subtrees(path("a", "e"));
                    final ZDD zaf = subtrees(path("a", "f"));

                    final ArrayList<ZDDNumber> znList = new ArrayList<ZDDNumber>();
                    long n = 0L;

                    for (int i = 0; i < _N; ++i) {
                        n += i;
                        znList.add(negabinary(i, zab));
                        znList.add(negabinary(i, zac));
                        znList.add(negabinary(i, zad));
                        znList.add(negabinary(i, zae));
                        znList.add(negabinary(i, zaf));
                    }

                    final ZDDNumber zn = ZDDNumber.pSum(znList);

                    assertEquals(5L * n, negabinary(zn, trees(path("a"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "b"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "c"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "d"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "e"))));
                    assertEquals(n, negabinary(zn, trees(path("a", "f"))));
                }

                return null;
            }

        }.eval();
    }

    /**
     * Testing binary representation of ZDD numbers.
     */
    @Test
    public void binary()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                for (final ZDD zdd : Arrays.asList(TOP, singleton(1L), set(1L, 2L, 3L))) {
                    for (long l = 0; l < _N; ++l) {
                        final ZDDNumber zn = binary(l, zdd);
                        final long l1 = binary(zn, zdd);
                        assertEquals(l, l1);
                    }
                }

                {
                    final ZDD[] zdds = { set(1L), set(2L), set(3L), set(1L, 2L, 3L), set(8L, 1L), set(10L, 2L) };
                    final ZDD u = union(zdds);

                    for (final ZDD z : zdds) {
                        for (long l = 0; l < _N; ++l) {
                            assertEquals(l, binary(binary(l, u), z));
                        }
                    }
                }

                {
                    final ZDD[] zdds = { set(1L), set(2L), set(3L), set(1L, 2L, 3L), set(8L, 1L), set(10L, 2L) };
                    final ZDD u = union(zdds);

                    for (final ZDD z : zdds) {
                        for (long l1 = 0; l1 < _N; ++l1) {
                            for (long l2 = 0; l2 < _N; ++l2) {
                                assertEquals(l1 + l2, binary(binaryAdd(binary(l1, u), binary(l2, u)), z));
                            }
                        }
                    }
                }

                return null;
            }
        }.eval();
    }

    /**
     * Testing negabinary representation of ZDD numbers.
     */
    @Test
    public void negabinary()
    {
        new ZDDContextTest() {
            @Override
            protected <Void> Void expression()
            {
                for (final ZDD zdd : Arrays.asList(TOP, singleton(1L), set(1L, 2L, 3L))) {
                    for (long l = 0; l < _N; ++l) {
                        assertEquals(l * -2L, negabinary(shift(negabinary(l, zdd)), zdd));
                        assertEquals(-l * -2L, negabinary(shift(negabinary(-l, zdd)), zdd));
                    }
                }

                for (final ZDD zdd : Arrays.asList(TOP, singleton(1L), set(1L, 2L, 3L))) {
                    for (long l = 0; l < _N; ++l) {
                        assertEquals(l, negabinary(negabinary(l, zdd), zdd));
                        assertEquals(-l, negabinary(negabinary(-l, zdd), zdd));
                    }
                }

                {
                    final ZDD[] zdds = { set(1L), set(2L), set(3L), set(1L, 2L, 3L), set(8L, 1L), set(10L, 2L) };
                    final ZDD u = union(zdds);

                    for (final ZDD z : zdds) {
                        for (long l = 0; l < _N; ++l) {
                            assertEquals(l, negabinary(negabinary(l, u), z));
                            assertEquals(-l, negabinary(negabinary(-l, u), z));
                        }
                    }
                }

                {
                    final ZDD[] zdds = { set(1L), set(2L), set(3L), set(1L, 2L, 3L), set(8L, 1L), set(10L, 2L) };
                    final ZDD u = union(zdds);

                    for (final ZDD z : zdds) {
                        assertEquals(-1L, negabinary(negabinarySub(negabinary(0L, u), negabinary(1L, u)), z));
                        assertEquals(3L, negabinary(negabinaryAdd(negabinary(1L, u), negabinary(2L, u)), z));
                        assertEquals(3L, negabinary(negabinaryAdd(negabinary(2L, u), negabinary(1L, u)), z));
                        assertEquals(5L, negabinary(negabinaryAdd(negabinary(3L, u), negabinary(2L, u)), z));
                        assertEquals(2L, negabinary(negabinaryAdd(negabinary(1L, u), negabinary(1L, u)), z));
                    }
                }

                {
                    final ZDD[] zdds = { set(1L), set(2L), set(3L), set(1L, 2L, 3L), set(8L, 1L), set(10L, 2L) };
                    final ZDD u = union(zdds);

                    for (long i = 0; i < _N; ++i) {

                        for (long j = 0; j < _N; ++j) {

                            assertEquals(i + j, negabinary(negabinaryAdd(negabinary(i, u), negabinary(j, u)), u));
                            assertEquals(i - j, negabinary(negabinarySub(negabinary(i, u), negabinary(j, u)), u));

                            for (final ZDD z : zdds) {

                                assertEquals(i + j, negabinary(negabinaryAdd(negabinary(i, z), negabinary(j, z)), z));
                                assertEquals(i - j, negabinary(negabinarySub(negabinary(i, z), negabinary(j, z)), z));

                                assertEquals(i + j, negabinary(negabinaryAdd(negabinary(i, u), negabinary(j, u)), z));
                                assertEquals(i - j, negabinary(negabinarySub(negabinary(i, u), negabinary(j, u)), z));

                                assertEquals(i + j, negabinary(negabinaryAdd(negabinary(i, z), negabinary(j, u)), z));
                                assertEquals(i - j, negabinary(negabinarySub(negabinary(i, z), negabinary(j, u)), z));

                                assertEquals(i + j, negabinary(negabinaryAdd(negabinary(i, u), negabinary(j, z)), z));
                                assertEquals(i - j, negabinary(negabinarySub(negabinary(i, u), negabinary(j, z)), z));
                            }
                        }
                    }
                }

                return null;
            }
        }.eval();
    }
}
