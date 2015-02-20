import java.util.*;

/**
 * Created by pva701 on 2/20/15.
 */
public class ArraySet<T extends Comparable<T>> extends AbstractSet<T> implements NavigableSet<T> {
    private T[] source;
    private int startIndex;
    private int endIndex;

    private ArraySet(T[] source, int from, int to) {
        this.source = source;
        startIndex = from;
        endIndex = to;
    }

    @Override
    public T lower(T t) {//TODO write
        int index = searchBound(startIndex, endIndex, t, getComparator(false)) - 1;
        if (index < startIndex)
            return null;
        return source[index];
    }

    @Override
    public T floor(T t) {//TODO write
        int index = searchBound(startIndex, endIndex, t, getComparator(false));
        if (index < startIndex)
            return null;
        return source[index];
    }

    @Override
    public T ceiling(T t) {
        int index = searchBound(startIndex, endIndex, t, getComparator(false));
    }

    @Override
    public T higher(T t) {
        return null;
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
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return null;
    }

    @Override
    public Iterator<T> descendingIterator() {
        return null;
    }

    private Comparator<T> getComparator(final boolean inclusive) {
        if (inclusive)
            return new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return (o1.compareTo(o2) > 0 ? 1 : -1);
                }
            };

        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return (o1.compareTo(o2) >= 0 ? 1 : -1);
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
    public NavigableSet<T> subSet(T fromElement, final boolean fromInclusive, T toElement, final boolean toInclusive) {
        int l = searchBound(startIndex, endIndex, fromElement, getComparator(fromInclusive));
        int r = searchBound(startIndex, endIndex, toElement, getComparator(toInclusive));
        return new ArraySet<T>(source, l, r);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        int r = searchBound(startIndex, endIndex, toElement, getComparator(inclusive));
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
        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        };
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
    public boolean contains(Object o) {
        if (o == null)
            throw new NullPointerException();
        T f = floor((T)o);
        if (f == null)
            return false;
        return f.compareTo((T)o) == 0;
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
}
