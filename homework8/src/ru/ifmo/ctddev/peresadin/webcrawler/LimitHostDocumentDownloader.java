package ru.ifmo.ctddev.peresadin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitHostDocumentDownloader {
    private final Map<String, Deque<String>> queueUrlForHost = new HashMap<>();
    private final Map<String, Integer> countDownloadsFromHost = new HashMap<>();
    private final Map<String, Document> loadedPages = new HashMap<>();
    private ParallelUtils.ThreadPool threadPool;
    private Downloader downloader;
    private int perHost;
    private AtomicInteger numDownloads;

    public static interface AfterDownload {
        void afterDownload(Document document);
    }

    public LimitHostDocumentDownloader(ParallelUtils.ThreadPool threadPool, Downloader downloader, int perHost) {
        this.threadPool = threadPool;
        this.downloader = downloader;
        this.perHost = perHost;
    }

    public boolean isEmpty() {
        return numDownloads.intValue() == 0;
    }

    public void download(String url, AfterDownload afterDownload) {
        numDownloads.incrementAndGet();
        threadPool.execute(()->{
            String host;
            try {
                host = URLUtils.getHost(url);
            } catch (MalformedURLException e) {
                return;
            }

            try {
                if (!tryDownload(url)) {
                    synchronized (queueUrlForHost) {
                        if (!queueUrlForHost.containsKey(host))
                            queueUrlForHost.put(host, new LinkedList<>());
                        queueUrlForHost.get(host).add(url);
                    }
                    return;
                }
            } catch (IOException e) {}

            final Document doc = loadedPages.get(url);
            afterDownload.afterDownload(doc);
            String qUrl = atomicPeekFirst(host);
            try {
                if (qUrl != null && !tryDownload(qUrl))//TODO wrong
                    atomicRemoveFirst(host);
            } catch (IOException e) {}
        });
    }

    private boolean tryDownload(String url) throws IOException {
        String host = URLUtils.getHost(url);
        synchronized (this) {
            if (loadedPages.containsKey(url))
                return true;
            if (!countDownloadsFromHost.containsKey(host))
                countDownloadsFromHost.put(host, 0);
            if (countDownloadsFromHost.get(host) < perHost) {
                countDownloadsFromHost.put(host, countDownloadsFromHost.get(host) + 1);
            } else
                return false;
        }
        Document doc = downloader.download(url);
        synchronized (this) {
            countDownloadsFromHost.put(host, countDownloadsFromHost.get(host) - 1);
            loadedPages.put(url, doc);
        }
        return true;
    }

    private String atomicPeekFirst(String host) {
        synchronized (queueUrlForHost) {
            if (queueUrlForHost.containsKey(host) && queueUrlForHost.get(host).size() != 0) {
                return queueUrlForHost.get(host).peekFirst();
            }
        }
        return null;
    }

    private void atomicRemoveFirst(String host) {
        queueUrlForHost.get(host).removeFirst();
    }
}
