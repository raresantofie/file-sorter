package com.filesorter;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class FolderFactory {

    public static final String OUTPUT_LOCATION = "/Users/Rares_Antofie/sorted";
    private final List<Image> imageList;

    public FolderFactory(List<Image> imageList) {
        this.imageList = imageList;
    }

    public void createFolderHierarchy() {
        imageList.forEach(image -> {
            LocalDate localDate = image.getDate();
            if (localDate == null) {
                new File(String.format("%s/%s", OUTPUT_LOCATION, "unsorted")).mkdir();
                System.out.println(String.format("Transfering file from path %s to unsorted", image.getName()));
            } else {
                String yearPath = createFolder(OUTPUT_LOCATION, String.valueOf(image.getDate().getYear()));
                createFolder(yearPath, image.getDate().getMonth().toString());
            }
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
