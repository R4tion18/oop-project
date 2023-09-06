package com.oopproject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class Album extends Playlist  {
    private int year = 0;
    private String artist = null;

    public Album(String name, Library library) {
        super(name, library);
    }

    public Album(String name, Library library, File folder) {
        this(name, library, folder.listFiles());
    }

    public Album(String name, Library library, File[] files) {
        this(name, library);
        getLibrary().addMultipleFiles(new CopyOnWriteArrayList<File>(files)).forEach(this::addSong);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void addFrom(Vector<Integer> indexes)    {
        if (songs.isEmpty())  {
            songs = new CopyOnWriteArrayList<>(new ArrayList<>(indexes.size()));
        }
        for (int index : indexes)   {
            songs.add(Song.getTrack(getLibrary().getSong(index)), index);
        }
    }

    @Override
    public void addSong(int index) {
        Song song = new Song(Song.getFile(getLibrary().getSong(index)));

        if (getArtist() == null)  {
            setArtist(song.getArtist());
        }   else if (song.getArtist() == null)    {
            song.setArtist(getArtist());
        }

         if (getYear() == 0)    {
             setYear(song.getYear());
         }  else if (song.getYear() == 0) {
             song.setYear(getYear());
         }

        songs.stream()
                .mapToInt(s -> s)
                .filter(s ->
                        Song.getTrack(getLibrary().getSong(s)) > song.getTrack())
                .findFirst()
                .ifPresent(s -> songs.add(songs.indexOf(s), index));
    }

    @Override
    public void removeSong(int index) {
        songs.remove(Integer.valueOf(index));

        songs.stream()
                .filter(s ->
                        Song.getTrack(getLibrary().getSong(s)) >= index)
                .forEach(s ->
                        Song.setTrack(getLibrary().getSong(s), Song.getTrack(getLibrary().getSong(s)) - 1));
    }

    public void removeSongNotTrack(int index)   {
        songs.remove(Integer.valueOf(index));
    }
}
