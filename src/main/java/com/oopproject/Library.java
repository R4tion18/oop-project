package com.oopproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A Library to organise songs into playlists and albums.
 * @author francescorighi
 * @author riccardovezzani
 * @version 2023.8.18
 * @see Song
 * @see Playlist
 * @see Album
 */
public class Library {
    private Properties properties;
    private final ConcurrentHashMap<Integer, String> songs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Playlist> playlists = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Album> albums = new ConcurrentHashMap<>();

    /** Initialises a Library at the start of the application.
     *
     * @param properties contains the name of the directory where the Library files are stored.
     * @param isFirstSetup specifies if this is the first time the application was started on this system.
     * @throws RuntimeException if there is an error when opening the directory.
     */
    public Library(Properties properties, boolean isFirstSetup) {
        this.properties = properties;
        File directory;
        try {
            directory = new File(new URI(this.properties.getProperty("libraryFolder")));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);  //change exception handling
        }
        CopyOnWriteArrayList<File> files = getFiles(directory);

        if (!isFirstSetup)  {
            new Library(files);
        } else {
            CopyOnWriteArrayList<File> sorted = new CopyOnWriteArrayList<>();
            files.stream()
                    .sorted()
                    .collect(Collectors.toCollection(() -> sorted));

            IntStream.range(0, sorted.size())
                    .forEachOrdered(i -> addNewSong(Song.getString(sorted.get(i)), i));
        }
    }

    /** Creates a new Library instance from a list of files.
     * @param files is the list of song files in the Library.
     */

    public Library(CopyOnWriteArrayList<File> files) {
        files.forEach(file -> {
            if (Song.getIndexString(file) == null) {
                addNewSong(Song.getString(file), getHighestIndex() + 1);
            }

            Arrays.stream(properties.getProperty(Song.getIndexString(file)).split("\n"))    //look over with new library
                    .forEach(playlist -> {
                        playlists.putIfAbsent(playlist, new Playlist(playlist, this));
                        playlists.get(playlist)
                                .addSong(Song.getIndex(file));
                    });
        });
    }

    public int getLibrarySize() {
        return songs.size();
    }

    public int getHighestIndex()    {
        return properties.stringPropertyNames()
                .stream()
                .skip(1)
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
    }

    public ObservableList<String> getSongNames() {
        return FXCollections.observableArrayList(songs.values()
                .stream()
                .map(Song::getTitle)
                .toList());
    }

    public CopyOnWriteArrayList<File> getFiles(File directory)   {
        return new CopyOnWriteArrayList<>(List.of(Objects.requireNonNull(directory.listFiles())));
    }

    public int getNumberOfPlaylists()   {
        return playlists.size();
    }

    public ObservableList<String> getPlaylistNames()    {
        return FXCollections.observableArrayList(playlists.values()
                .stream()
                .map(Playlist::getName)
                .toList());
    }

    public int getNumberOfAlbums()  {
        return albums.size();
    }

    public ObservableList<String> getAlbumNames()   {
        return FXCollections.observableArrayList(albums.values()
                .stream()
                .map(Album::getName)
                .toList());
    }

    public String getSong(int index) {
        return songs.get(index);
    }

    public Playlist getPlaylist(String name)    {
        return playlists.get(name);
    }

    public Album getAlbum(String name)  {
        return albums.get(name);
    }

    public Vector<Integer> addFromFolder(String folderUri) {
        File folder;

        try {
            folder = new File(new URI(folderUri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);  //change exception handling
        }

        return addMultipleFiles(new CopyOnWriteArrayList<>(Objects.requireNonNull(folder.listFiles())));
    }

    public Vector<Integer> addMultipleFiles(CopyOnWriteArrayList<File> files)  {
        Vector<Integer> indexes = new Vector<>();
        for (File file : files) {
            indexes.add(addSongFile(file.toURI().toString()));
        }
        return indexes;
    }

    public int addSongFile(String uri)    {
        File oldFile;
        try {
            oldFile = new File(new URI(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);  //change exception handling
        }
        File song = new File(properties.getProperty("libraryFolder"), uri);
        boolean delete = oldFile.delete();
        return addNewSong(Song.getString(song), getHighestIndex() + 1);
    }

    public int addNewSong(String uri, int index)  {
        if(Song.getIndexString(uri) == null) {
            Song.setIndex(uri, index);
            properties.setProperty(String.valueOf(index), "");
            songs.put(index, uri);
        }

        String album = Song.getAlbum(uri);
        if (album != null) {
            albums.putIfAbsent(album, new Album(album, this));
            albums.get(album).addSong(Song.getIndex(uri));
        }

        return index;
    }

    public void deleteSong(int index)   {
        Arrays.stream(properties.getProperty(String.valueOf(index)).split("\n"))
                .forEach(playlist -> playlists.get(playlist).removeSong(index));

        String album = Song.getAlbum(getSong(index));
        if (!(album == null)) {
            albums.get(album).removeSong(index);
        }

        File song = new File(songs.get(index));
        boolean delete = song.delete();
        songs.remove(index);
        properties.remove(String.valueOf(index));
    }

    public void createPlaylist(String name, File folder)    {
        playlists.putIfAbsent(name, new Playlist(name, this, folder));
    }

    public void createPlaylist(String name, CopyOnWriteArrayList<File> files)   {
        playlists.putIfAbsent(name, new Playlist(name, this, files.toArray(File[]::new)));
    }

    public void createPlaylist(String name)    {
        playlists.putIfAbsent(name, new Playlist(name, this));
    }

    public void putInPlaylist(int index, String playlist)  {
        if (!properties.getProperty(String.valueOf(index)).contains(playlist))   {
            properties.setProperty(Integer.toString(index), playlist + "\n");
        }
    }

    public void removeFromPlaylist(int index, String playlist)  {
        StringBuilder songPlaylists = new StringBuilder(properties.getProperty(String.valueOf(index)));
        songPlaylists.delete(songPlaylists.indexOf(playlist), songPlaylists.lastIndexOf(playlist) + 1);

        if (songPlaylists.toString().startsWith("\n"))    {
            songPlaylists.deleteCharAt(0);
        }   else {
            songPlaylists.delete(songPlaylists.indexOf("\n\n"), songPlaylists.lastIndexOf("\n\n"));
        }

        properties.setProperty(String.valueOf(index), songPlaylists.toString());
    }

    public void deletePlaylist(String playlist, Boolean fromLibrary)    {
        if (fromLibrary)    {
            playlists.get(playlist).getSongs().forEach(this::deleteSong);
        }   else {
            playlists.get(playlist).getSongs().forEach(i -> removeFromPlaylist(i, playlist));
        }

        playlists.remove(playlist);
    }

    public void createAlbum(String name, String artist, CopyOnWriteArrayList<File> files)   {
        albums.putIfAbsent(name, new Album(name, this, files.toArray(File[]::new)));
        albums.get(name).setArtist(artist);
    }

    public void createAlbum(String name, String artist, File folder)   {
        albums.putIfAbsent(name, new Album(name, this, folder));
        albums.get(name).setArtist(artist);
    }

    public void createAlbum(String name, String artist) {
        albums.putIfAbsent(name, new Album(name, this));
        albums.get(name).setArtist(artist);
    }

    public void createAlbum(String name)    {
        albums.putIfAbsent(name, new Album(name, this));
    }

    public void deleteAlbum(String album, Boolean fromLibrary)   {
        if (fromLibrary)    {
            albums.get(album).getSongs().forEach(this::deleteSong);
        }

        albums.remove(album);
    }
}
