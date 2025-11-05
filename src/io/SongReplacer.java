package io;

import constants.StrikersChargedConstants;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class SongReplacer {

    private static final int DSP_HEADER_SIZE = 0x60;
    private static final int INTERLEAVE_CHUNK_SIZE = 0x6B40;


    public static boolean replaceSong(File nlxwbFile, File leftChannelFile, File rightChannelFile, int songIndex) throws IOException {
        createIDSP(leftChannelFile, rightChannelFile);

        long allowedFileSize;

        if (songIndex + 1 >= StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS.length) {
            allowedFileSize = nlxwbFile.length() - StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS[songIndex];
        }
        else {
            allowedFileSize = StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS[songIndex+1] - StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS[songIndex];
        }

        File idspFile = new File("temp.idsp");

        if (idspFile.length() > allowedFileSize) {
            JOptionPane.showMessageDialog(null, "Files are too large!");
            idspFile.delete();
            return false;
        }

        byte[] idspFileBytes = Files.readAllBytes(idspFile.toPath());

        try (RandomAccessFile nlxwbRaf = new RandomAccessFile(nlxwbFile, "rw")) {
            nlxwbRaf.seek(StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS[songIndex]);
            nlxwbRaf.write(idspFileBytes);
        }

        logSongReplacement(songIndex, leftChannelFile, rightChannelFile, nlxwbFile.getAbsolutePath());

        return true;
    }

    private static void logSongReplacement(int songIndex, File leftChannel, File rightChannel, String nlxwbFilePath) {
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

        String songName = "";

        for (int i = 0; i< StrikersChargedConstants.STRIKERS_CHARGED_SONGS.length; i++) {
            if (songIndex == StrikersChargedConstants.STRIKERS_CHARGED_SONGS[i].getSongIndex()) {
                songName = StrikersChargedConstants.STRIKERS_CHARGED_SONGS[i].getSongDisplayName();
            }
        }

        songMap.put(songName, leftChannel.getName() + "|" + rightChannel.getName());

        try (PrintWriter outputStream = new PrintWriter(new FileOutputStream(logFile))) {
            for (Map.Entry<String, String> entry : songMap.entrySet()) {
                outputStream.println(entry.getKey() + "|" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createIDSP(File leftFile, File rightFile) throws IOException {
        try (RandomAccessFile left = new RandomAccessFile(leftFile, "r");
             RandomAccessFile right = new RandomAccessFile(rightFile, "r");
             FileOutputStream out = new FileOutputStream("temp.idsp")) {

            long audioSize = left.length();

            out.write(new byte[]{0x49, 0x44, 0x53, 0x50}); //IDSP magic

            out.write(new byte[]{0x00, 0x00, 0x6B, 0x40}); //interleave size

            int dataSize = (int)(audioSize - DSP_HEADER_SIZE);
            out.write(intToBytesBE(dataSize));

            byte[] leftHeader = new byte[DSP_HEADER_SIZE];
            byte[] rightHeader = new byte[DSP_HEADER_SIZE];
            left.readFully(leftHeader);
            right.readFully(rightHeader);
            out.write(leftHeader);
            out.write(rightHeader);

            byte[] leftChunk = new byte[INTERLEAVE_CHUNK_SIZE];
            byte[] rightChunk = new byte[INTERLEAVE_CHUNK_SIZE];

            while (true) {
                int leftRead = left.read(leftChunk);
                int rightRead = right.read(rightChunk);

                if (leftRead == -1 && rightRead == -1) break;

                if (leftRead > 0 && leftRead < INTERLEAVE_CHUNK_SIZE) {
                    for (int i = leftRead; i < INTERLEAVE_CHUNK_SIZE; i++) leftChunk[i] = 0;
                    leftRead = INTERLEAVE_CHUNK_SIZE;
                }

                if (rightRead > 0 && rightRead < INTERLEAVE_CHUNK_SIZE) {
                    for (int i = rightRead; i < INTERLEAVE_CHUNK_SIZE; i++) rightChunk[i] = 0;
                    rightRead = INTERLEAVE_CHUNK_SIZE;
                }

                if (leftRead > 0) out.write(leftChunk, 0, leftRead);
                if (rightRead > 0) out.write(rightChunk, 0, rightRead);
            }

            byte[] footer = new byte[0x14];
            for (int i = 0; i < footer.length; i++) footer[i] = 0x30;
            out.write(footer);
        }
    }

    private static byte[] intToBytesBE(int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }
}
