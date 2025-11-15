package io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class IDSPCreator {

    private static final int DSP_HEADER_SIZE = 0x60;
    private static final int INTERLEAVE_CHUNK_SIZE = 0x6B40;
    private static final int INTERLEAVE_CHUNK_SIZE_MENU = 0xD680;

    public static void createStrikersChargedIDSPFile(File leftFile, File rightFile, File idspFile, boolean isStream) throws IOException {
        try (RandomAccessFile left = new RandomAccessFile(leftFile, "r");
             RandomAccessFile right = new RandomAccessFile(rightFile, "r");
             FileOutputStream out = new FileOutputStream(idspFile)) {

            long audioSize = left.length();

            out.write(new byte[]{0x49, 0x44, 0x53, 0x50}); //IDSP magic

            int interleave;

            if (isStream) {
                out.write(new byte[]{0x00, 0x00, 0x6B, 0x40}); //interleave size
                interleave = INTERLEAVE_CHUNK_SIZE;
            }

            else {
                out.write(new byte[]{0x00, 0x00, (byte) 0xD6, (byte) 0x80}); //interleave size
                interleave = INTERLEAVE_CHUNK_SIZE_MENU;
            }

            int dataSize = (int)(audioSize - DSP_HEADER_SIZE);
            out.write(intToBytesBE(dataSize));

            byte[] leftHeader = new byte[DSP_HEADER_SIZE];
            byte[] rightHeader = new byte[DSP_HEADER_SIZE];
            left.readFully(leftHeader);
            right.readFully(rightHeader);
            out.write(leftHeader);
            out.write(rightHeader);

            byte[] leftChunk = new byte[interleave];
            byte[] rightChunk = new byte[interleave];

            while (true) {
                int leftRead = left.read(leftChunk);
                int rightRead = right.read(rightChunk);

                if (leftRead == -1 && rightRead == -1) break;

                if (leftRead > 0 && leftRead < interleave) {
                    for (int i = leftRead; i < interleave; i++) leftChunk[i] = 0;
                    leftRead = interleave;
                }

                if (rightRead > 0 && rightRead < interleave) {
                    for (int i = rightRead; i < interleave; i++) rightChunk[i] = 0;
                    rightRead = interleave;
                }

                if (leftRead > 0) out.write(leftChunk, 0, leftRead);
                if (rightRead > 0) out.write(rightChunk, 0, rightRead);
            }

            byte[] footer = new byte[0x14];
            for (int i = 0; i < footer.length; i++) footer[i] = 0x30;
            out.write(footer);
        }
    }

    public static void createSuperMarioStrikersIDSPFile(File leftFile, File rightFile, File idspFile, int interleave) throws IOException {
        try (RandomAccessFile left = new RandomAccessFile(leftFile, "r");
             RandomAccessFile right = new RandomAccessFile(rightFile, "r");
             FileOutputStream out = new FileOutputStream(idspFile)) {

            long audioSize = left.length();

            out.write(new byte[]{0x49, 0x44, 0x53, 0x50}); // IDSP magic

            out.write(intToBytesBE(interleave)); //write interleave

            int dataSize = (int) (audioSize - DSP_HEADER_SIZE);
            out.write(intToBytesBE(dataSize));

            byte[] leftHeader = new byte[DSP_HEADER_SIZE];
            byte[] rightHeader = new byte[DSP_HEADER_SIZE];
            left.readFully(leftHeader);
            right.readFully(rightHeader);
            out.write(leftHeader);
            out.write(rightHeader);

            byte[] leftChunk = new byte[interleave];
            byte[] rightChunk = new byte[interleave];

            while (true) {
                int leftRead = left.read(leftChunk);
                int rightRead = right.read(rightChunk);

                if (leftRead == -1 && rightRead == -1)
                    break;

                if (leftRead > 0)
                    out.write(leftChunk, 0, leftRead);

                if (rightRead > 0)
                    out.write(rightChunk, 0, rightRead);
            }
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
