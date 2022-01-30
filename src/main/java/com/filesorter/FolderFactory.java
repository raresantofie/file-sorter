package com.filesorter;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class FolderFactory {

    public static final String OUTPUT_LOCATION = "/Users/Rares_Antofie/sorted";
    private final Stream<LocalDate> imageDates;

    static {
        new File(String.format("%s/%s", OUTPUT_LOCATION, "unsorted")).mkdir();
    }

    public FolderFactory(Stream<LocalDate> imageDates) {
        this.imageDates = imageDates;
    }

    public void createFolderHierarchy() {
        imageDates.forEach(imageDate -> {
            String yearPath = createFolder(OUTPUT_LOCATION, String.valueOf(imageDate.getYear()));
            createFolder(yearPath, imageDate.getMonth().toString());
        });
    }

    public String createFolder(String output, String date) {
        File folder = new File(String.format("%s/%s", output, date));
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getPath();
    }
}
