package constants;

import helpers.Song;

public class StrikersChargedConstants {

    public static final Song[] STRIKERS_CHARGED_SONGS = {
            new Song(0, "Sudden Death"),
            new Song(1, "Loading Screen"),
            new Song(2, "Main Menu"),
            new Song(3, "Pause Menu"),
            new Song(4, "Galactic Stadium"),
            new Song(5, "The Vice"),
            new Song(6, "Thunder Island"),
            new Song(8, "The Lava Pit"),
            new Song(10, "Sand Tomb"),
            new Song(11, "Stormship Stadium"),
            new Song(14, "The Wastelands"),
            new Song(15, "Crystal Canyon"),
            new Song(19, "The Dump"),
            new Song(20, "End of Championship"),
            new Song(21, "Road to the Striker Cup A"),
            new Song(22, "Broken Dreams"),
            new Song(23, "Record Breakers"),
            new Song(24, "Road to the Striker Cup B"),
            new Song(25, "To the Next Round"),
            new Song(26, "Classroom"),
            new Song(27, "Star"),
            new Song(28, "Super Mario"),
            new Song(29, "Super Luigi"),
            new Song(30, "Results"),
            new Song(31, "Hall of Fame"),
            new Song(32, "Credits")
    };

    //hardcoding these in the edge case the DSP data somehow generates IDSP
    //yes, these are identical in all versions of the game
    public static final long[] STRIKERS_CHARGED_SONG_OFFSETS = {
            0x00000000,
            0x0035A0E0,
            0x00711F40,
            0x0078AAA0,
            0x0103C280,
            0x01B2E1E0,
            0x023FA6C0,
            0x02CC6BA0,
            0x03B3AE80,
            0x04457A60,
            0x052CBD40,
            0x05B7D520,
            0x06457080,
            0x072CB360,
            0x0813F640,
            0x089F0E20,
            0x092CA980,
            0x0A13EC60,
            0x0AFB2F40,
            0x0BE27220,
            0x0C6D8A00,
            0x0CAD38E0,
            0x0CBED240,
            0x0CD71FA0,
            0x0CE05800,
            0x0D1D8360,
            0x0D2F1CC0,
            0x0DB95E20,
            0x0DD0D500,
            0x0DE69EE0,
            0x0DFD3F40,
            0x0E3E9B20,
            0x0E553B80
    };
}
