# MarioStrikersChargedMusicReplacer

### Mario Strikers Charged Music Replacer
* A tool allows you to replace music in the STREAM_GEN_Music.nlxwb file of Mario Strikers Charged
* Note that this won't let you replace character themes and such since unfortunately they have character voices baked into them
* This tool also lets you generate IDSP files for use in the prequel, Super Mario Strikers (that game has basically no music, but it's neat anyway)

### Important Note
* Looping points aren't supported since the game will just ignore them
* The game will just loop start to end, so keep that in mind (so repeat the looping section if needed)

### Music Replacement Notes
* Audio must be in DSP format split into two channels and must be smaller than the original (which this tool will check)
* The tracks are around 3 minutes long and are at 44.1 Khz sample rate, so you can get away with shorter tracks or lowering sample rate to 32Khz if needed
* This tool will attempt to auto select the other track of your song, so name it either with the same file name appended with _L and _R or (channel 0) and (channel 1)
    * Example: mario_L.dsp and mario_R.dsp or luigi (channel 0).dsp and luigi (channel 1).dsp

### Next Level Games IDSP Format Documentation (which is what Strikers Charged internally uses)
* IDSP Header
    * 0x04 bytes - File magic (always IDSP)
    * 0x04 bytes - Interleave Chunk Size
    * 0x04 bytes - Total Audio Data Size per Channel
    * 0x60 bytes - Left Channel DSP Header
    * 0x60 bytes - Right Channel DSP Header

* The Rest of the IDSP
    * Left Channel Data Chunks that are the size defined in the header (last chunk is padded)
    * Right Channel Data Chunks that are the size defined in the header (last chunk is padded)
    * 0x14 bytes of 0x30 (this is the footer) (Super Mario Strikers doesn't have this)

### Special Thanks/Credits
* This documentation on the IDSP header and DSP format helped a lot too
    * https://www.metroid2002.com/retromodding/wiki/DSP_(File_Format)
    * https://github.com/vgmstream/vgmstream/blob/master/src/meta/ngc_dsp_std.c
