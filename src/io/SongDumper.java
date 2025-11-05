package io;

import constants.StrikersChargedConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SongDumper {

    public static void dumpAllSongs(File nlxwbFile, File outputFolder) throws IOException {
        splitFileByIDSP(nlxwbFile, outputFolder);
    }

    private static void splitFileByIDSP(File file, File outputFolder) throws IOException {
        byte[] fileData = Files.readAllBytes(file.toPath());
        long[] positions = StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS;

        Path outputDir = outputFolder.toPath();
        Files.createDirectories(outputDir);

        for (int i = 0; i < positions.length; i++) {
            long start = positions[i];
            long end = (i + 1 < positions.length) ? positions[i+1] : fileData.length;

            byte[] idspChunk = new byte[(int)(end - start)];
            System.arraycopy(fileData, (int)start, idspChunk, 0, idspChunk.length);

            Path outputPath = outputDir.resolve(String.format("chunk_%03d.idsp", i));
            Files.write(outputPath, idspChunk);
        }
    }
}
