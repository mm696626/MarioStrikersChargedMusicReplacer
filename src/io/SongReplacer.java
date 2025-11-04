package io;

import helpers.OffsetGetter;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

public class SongReplacer {

    private static final int DSP_HEADER_SIZE = 0x60;
    private static final int INTERLEAVE_CHUNK_SIZE = 0x6B40;


    public static boolean replaceSong(File nlxwbFile, File leftChannelFile, File rightChannelFile, int songIndex) throws IOException {
        int[] songOffsets = OffsetGetter.findIDSPOffsets(Files.readAllBytes(nlxwbFile.toPath()));

        createIDSP(leftChannelFile, rightChannelFile);

        int allowedFileSize;

        if (songIndex + 1 >= songOffsets.length) {
            allowedFileSize = (int)nlxwbFile.length() - songOffsets[songIndex];
        }
        else {
            allowedFileSize = songOffsets[songIndex+1] - songOffsets[songIndex];
        }

        File idspFile = new File("temp.idsp");

        if (idspFile.length() > allowedFileSize) {
            JOptionPane.showMessageDialog(null, "Files are too large!");
            idspFile.delete();
            return false;
        }

        byte[] idspFileBytes = Files.readAllBytes(idspFile.toPath());

        try (RandomAccessFile nlxwbRaf = new RandomAccessFile(nlxwbFile, "rw")) {
            nlxwbRaf.seek(songOffsets[songIndex]);
            nlxwbRaf.write(idspFileBytes);
        }

        return true;
    }

    private static void createIDSP(File leftFile, File rightFile) throws IOException {
        try (RandomAccessFile left = new RandomAccessFile(leftFile, "r");
             RandomAccessFile right = new RandomAccessFile(rightFile, "r");
             FileOutputStream out = new FileOutputStream("temp.idsp")) {

            long audioSize = left.length();

            out.write("IDSP".getBytes("ASCII"));

            out.write(new byte[]{0x00, 0x00, 0x6B, 0x40});

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
