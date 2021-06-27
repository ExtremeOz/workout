package com.a_track_it.workout.common;


import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Copied from [android-architecture] project, i changed class to singleton.
 *
 * See: https://github.com/googlesamples/android-architecture/blob/todo-mvp/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/util/AppExecutors.java
 *
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 *
 * Created by Shawn Wang on 3/26/19.
 */
public class AppExecutors {

    private static final int THREAD_COUNT = 7;
    private final ExecutorService service;
    private final Executor diskIO;

    private final Executor networkIO;

    private final Executor mainThread;

    private static final class InstanceHolder {
        private static AppExecutors instance = new AppExecutors(new DiskIOThreadExecutor(),
                Executors.newFixedThreadPool(THREAD_COUNT), new MainThreadExecutor());
    }

    public static AppExecutors getInstance() {
        return InstanceHolder.instance;
    }
    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread ) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
        this.service = Executors.newFixedThreadPool(THREAD_COUNT);
    }
    public ExecutorService diskService(){ return this.service;}

   // public void doShutDown(){
   //     if (!service.isShutdown()) service.shutdown();
   // }
    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }

    static class DiskIOThreadExecutor implements Executor {

        private final Executor mDiskIO;

        public DiskIOThreadExecutor() {
            mDiskIO = Executors.newSingleThreadExecutor();
        }

        @Override
        public void execute(@NonNull Runnable command) {
            mDiskIO.execute(command);
        }
    }

}

