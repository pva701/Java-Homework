package ru.ifmo.ctddev.peresadin.iterativeparallelism;

import java.util.*;
import java.util.function.Function;

/**
 * Created by pva701 on 3/20/15.
 */
public class ParallelUtils {
    private ParallelUtils() {}

    public static interface Callbacks {
        public static interface OnFinish {
            void onFinish();
        }
    }

    public static class  Worker {

        public Worker(Runnable runnable, Callbacks.OnFinish fun) {
            new Thread(() -> {
                runnable.run();
                fun.onFinish();
            }).start();
        }
    }

    public static class ReusedWorker {
        private Runnable runnable;
        private Callbacks.OnFinish onFinish;
        private boolean isClosed;

        public ReusedWorker() {
            new Thread(()->{
                synchronized (ReusedWorker.this) {
                    try {
                        while (true) {
                            wait();
                            if (isClosed) {
                                break;
                            }
                            if (runnable != null) {
                                runnable.run();
                                if (onFinish != null) {
                                    onFinish.onFinish();
                                }
                            }
                            runnable = null;
                            onFinish = null;
                        }
                    } catch (InterruptedException e) {
                        //TODO write
                    }
                }
            }).start();
        }

        public synchronized void run(Runnable runnable, Callbacks.OnFinish onFinish) {
            this.runnable = runnable;
            this.onFinish = onFinish;
            notify();
        }

        public synchronized void close() {
            isClosed = true;
            notify();
        }
    }

    public static class Controller {
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

    public static class ParallelMapperImpl {
        private int threads;
        private ReusedWorker[] workers;
        private boolean isRunning;
        private Deque<Thread> queue = new ArrayDeque<>();

        public ParallelMapperImpl(int threads) {
            workers = new ReusedWorker[threads];
            for (int i = 0; i < threads; ++i)
                workers[i] = new ReusedWorker();
        }

        <T, R> List<R> run(
                Function<? super T, ? extends R> f,
                List<? extends T> args)
                throws InterruptedException {
            queue.add(Thread.currentThread());
            synchronized (this) {
                while (isRunning || queue.peekFirst() != Thread.currentThread()) {
                    wait();
                }
                queue.removeFirst();
                isRunning = true;
            }

            threads = (workers.length < args.size() ? workers.length : args.size());
            int len = args.size() / threads;
            int startPosition = 0;
            List<R> result = new ArrayList<>();
            for (int i = 0; i < args.size(); ++i) {
                result.add(null);
            }
            for (int i = 0; i < threads; ++i) {
                int curLength = len;
                if (i < args.size() % threads) {
                    ++curLength;
                }

                final int l = startPosition;
                final int r = startPosition + curLength - 1;
                startPosition += curLength;
                workers[i].run(() -> {
                    for (int j = l; j <= r; ++j)
                        result.set(j, f.apply(args.get(j)));
                },
                () -> {
                    synchronized (ParallelMapperImpl.this) {
                        --threads;
                        if (threads == 0) {
                            ParallelMapperImpl.this.notify();
                        }
                    }
                });
            }

            synchronized (this) {
                while (threads != 0) {
                    wait();
                }
                isRunning = false;
                notify();
            }
            return result;
        }


        void close() throws InterruptedException {

        }
    }
}
