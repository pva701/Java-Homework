package ru.ifmo.ctddev.peresadin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private int downloaders;
    private int extractors;
    private int perHost;
    private ParallelUtils.ThreadPool downloadThreadPool;
    private ParallelUtils.ThreadPool extractorThreadPool;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = downloaders;
        this.extractors = extractors;
        this.perHost = perHost;
        downloadThreadPool = new ParallelUtils.ThreadPool(downloaders);
        extractorThreadPool = new ParallelUtils.ThreadPool(extractors);
    }

    private void runDownload(
            String url,
            final int curDepth,
            final int maxDepth,
            final List<String> result,
            final LimitHostDocumentDownloader limitDownloader) {

        limitDownloader.download(url, doc->{
            extractorThreadPool.execute(() -> {
                try {
                    List<String> links = doc.extractLinks();
                    synchronized (result) {
                        result.addAll(links);
                    }
                    if (curDepth < maxDepth) {
                        for (String e : links)
                            runDownload(e, curDepth + 1, maxDepth, result, limitDownloader);
                    }
                } catch (IOException e) {}
            });
        });



    }

    @Override
    public List<String> download(String url, int depth) throws IOException {
        List<String> ret = new ArrayList<>();
        LimitHostDocumentDownloader limitDownloader = new LimitHostDocumentDownloader(downloadThreadPool, downloader, perHost);
        runDownload(url, 1, depth, ret, limitDownloader);
        synchronized (limitDownloader) {
            while (!limitDownloader.isEmpty()) {
                try {
                    limitDownloader.wait();
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
