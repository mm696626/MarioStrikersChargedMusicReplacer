# MarioStrikersChargedMusicReplacer

### Mario Strikers Charged Music Replacer
* A tool allows you to replace music in the STREAM_GEN_Music.nlxwb (**for gameplay songs**) and FE_GEN_Music.nlxwb (**for menu songs**) files of Mario Strikers Charged
* When replacing, **you must have the corresponding resbun file in the same location**
  * This file contains the offsets and file sizes of the idsps in the nlxwb file
* Note that this won't let you replace character themes and such since unfortunately they have character voices baked into them and are in another file anyway
  * This also won't let you replace the match highlights theme and Megastrike theme since they're also located in that file as well
* You can also create IDSP files as well for both Strikers games (which would technically allow music replacement in Super Mario Strikers as well) 

### Important Note
* Looping points aren't supported since the game will just ignore them (Super Mario Strikers does this too)
  * This is a game limitation due to how they programmed the music playback
* The game will just loop start to end, so keep that in mind (so repeat the looping section if needed or loop it so the start is the loop point)

### Music Replacement Notes
* Audio must be in DSP format split into two channels
* This tool will attempt to auto select the other track of your song, so name it either with the same file name appended with _L and _R or (channel 0) and (channel 1)
    * Example: mario_L.dsp and mario_R.dsp or luigi (channel 0).dsp and luigi (channel 1).dsp

### Next Level Games IDSP Format Documentation (which is what the Mario Strikers games internally use)
* IDSP Header
    * 0x04 bytes - File magic (always IDSP)
    * 0x04 bytes - Interleave Chunk Size
    * 0x04 bytes - Total Audio Data Size per Channel
    * 0x60 bytes - Left Channel DSP Header
    * 0x60 bytes - Right Channel DSP Header

* The Rest of the IDSP
    * Left Channel Data Chunks that are the size defined in the header (last chunk is padded. Super Mario Strikers does not do this)
    * Right Channel Data Chunks that are the size defined in the header (last chunk is padded. Super Mario Strikers does not do this)
    * 0x14 bytes of 0x30 (this is the footer. Super Mario Strikers does not have this)

### Special Thanks/Credits
* This documentation on the IDSP header and DSP format helped a lot too
    * https://www.metroid2002.com/retromodding/wiki/DSP_(File_Format)
    * https://github.com/vgmstream/vgmstream/blob/master/src/meta/ngc_dsp_std.c
