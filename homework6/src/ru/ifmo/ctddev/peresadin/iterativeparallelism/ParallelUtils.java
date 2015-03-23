package ru.ifmo.ctddev.peresadin.iterativeparallelism;

import java.util.*;
import java.util.function.Function;

/**
 * Created by pva701 on 3/20/15.
 */
public class ParallelUtils {
    private ParallelUtils() {}

    public static interface ParallelMapper extends AutoCloseable {
        <T, R> List<R> run(
                Function<? super T, ? extends R> f,
                List<? extends T> args
        ) throws InterruptedException;

        @Override
        void close() throws InterruptedException;
    }

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
                        isClosed = true;
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
        public enum State {RUNNING, NOT_RUNNING, CLOSED}

        private int workedThreads;
        private ReusedWorker[] workers;
        private State state;
        private Deque<Thread> queue = new ArrayDeque<>();

        public ParallelMapperImpl(int threads) {
            state = State.NOT_RUNNING;
            workers = new ReusedWorker[threads];
            for (int i = 0; i < threads; ++i)
                workers[i] = new ReusedWorker();
        }

        public <T, R> List<R> run(
                Function<? super T, ? extends R> f,
                List<? extends T> args)
                throws InterruptedException {
            synchronized (this) {
                queue.add(Thread.currentThread());
                while (state == State.RUNNING || queue.peekFirst() != Thread.currentThread()) {
                    wait();
                }
                queue.removeFirst();
                if (state == State.CLOSED) {
                    throw new InterruptedException();
                }
                state = State.RUNNING;
            }

            workedThreads = (workers.length < args.size() ? workers.length : args.size());
            int len = args.size() / workedThreads;
            int startPosition = 0;
            List<R> result = new ArrayList<>(args.size());
            for (int i = 0; i < args.size(); ++i) {
                result.add(null);
            }
            int totalThreads = workedThreads;
            for (int i = 0; i < totalThreads; ++i) {
                int curLength = len;
                if (i < args.size() % totalThreads) {
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
                        --workedThreads;
                        if (workedThreads == 0) {
                            ParallelMapperImpl.this.notifyAll();
                        }
                    }
                });
            }

            synchronized (this) {
                while (workedThreads != 0) {
                    wait();
                }
                state = State.NOT_RUNNING;
                notifyAll();
            }
            return result;
        }


        public synchronized void close() throws InterruptedException {
            for (int i = 0; i < workers.length; ++i)
                workers[i].close();
            state = State.CLOSED;
        }


        public State getState() {
            return state;
        }
    }
}
