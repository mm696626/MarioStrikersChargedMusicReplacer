package ui;

import constants.StrikersChargedConstants;
import helpers.DSPPair;
import helpers.ReplaceJob;
import helpers.Song;
import io.IDSPCreator;
import io.SongDumper;
import io.SongReplacer;
import io.ValidOffsetsChecker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MarioStrikersChargedMusicReplacerUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, dumpAllSongsFromNLXWB, replaceSong, createIDSP, selectNLXWB;
    private String leftChannelPath = "";
    private String rightChannelPath = "";

    private File savedDSPFolder;
    private File savedOutputFolder;
    private File defaultSavedDSPFolder;
    private File defaultOutputFolder;
    private File defaultNLXWB;

    private String nlxwbPath;

    private JLabel leftChannelLabel;
    private JLabel rightChannelLabel;
    private JLabel nlxwbFilePathLabel;
    private JLabel defaultDSPFolderLabel;
    private JLabel defaultOutputFolderLabel;
    private JLabel defaultNLXWBLabel;

    private JComboBox<String> songSelector;

    private DefaultListModel<ReplaceJob> jobQueueModel;
    private JList<ReplaceJob> jobQueueList;
    private JButton addToQueueButton, removeQueueButton, clearQueueButton, runBatchButton;
    private JButton modifyWithRandomSongs;

    private JCheckBox autoAddToQueue = null;
    private JCheckBox deleteDSPAfterModify = null;

    public MarioStrikersChargedMusicReplacerUI() {
        setTitle("Mario Strikers Charged Music Replacer");
        generateUI();
        initSettingsFile();
        loadSettingsFile();
    }

    private void generateUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel musicReplacerPanel = new JPanel();
        musicReplacerPanel.setLayout(new BoxLayout(musicReplacerPanel, BoxLayout.Y_AXIS));

        JPanel songPanel = new JPanel(new GridBagLayout());
        songPanel.setBorder(BorderFactory.createTitledBorder("Song Selection"));
        songPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // important for BoxLayout

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        songPanel.add(new JLabel("Song:"), gbc);

        songSelector = new JComboBox<>();
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
            public void insertUpdate(DocumentEvent e) { filterSongs(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterSongs(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterSongs(); }
        });

        JPanel dumpReplacePanel = new JPanel(new GridBagLayout());
        dumpReplacePanel.setBorder(BorderFactory.createTitledBorder("Dump/Replace Songs"));
        dumpReplacePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints dumpReplaceGBC = new GridBagConstraints();
        dumpReplaceGBC.insets = new Insets(5, 5, 5, 5);
        dumpReplaceGBC.fill = GridBagConstraints.HORIZONTAL;

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);
        leftChannelLabel = new JLabel("No file selected");

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);
        rightChannelLabel = new JLabel("No file selected");

        dumpAllSongsFromNLXWB = new JButton("Dump All Songs from NLXWB");
        dumpAllSongsFromNLXWB.addActionListener(this);

        modifyWithRandomSongs = new JButton("Modify Songs with Random Songs");
        modifyWithRandomSongs.addActionListener(this);

        replaceSong = new JButton("Replace Song");
        replaceSong.addActionListener(this);

        createIDSP = new JButton("Create IDSP");
        createIDSP.addActionListener(this);

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
        dumpReplacePanel.add(replaceSong, dumpReplaceGBC);
        dumpReplaceGBC.gridx = 1;
        dumpReplacePanel.add(modifyWithRandomSongs, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 3;
        dumpReplacePanel.add(dumpAllSongsFromNLXWB, dumpReplaceGBC);
        dumpReplaceGBC.gridx = 1;
        dumpReplacePanel.add(createIDSP, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 4;
        dumpReplaceGBC.gridwidth = 2;
        dumpReplacePanel.add(selectNLXWB, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 5;
        dumpReplaceGBC.gridwidth = 1;
        dumpReplacePanel.add(nlxwbFilePathLabel, dumpReplaceGBC);

        dumpReplaceGBC.gridx = 0; dumpReplaceGBC.gridy = 6;
        dumpReplacePanel.add(autoAddToQueue, dumpReplaceGBC);
        dumpReplaceGBC.gridx = 1;
        dumpReplacePanel.add(deleteDSPAfterModify, dumpReplaceGBC);

        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("Batch Replacement Job Queue"));
        queuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        musicReplacerPanel.add(songPanel);
        musicReplacerPanel.add(Box.createVerticalStrut(10));
        musicReplacerPanel.add(dumpReplacePanel);
        musicReplacerPanel.add(Box.createVerticalStrut(10));
        musicReplacerPanel.add(queuePanel);

        JPanel musicReplacerTabPanel = new JPanel(new BorderLayout());
        musicReplacerTabPanel.add(musicReplacerPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Music Replacer", musicReplacerTabPanel);

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints settingsGBC = new GridBagConstraints();
        settingsGBC.insets = new Insets(5, 5, 5, 5);
        settingsGBC.fill = GridBagConstraints.HORIZONTAL;

        settingsGBC.gridx = 0; settingsGBC.gridy = 0;
        settingsPanel.add(new JLabel("Default DSP Folder:"), settingsGBC);

        defaultDSPFolderLabel = new JLabel(defaultSavedDSPFolder != null ? defaultSavedDSPFolder.getAbsolutePath() : "None");
        settingsGBC.gridx = 1;
        settingsPanel.add(defaultDSPFolderLabel, settingsGBC);

        JButton chooseDefaultDSPButton = new JButton("Change");
        chooseDefaultDSPButton.addActionListener(e -> chooseDefaultDSPFolder());
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultDSPButton, settingsGBC);

        settingsGBC.gridx = 0; settingsGBC.gridy = 1;
        settingsPanel.add(new JLabel("Default Dump Output Folder:"), settingsGBC);

        defaultOutputFolderLabel = new JLabel(defaultOutputFolder != null ? defaultOutputFolder.getAbsolutePath() : "None");
        settingsGBC.gridx = 1;
        settingsPanel.add(defaultOutputFolderLabel, settingsGBC);

        JButton chooseDefaultOutputFolderButton = new JButton("Change");
        chooseDefaultOutputFolderButton.addActionListener(e -> chooseDefaultOutputFolder());
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultOutputFolderButton, settingsGBC);

        settingsGBC.gridx = 0; settingsGBC.gridy = 2;
        settingsPanel.add(new JLabel("Default NLXWB:"), settingsGBC);

        defaultNLXWBLabel = new JLabel(defaultNLXWB != null ? defaultNLXWB.getAbsolutePath() : "None");
        settingsGBC.gridx = 1;
        settingsPanel.add(defaultNLXWBLabel, settingsGBC);

        JButton chooseDefaultNLXWBButton = new JButton("Change");
        chooseDefaultNLXWBButton.addActionListener(e -> chooseDefaultNLXWB());
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultNLXWBButton, settingsGBC);

        JButton resetReplacerButton = new JButton("Reset Replacer");
        resetReplacerButton.addActionListener(e -> resetReplacer());
        settingsGBC.gridx = 0; settingsGBC.gridy = 3;
        settingsGBC.gridwidth = 3;
        settingsPanel.add(resetReplacerButton, settingsGBC);

        tabbedPane.addTab("Settings", settingsPanel);

        add(tabbedPane);
    }

    private void initSettingsFile() {
        File settingsFile = new File("settings.txt");
        PrintWriter outputStream;
        if (!settingsFile.exists()) {
            try {
                outputStream = new PrintWriter(new FileOutputStream(settingsFile));
            }
            catch (FileNotFoundException f) {
                return;
            }

            outputStream.println("defaultSavedDSPFolder:None");
            outputStream.println("defaultOutputFolder:None");
            outputStream.println("defaultNLXWB:None");
            outputStream.close();
        }
    }

    private void loadSettingsFile() {
        File settingsFile = new File("settings.txt");
        try (Scanner inputStream = new Scanner(new FileInputStream(settingsFile))) {
            while (inputStream.hasNextLine()) {
                String line = inputStream.nextLine();
                String[] parts = line.split(":", 2);
                if (parts.length < 2) continue;
                String key = parts[0];
                String value = parts[1];

                if (key.equals("defaultSavedDSPFolder")) {
                    if (!value.equals("None")) defaultSavedDSPFolder = new File(value);
                }
                if (key.equals("defaultOutputFolder")) {
                    if (!value.equals("None")) defaultOutputFolder = new File(value);
                }
                if (key.equals("defaultNLXWB")) {
                    if (!value.equals("None")) defaultNLXWB = new File(value);
                }
            }

            if (defaultSavedDSPFolder != null) {
                defaultDSPFolderLabel.setText(defaultSavedDSPFolder.getAbsolutePath());

                if (defaultSavedDSPFolder.exists()) {
                    savedDSPFolder = defaultSavedDSPFolder;
                }
            }

            if (defaultOutputFolder != null) {
                defaultOutputFolderLabel.setText(defaultOutputFolder.getAbsolutePath());

                if (defaultOutputFolder.exists()) {
                    savedOutputFolder = defaultOutputFolder;
                }
            }

            if (defaultNLXWB != null) {
                defaultNLXWBLabel.setText(defaultNLXWB.getAbsolutePath());

                if (defaultNLXWB.exists()) {
                    nlxwbPath = defaultNLXWB.getAbsolutePath();
                    nlxwbFilePathLabel.setText("Selected NLXWB: " + nlxwbPath);
                    populateSongsForNLXWB(defaultNLXWB.getName());
                }
            }


        } catch (FileNotFoundException e) {
            return;
        }
    }

    private void chooseDefaultDSPFolder() {
        JFileChooser defaultDSPFolderChooser = new JFileChooser();
        defaultDSPFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        defaultDSPFolderChooser.setDialogTitle("Select Default DSP Folder");
        defaultDSPFolderChooser.setAcceptAllFileFilterUsed(false);
        int result = defaultDSPFolderChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            defaultSavedDSPFolder = defaultDSPFolderChooser.getSelectedFile();
            defaultDSPFolderLabel.setText(defaultSavedDSPFolder.getAbsolutePath());
            savedDSPFolder = defaultSavedDSPFolder;
            saveSettingsToFile();
        }
    }

    private void chooseDefaultOutputFolder() {
        JFileChooser defaultOutputFolderChooser = new JFileChooser();
        defaultOutputFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        defaultOutputFolderChooser.setDialogTitle("Select Default Dump Output Folder");
        defaultOutputFolderChooser.setAcceptAllFileFilterUsed(false);
        int result = defaultOutputFolderChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            defaultOutputFolder = defaultOutputFolderChooser.getSelectedFile();
            defaultOutputFolderLabel.setText(defaultOutputFolder.getAbsolutePath());
            savedOutputFolder = defaultOutputFolder;
            saveSettingsToFile();
        }
    }

    private void chooseDefaultNLXWB() {
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
            defaultNLXWB = selectedNLXWB;
            nlxwbPath = selectedNLXWB.getAbsolutePath();
            nlxwbFilePathLabel.setText("Selected NLXWB: " + selectedNLXWB.getAbsolutePath());
            defaultNLXWBLabel.setText(defaultNLXWB.getAbsolutePath());
            populateSongsForNLXWB("STREAM_GEN_Music");
            saveSettingsToFile();

            if (jobQueueModel != null) {
                jobQueueModel.clear();
            }
        }
        else if (selectedNLXWB.getName().equals("FE_GEN_Music.nlxwb") && validOffsets) {
            defaultNLXWB = selectedNLXWB;
            nlxwbPath = selectedNLXWB.getAbsolutePath();
            nlxwbFilePathLabel.setText("Selected NLXWB: " + selectedNLXWB.getAbsolutePath());
            defaultNLXWBLabel.setText(defaultNLXWB.getAbsolutePath());
            populateSongsForNLXWB("FE_GEN_Music");
            saveSettingsToFile();

            if (jobQueueModel != null) {
                jobQueueModel.clear();
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "This isn't the correct NLXWB");
        }
    }

    private void saveSettingsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:" + (defaultSavedDSPFolder != null ? defaultSavedDSPFolder.getAbsolutePath() : "None"));
            writer.println("defaultOutputFolder:" + (defaultOutputFolder != null ? defaultOutputFolder.getAbsolutePath() : "None"));
            writer.println("defaultNLXWB:" + (defaultNLXWB != null ? defaultNLXWB : "None"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + e.getMessage());
        }
    }

    private void resetReplacer() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset the replacer along with the settings?\nThis will reset the replacer as if it was never run before.",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        defaultSavedDSPFolder = null;

        if (defaultDSPFolderLabel != null) {
            defaultDSPFolderLabel.setText("None");
        }

        defaultOutputFolder = null;

        if (defaultOutputFolderLabel != null) {
            defaultOutputFolderLabel.setText("None");
        }

        defaultNLXWB = null;

        if (defaultNLXWBLabel != null) {
            defaultNLXWBLabel.setText("None");
        }

        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:None");
            writer.println("defaultOutputFolder:None");
            writer.println("defaultNLXWB:None");

            JOptionPane.showMessageDialog(this, "Replacer has been reset.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to reset replacer: " + e.getMessage());
        }
    }

    private String[] getSongNameArray() {
        Song[] songArray;
        String[] songNameArray = new String[0];

        if (nlxwbPath == null || nlxwbPath.isEmpty()) {
            return songNameArray;
        }

        if (nlxwbPath.endsWith("STREAM_GEN_Music.nlxwb")) {
            songNameArray = new String[StrikersChargedConstants.STRIKERS_CHARGED_SONGS.length];
            songArray = StrikersChargedConstants.STRIKERS_CHARGED_SONGS;

            for (int i=0; i<songNameArray.length; i++) {
                songNameArray[i] = songArray[i].getSongDisplayName();
            }
        }
        else if (nlxwbPath.endsWith("FE_GEN_Music.nlxwb")) {
            songNameArray = new String[StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONGS.length];
            songArray = StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONGS;

            for (int i=0; i<songNameArray.length; i++) {
                songNameArray[i] = songArray[i].getSongDisplayName();
            }
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
            populateSongsForNLXWB("STREAM_GEN_Music");

            if (jobQueueModel != null) {
                jobQueueModel.clear();
            }
        }
        else if (selectedNLXWB.getName().equals("FE_GEN_Music.nlxwb") && validOffsets) {
            nlxwbPath = selectedNLXWB.getAbsolutePath();
            nlxwbFilePathLabel.setText("Selected NLXWB: " + nlxwbPath);
            populateSongsForNLXWB("FE_GEN_Music");

            if (jobQueueModel != null) {
                jobQueueModel.clear();
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "This isn't the correct NLXWB");
        }
    }

    private void populateSongsForNLXWB(String nlxwbType) {
        ArrayList<String> songs = new ArrayList<>();

        if (nlxwbType.endsWith(".nlxwb")) {
            nlxwbType = nlxwbType.substring(0, nlxwbType.lastIndexOf(".nlxwb"));
        }

        if (nlxwbType.equals("STREAM_GEN_Music")) {
            for (Song song : StrikersChargedConstants.STRIKERS_CHARGED_SONGS) {
                songs.add(song.getSongDisplayName());
            }
        }
        else if (nlxwbType.equals("FE_GEN_Music")) {
            for (Song song : StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONGS) {
                songs.add(song.getSongDisplayName());
            }
        }

        if (songs.isEmpty()) {
            songSelector.setModel(new DefaultComboBoxModel<>(new String[] {"No songs available"}));
        } else {
            Collections.sort(songs, String.CASE_INSENSITIVE_ORDER);
            songSelector.setModel(new DefaultComboBoxModel<>(songs.toArray(new String[0])));
        }
    }


    private int getSongIndexFromName(String selectedSong) {
        if (nlxwbPath == null) {
            return -1;
        }

        boolean isStream = nlxwbPath.endsWith("STREAM_GEN_Music.nlxwb");
        boolean isFrontEnd = nlxwbPath.endsWith("FE_GEN_Music.nlxwb");

        if (isStream) {
            for (Song song : StrikersChargedConstants.STRIKERS_CHARGED_SONGS) {
                if (selectedSong.equals(song.getSongDisplayName())) {
                    return song.getSongIndex();
                }
            }
        }

        if (isFrontEnd) {
            for (Song song : StrikersChargedConstants.STRIKERS_CHARGED_MENU_SONGS) {
                if (selectedSong.equals(song.getSongDisplayName())) {
                    return song.getSongIndex();
                }
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

        if (e.getSource() == dumpAllSongsFromNLXWB) {
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

            File dumpOutputFolder;

            if (savedOutputFolder == null || !savedOutputFolder.exists()) {
                JFileChooser dumpOutputFolderChooser = new JFileChooser();
                dumpOutputFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                dumpOutputFolderChooser.setDialogTitle("Select Dump Output Folder");
                dumpOutputFolderChooser.setAcceptAllFileFilterUsed(false);
                int result = dumpOutputFolderChooser.showOpenDialog(this);

                if (result != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                dumpOutputFolder = dumpOutputFolderChooser.getSelectedFile();
            }
            else {
                dumpOutputFolder = savedOutputFolder;
            }

            try {
                SongDumper.dumpAllSongs(selectedNLXWB, dumpOutputFolder);
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
                JOptionPane.showMessageDialog(this, "Please select a song name before replacing.");
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

        if (e.getSource() == createIDSP) {
            if (leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel wasn't chosen!");
                return;
            }

            File leftChannelFile = new File(leftChannelPath);
            File rightChannelFile = new File(rightChannelPath);

            if (!leftChannelFile.exists() || !rightChannelFile.exists()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel doesn't exist!");
                return;
            }

            File idspFile;

            JFileChooser idspFileSaver = new JFileChooser();
            idspFileSaver.setFileSelectionMode(JFileChooser.FILES_ONLY);
            idspFileSaver.setDialogTitle("Select IDSP Save Location");

            FileNameExtensionFilter idspFilter = new FileNameExtensionFilter("IDSP Files", "idsp");
            idspFileSaver.setFileFilter(idspFilter);
            idspFileSaver.setAcceptAllFileFilterUsed(false);

            int saveLocationSelected = idspFileSaver.showSaveDialog(this);
            if (saveLocationSelected == JFileChooser.APPROVE_OPTION) {
                idspFile = idspFileSaver.getSelectedFile();

                String name = idspFile.getName();
                String baseName;
                String extension = "";

                int dotIndex = name.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < name.length() - 1) {
                    baseName = name.substring(0, dotIndex);
                    extension = name.substring(dotIndex + 1);
                } else {
                    baseName = name;
                }

                baseName = baseName.replaceAll("[^a-zA-Z0-9_ ]", "_");

                if (!extension.equalsIgnoreCase("idsp")) {
                    extension = "idsp";
                }

                String sanitizedFileName = baseName + "." + extension;
                idspFile = new File(idspFile.getParentFile(), sanitizedFileName);
            }
            else {
                return;
            }

            String[] formatOptions = { "Mario Strikers Charged", "Super Mario Strikers" };
            String selectedFormat = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose an IDSP format:",
                    "IDSP Format?",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    formatOptions,
                    formatOptions[0]
            );

            if (selectedFormat != null) {
                boolean strikersChargedFormat = selectedFormat.equals("Mario Strikers Charged");

                try {
                    if (strikersChargedFormat) {
                        int interleave;

                        if (idspFile.exists()) {
                            try (RandomAccessFile idspRaf = new RandomAccessFile(idspFile, "r")) {
                                idspRaf.seek(0x04);
                                interleave = idspRaf.readInt();
                            }

                            idspFile.delete();
                            IDSPCreator.createStrikersChargedIDSPFile(leftChannelFile, rightChannelFile, idspFile, interleave);
                        }
                        else {
                            IDSPCreator.createStrikersChargedIDSPFile(leftChannelFile, rightChannelFile, idspFile, 0x6B40);
                        }
                    } else {
                        int interleave;

                        if (idspFile.exists()) {
                            try (RandomAccessFile idspRaf = new RandomAccessFile(idspFile, "r")) {
                                idspRaf.seek(0x04);
                                interleave = idspRaf.readInt();
                            }

                            idspFile.delete();
                            IDSPCreator.createSuperMarioStrikersIDSPFile(leftChannelFile, rightChannelFile, idspFile, interleave);
                        } else {
                            IDSPCreator.createSuperMarioStrikersIDSPFile(leftChannelFile, rightChannelFile, idspFile, 0x6B40);
                        }
                    }

                    JOptionPane.showMessageDialog(this, "IDSP file has been created!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "The IDSP file couldn't be created!");
                    return;
                }
            }
        }

        if (e.getSource() == modifyWithRandomSongs) {

            if (nlxwbPath == null || nlxwbPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No NLXWB file was chosen!");
                return;
            }

            File nlxwbFile = new File(nlxwbPath);
            if (!nlxwbFile.exists()) {
                JOptionPane.showMessageDialog(this, "The selected NLXWB file doesn't exist!");
                return;
            }

            File dspFolderForRandomization;

            if (savedDSPFolder == null || !savedDSPFolder.exists()) {
                JFileChooser dspFolderChooser = new JFileChooser();
                dspFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                dspFolderChooser.setDialogTitle("Select DSP Folder for Randomization");
                dspFolderChooser.setAcceptAllFileFilterUsed(false);
                int result = dspFolderChooser.showOpenDialog(this);

                if (result != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                dspFolderForRandomization = dspFolderChooser.getSelectedFile();
            }
            else {
                dspFolderForRandomization = savedDSPFolder;
            }

            ArrayList<DSPPair> dspPairs = new ArrayList<>();

            if (dspFolderForRandomization != null) {
                dspPairs = DSPPair.detectDSPPairs(dspFolderForRandomization);
            }

            if (dspPairs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No DSP pairs exist in the DSP folder!");
                return;
            }


            String[] songNames = getSongNameArray();

            if (songNames == null) {
                return;
            }

            Arrays.sort(songNames);

            JPanel mainPanel = new JPanel(new BorderLayout());
            JPanel topPanel = new JPanel(new BorderLayout());

            JTextField searchField = new JTextField();
            searchField.setToolTipText("Search songs...");
            topPanel.add(searchField, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton selectAllButton = new JButton("Select All");
            JButton selectNoneButton = new JButton("Select None");

            buttonPanel.add(selectAllButton);
            buttonPanel.add(selectNoneButton);
            topPanel.add(buttonPanel, BorderLayout.SOUTH);

            JPanel checkboxPanel = new JPanel();
            checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));

            JCheckBox[] songCheckboxes = new JCheckBox[songNames.length];

            for (int i=0; i<songNames.length; i++) {
                songCheckboxes[i] = new JCheckBox(songNames[i]);
                checkboxPanel.add(songCheckboxes[i]);
            }

            JScrollPane scrollPane = new JScrollPane(checkboxPanel);
            scrollPane.setPreferredSize(new Dimension(400, 400));

            mainPanel.add(topPanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            searchField.getDocument().addDocumentListener(new DocumentListener() {
                private void filter() {
                    String query = searchField.getText().trim().toLowerCase();

                    checkboxPanel.removeAll();

                    for (int i = 0; i < songNames.length; i++) {
                        String songName = songNames[i].toLowerCase();
                        JCheckBox cb = songCheckboxes[i];

                        if (query.isEmpty() || songName.contains(query)) {
                            checkboxPanel.add(cb);
                        }
                    }

                    checkboxPanel.revalidate();
                    checkboxPanel.repaint();
                }

                @Override
                public void insertUpdate(DocumentEvent e) { filter(); }

                @Override
                public void removeUpdate(DocumentEvent e) { filter(); }

                @Override
                public void changedUpdate(DocumentEvent e) { filter(); }
            });

            selectAllButton.addActionListener(ev -> {
                for (JCheckBox cb : songCheckboxes) {
                    cb.setSelected(true);
                }
            });

            selectNoneButton.addActionListener(ev -> {
                for (JCheckBox cb : songCheckboxes) {
                    cb.setSelected(false);
                }
            });

            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    mainPanel,
                    "Select Songs to Randomize",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (confirm != JOptionPane.OK_OPTION) {
                return;
            }

            boolean atLeastOneSelected = false;
            for (JCheckBox cb : songCheckboxes) {
                if (cb.isSelected()) {
                    atLeastOneSelected = true;
                    break;
                }
            }
            if (!atLeastOneSelected) {
                JOptionPane.showMessageDialog(this, "You must select at least one song");
                return;
            }

            int minimizeRepeatsResponse = JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to minimize repeat replacement songs?",
                    "Minimize Repeats",
                    JOptionPane.YES_NO_OPTION
            );

            int backupNLXWBresponse = JOptionPane.showConfirmDialog(
                    this,
                    "This will modify the NLXWB for all randomizations.\nDo you want to back up the NLXWB file first?",
                    "Backup?",
                    JOptionPane.YES_NO_OPTION
            );

            if (backupNLXWBresponse == JOptionPane.YES_OPTION) {
                backupNLXWB(nlxwbFile);
            }

            boolean minimizeRepeats;
            minimizeRepeats = minimizeRepeatsResponse == JOptionPane.YES_OPTION;

            Random rng = new Random();
            List<DSPPair> dspPairPool = new ArrayList<>(dspPairs);

            int songCheckboxIndex = 0;
            for (String songName : songNames) {

                JCheckBox songRandomized = songCheckboxes[songCheckboxIndex];
                songCheckboxIndex++;

                if (songRandomized == null || !songRandomized.isSelected()) {
                    continue;
                }

                if (dspPairPool.isEmpty()) {
                    dspPairPool.addAll(dspPairs);
                }

                int randomIndex = rng.nextInt(dspPairPool.size());

                DSPPair chosenSongPair;
                if (minimizeRepeats) {
                    chosenSongPair = dspPairPool.remove(randomIndex);
                }
                else {
                    chosenSongPair = dspPairPool.get(randomIndex);
                }

                int songIndex = getSongIndexFromName(songName);

                if (songIndex == -1) {
                    continue;
                }

                try {
                    boolean replacedSuccessfully = SongReplacer.replaceSong(nlxwbFile, chosenSongPair.getLeft(), chosenSongPair.getRight(), songIndex, deleteDSPAfterModify.isSelected());

                    if (replacedSuccessfully) {
                        File idspFile = new File("temp.idsp");
                        idspFile.delete();
                    }
                }
                catch (Exception ex) {
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Randomization completed.");
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