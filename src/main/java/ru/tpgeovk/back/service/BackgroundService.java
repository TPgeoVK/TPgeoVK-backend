package ru.tpgeovk.back.service;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BackgroundService {

    private final ExecutorService executorService = Executors.newCachedThreadPool();
}
