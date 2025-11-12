package io;

import helpers.OffsetGetter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class SongDumper {

    public static void dumpAllSongs(File nlxwbFile, File outputFolder) throws IOException {
        splitFileByIDSP(nlxwbFile, outputFolder);
    }

    private static void splitFileByIDSP(File file, File outputFolder) throws IOException {
        byte[] fileData = Files.readAllBytes(file.toPath());
        ArrayList<Integer> positions = OffsetGetter.findIDSPOffsets(fileData);

        if (positions.isEmpty()) {
            throw new IOException("No IDSP headers found in file.");
        }

        Path outputDir = outputFolder.toPath();
        Files.createDirectories(outputDir);

        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i);
            int end = (i + 1 < positions.size()) ? positions.get(i + 1) : fileData.length;

            byte[] idspChunk = new byte[end - start];
            System.arraycopy(fileData, start, idspChunk, 0, idspChunk.length);

            Path outputPath = outputDir.resolve(String.format("%03d.idsp", i));
            Files.write(outputPath, idspChunk);
        }
    }
}
