package io;

import constants.StrikersChargedConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SongDumper {

    public static void dumpSong(File nlxwbFile, File outputFolder, int selectedSongIndex) throws IOException {
        byte[] fileData = Files.readAllBytes(nlxwbFile.toPath());
        long[] positions = StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS;

        Path outputDir = outputFolder.toPath();
        Files.createDirectories(outputDir);

        long start = positions[selectedSongIndex];
        long end = (selectedSongIndex + 1 < positions.length) ? positions[selectedSongIndex+1] : fileData.length;

        byte[] idspChunk = new byte[(int)(end - start)];
        System.arraycopy(fileData, (int)start, idspChunk, 0, idspChunk.length);

        Path outputPath = outputDir.resolve(getSongNameFromIndex(selectedSongIndex) + ".idsp");
        Files.write(outputPath, idspChunk);
    }

    public static void dumpAllSongs(File nlxwbFile, File outputFolder) throws IOException {
        byte[] fileData = Files.readAllBytes(nlxwbFile.toPath());
        long[] positions = StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS;

        Path outputDir = outputFolder.toPath();
        Files.createDirectories(outputDir);

        for (int i = 0; i < positions.length; i++) {
            long start = positions[i];
            long end = (i + 1 < positions.length) ? positions[i+1] : fileData.length;

            byte[] idspChunk = new byte[(int)(end - start)];
            System.arraycopy(fileData, (int)start, idspChunk, 0, idspChunk.length);

            Path outputPath = outputDir.resolve(getSongNameFromIndex(i) + ".idsp");
            Files.write(outputPath, idspChunk);
        }
    }

    private static String getSongNameFromIndex(int index) {
        for (int i=0; i<StrikersChargedConstants.STRIKERS_CHARGED_SONGS.length; i++) {
            if (index == StrikersChargedConstants.STRIKERS_CHARGED_SONGS[i].getSongIndex()) {
                return StrikersChargedConstants.STRIKERS_CHARGED_SONGS[i].getSongDisplayName();
            }
        }

        return "chunk_" + index;
    }
}
