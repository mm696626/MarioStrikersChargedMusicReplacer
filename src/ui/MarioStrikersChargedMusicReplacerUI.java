package ui;

import constants.StrikersChargedConstants;
import helpers.DSPPair;
import helpers.Song;
import io.SongDumper;
import io.SongReplacer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class MarioStrikersChargedMusicReplacerUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, dumpAllSongs, replaceSong, selectNLXWB;
    private String leftChannelPath = "";
    private String rightChannelPath = "";

    private File savedDSPFolder;

    private String nlxwbPath;

    private JLabel leftChannelLabel;
    private JLabel rightChannelLabel;
    private JLabel nlxwbFilePathLabel;

    private JComboBox<String> songSelector;

    public MarioStrikersChargedMusicReplacerUI() {
        setTitle("Mario Strikers Charged Music Replacer");
        generateUI();
    }

    private void generateUI() {

        JPanel musicReplacerPanel = new JPanel();
        musicReplacerPanel.setLayout(new BoxLayout(musicReplacerPanel, BoxLayout.Y_AXIS));

        JPanel songPanel = new JPanel(new GridBagLayout());
        songPanel.setBorder(BorderFactory.createTitledBorder("Song Selection"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        songPanel.add(new JLabel("Song:"), gbc);

        songSelector = new JComboBox<>(initializeSongArray());
        gbc.gridx = 1;
        songPanel.add(songSelector, gbc);


        JLabel filterLabel = new JLabel("Search Songs:");
        JTextField songSearchField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 2;
        songPanel.add(filterLabel, gbc);

        gbc.gridx = 1;
        songPanel.add(songSearchField, gbc);

        songSearchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filterSongs() {
                String filterText = songSearchField.getText().toLowerCase();
                String[] songNameArray = getSongNameArray();

                String currentSelection = (String) songSelector.getSelectedItem();

                ArrayList<String> filtered = new ArrayList<>();
                for (String song : songNameArray) {
                    if (song.toLowerCase().contains(filterText)) {
                        filtered.add(song);
                    }
                }

                Collections.sort(filtered);
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(filtered.toArray(new String[0]));
                songSelector.setModel(model);

                if (currentSelection != null && filtered.contains(currentSelection)) {
                    songSelector.setSelectedItem(currentSelection);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterSongs();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterSongs();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterSongs();
            }
        });

        JPanel dumpModifyPanel = new JPanel(new GridBagLayout());
        dumpModifyPanel.setBorder(BorderFactory.createTitledBorder("Dump/Modify Songs"));
        GridBagConstraints dumpModifyGBC = new GridBagConstraints();
        dumpModifyGBC.insets = new Insets(5, 5, 5, 5);
        dumpModifyGBC.fill = GridBagConstraints.HORIZONTAL;

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);
        leftChannelLabel = new JLabel("No file selected");

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);
        rightChannelLabel = new JLabel("No file selected");

        dumpAllSongs = new JButton("Dump All Songs");
        dumpAllSongs.addActionListener(this);

        replaceSong = new JButton("Replace Song");
        replaceSong.addActionListener(this);

        selectNLXWB = new JButton("Select NLXWB");
        selectNLXWB.addActionListener(this);

        nlxwbFilePathLabel = new JLabel("No NLXWB file selected");

        dumpModifyGBC.gridx = 0; dumpModifyGBC.gridy = 0;
        dumpModifyPanel.add(pickLeftChannel, dumpModifyGBC);
        dumpModifyGBC.gridx = 1;
        dumpModifyPanel.add(leftChannelLabel, dumpModifyGBC);

        dumpModifyGBC.gridx = 0; dumpModifyGBC.gridy = 1;
        dumpModifyPanel.add(pickRightChannel, dumpModifyGBC);
        dumpModifyGBC.gridx = 1;
        dumpModifyPanel.add(rightChannelLabel, dumpModifyGBC);

        dumpModifyGBC.gridx = 0; dumpModifyGBC.gridy = 2;
        dumpModifyPanel.add(dumpAllSongs, dumpModifyGBC);

        dumpModifyGBC.gridx = 1; dumpModifyGBC.gridy = 2;
        dumpModifyPanel.add(replaceSong, dumpModifyGBC);

        dumpModifyGBC.gridx = 0; dumpModifyGBC.gridy = 3;
        dumpModifyPanel.add(selectNLXWB, dumpModifyGBC);

        dumpModifyGBC.gridx = 1;
        dumpModifyPanel.add(nlxwbFilePathLabel, dumpModifyGBC);

        musicReplacerPanel.add(songPanel);
        musicReplacerPanel.add(Box.createVerticalStrut(10));
        musicReplacerPanel.add(dumpModifyPanel);

        setLayout(new BorderLayout());
        add(musicReplacerPanel, BorderLayout.CENTER);
    }

    private String[] initializeSongArray() {

        String[] initialSongArray = new String[StrikersChargedConstants.STRIKERS_CHARGED_SONGS.length];

        for (int i=0; i<initialSongArray.length; i++) {
            initialSongArray[i] = StrikersChargedConstants.STRIKERS_CHARGED_SONGS[i].getSongDisplayName();
        }

        Arrays.sort(initialSongArray, String.CASE_INSENSITIVE_ORDER);
        return initialSongArray;
    }

    private String[] getSongNameArray() {
        Song[] songArray;
        String[] songNameArray;

        songNameArray = new String[StrikersChargedConstants.STRIKERS_CHARGED_SONGS.length];
        songArray = StrikersChargedConstants.STRIKERS_CHARGED_SONGS;

        for (int i=0; i<songNameArray.length; i++) {
            songNameArray[i] = songArray[i].getSongDisplayName();
        }

        return songNameArray;
    }

    private void useSavedDSPFolder() {
        if (savedDSPFolder == null) return;

        ArrayList<DSPPair> dspPairs = DSPPair.detectDSPPairs(savedDSPFolder);

        if (dspPairs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching DSP pairs found in the saved folder.");
            savedDSPFolder = null;
            return;
        }

        DSPPair selectedPair = showSearchableDSPDialog(dspPairs);
        if (selectedPair != null) {
            leftChannelPath = selectedPair.getLeft().getAbsolutePath();
            rightChannelPath = selectedPair.getRight().getAbsolutePath();
            leftChannelLabel.setText(selectedPair.getLeft().getName());
            rightChannelLabel.setText(selectedPair.getRight().getName());
        }
    }

    private DSPPair showSearchableDSPDialog(ArrayList<DSPPair> dspPairs) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select DSP Pair", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JTextField searchField = new JTextField();
        dialog.add(searchField, BorderLayout.NORTH);

        DefaultListModel<DSPPair> listModel = new DefaultListModel<>();
        dspPairs.forEach(listModel::addElement);

        JList<DSPPair> dspList = new JList<>(listModel);
        dspList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dspList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DSPPair pair) {
                    setText(pair.getLeft().getName() + " / " + pair.getRight().getName());
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(dspList);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String filter = searchField.getText().trim().toLowerCase();
                listModel.clear();
                for (DSPPair pair : dspPairs) {
                    String name = pair.getLeft().getName().toLowerCase() + " " + pair.getRight().getName().toLowerCase();
                    if (name.contains(filter)) listModel.addElement(pair);
                }
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        final DSPPair[] selectedPair = {null};

        dspList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && dspList.getSelectedValue() != null) {
                    selectedPair[0] = dspList.getSelectedValue();
                    dialog.dispose();
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && dspList.getSelectedValue() != null) {
                    selectedPair[0] = dspList.getSelectedValue();
                    dialog.dispose();
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                int size = dspList.getModel().getSize();
                int index = dspList.getSelectedIndex();

                if (code == KeyEvent.VK_DOWN) {
                    if (size > 0) {
                        if (index < size - 1) dspList.setSelectedIndex(index + 1);
                        else dspList.setSelectedIndex(0);
                        dspList.ensureIndexIsVisible(dspList.getSelectedIndex());
                    }
                    e.consume();
                } else if (code == KeyEvent.VK_UP) {
                    if (size > 0) {
                        if (index > 0) dspList.setSelectedIndex(index - 1);
                        else dspList.setSelectedIndex(size - 1);
                        dspList.ensureIndexIsVisible(dspList.getSelectedIndex());
                    }
                    e.consume();
                }
            }
        });

        okButton.addActionListener(e -> {
            selectedPair[0] = dspList.getSelectedValue();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dspList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && dspList.getSelectedValue() != null) {
                    selectedPair[0] = dspList.getSelectedValue();
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);
        return selectedPair[0];
    }

    private void chooseLeftChannelPath() {
        if (savedDSPFolder != null) {
            useSavedDSPFolder();
            return;
        }

        int response = JOptionPane.showConfirmDialog(
                this,
                "Would you like to pick a folder of DSPs to select a song from?\n(Your choice will be remembered until closing the program)",
                "Choose DSP Folder",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            JFileChooser dspFolderChooser = new JFileChooser();
            dspFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dspFolderChooser.setDialogTitle("Select Folder with DSP Files");
            dspFolderChooser.setAcceptAllFileFilterUsed(false);

            int folderSelected = dspFolderChooser.showOpenDialog(this);
            if (folderSelected == JFileChooser.APPROVE_OPTION) {
                savedDSPFolder = dspFolderChooser.getSelectedFile();
                useSavedDSPFolder();
            }
        } else {
            chooseDSP(true);
        }
    }

    private void chooseRightChannelPath() {
        if (savedDSPFolder != null) {
            useSavedDSPFolder();
            return;
        }

        int response = JOptionPane.showConfirmDialog(
                this,
                "Would you like to pick a folder of DSPs to select a song from?\n(Your choice will be remembered until closing the program or if you set a default folder)",
                "Choose DSP Folder",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            JFileChooser dspFolderChooser = new JFileChooser();
            dspFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dspFolderChooser.setDialogTitle("Select Folder with DSP Files");
            dspFolderChooser.setAcceptAllFileFilterUsed(false);

            int folderSelected = dspFolderChooser.showOpenDialog(this);
            if (folderSelected == JFileChooser.APPROVE_OPTION) {
                savedDSPFolder = dspFolderChooser.getSelectedFile();
                useSavedDSPFolder();
            }
        } else {
            chooseDSP(false);
        }
    }

    private void chooseDSP(boolean isLeft) {
        JFileChooser dspFileChooser = new JFileChooser();

        if (isLeft) {
            dspFileChooser.setDialogTitle("Select DSP Left Channel");
        }
        else {
            dspFileChooser.setDialogTitle("Select DSP Right Channel");
        }

        dspFileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter dspFilter = new FileNameExtensionFilter("DSP Files", "dsp");
        dspFileChooser.setFileFilter(dspFilter);

        int userSelection = dspFileChooser.showOpenDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = dspFileChooser.getSelectedFile();

        if (isLeft) {
            leftChannelPath = selectedFile.getAbsolutePath();
            leftChannelLabel.setText(selectedFile.getName());
        }

        else {
            rightChannelPath = selectedFile.getAbsolutePath();
            rightChannelLabel.setText(selectedFile.getName());
        }

        File otherChannel = detectOtherChannel(selectedFile, isLeft);
        if (otherChannel != null) {
            if (isLeft) {
                rightChannelPath = otherChannel.getAbsolutePath();
                rightChannelLabel.setText(otherChannel.getName());
            }
            else {
                leftChannelPath = otherChannel.getAbsolutePath();
                leftChannelLabel.setText(otherChannel.getName());
            }
        }
    }

    private File detectOtherChannel(File selectedFile, boolean isLeftSelected) {
        String fileName = selectedFile.getName();
        File parentDir = selectedFile.getParentFile();

        String otherChannelName = null;

        if (fileName.endsWith("_L.dsp") && isLeftSelected) {
            otherChannelName = fileName.replace("_L.dsp", "_R.dsp");
        } else if (fileName.endsWith("_R.dsp") && !isLeftSelected) {
            otherChannelName = fileName.replace("_R.dsp", "_L.dsp");
        } else if (fileName.endsWith("(channel 0).dsp") && isLeftSelected) {
            otherChannelName = fileName.replace("(channel 0).dsp", "(channel 1).dsp");
        } else if (fileName.endsWith("(channel 1).dsp") && !isLeftSelected) {
            otherChannelName = fileName.replace("(channel 1).dsp", "(channel 0).dsp");
        } else if (fileName.endsWith("_l.dsp") && isLeftSelected) {
            otherChannelName = fileName.replace("_l.dsp", "_r.dsp");
        } else if (fileName.endsWith("_r.dsp") && !isLeftSelected) {
            otherChannelName = fileName.replace("_r.dsp", "_l.dsp");
        }

        if (otherChannelName != null) {
            File otherChannelFile = new File(parentDir, otherChannelName);
            if (otherChannelFile.exists()) {
                return otherChannelFile;
            }
        }

        return null;
    }

    private void initNLXWBPath() {
        JFileChooser nlxwbFileChooser = new JFileChooser();
        nlxwbFileChooser.setDialogTitle("Select NLXWB file");
        nlxwbFileChooser.setAcceptAllFileFilterUsed(false);

        FileNameExtensionFilter nlxwbFilter = new FileNameExtensionFilter("NLXWB Files", "nlxwb");
        nlxwbFileChooser.setFileFilter(nlxwbFilter);

        int userSelection = nlxwbFileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedNLXWB = nlxwbFileChooser.getSelectedFile();

        if (selectedNLXWB.getName().equals("STREAM_GEN_Music.nlxwb")) {
            nlxwbPath = selectedNLXWB.getAbsolutePath();
            nlxwbFilePathLabel.setText("Selected NLXWB: " + nlxwbPath);
        }
        else {
            JOptionPane.showMessageDialog(this, "This isn't the correct NLXWB. The correct one is STREAM_GEN_Music.nlxwb");
        }
    }

    private int getSongIndexFromName(String selectedSong) {
        for (int i = 0; i< StrikersChargedConstants.STRIKERS_CHARGED_SONGS.length; i++) {
            if (selectedSong.equals(StrikersChargedConstants.STRIKERS_CHARGED_SONGS[i].getSongDisplayName())) {
                return StrikersChargedConstants.STRIKERS_CHARGED_SONGS[i].getSongIndex();
            }
        }

        return -1;
    }

    private static File getNLXWBFileName(File pdtFile, String timestamp) {
        String baseName = pdtFile.getName();
        int extIndex = baseName.lastIndexOf(".");
        if (extIndex != -1) {
            baseName = baseName.substring(0, extIndex);
        }

        String backupFileName = baseName + "_Backup_" + timestamp + ".pdt";
        return new File(pdtFile.getParent(), backupFileName);
    }

    private void backupNLXWB(File pdtFile) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupFile = getNLXWBFileName(pdtFile, timestamp);
            Files.copy(pdtFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to create backup: " + ex.getMessage());
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pickLeftChannel) {
            chooseLeftChannelPath();
        }

        if (e.getSource() == pickRightChannel) {
            chooseRightChannelPath();
        }

        if (e.getSource() == dumpAllSongs) {
            if (nlxwbPath == null || nlxwbPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Choose a NLXWB before continuing");
                return;
            }

            JFileChooser dumpOutputFolderChooser = new JFileChooser();
            dumpOutputFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dumpOutputFolderChooser.setDialogTitle("Select Dump Output Folder");
            dumpOutputFolderChooser.setAcceptAllFileFilterUsed(false);
            int result = dumpOutputFolderChooser.showOpenDialog(this);

            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File dumpOutputFolder = dumpOutputFolderChooser.getSelectedFile();

            try {
                SongDumper.dumpAllSongs(new File(nlxwbPath), dumpOutputFolder);
            }
            catch (Exception ex) {
                return;
            }

            JOptionPane.showMessageDialog(this, "Songs have been dumped!");
        }

        if (e.getSource() == selectNLXWB) {
            initNLXWBPath();
        }

        if (e.getSource() == replaceSong) {
            if (leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel wasn't chosen!");
                return;
            }

            if (nlxwbPath == null || nlxwbPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "The NLXWB wasn't chosen!");
                return;
            }

            File leftChannelFile = new File(leftChannelPath);
            File rightChannelFile = new File(rightChannelPath);
            File nlxwbFile = new File(nlxwbPath);

            if (!leftChannelFile.exists() || !rightChannelFile.exists()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel doesn't exist!");
                return;
            }

            if (!nlxwbFile.exists()) {
                JOptionPane.showMessageDialog(this, "The NLXWB file doesn't exist!");
                return;
            }

            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to make a backup of the NLXWB file?",
                    "Backup NLXWB",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                backupNLXWB(nlxwbFile);
            }

            String selectedSong = (String) songSelector.getSelectedItem();
            if (selectedSong == null || selectedSong.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a song name before generating.");
                return;
            }

            int songIndex = getSongIndexFromName(selectedSong);

            if (songIndex == -1) {
                return;
            }

            try {
                boolean replacedSuccessfully = SongReplacer.replaceSong(nlxwbFile, leftChannelFile, rightChannelFile, songIndex);

                if (replacedSuccessfully) {
                    JOptionPane.showMessageDialog(null, selectedSong + " replaced successfully!");
                    File idspFile = new File("temp.idsp");
                    idspFile.delete();
                }
            }
            catch (Exception ex) {
                return;
            }
        }
    }
}