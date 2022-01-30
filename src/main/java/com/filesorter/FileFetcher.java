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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileFetcher {

    public static Map<Class<?>, Integer> TAGS =
            Map.of(ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL,
                    QuickTimeMetadataDirectory.class, QuickTimeMetadataDirectory.TAG_CREATION_DATE);

    public List<Image> fetchFiles(String root) throws IOException {
        return getFilesRecursively(root)
                        .stream()
                        .map(this::imageBuilder)
                        .collect(Collectors.toList());
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
        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            Class<? extends Directory> directoryClz = getDirectoryStrategy(file.getPath());
            Directory directory
                    = metadata.getFirstDirectoryOfType(directoryClz);
            return directory
                    .getDate(TAGS.get(directoryClz))
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    public Class<? extends Directory> getDirectoryStrategy(String path) {
        Class<? extends Directory> directory;
        if (path.contains(".mov")) {
            directory = QuickTimeMetadataDirectory.class;
        } else {
            directory = ExifSubIFDDirectory.class;
        }
        return directory;
    }



}
