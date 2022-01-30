package com.filesorter;

import java.io.IOException;

public class FileSorterApplication {

    public static void main(String... args) {
        FileFetcher fileFetcher = new FileFetcher();

        try {
            fileFetcher.fetchFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
