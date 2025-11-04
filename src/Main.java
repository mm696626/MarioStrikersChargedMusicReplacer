// Mario Strikers Charged Music Replacer by Matt McCullough
// A tool to replace the music in Mario Strikers Charged

import ui.MarioStrikersChargedMusicReplacerUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        MarioStrikersChargedMusicReplacerUI marioStrikersChargedMusicReplacerUI = new MarioStrikersChargedMusicReplacerUI();
        marioStrikersChargedMusicReplacerUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marioStrikersChargedMusicReplacerUI.pack();
        marioStrikersChargedMusicReplacerUI.setVisible(true);
    }
}