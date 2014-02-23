package net.ftod.zcube.zdd;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ZDDLongReducer<T> {

    abstract protected T reduce(Iterator<ZDDLong> i);

    private final T reduce(final File file) throws IOException
    {
        final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        try {
            return reduce(new Iterator<ZDDLong>() {

                private ZDDLong next = null;

                @Override
                public boolean hasNext()
                {
                    if (next != null) {
                        return true;
                    }

                    try {
                        next = ZDDLong.read(dis);
                    } catch (final IOException e) {
                        return false;
                    }

                    return true;
                }

                @Override
                public ZDDLong next()
                {
                    if (next == null) {
                        throw new NoSuchElementException();
                    }

                    final ZDDLong _next = next;
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

    private static final ZDDLongReducer<ZDDNumber> SUM_SUBTREES = new ZDDLongReducer<ZDDNumber>() {
        @Override
        protected ZDDNumber reduce(final Iterator<ZDDLong> i)
        {
            return ZDDNumber.pSumSubtrees(i);
        }
    };

    public static ZDDNumber sumSubtrees(final File file) throws IOException
    {
        return SUM_SUBTREES.reduce(file);
    }

}
