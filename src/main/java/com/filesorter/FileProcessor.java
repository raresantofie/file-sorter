package com.filesorter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FileProcessor {

    public void process(List<Image> imageList) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Runnable> runnables = new ArrayList<>();
        AtomicInteger movedImages = new AtomicInteger();
        imageList.forEach(image -> runnables.add(() -> {
            File source = new File(image.getPath());
            if (image.getDate() == null) {
                File dest = new File(String.format("%s/%s/%s",FolderFactory.OUTPUT_LOCATION, "unsorted", image.getName()));
                try {
                    Files.copy(Path.of(source.getPath()), Path.of(dest.getPath()), StandardCopyOption.REPLACE_EXISTING);
                    movedImages.getAndIncrement();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                File dest = new File(String.format("%s/%s/%s/%s",FolderFactory.OUTPUT_LOCATION, image.getDate().getYear(), image.getDate().getMonth().toString(), image.getName()));
                try {
                    Files.copy(Path.of(source.getPath()), Path.of(dest.getPath()), StandardCopyOption.REPLACE_EXISTING);
                    movedImages.getAndIncrement();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        runnables.forEach(executorService::submit);

        Runnable logProgress = () -> System.out.printf("Moved %d out of %d images%n", movedImages.get(), imageList.size());
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(logProgress, 0, 5, TimeUnit.SECONDS);
        executorService.shutdown();
        boolean finished = executorService.isTerminated();
        while (!finished) {
            finished = executorService.isTerminated();
        }
        executor.shutdown();
        System.out.println("Finished execution");
    }

}
