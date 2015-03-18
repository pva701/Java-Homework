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
 * Created by pva701 on 3/14/15.
 */
public class IterativeParallelism implements ListIP {

    public IterativeParallelism() {}
    private static class  Worker {
        public interface OnFinish {
            void onFinish();
        }

        public Worker(Runnable runnable, OnFinish fun) {
            new Thread(() -> {
                runnable.run();
                fun.onFinish();
            }).start();
        }
    }

    private static class Controller {
        private int threadsWork;

        public Controller(Runnable[] runnables) throws InterruptedException{
            threadsWork = runnables.length;
            for (Runnable r : runnables) {
                new Worker(r, () -> {
                    synchronized (Controller.this) {
                        threadsWork--;
                        Controller.this.notify();
                    }
                });
            }

            synchronized (this) {
                while (threadsWork > 0) {
                    wait();
                }
            }
        }
    }

    private static <Res, T> Res calc(int threads,
                                           List<? extends T> list,
                                           Supplier<Res> zero,
                                           BiFunction<? super Res, ? super T, ? extends Res> applier,
                                           BiFunction<? super Res, ? super Res, ? extends Res> merger) throws InterruptedException{
        threads = Math.min(list.size(), threads);
        if (threads <= 0)
            throw new IllegalArgumentException("threads " + threads);
        int len = list.size() / threads;
        Runnable[] runs = new Runnable[threads];
        ArrayList<Res> results = new ArrayList<>();
        for (int i = 0; i < threads; ++i)
            results.add(null);
        int startPosition = 0;
        for (int i = 0; i < threads; ++i) {
            int curLen = len;
            if (i < list.size() % threads) {
                curLen++;
            }
            final int l = startPosition;
            final int r = startPosition + curLen - 1;
            int ith = i;
            startPosition += curLen;
            runs[i] = () -> {
                Res res = zero.get();
                for (int j = l; j <= r; ++j)
                    res = applier.apply(res, list.get(j));
                results.set(ith, res);
            };
        }
        new Controller(runs);//blocked
        Res res = zero.get();
        for (Res e : results)
            res = merger.apply(res, e);
        return res;
    }

    private void ensureThreads(int threads) {
        if (threads <= 0)
            throw new IllegalArgumentException("threads = " + threads);
    }

    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException{
        ensureThreads(threads);
        BiFunction<T, T, T> f = (x,y)->(comparator.compare(x, y) <= 0 ? x : y);
        if (list.size() != 0)
            return calc(threads, list.subList(1, list.size()), ()->list.get(0), f, f);
        else
            return null;
    }

    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        ensureThreads(threads);
        BiFunction<T, T, T> f = (x,y)->(comparator.compare(x, y) >= 0 ? x : y);
        if (list.size() != 0)
            return calc(threads, list.subList(1, list.size()), ()->list.get(0), f, f);
        else
            return null;
    }

    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super  T> predicate) throws InterruptedException{
        ensureThreads(threads);
        return calc(threads, list, ()->true, (x,y)->x&&predicate.test(y), (x, y)->x&&y);
    }

    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException{
        ensureThreads(threads);
        return calc(threads, list, ()->false, (x,y)->x||predicate.test(y), (x, y)->x||y);
    }

    public <T> List<T> filter(final int threads, final List<? extends T> list, final Predicate<? super T> predicate) throws InterruptedException{
        ensureThreads(threads);
        return calc(threads, list, ArrayList::new,
                (lst, el)->{
                    if (predicate.test(el))
                        lst.add(el);
                    return lst;
                },
                (lst1, lst2)->{
                    lst1.addAll(lst2);
                    return lst1;
                });
    }


    public <T, R> List<R> map(final int threads, final List<? extends T> list, Function<? super T, ? extends R> function) throws InterruptedException{
        ensureThreads(threads);
        return calc(threads, list, ArrayList::new,
                (lst, el)->{
                    lst.add(function.apply(el));
                    return lst;
                },
                (lst1, lst2)->{
                    lst1.addAll(lst2);
                    return lst1;
                });
    }

    public String concat(int threads, List<?> list) throws InterruptedException{
        ensureThreads(threads);
        return calc(threads, list, StringBuilder::new,
                (str, el)->{
                    str.append(el.toString());
                    return str;
                },
                (str1, str2)->{
                    str1.append(str2);
                    return str1;
                }).toString();
    }
}
