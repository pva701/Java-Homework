import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by pva701 on 2/20/15.
 */
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private T[] source;
    private int startIndex;
    private int endIndex;
    private Comparator<? super T> comparator;

    private void constructor(Object[] array) {
        if (array.length == 0)
            return;
        T[] copy = (T[])Arrays.copyOf(array, array.length);
        if (comparator == null)
            Arrays.sort(copy);
        else
            Arrays.sort(copy, comparator);
        int unique = 1;
        for (int i = 1; i < copy.length; ++i)
            if (compare(copy[i - 1], copy[i]) != 0) unique++;
        source = (T[])Array.newInstance(array.getClass().getComponentType(), unique);
        source[0] = copy[0];
        unique = 1;
        for (int i = 1; i < copy.length; ++i)
            if (compare(copy[i - 1], copy[i]) != 0)
                source[unique++] = copy[i];
        startIndex = 0;
        endIndex = source.length;
    }

    private ArraySet(T[] source, int from, int to) {
        this.source = source;
        startIndex = from;
        endIndex = to;
    }

    private ArraySet(T[] source, int from, int to, Comparator<? super T> comparator) {
        this.source = source;
        this.comparator = comparator;
        startIndex = from;
        endIndex = to;
    }

    public ArraySet() {
    }

    public ArraySet(T[] array) {
        constructor(array);
    }

    public ArraySet(T[] array, Comparator<? super T> comparator) {
        this.comparator = comparator;
        constructor(array);
    }

    public ArraySet(Collection<? extends T> collection) {
        constructor(collection.toArray());
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        this.comparator = comparator;
        constructor(collection.toArray());
    }

    public ArraySet(SortedSet<? super T> set) {
        this.comparator = set.comparator();
         constructor(set.toArray());
    }


    @Override
    public T lower(T t) {
        int index = searchBound(startIndex, endIndex, t, getComparator(true)) - 1;
        if (index < startIndex)
            return null;
        return source[index];
    }

    @Override
    public T floor(T t) {
        int index = searchBound(startIndex, endIndex, t, getComparator(false)) - 1;
        if (index < startIndex)
            return null;
        return source[index];
    }

    @Override
    public T ceiling(T t) {
        int index = searchBound(startIndex, endIndex, t, getComparator(true));
        if (index == endIndex)
            return null;
        return source[index];
    }

    @Override
    public T higher(T t) {
        int index = searchBound(startIndex, endIndex, t, getComparator(false));
        if (index == endIndex)
            return null;
        return source[index];
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        T[] subArray = Arrays.copyOfRange(source, startIndex, endIndex);
        for (int i = 0; i < subArray.length / 2; ++i) {
            T tmp = subArray[i];
            subArray[i] = subArray[subArray.length - i - 1];
            subArray[subArray.length - i - 1] = tmp;
        }
        if (comparator == null)
            return new ArraySet<T>(subArray, 0, subArray.length);
        return new ArraySet<T>(subArray, 0, subArray.length, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                int r = comparator.compare(o1, o2);
                if (r < 0)
                    return 1;
                if (r > 0)
                    return -1;
                return 0;
            }
        });
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int i = startIndex;
            @Override
            public boolean hasNext() {
                return i != endIndex;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return source[i++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new Iterator<T>() {
            int i = endIndex - 1;
            @Override
            public boolean hasNext() {
                return i != startIndex - 1;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return source[i--];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Comparator<T> getComparator(final boolean inclusive) {
        if (inclusive)
            return new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return (this.compare(o1, o2) > 0 ? 1 : -1);
                }
            };

        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return (this.compare(o1, o2) >= 0 ? 1 : -1);
            }
        };
    }

    private int searchBound(int from, int to, T x, Comparator<T> cmp) {
        if (x == null)
            throw new IllegalArgumentException("element is null");
        int l = from - 1, r = to, mid;
        while (l < r) {
            mid = (l + r) / 2;
            if (cmp.compare(x, source[mid]) >= 0)
                l = mid;
            else
                r = mid;
        }
        return r;
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int l = searchBound(startIndex, endIndex, fromElement, getComparator(fromInclusive));
        int r = searchBound(startIndex, endIndex, toElement, getComparator(!toInclusive));
        return new ArraySet<T>(source, l, r);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        int r = searchBound(startIndex, endIndex, toElement, getComparator(!inclusive));
        return new ArraySet<T>(source, startIndex, r);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        int l = searchBound(startIndex, endIndex, fromElement, getComparator(inclusive));
        return new ArraySet<T>(source, l, endIndex);
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public T first() {
        if (size() == 0)
            return null;
        return source[startIndex];
    }

    @Override
    public T last() {
        if (size() == 0)
            return null;
        return source[endIndex - 1];
    }

    @Override
    public int size() {
        return endIndex - startIndex;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {//TODO write
        if (o == null)
            throw new NullPointerException();
        T castO = (T)o;
        T f = floor(castO);
        if (f == null)
            return false;
        return compare(f, castO) == 0;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOfRange(source, startIndex, endIndex);
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        System.arraycopy(source, startIndex, a, 0, size());
        return a;
    }

    private int compare(T a, T b) {
        if (comparator == null)
            return ((Comparable<T>)a).compareTo(b);
        return comparator.compare(a, b);
    }
}
