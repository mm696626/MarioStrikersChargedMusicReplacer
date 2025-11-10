package ui;

import constants.StrikersChargedConstants;
import helpers.DSPPair;
import helpers.ReplaceJob;
import helpers.Song;
import io.SongDumper;
import io.SongReplacer;
import io.ValidOffsetsChecker;

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

    private JButton pickLeftChannel, pickRightChannel, dumpSong, dumpAllSongs, replaceSong, selectNLXWB;
    private String leftChannelPath = "";
    private String rightChannelPath = "";

    private File savedDSPFolder;

    private String nlxwbPath;

    private JLabel leftChannelLabel;
    private JLabel rightChannelLabel;
    private JLabel nlxwbFilePathLabel;

    private JComboBox<String> songSelector;

    private DefaultListModel<ReplaceJob> jobQueueModel;
    private JList<ReplaceJob> jobQueueList;
    private JButton addToQueueButton, removeQueueButton, clearQueueButton, runBatchButton;

    private JCheckBox autoAddToQueue = null;
    private JCheckBox deleteDSPAfterModify = null;

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

        JPanel dumpReplacePanel = new JPanel(new GridBagLayout());
        dumpReplacePanel.setBorder(BorderFactory.createTitledBorder("Dump/Replace Songs"));
        GridBagConstraints dumpReplaceGBC = new GridBagConstraints();
        dumpReplaceGBC.insets = new Insets(5, 5, 5, 5);
        dumpReplaceGBC.fill = GridBagConstraints.HORIZONTAL;

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);
        leftChannelLabel = new JLabel("No file selected");

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);
        rightChannelLabel = new JLabel("No file selected");

        dumpSong = new JButton("Dump Selected Song");
        dumpSong.addActionListener(this);

        dumpAllSongs = new JButton("Dump All Songs");
        dumpAllSongs.addActionListener(this);

        replaceSong = new JButton("Replace Song");
        replaceSong.addActionListener(this);

        selectNLXWB = new JButton("Select NLXWB");
        selectNLXWB.addActionListener(this);

        nlxwbFilePathLabel = new JLabel("No NLXWB file selected");

        autoAddToQueue = new JCheckBox("Automatically Add DSP Pairs from DSP Folder to Queue");
        deleteDSPAfterModify = new JCheckBox("Delete Source DSPs after Modify");

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 0;
        dumpReplacePanel.add(pickLeftChannel, dumpReplaceGBC);
        dumpReplaceGBC.gridx = 1;
        dumpReplacePanel.add(leftChannelLabel, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 1;
        dumpReplacePanel.add(pickRightChannel, dumpReplaceGBC);
        dumpReplaceGBC.gridx = 1;
        dumpReplacePanel.add(rightChannelLabel, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 2;
        dumpReplacePanel.add(dumpSong, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 1; dumpReplaceGBC.gridy = 2;
        dumpReplacePanel.add(dumpAllSongs, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 3;
        dumpReplaceGBC.gridwidth = 2;
        dumpReplacePanel.add(replaceSong, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 4;
        dumpReplacePanel.add(selectNLXWB, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 5;
        dumpReplaceGBC.gridwidth = 1;
        dumpReplacePanel.add(nlxwbFilePathLabel, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 6;
        dumpReplacePanel.add(autoAddToQueue, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 1;
        dumpReplacePanel.add(deleteDSPAfterModify, dumpReplaceGBC);

        musicReplacerPanel.add(songPanel);
        musicReplacerPanel.add(Box.createVerticalStrut(10));
        musicReplacerPanel.add(dumpReplacePanel);

        setLayout(new BorderLayout());
        add(musicReplacerPanel, BorderLayout.CENTER);

        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("Batch Replacement Job Queue"));

        jobQueueModel = new DefaultListModel<>();
        jobQueueList = new JList<>(jobQueueModel);
        jobQueueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(jobQueueList);

        JPanel queueButtonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        addToQueueButton = new JButton("Add");
        removeQueueButton = new JButton("Remove");
        clearQueueButton = new JButton("Clear All");
        runBatchButton = new JButton("Run Batch");

        addToQueueButton.addActionListener(this);
        removeQueueButton.addActionListener(this);
        clearQueueButton.addActionListener(this);
        runBatchButton.addActionListener(this);

        queueButtonPanel.add(addToQueueButton);
        queueButtonPanel.add(removeQueueButton);
        queueButtonPanel.add(clearQueueButton);
        queueButtonPanel.add(runBatchButton);

        queuePanel.add(scrollPane, BorderLayout.CENTER);
        queuePanel.add(queueButtonPanel, BorderLayout.SOUTH);

        add(queuePanel, BorderLayout.SOUTH);
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

            if (autoAddToQueue.isSelected()) {
                addToQueue();
            }
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

    private void addToQueue() {
        String songName = (String) songSelector.getSelectedItem();
        if (songName == null || leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select song and both DSP channels before adding.");
            return;
        }

        for (int i = 0; i < jobQueueModel.getSize(); i++) {
            ReplaceJob job = jobQueueModel.getElementAt(i);
            if (job.getSongName().equals(songName)) {
                JOptionPane.showMessageDialog(this, "You already have a job in the queue for this song!");
                return;
            }
        }

        jobQueueModel.addElement(new ReplaceJob(songName, leftChannelPath, rightChannelPath));
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
        boolean validOffsets;

        try {
            validOffsets = ValidOffsetsChecker.checkValidOffsets(selectedNLXWB);
        }
        catch (Exception e) {
            return;
        }

        if (selectedNLXWB.getName().equals("STREAM_GEN_Music.nlxwb") && validOffsets) {
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

    private static File getNLXWBFileName(File nlxwbFile, String timestamp) {
        String baseName = nlxwbFile.getName();
        int extIndex = baseName.lastIndexOf(".");
        if (extIndex != -1) {
            baseName = baseName.substring(0, extIndex);
        }

        String backupFileName = baseName + "_Backup_" + timestamp + ".nlxwb";
        return new File(nlxwbFile.getParent(), backupFileName);
    }

    private void backupNLXWB(File nlxwbFile) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupFile = getNLXWBFileName(nlxwbFile, timestamp);
            Files.copy(nlxwbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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

        if (e.getSource() == dumpSong) {
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

            String selectedSong = (String) songSelector.getSelectedItem();
            if (selectedSong == null || selectedSong.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a song name before generating.");
                return;
            }

            int songIndex = getSongIndexFromName(selectedSong);

            try {
                SongDumper.dumpSong(new File(nlxwbPath), dumpOutputFolder, songIndex);
            }
            catch (Exception ex) {
                return;
            }

            JOptionPane.showMessageDialog(this, "Songs have been dumped!");
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
                boolean replacedSuccessfully = SongReplacer.replaceSong(nlxwbFile, leftChannelFile, rightChannelFile, songIndex, deleteDSPAfterModify.isSelected());

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

        if (e.getSource() == addToQueueButton) {
            addToQueue();
        }

        if (e.getSource() == removeQueueButton) {
            int selectedIndex = jobQueueList.getSelectedIndex();
            if (selectedIndex != -1) {
                jobQueueModel.remove(selectedIndex);
            }
        }

        if (e.getSource() == clearQueueButton) {
            jobQueueModel.clear();
        }

        if (e.getSource() == runBatchButton) {
            if (jobQueueModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Queue is empty!");
                return;
            }

            if (nlxwbPath == null || nlxwbPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No NLXWB file was chosen!");
                return;
            }

            File nlxwbFile = new File(nlxwbPath);
            if (!nlxwbFile.exists()) {
                JOptionPane.showMessageDialog(this, "The selected NLXWB file doesn't exist!");
                return;
            }

            int response = JOptionPane.showConfirmDialog(
                    this,
                    "This will modify the NLXWB for all jobs in the queue.\nDo you want to back up the NLXWB file first?",
                    "Backup?",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                backupNLXWB(nlxwbFile);
            }

            for (int i = 0; i < jobQueueModel.size(); i++) {
                ReplaceJob replaceJob = jobQueueModel.getElementAt(i);

                int songIndex = getSongIndexFromName(replaceJob.getSongName());

                if (songIndex == -1) {
                    continue;
                }

                File leftDSP = new File(replaceJob.getLeftDSP());
                File rightDSP = new File(replaceJob.getRightDSP());

                if (!leftDSP.exists() || !rightDSP.exists()) {
                    JOptionPane.showMessageDialog(this, "DSP files for " + replaceJob.getSongName() + " not found. Skipping.");
                    continue;
                }

                try {
                    boolean replacedSuccessfully = SongReplacer.replaceSong(nlxwbFile, leftDSP, rightDSP, songIndex, deleteDSPAfterModify.isSelected());

                    if (replacedSuccessfully) {
                        File idspFile = new File("temp.idsp");
                        idspFile.delete();
                    }
                }
                catch (Exception ex) {
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Batch process completed.");
            jobQueueModel.clear();
        }
    }
}