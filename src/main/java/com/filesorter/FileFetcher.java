package com.filesorter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.media.QuickTimeMediaDirectory;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileFetcher {


    public void fetchFiles() throws IOException {

        String root = "/Users/Rares_Antofie/Documents/exported";
        var imageList =
                getFilesRecursively(root)
                        .stream()
                        .map(this::imageBuilder)
                        .collect(Collectors.toList());

        process(imageList);
    }

    public List<File> getFilesRecursively(String rootPath) {
        List<File> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Path.of(rootPath))) {
            result = walk.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    public Image imageBuilder(File file) {
        return new Image(file.getName(), getDate(file), file.getPath());
    }

    public LocalDate getDate(File file) {
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }
        if (metadata == null) {
            return null;
        }
        try {
            Directory directory
                    = metadata.getFirstDirectoryOfType(getDirectoryStrategy(file.getPath()));
            return directory
                    .getDate(getTagType(getDirectoryStrategy(file.getPath())))
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    public Class<? extends Directory> getDirectoryStrategy(String path) {
        Class<? extends Directory> directory = null;
        if (path.contains(".mov")) {
            directory = QuickTimeMetadataDirectory.class;
        } else {
            directory = ExifSubIFDDirectory.class;
        }
        return directory;
    }

    public int getTagType(Class<? extends Directory> clz) {
        if (clz.equals(ExifSubIFDDirectory.class)) {
            return ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL;
        } else if (clz.equals(QuickTimeMetadataDirectory.class)) {
            return QuickTimeMetadataDirectory.TAG_CREATION_DATE;
        }
        return 0;
    }

    public void process(List<Image> imageList) {
        FolderFactory folderFactory = new FolderFactory(imageList);
        folderFactory.createFolderHierarchy();
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
