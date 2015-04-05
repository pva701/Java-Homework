package ru.ifmo.ctddev.peresadin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private int downloaders;
    private int extractors;
    private int perHost;
    private ParallelUtils.HostLimitThreadPool downloadThreadPool;
    private ParallelUtils.ThreadPool extractorThreadPool;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = downloaders;
        this.extractors = extractors;
        this.perHost = perHost;
        downloadThreadPool = new ParallelUtils.HostLimitThreadPool(downloaders, perHost);
        extractorThreadPool = new ParallelUtils.ThreadPool(extractors);
    }

    private Document download(String url, Map<String, Document> loadedPages) {
        if (loadedPages.containsKey(url))
            return loadedPages.get(url);
        try {
            return downloader.download(url);
        } catch (IOException e) {
            return null;
        }
    }

    private void decCounter(AtomicInteger counter) {
        counter.decrementAndGet();
        if (counter.get() == 0)
            counter.notify();
    }

    private void runDownload(
            String url,
            final int curDepth,
            final int maxDepth,
            final List<String> result,
            Map<String, Document> loadedPages,
            final AtomicInteger counterDownloads) {
        counterDownloads.incrementAndGet();

        String host = null;
        try {
            host = URLUtils.getHost(url);
        } catch (IOException e) {}
        if (host == null)
            return;

        downloadThreadPool.execute(new ParallelUtils.UrlDownloadTask(
        ()->{
            final Document doc = download(url, loadedPages);
            if (doc == null) {
                decCounter(counterDownloads);
                return;
            }
            loadedPages.put(url, doc);
            extractorThreadPool.execute(() -> {
                try {
                    List<String> links = doc.extractLinks();
                    synchronized (result) {
                        result.addAll(links);
                    }
                    if (curDepth < maxDepth) {
                        for (String e : links)
                            runDownload(e, curDepth + 1, maxDepth, result, loadedPages, counterDownloads);
                    }
                } catch (IOException e) {}
                decCounter(counterDownloads);
            });
        }, host));

    }

    @Override
    public List<String> download(String url, int depth) throws IOException {
        List<String> ret = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        runDownload(url, 1, depth, ret, new HashMap<>(), counter);
        synchronized (counter) {
            while (counter.get() != 0) {
                try {
                    counter.wait();
                } catch (InterruptedException e) {}
            }
        }
        return ret;
    }

    @Override
    public void close() {
        //TODO implement
    }
}
