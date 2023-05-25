package com.oopproject;

import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public record Song(File file) {

    public Mp3File getMp3File() {
        return getMp3File(file);
    }

    public static Mp3File getMp3File(File file) {
        Mp3File mp3 = null;
        try {
            mp3 = new Mp3File(file);
        } catch (IOException e) {
            e.printStackTrace();    //change exception handling
        } catch (UnsupportedTagException e) {
            e.getCause().printStackTrace(); //change exception handling
        } catch (InvalidDataException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);  //change exception handling
        }

        return mp3;
    }

    public Optional<ID3v2> getTag() {
        return getTag(file);
    }

    public static Optional<ID3v2> getTag(String uri) {
        return getTag(getFile(uri));
    }

    public static Optional<ID3v2> getTag(File file) {
        if (getMp3File(file).hasId3v2Tag()) {
            return Optional.of(getMp3File(file).getId3v2Tag());
        }

        return Optional.empty();
    }

    public void createTag() {
        createTag(file);
    }

    public static void createTag(String uri) {
        createTag(getFile(uri));
    }

    public static void createTag(File file) {
        if (getTag(file).isEmpty()) {
            getMp3File(file).setId3v2Tag(new ID3v24Tag());
        }
    }

    public void setIndex(Integer index) {
        setIndex(file, index);
    }

    public static void setIndex(String uri, Integer index) {
        setIndex(getFile(uri), index);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void setIndex(File file, Integer index) {
        createTag(file);
        getTag(file).get().setComment(index.toString());
    }

    public String getIndexString() {
        return getIndexString(file);
    }

    public static String getIndexString(String uri) {
        return getIndexString(getFile(uri));
    }

    public static String getIndexString(File file) {
        return getMp3File(file).getId3v2Tag().getComment().split(",")[0];
    }

    public int getIndex() {
        return getIndex(file);
    }

    public static int getIndex(String uri) {
        return getIndex(getFile(uri));
    }

    public static int getIndex(File file) {
        return Integer.parseInt(getIndexString(file));
    }

    public void setConsecutive(Integer index) {
        setConsecutive(file, index);
    }

    public static void setConsecutive(String uri, Integer index) {
        setConsecutive(getFile(uri), index);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void setConsecutive(File file, Integer index) {
        createTag(file);
        getTag(file).get().setComment(getTag(file).get().getComment() + "," + index.toString());
    }

    public String getConsecutiveString() {
        return getConsecutiveString(file);
    }

    public static String getConsecutiveString(String uri) {
        return getConsecutiveString(getFile(uri));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static String getConsecutiveString(File file) {
        createTag(file);
        return getTag(file).get().getComment().split(",")[1];
    }

    public int getConsecutive() {
        return getConsecutive(file);
    }

    public static int getConsecutive(String uri) {
        return getConsecutive(getFile(uri));
    }

    public static int getConsecutive(File file) {
        return Integer.parseInt(getConsecutiveString(file));
    }

    public static String getString(URI uri) {
        return uri.toString();
    }

    public static String getString(File file) {
        return getString(getURI(file));
    }

    public static URI getURI(File file) {
        return file.toURI();
    }

    public static URI getURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);  //change exception handling
        }
    }

    public static File getFile(URI uri) {
        return new File(uri);
    }

    public static File getFile(String uri) {
        return getFile(getURI(uri));
    }

    public String getTitle() {
        return getTitle(file);
    }

    public static String getTitle(String uri) {
        return getTitle(getFile(uri));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static String getTitle(File file) {
        createTag(file);
        return getTag(file).get().getTitle();
    }

    public String getAlbum() {
        return getAlbum(file);
    }

    public static String getAlbum(String uri) {
        return getAlbum(getFile(uri));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static String getAlbum(File file) {
        createTag(file);
        return getTag(file).get().getAlbum();
    }

    public String getAlbumArtist() {
        return getAlbumArtist(file);
    }

    public static String getAlbumArtist(String uri) {
        return getAlbumArtist(getFile(uri));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static String getAlbumArtist(File file) {
        return getTag(file).get().getAlbumArtist();
    }

    public String getArtist() {
        return getArtist(file);
    }

    public static String getArtist(String uri) {
        return getArtist(getFile(uri));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static String getArtist(File file) {
        return getTag(file).get().getArtist();
    }

    public int getYear() {
        return getYear(file);
    }

    public static int getYear(String uri) {
        return getYear(getFile(uri));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static int getYear(File file) {
        return Integer.parseInt(getTag(file).get().getYear());
    }

    public int getTrack() {
        return getTrack(file);
    }

    public static int getTrack(String uri) {
        return getTrack(getFile(uri));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static int getTrack(File file) {
        return Integer.parseInt(getTag(file).get().getTrack());
    }
}
