import java.util.*;

/**
 * Created by pva701 on 2/20/15.
 */
public class ArraySet<T extends Comparable<T>> implements NavigableSet<T> {
    private T[] elements;

    @Override
    public T lower(T t) {
        return null;
    }

    @Override
    public T floor(T t) {
        return null;
    }

    @Override
    public T ceiling(T t) {
        return null;
    }

    @Override
    public T higher(T t) {
        return null;
    }

    @Override
    public T pollFirst() {
        return null;
    }

    @Override
    public T pollLast() {
        return null;
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

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        return null;
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        int i = elements.length - 1;
        int bound = (inclusive ? 0 : 1);
        while (i >= 0 && toElement.compareTo(elements[i]) >= bound) --i;
        ++i;
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return null;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return null;
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return null;
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return null;
    }

    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    @Override
    public T first() {
        return elements[0];
    }

    @Override
    public T last() {
        return elements[elements.length - 1];
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            for (int i = 0; i < elements.length; ++i)
                if (elements[i] == null)
                    return true;
        } else {
            for (int i = 0; i < elements.length; ++i)
                if (o.equals(elements[i]))
                    return true;
        }
        return false;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(T1[] a) {
        System.arraycopy(elements, 0, a, 0, elements.length);
        return a;
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException("add doesn't supported!");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("remove doesn't supported!");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("addAll doesn't supported!");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll doesn't supported!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear doesn't supported!");
    }
}
