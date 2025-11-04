package ui;

import constants.StrikersChargedSongNames;
import helpers.DSPPair;
import helpers.GenerateJob;
import helpers.Song;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class MarioStrikersChargedMusicReplacerUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, generateSTM;
    private String leftChannelPath = "";
    private String rightChannelPath = "";

    private File savedDSPFolder;
    private File savedOutputFolder;
    private File defaultSavedDSPFolder;
    private File defaultOutputFolder;

    private JLabel leftChannelLabel;
    private JLabel rightChannelLabel;
    private JLabel defaultDSPFolderLabel;
    private JLabel defaultOutputFolderLabel;

    private JComboBox<String> songSelector;

    private DefaultListModel<GenerateJob> jobQueueModel;
    private JList<GenerateJob> jobQueueList;
    private JButton addToQueueButton, removeQueueButton, clearQueueButton, runBatchButton;

    private JCheckBox autoAddToQueue;
    private JCheckBox deleteDSPAfterGenerate;

    public MarioStrikersChargedMusicReplacerUI() {
        setTitle("Mario Strikers Charged Music Replacer");
        initSettingsFile();
        loadSettingsFile();
        generateUI();
    }

    private void generateUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

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

        JPanel stmPanel = new JPanel(new GridBagLayout());
        stmPanel.setBorder(BorderFactory.createTitledBorder("DumpModify Songs"));
        GridBagConstraints stmGBC = new GridBagConstraints();
        stmGBC.insets = new Insets(5, 5, 5, 5);
        stmGBC.fill = GridBagConstraints.HORIZONTAL;

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);
        leftChannelLabel = new JLabel("No file selected");

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);
        rightChannelLabel = new JLabel("No file selected");

        generateSTM = new JButton("Generate STM");
        generateSTM.addActionListener(this);

        stmGBC.gridx = 0; stmGBC.gridy = 0;
        stmPanel.add(pickLeftChannel, stmGBC);
        stmGBC.gridx = 1;
        stmPanel.add(leftChannelLabel, stmGBC);

        stmGBC.gridx = 0; stmGBC.gridy = 1;
        stmPanel.add(pickRightChannel, stmGBC);
        stmGBC.gridx = 1;
        stmPanel.add(rightChannelLabel, stmGBC);

        stmGBC.gridx = 0; stmGBC.gridy = 2;
        stmPanel.add(generateSTM, stmGBC);

        autoAddToQueue = new JCheckBox("Automatically Add DSP Pairs from DSP Folder to Queue");
        stmGBC.gridx = 0; stmGBC.gridy = 3;
        stmPanel.add(autoAddToQueue, stmGBC);

        deleteDSPAfterGenerate = new JCheckBox("Delete Source DSPs after Generation");
        stmGBC.gridx = 1;
        stmPanel.add(deleteDSPAfterGenerate, stmGBC);

        musicReplacerPanel.add(songPanel);
        musicReplacerPanel.add(Box.createVerticalStrut(10));
        musicReplacerPanel.add(stmPanel);

        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("Batch Generation Job Queue"));

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

        musicReplacerPanel.add(Box.createVerticalStrut(10));
        musicReplacerPanel.add(queuePanel);

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints settingsGBC = new GridBagConstraints();
        settingsGBC.insets = new Insets(5, 5, 5, 5);
        settingsGBC.fill = GridBagConstraints.HORIZONTAL;

        settingsGBC.gridx = 0;
        settingsGBC.gridy = 0;
        settingsPanel.add(new JLabel("Default DSP Folder:"), settingsGBC);

        defaultDSPFolderLabel = new JLabel(defaultSavedDSPFolder != null ? defaultSavedDSPFolder.getAbsolutePath() : "None");
        settingsGBC.gridx = 1;
        settingsPanel.add(defaultDSPFolderLabel, settingsGBC);

        JButton chooseDefaultDSPButton = new JButton("Change");
        chooseDefaultDSPButton.addActionListener(e -> chooseDefaultDSPFolder());
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultDSPButton, settingsGBC);

        settingsGBC.gridx = 0;
        settingsGBC.gridy = 1;
        settingsPanel.add(new JLabel("Default Output Folder:"), settingsGBC);

        defaultOutputFolderLabel = new JLabel(defaultOutputFolder != null ? defaultOutputFolder.getAbsolutePath() : "None");
        settingsGBC.gridx = 1;
        settingsPanel.add(defaultOutputFolderLabel, settingsGBC);

        JButton chooseDefaultOutputFolderButton = new JButton("Change");
        chooseDefaultOutputFolderButton.addActionListener(e -> chooseDefaultOutputFolder());
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultOutputFolderButton, settingsGBC);

        JButton resetReplacerButton = new JButton("Reset Generator");
        resetReplacerButton.addActionListener(e -> resetGenerator());
        settingsGBC.gridx = 0;
        settingsGBC.gridy = 3;
        settingsGBC.gridwidth = 3;
        settingsPanel.add(resetReplacerButton, settingsGBC);

        tabbedPane.addTab("Music Replacer", musicReplacerPanel);
        tabbedPane.addTab("Settings", settingsPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
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
            }

            if (defaultSavedDSPFolder != null && defaultSavedDSPFolder.exists()) {
                savedDSPFolder = defaultSavedDSPFolder;
            }

            if (defaultOutputFolder != null && defaultOutputFolder.exists()) {
                savedOutputFolder = defaultOutputFolder;
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
        defaultOutputFolderChooser.setDialogTitle("Select Default Output Folder");
        defaultOutputFolderChooser.setAcceptAllFileFilterUsed(false);
        int result = defaultOutputFolderChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            defaultOutputFolder = defaultOutputFolderChooser.getSelectedFile();
            defaultOutputFolderLabel.setText(defaultOutputFolder.getAbsolutePath());
            savedOutputFolder = defaultOutputFolder;
            saveSettingsToFile();
        }
    }

    private void saveSettingsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:" + (defaultSavedDSPFolder != null ? defaultSavedDSPFolder.getAbsolutePath() : "None"));
            writer.println("defaultOutputFolder:" + (defaultOutputFolder != null ? defaultOutputFolder.getAbsolutePath() : "None"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + e.getMessage());
        }
    }

    private void resetGenerator() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset the generator along with the settings?\nThis will reset the generator as if it was never run before.",
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

        File songReplacementsFolder = new File("song_replacements");

        if (songReplacementsFolder.exists()) {
            deleteFolder(songReplacementsFolder);
        }

        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:None");
            writer.println("defaultOutputFolder:None");
            writer.println("defaultGame:None");

            JOptionPane.showMessageDialog(this, "Generator has been reset.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to reset generator: " + e.getMessage());
        }
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private String[] initializeSongArray() {

        String[] initialSongArray = new String[StrikersChargedSongNames.STRIKERS_CHARGED_SONGS.length];

        for (int i=0; i<initialSongArray.length; i++) {
            initialSongArray[i] = StrikersChargedSongNames.STRIKERS_CHARGED_SONGS[i].getSongDisplayName();
        }

        return initialSongArray;
    }

    private String[] getSongNameArray() {
        Song[] songArray;
        String[] songNameArray;

        songNameArray = new String[StrikersChargedSongNames.STRIKERS_CHARGED_SONGS.length];
        songArray = StrikersChargedSongNames.STRIKERS_CHARGED_SONGS;

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

    private void addToQueue() {
        String songFileName = (String) songSelector.getSelectedItem();
        if (songFileName == null || leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select song and both DSP channels before adding.");
            return;
        }

        jobQueueModel.addElement(new GenerateJob(songFileName, leftChannelPath, rightChannelPath));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pickLeftChannel) {
            chooseLeftChannelPath();
        }

        if (e.getSource() == pickRightChannel) {
            chooseRightChannelPath();
        }

        if (e.getSource() == generateSTM) {
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

            String selectedSong = (String) songSelector.getSelectedItem();
            if (selectedSong == null || selectedSong.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a song name before generating.");
                return;
            }


            File outputDir;

            if (savedOutputFolder != null && savedOutputFolder.exists()) {
                outputDir = savedOutputFolder;
            }
            else {
                JFileChooser folderChooser = new JFileChooser();
                folderChooser.setDialogTitle("Select Output Folder");
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                folderChooser.setAcceptAllFileFilterUsed(false);

                int userSelection = folderChooser.showOpenDialog(this);
                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                outputDir = folderChooser.getSelectedFile();
            }

            //boolean generatedSuccessfully = STMGenerator.generateSTM(leftChannelFile, rightChannelFile, outputSTMFile, selectedSong, selectedGame, deleteDSPAfterGenerate.isSelected());

//            if (generatedSuccessfully) {
//                JOptionPane.showMessageDialog(null, "STM file generated successfully!");
//            }
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

            File outputDir;

            if (savedOutputFolder != null && savedOutputFolder.exists()) {
                outputDir = savedOutputFolder;
            }
            else {
                JFileChooser folderChooser = new JFileChooser();
                folderChooser.setDialogTitle("Select Output Folder");
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                folderChooser.setAcceptAllFileFilterUsed(false);

                int userSelection = folderChooser.showOpenDialog(this);
                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                outputDir = folderChooser.getSelectedFile();
            }

            for (int i = 0; i < jobQueueModel.size(); i++) {
                GenerateJob generateJob = jobQueueModel.getElementAt(i);


                File leftDSP = new File(generateJob.getLeftDSP());
                File rightDSP = new File(generateJob.getRightDSP());

                if (!leftDSP.exists() || !rightDSP.exists()) {
                    JOptionPane.showMessageDialog(this, "DSP files for " + generateJob.getSongFileName() + " not found. Skipping.");
                    continue;
                }

                //String selectedSong = generateJob.getSongFileName();
                //String outputSTMFileName = SongFileNameHelper.getFileNameFromSong(originalSelectedGame, selectedSong);

//                if (outputSTMFileName == null) {
//                    return;
//                }

                //File outputSTMFile = new File(outputDir, outputSTMFileName);

                //boolean generatedSuccessfully = STMGenerator.generateSTM(leftDSP, rightDSP, outputSTMFile, selectedSong, selectedGame, deleteDSPAfterGenerate.isSelected());

//                if (!generatedSuccessfully) {
//                    JOptionPane.showMessageDialog(null, "Something went wrong with the job for " + generateJob.getSongFileName());
//                }
            }

            JOptionPane.showMessageDialog(this, "Batch process completed.");
            jobQueueModel.clear();
        }
    }
}