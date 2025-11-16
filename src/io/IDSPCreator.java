package io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class IDSPCreator {

    private static final int DSP_HEADER_SIZE = 0x60;
    private static final int INTERLEAVE_CHUNK_SIZE = 0x6B40;
    private static final int INTERLEAVE_CHUNK_SIZE_MENU = 0xD680;

    public static void createStrikersChargedIDSPFileForNLXWB(File leftFile, File rightFile, File idspFile, boolean isStream) throws IOException {
        int interleave;

        if (isStream) {
            interleave = INTERLEAVE_CHUNK_SIZE;
        }

        else {
            interleave = INTERLEAVE_CHUNK_SIZE_MENU;
        }

        createStrikersChargedIDSPFile(leftFile, rightFile, idspFile, interleave);
    }

    public static void createStrikersChargedIDSPFile(File leftFile, File rightFile, File idspFile, int interleave) throws IOException {
        //pad audio data to be divisible by 0x20 just in case Strikers Charged doesn't like it
        padDSPFileTo0x20(leftFile);
        padDSPFileTo0x20(rightFile);

        try (RandomAccessFile left = new RandomAccessFile(leftFile, "r");
             RandomAccessFile right = new RandomAccessFile(rightFile, "r");
             FileOutputStream out = new FileOutputStream(idspFile)) {

            long audioSize = left.length();

            out.write(new byte[]{0x49, 0x44, 0x53, 0x50}); //IDSP magic

            out.write(intToBytesBE(interleave)); //write interleave

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
        //pad audio data to be divisible by 0x20 since Super Mario Strikers doesn't like it
        padDSPFileTo0x20(leftFile);
        padDSPFileTo0x20(rightFile);

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

    private static void padDSPFileTo0x20(File dspFile) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(dspFile, "rw")) {
            long length = raf.length();
            long pad = (0x20 - (length % 0x20)) & 0x1F;

            if (pad > 0) {
                backupDSPFile(dspFile);
                raf.seek(length);
                raf.write(new byte[(int)pad]);
            }
        }
    }

    private static void backupDSPFile(File dspFile) throws IOException {
        String dspFileName = dspFile.getName();
        int dot = dspFileName.lastIndexOf('.');
        String backupName;

        if (dot != -1) {
            backupName = dspFileName.substring(0, dot) + "_backup" + dspFileName.substring(dot);
        } else {
            backupName = dspFileName + "_backup";
        }

        File backupDSPFile = new File(dspFile.getParentFile(), backupName);

        Files.copy(dspFile.toPath(), backupDSPFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
