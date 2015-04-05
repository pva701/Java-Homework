package ru.ifmo.ctddev.peresadin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by pva701 on 4/3/15.
 */
public class ParallelUtils {
    private ParallelUtils() {}

    public static abstract class AbstractThreadPool<T> {
        protected final Deque<T> tasksQueue = new LinkedList<>();
        protected Thread[] workers;
        public AbstractThreadPool(int threads) {
            workers = new Thread[threads];
            for (int i = 0; i < threads; ++i) {
                workers[i] = createThread();
                workers[i].start();
            }
        }

        public void execute(T task) {
            synchronized (tasksQueue) {
                tasksQueue.add(task);
                tasksQueue.notify();
            }
        }

        protected Thread createThread() {
            return new Thread(()->{
                while (!Thread.interrupted()) {
                    T curTask;
                    synchronized (tasksQueue) {
                        if (tasksQueue.isEmpty()) {
                            try {
                                tasksQueue.wait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                        curTask = tasksQueue.pollFirst();
                    }
                    if (curTask != null)
                        handleTask(curTask);
                }
            });
        }

        public void close() {
            for (int i = 0; i < workers.length; ++i)
                workers[i].interrupt();
        }

        protected abstract void handleTask(T task);
    }

    public static class ThreadPool extends AbstractThreadPool<Runnable> {
        public ThreadPool(int threads) {
            super(threads);
        }

        @Override
        public void handleTask(Runnable task) {
            task.run();
        }
    }

    public static class UrlDownloadTask {
        public Runnable runnable;
        public String host;
        public UrlDownloadTask(Runnable runnable, String host) {
            this.runnable = runnable;
            this.host = host;
        }
    }

    public static class HostLimitThreadPool extends AbstractThreadPool<UrlDownloadTask> {
        private final Map<String, Integer> countDownloadsFromHost = new HashMap<>();

        private int perHost;
        public HostLimitThreadPool(int threads, int perHost) {
            super(threads);
            this.perHost = perHost;
        }

        @Override
        protected void handleTask(UrlDownloadTask task) {
            String host = task.host;
            synchronized (this) {
                if (!countDownloadsFromHost.containsKey(host)) {
                    countDownloadsFromHost.put(host, 0);
                }

                if (countDownloadsFromHost.get(host) < perHost) {
                    countDownloadsFromHost.put(host, countDownloadsFromHost.get(host) + 1);
                } else {
                    tasksQueue.add(task);
                    return;
                }
            }
            task.runnable.run();
            synchronized (this) {
                countDownloadsFromHost.put(host, countDownloadsFromHost.get(host) - 1);
            }
        }
    }
}
