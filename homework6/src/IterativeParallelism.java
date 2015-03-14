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
public class IterativeParallelism {

    private static class  Worker {
        private Runnable runnable;

        public interface OnFinish {
            void onFinish();
        }

        public Worker(Runnable runnable, OnFinish fun) {
            this.runnable = runnable;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    fun.onFinish();
                }
            });
        }
    }

    private static class Controller {
        private int threadsWork;

        public Controller(Runnable[] runnables) {
            threadsWork = runnables.length;
            for (int i = 0; i < runnables.length; ++i) {
                new Worker(runnables[i], new Worker.OnFinish() {
                    @Override
                    public void onFinish() {
                        synchronized (Controller.this) {
                            threadsWork--;
                            notify();
                        }
                    }
                });
            }

            synchronized (this) {
                while (threadsWork > 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted ex in Controller");
                    }
                }
            }
        }
    }

    private static <Res, T> Res calc(int threads,
                                           List<T> list,
                                           Supplier<Res> zero,
                                           BiFunction<Res, T, Res> applier,
                                           BiFunction<Res, Res, Res> merger) {
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
            final int ith = i;
            runs[i] = new Runnable() {
                @Override
                public void run() {
                    Res res = zero.get();
                    for (int j = l; j <= r; ++j)
                        res = applier.apply(res, list.get(j));
                    results.set(ith, res);
                }
            };
        }
        new Controller(runs);//blocked
        Res res = zero.get();
        for (int i = 0; i < results.size(); ++i)
            res = merger.apply(res, results.get(i));
        return res;
    }

    private static void ensureThreads(int threads) {
        if (threads <= 0)
            throw new IllegalArgumentException("threads = " + threads);
    }

    public static <T> T minimum(int threads, List<T> list, Comparator<? super T> comparator) {
        ensureThreads(threads);
        BiFunction<T, T, T> f = (x,y)->(comparator.compare(x, y) < 0 ? y : x);
        if (list.size() != 0)
            return calc(threads, list.subList(1, list.size()), ()->list.get(0), f, f);
        else
            return null;
    }

    public static <T> T maximum(int threads, List<T> list, Comparator<T> comparator) {
        ensureThreads(threads);
        BiFunction<T, T, T> f = (x,y)->(comparator.compare(x, y) > 0 ? y : x);
        if (list.size() != 0)
            return calc(threads, list.subList(1, list.size()), ()->list.get(0), f, f);
        else
            return null;
    }

    public static <T> boolean all(int threads, List<T> list, Predicate<T> predicate) {
        ensureThreads(threads);
        return calc(threads, list, ()->true, (x,y)->x&&predicate.test(y), (x, y)->x&&y);
    }

    public static <T> boolean any(int threads, List<T> list, Predicate<T> predicate) {
        ensureThreads(threads);
        return calc(threads, list, ()->false, (x,y)->x||predicate.test(y), (x, y)->x||y);
    }

    public static <T, R> List<R> map(int threads, List<T> list, Function<T, R> function) {
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

    public static <T> String concat(int threads, List<T> list) {
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
