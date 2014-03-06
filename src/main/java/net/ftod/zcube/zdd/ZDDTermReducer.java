package net.ftod.zcube.zdd;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ZDDTermReducer<T> {

    private ZDDTermReducer() {
        super();
    }

    abstract protected T reduce(Iterator<ZDDTerm> i);

    private final T reduce(final File file) throws IOException
    {
        final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file), 262144));

        try {
            return reduce(new Iterator<ZDDTerm>() {

                private ZDDTerm next = null;

                @Override
                public boolean hasNext()
                {
                    if (next != null) {
                        return true;
                    }

                    try {
                        next = ZDDTerm.read(dis);
                    } catch (final IOException e) {
                        return false;
                    }

                    return true;
                }

                @Override
                public ZDDTerm next()
                {
                    if (next == null) {
                        throw new NoSuchElementException();
                    }

                    final ZDDTerm _next = next;
                    next = null;
                    return _next;
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            });
        } finally {
            dis.close();
        }
    }

    private static final ZDDTermReducer<ZDDNumber> SUM_SUBTREES = new ZDDTermReducer<ZDDNumber>() {
        @Override
        protected ZDDNumber reduce(final Iterator<ZDDTerm> i)
        {
            return ZDDNumber.pSumSubtrees(i);
        }
    };

    public static ZDDNumber sumSubtrees(final File file) throws IOException
    {
        return SUM_SUBTREES.reduce(file);
    }

}
