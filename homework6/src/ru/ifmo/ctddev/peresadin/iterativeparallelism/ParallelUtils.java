package ru.ifmo.ctddev.peresadin.iterativeparallelism;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

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
                        while (!isClosed) {
                            if (runnable == null)
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

    }
