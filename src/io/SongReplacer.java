package io;

import constants.StrikersChargedConstants;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class SongReplacer {

    public static boolean replaceSong(File nlxwbFile, File leftChannelFile, File rightChannelFile, int songIndex, boolean deleteDSPAfterModify) throws IOException {
        File idspFile = new File("temp.idsp");

        long allowedFileSize;

        File resbunFile = new File(nlxwbFile.getParentFile(), nlxwbFile.getName().replace(".nlxwb", ".resbun"));

        boolean isStream = nlxwbFile.getName().endsWith("STREAM_GEN_Music.nlxwb");
        boolean isFrontend = nlxwbFile.getName().endsWith("FE_GEN_Music.nlxwb");

        if (isStream) {
            if (songIndex + 1 >= StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS.length) {
                allowedFileSize = nlxwbFile.length() - StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS[songIndex];
            }
            else {
                allowedFileSize = StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS[songIndex+1] - StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS[songIndex];
            }
        }
        else if (isFrontend) {
            if (songIndex + 1 >= StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONGS.length) {
                allowedFileSize = nlxwbFile.length() - StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONG_OFFSETS[songIndex];
            }
            else {
                allowedFileSize = StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONG_OFFSETS[songIndex+1] - StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONG_OFFSETS[songIndex];
            }
        }
        else {
            return false;
        }

        IDSPCreator.createStrikersChargedIDSPFileForNLXWB(leftChannelFile, rightChannelFile, idspFile, isStream);
        byte[] idspFileBytes = Files.readAllBytes(idspFile.toPath());

        if (idspFile.length() > allowedFileSize) {
            long[] offsetTable;

            if (isStream) {
                offsetTable = StrikersChargedConstants.STRIKERS_CHARGED_RESBUN_OFFSETS;
            }
            else {
                offsetTable = StrikersChargedConstants.STRIKERS_CHARGED_MENU_RESBUN_OFFSETS;
            }
            try (RandomAccessFile resbunRaf = new RandomAccessFile(resbunFile, "rw")) {
                resbunRaf.seek(offsetTable[songIndex]);
                resbunRaf.writeInt((int)nlxwbFile.length());
                resbunRaf.writeInt((int)idspFile.length());
            }

            try (RandomAccessFile nlxwbRaf = new RandomAccessFile(nlxwbFile, "rw")) {
                nlxwbRaf.seek(nlxwbRaf.length());
                nlxwbRaf.write(idspFileBytes);
            }
        }

        else {
            long[] offsetTable;
            long songOffset;

            if (isStream) {
                offsetTable = StrikersChargedConstants.STRIKERS_CHARGED_RESBUN_OFFSETS;
                songOffset = StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS[songIndex];
            }
            else {
                offsetTable = StrikersChargedConstants.STRIKERS_CHARGED_MENU_RESBUN_OFFSETS;
                songOffset = StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONG_OFFSETS[songIndex];
            }

            try (RandomAccessFile nlxwbRaf = new RandomAccessFile(nlxwbFile, "rw")) {
                nlxwbRaf.seek(songOffset);
                nlxwbRaf.write(idspFileBytes);
            }

            try (RandomAccessFile resbunRaf = new RandomAccessFile(resbunFile, "rw")) {
                resbunRaf.seek(offsetTable[songIndex]);
                resbunRaf.writeInt((int)songOffset);
                resbunRaf.writeInt((int)idspFile.length());
            }
        }

        logSongReplacement(songIndex, leftChannelFile, rightChannelFile, nlxwbFile.getAbsolutePath(), isStream);

        if (deleteDSPAfterModify) {
            leftChannelFile.delete();
            rightChannelFile.delete();
        }

        return true;
    }

    private static String getSongNameFromIndex(int songIndex, boolean isStream) {
        String songName = "";

        if (isStream) {
            for (int i = 0; i< StrikersChargedConstants.STRIKERS_CHARGED_SONGS.length; i++) {
                if (songIndex == StrikersChargedConstants.STRIKERS_CHARGED_SONGS[i].getSongIndex()) {
                    songName = StrikersChargedConstants.STRIKERS_CHARGED_SONGS[i].getSongDisplayName();
                }
            }
        }
        else {
            for (int i = 0; i< StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONGS.length; i++) {
                if (songIndex == StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONGS[i].getSongIndex()) {
                    songName = StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONGS[i].getSongDisplayName();
                }
            }
        }

        return songName;
    }

    private static void logSongReplacement(int songIndex, File leftChannel, File rightChannel, String nlxwbFilePath, boolean isStream) {
        File songReplacementsFolder = new File("song_replacements");
        if (!songReplacementsFolder.exists()) {
            songReplacementsFolder.mkdirs();
        }

        File logFile;

        String hash = Integer.toHexString(nlxwbFilePath.hashCode());
        String baseFileName = "Strikers Charged" + "_" + hash;
        logFile = new File("song_replacements", baseFileName + ".txt");

        Map<String, String> songMap = new TreeMap<>();

        if (logFile.exists()) {
            try (Scanner inputStream = new Scanner(new FileInputStream(logFile))) {
                while (inputStream.hasNextLine()) {
                    String line = inputStream.nextLine();
                    String[] parts = line.split("\\|");

                    if (parts.length >= 3) {
                        String existingSongName = parts[0];
                        String left = parts[1];
                        String right = parts[2];
                        songMap.put(existingSongName, left + "|" + right);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String songName = getSongNameFromIndex(songIndex, isStream);

        songMap.put(songName, leftChannel.getName() + "|" + rightChannel.getName());

        try (PrintWriter outputStream = new PrintWriter(new FileOutputStream(logFile))) {
            for (Map.Entry<String, String> entry : songMap.entrySet()) {
                outputStream.println(entry.getKey() + "|" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
