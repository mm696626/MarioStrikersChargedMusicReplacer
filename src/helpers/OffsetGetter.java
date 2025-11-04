package helpers;

import java.util.ArrayList;

public class OffsetGetter {

    private static final byte[] IDSP_BYTES = new byte[] { 'I', 'D', 'S', 'P' };

    public static ArrayList<Integer> findIDSPOffsets(byte[] data) {
        ArrayList<Integer> idspOffsets = new ArrayList<>();

        for (int i = 0; i <= data.length - IDSP_BYTES.length; i++) {
            boolean match = true;
            for (int j = 0; j < IDSP_BYTES.length; j++) {
                if (data[i + j] != IDSP_BYTES[j]) {
                    match = false;
                    break;
                }
            }
            if (match) idspOffsets.add(i);
        }

        return idspOffsets;
    }
}
