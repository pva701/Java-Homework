package ru.ifmo.ctddev.peresadin.iterativeparallelism;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * This class consists exclusively of methods for parallel list processing.
 * All methods throw {@link java.lang.InterruptedException} if any thread was interrupted in the process.
 *
 * @author pva701
 * @see #map(int, java.util.List, java.util.function.Function)
 * @see #filter(int, java.util.List, java.util.function.Predicate)
 * @see #minimum(int, java.util.List, java.util.Comparator)
 * @see #maximum(int, java.util.List, java.util.Comparator)
 * @see #concat(int, java.util.List)
 * @see #all(int, java.util.List, java.util.function.Predicate)
 * @see #any(int, java.util.List, java.util.function.Predicate)
 */
public class IterativeParallelism implements ListIP {

    /**
     * Default constructor.
     */
    public IterativeParallelism() {}


    private static void ensureThreads(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads = " + threads);
        }
    }


    private static <Res, T> Res calc(
            int threads,
            List<? extends T> list,
            Supplier<Res> zero,
            BiFunction<? super Res, ? super T, ? extends Res> applier,
            BiFunction<? super Res, ? super Res, ? extends Res> merger
    ) throws InterruptedException{
        ensureThreads(threads);
        int len = list.size() / threads;
        Runnable[] runs = new Runnable[threads];
        List<Res> results = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            results.add(null);
        }
        int startPosition = 0;
        for (int i = 0; i < threads; ++i) {
            int curLen = len;
            if (i < list.size() % threads) {
                curLen++;
            }
            final int l = startPosition;
            final int r = startPosition + curLen - 1;
            final int ith = i;
            startPosition += curLen;
            runs[i] = () -> {
                Res res = zero.get();
                for (int j = l; j <= r; ++j)
                    res = applier.apply(res, list.get(j));
                results.set(ith, res);
            };
        }
        new ParallelUtils.Controller(runs);//blocked
        Res res = zero.get();
        for (Res e : results)
            res = merger.apply(res, e);
        return res;
    }

    /**
     * Returns the minimum element of the {@code list} according to the {@code comparator}.
     * Returns null if the {@code list} is empty.
     *
     * @param threads number of threads to be used for list processing.
     * @param list A {@link java.util.List} to be processed.
     * @param comparator {@link java.util.Comparator} to be used to compare {@code list} elements.
     * @param <T> the type of elements in the {@code list}.
     * @return the smallest element in the {@code list} or null if {@code list} is empty.
     * @throws java.lang.InterruptedException if any thread was interrupted during it's task.
     */

    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException{
        BiFunction<T, T, T> f = (x,y)->(comparator.compare(x, y) <= 0 ? x : y);
        if (list.size() != 0) {
            return calc(threads, list, ()->list.get(0), f, f);
        } else {
            return null;
        }
    }

    /**
     * Returns the maximum element of the {@code list} according to the {@code comparator}.
     * Returns null if the {@code list} is empty.
     *
     * @param threads number of threads to be used for list processing.
     * @param list A {@link java.util.List} to be processed
     * @param comparator {@link java.util.Comparator} to be used to compare {@code list} elements.
     * @param <T> the type of elements in the {@code list}.
     * @return the smallest element in the {@code list} or null if {@code list} is empty.
     * @throws java.lang.InterruptedException if any thread was interrupted during it's task.
     */

    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        BiFunction<T, T, T> f = (x,y)->(comparator.compare(x, y) >= 0 ? x : y);
        if (list.size() != 0) {
            return calc(threads, list, ()->list.get(0), f, f);
        } else {
            return null;
        }
    }

    /**
     * Returns whether all elements of the given {@link java.util.List} satisfy
     * provided {@link java.util.function.Predicate}. Returns true if the {@code list} is empty.
     *
     * @param threads number of threads to be used for list processing.
     * @param list the {@link java.util.List} to be tested.
     * @param predicate a {@link java.util.function.Predicate} to test elements of this {@code list} against.
     * @param <T> the type of elements in the {@code list}.
     * @return {@code true} if all elements in the {@code list} satisfy the {@code predicate} or the {@code list} is empty,
     * false otherwise
     * @throws java.lang.InterruptedException if any thread was interrupted during it's task.
     */
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super  T> predicate) throws InterruptedException{
        return calc(threads, list, ()->true, (x,y)->x&&predicate.test(y), (x, y)->x && y);
    }

    /**
     * Returns whether any elements of the given {@link java.util.List} satisfy
     * the provided {@link java.util.function.Predicate}. Returns false if the {@code list} is empty.
     *
     * @param threads number of threads to be used for list processing.
     * @param list the {@link java.util.List} to be tested.
     * @param predicate a {@link java.util.function.Predicate} to test elements of this {@code list} against.
     * @param <T> the type of elements in the {@code list}.
     * @return {@code true} if any elements of the {@code list} satisfy the {@code predicate},
     * false otherwise
     * @throws java.lang.InterruptedException if any thread was interrupted during it's task.
     */
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException{
        return calc(threads, list, () -> false, (x, y) -> x || predicate.test(y), (x, y) -> x || y);
    }

    /**
     * Returns whether any elements of the given {@link java.util.List} satisfy
     * the provided {@link java.util.function.Predicate}. Returns false if the {@code list} is empty.
     *
     * @param threads number of threads to be used for list processing.
     * @param list the {@link java.util.List} to be tested.
     * @param predicate a {@link java.util.function.Predicate} to test elements of this {@code list} against.
     * @param <T> the type of elements in the {@code list}.
     * @return {@code true} if any elements of the {@code list} satisfy the {@code predicate},
     * false otherwise
     * @throws java.lang.InterruptedException if any thread was interrupted during it's task.
     */
    public <T> List<T> filter(final int threads, final List<? extends T> list, final Predicate<? super T> predicate) throws InterruptedException{
        BiFunction<List<T>, T, List<T>> applier = (lst, el) -> {
            if (predicate.test(el)) {
                lst.add(el);
            }
            return lst;
        };
        return list(threads, list, applier);
    }

    private <T, R> List<R> list(
            int threads,
            List<? extends T> list,
            BiFunction<List<R>, T, List<R>> applier
    ) throws InterruptedException {
        return calc(threads, list, ArrayList::new,
                applier,
                (lst1, lst2)->{
                    lst1.addAll(lst2);
                    return lst1;
                });
    }


    /**
     * Returns the {@link java.util.List} consisting of the results of applying
     * the given {@code function} to the elements of this {@code list}.
     *
     * @param threads number of threads to be used for list processing.
     * @param list the {@link java.util.List} to be mapped.
     * @param function a {@link java.util.function.Function} to be applied to each {@code list} element.
     * @param <T> the type of elements in the {@code list}
     * @param <R> resulting type of elements in the new list as specified by the return type of {@code function}
     * @return the new {@link java.util.List}.
     * @throws java.lang.InterruptedException if any thread was interrupted during it's task.
     */

    public <T, R> List<R> map(final int threads, final List<? extends T> list, Function<? super T, ? extends R> function) throws InterruptedException{
        BiFunction<List<R>, T, List<R>> applier = (lst, el) -> {
            lst.add(function.apply(el));
            return lst;
        };
        return list(threads, list, applier);
    }

    /**
     * Returns the concatenation of string representations of elements in the {@code list}.
     * Returns empty {@link java.lang.String} if the {@code list} is empty.
     *
     * @param threads number of threads to be used for list processing.
     * @param list the {@link java.util.List} to be processed.
     * @return the result of concatenation.
     * @throws java.lang.InterruptedException if any thread was interrupted during it's task.
     */

    public String concat(int threads, List<?> list) throws InterruptedException{
        BiFunction<StringBuilder, Object, StringBuilder> f = (str1, str2) -> str1.append(str2);
        return calc(threads, list, StringBuilder::new, f, f).toString();
    }
}
