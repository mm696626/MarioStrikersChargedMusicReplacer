package io;

import constants.StrikersChargedConstants;
import helpers.OffsetGetter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class ValidOffsetsChecker {

    public static boolean checkValidOffsets(File nlxwbFile) throws IOException {
        byte[] fileData = Files.readAllBytes(nlxwbFile.toPath());
        ArrayList<Integer> nlxwbOffsets = OffsetGetter.findIDSPOffsets(fileData);

        if (nlxwbFile.getName().equals("STREAM_GEN_Music.nlxwb")) {
            int streamSongOffsetLength = StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS.length;
            if (nlxwbOffsets.size() < streamSongOffsetLength) {
                return false;
            }

            for (int i = 0; i< streamSongOffsetLength; i++) {
                boolean matchFound = false;

                for (int j = 0; j< streamSongOffsetLength; j++) {
                    if (nlxwbOffsets.get(i) == StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS[j]) {
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound) {
                    return false;
                }
            }
        }

        else if (nlxwbFile.getName().equals("FE_GEN_Music.nlxwb")) {
            int menuSongOffsetLength = StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONG_OFFSETS.length;
            if (nlxwbOffsets.size() < menuSongOffsetLength) {
                return false;
            }

            for (int i = 0; i< menuSongOffsetLength; i++) {
                boolean matchFound = false;

                for (int j = 0; j< menuSongOffsetLength; j++) {
                    if (nlxwbOffsets.get(i) == StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONG_OFFSETS[j]) {
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound) {
                    return false;
                }
            }
        }


        return true;
    }
}
