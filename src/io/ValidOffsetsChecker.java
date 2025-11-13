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
            if (nlxwbOffsets.size() < StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS.length) {
                return false;
            }

            for (int i=0; i<nlxwbOffsets.size(); i++) {
                boolean matchFound = false;

                for (int j=0; j<StrikersChargedConstants.STRIKERS_CHARGED_SONG_OFFSETS.length; j++) {
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
            if (nlxwbOffsets.size() < StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONGS.length) {
                return false;
            }

            for (int i=0; i<nlxwbOffsets.size(); i++) {
                boolean matchFound = false;

                for (int j=0; j<StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONG_OFFSETS.length; j++) {
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
