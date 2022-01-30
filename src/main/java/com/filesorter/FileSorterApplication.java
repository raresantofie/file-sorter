package com.filesorter;

import java.io.IOException;
import java.util.List;

public class FileSorterApplication {

    public static void main(String... args) {
        FileFetcher fileFetcher = new FileFetcher();
        String root = "/Users/Rares_Antofie/Documents/exported";

        try {
            List<Image> imageList = fileFetcher.fetchFiles(root);
            new FolderFactory(imageList.stream().map(Image::getDate)).createFolderHierarchy();
            new FileProcessor().process(imageList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
