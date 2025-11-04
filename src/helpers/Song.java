package helpers;

public class Song {
    private int songIndex;
    private String songDisplayName;

    public Song(int songIndex, String songDisplayName) {
        this.songIndex = songIndex;
        this.songDisplayName = songDisplayName;
    }

    public int getSongIndex() {
        return songIndex;
    }

    public String getSongDisplayName() {
        return songDisplayName;
    }
}