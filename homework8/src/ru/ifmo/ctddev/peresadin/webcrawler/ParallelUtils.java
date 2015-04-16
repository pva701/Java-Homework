package ru.ifmo.ctddev.peresadin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by pva701 on 4/3/15.
 */
public class ParallelUtils {
    private ParallelUtils() {}

    public static abstract class AbstractThreadPool<T> {
        protected final BlockingQueue<T> tasksQueue;
        protected Thread[] workers;

        public AbstractThreadPool(int threads) {
            threads = Math.min(threads, 250);
            tasksQueue = new LinkedBlockingQueue<>();
            workers = new Thread[threads];
            for (int i = 0; i < threads; ++i) {
                workers[i] = createThread();
                workers[i].start();
            }
        }

        public void execute(T task) {
            try {
                tasksQueue.put(task);
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }

        protected Thread createThread() {
            return new Thread(()->{
                while (!Thread.interrupted()) {
                    try {
                        handleTask(tasksQueue.take());
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        public void close() {
            for (Thread e : workers)
                e.interrupt();
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
                int cnt = countDownloadsFromHost.get(host);
                if (cnt < perHost) {
                    countDownloadsFromHost.put(host, cnt + 1);
                } else {
                    try {
                        tasksQueue.put(task);
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                    return;
                }
            }
            task.runnable.run();
            synchronized (countDownloadsFromHost) {
                countDownloadsFromHost.put(host, countDownloadsFromHost.get(host) - 1);
            }
        }
    }
}
