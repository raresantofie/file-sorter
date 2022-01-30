package com.filesorter;

import java.io.File;
import java.time.LocalDate;

public class Image {
    private String name;
    private LocalDate date;
    private String path;

    public Image(String name, LocalDate date, String path) {
        this.name = name;
        this.date = date;
        this.path = path;
    }

    @Override
    public String toString() {
        return "Image{" +
                "name='" + name + '\'' +
                ", date=" + date +
                ", path='" + path + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getPath() {
        return path;
    }
}
