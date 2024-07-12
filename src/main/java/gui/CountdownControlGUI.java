/*
 *  Copyright (C) 2024 - Joel Steffens
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gui;

import data.Settings;
import org.drjekyll.fontchooser.FontDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.prefs.Preferences;

public class CountdownControlGUI extends JFrame {
    private JTabbedPane mainTabbedPane;
    private JPanel contentPane;
    private JButton startBtn;
    private JButton stopBtn;
    private JButton restBtn;
    private JFormattedTextField presetTimeField;
    private JButton presetTimerBtn;
    private JFormattedTextField clockTimeField;
    private JButton startCountdownButton;
    private JLabel fontPreviewLabel;
    private JComboBox fontSizeSelector;
    private JTextField imageFilePath;
    private JButton chooseImageBtn;
    private JLabel textColorLabel;
    private JButton chooseTextColorBtn;
    private JLabel bgColorLabel;
    private JButton chooseBgColorBtn;
    private JComboBox bgModeSelector;
    private JFormattedTextField verticalPadField;
    private JFormattedTextField horizontalPadField;
    private JComboBox alignmentSelector;
    private JLabel timerLabel;
    private JButton chooseFontBtn;

    private final TimerGUI timerGUI;

    private final Preferences preferences;

    private Settings settings;

    public CountdownControlGUI(TimerGUI gui, Preferences preferences) {
        super();

        this.timerGUI = gui;
        this.preferences = preferences;
        this.timerGUI.setTimerUpdateCallback((text) -> {
            this.timerLabel.setText(text);
        });
    }

    private MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
            formatter.setPlaceholderCharacter('0');
        } catch (ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
            System.exit(-1);
        }
        return formatter;
    }

    public void init(Settings settings) {
        this.settings = settings;
        setTitle("Countdown");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(contentPane);

        setMinimumSize(new Dimension(500, 460));
        pack();
        setLocationRelativeTo(null);

        // Define additional settings
        this.presetTimeField.setText("00:05:00");
        this.clockTimeField.setText("10:00 h");

        this.presetTimeField.setFormatterFactory(new DefaultFormatterFactory(this.createFormatter("##:##:##")));
        this.clockTimeField.setFormatterFactory(new DefaultFormatterFactory(this.createFormatter("##:## h")));

        this.presetTimerBtn.addActionListener(this::timerEvent);
        this.startCountdownButton.addActionListener(this::timerEvent);

        NumberFormat integerFormat = NumberFormat.getIntegerInstance();
        integerFormat.setGroupingUsed(false);

        NumberFormatter numberFormatter = new NumberFormatter(integerFormat);
        numberFormatter.setAllowsInvalid(false);
        numberFormatter.setMinimum(0);
        numberFormatter.setMaximum(10000);

        this.horizontalPadField.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
        this.horizontalPadField.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));

        // Populate content
        for (Settings.BackgroundMode mode : Settings.BackgroundMode.values()) {
            this.bgModeSelector.addItem(mode);
        }


        for (Settings.Alignment alignment : Settings.Alignment.values()) {
            this.alignmentSelector.addItem(alignment);
        }

        // Add ActionListeners
        this.startBtn.addActionListener(this::timerEvent);
        this.stopBtn.addActionListener(this::timerEvent);
        this.restBtn.addActionListener(this::timerEvent);

        this.bgModeSelector.addItemListener(this::selectionChanged);
        this.alignmentSelector.addItemListener(this::selectionChanged);

        this.horizontalPadField.addPropertyChangeListener("value", this::insetsChanged);
        this.verticalPadField.addPropertyChangeListener("value", this::insetsChanged);

        this.chooseFontBtn.addActionListener(this::chooseEvent);
        this.chooseTextColorBtn.addActionListener(this::chooseEvent);
        this.chooseBgColorBtn.addActionListener(this::chooseEvent);
        this.chooseImageBtn.addActionListener(this::chooseEvent);

        // update UI
        this.bgModeSelector.setSelectedItem(settings.mode);
        this.alignmentSelector.setSelectedItem(settings.alignment);

        this.horizontalPadField.setValue(settings.marginX);
        this.verticalPadField.setValue(settings.marginY);

        this.imageFilePath.setText(settings.imagePath);
        this.fontPreviewLabel.setFont(settings.font.deriveFont(20.0f));

        this.textColorLabel.setBackground(settings.textColor);
        this.bgColorLabel.setBackground(settings.backgroundColor);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                settings.saveTo(preferences);
            }
        });
    }

    private void updateAppearance() {
        this.timerGUI.updateAppearance(this.settings);
    }

    private void selectionChanged(ItemEvent itemEvent) {
        if (itemEvent.getSource() == this.alignmentSelector) {
            settings.alignment = (Settings.Alignment) itemEvent.getItem();
            this.updateAppearance();
        } else if (itemEvent.getSource() == this.bgModeSelector) {
            settings.mode = (Settings.BackgroundMode) itemEvent.getItem();
            this.updateAppearance();
        }
    }


    private void insetsChanged(PropertyChangeEvent event) {
        if (event.getNewValue() != null) {
            if (event.getSource() == this.horizontalPadField) {
                this.settings.marginX = (int) event.getNewValue();
                this.updateAppearance();
            } else if (event.getSource() == this.verticalPadField) {
                this.settings.marginY = (int) event.getNewValue();
                this.updateAppearance();
            }
        }
    }

    private void chooseEvent(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "chooseTextColor" -> {
                var newColor = JColorChooser.showDialog(this, "Choose Text color", settings.textColor, true);
                if (newColor != null) {
                    settings.textColor = newColor;
                    this.textColorLabel.setBackground(settings.textColor);
                    this.updateAppearance();
                }
            }
            case "chooseBgColor" -> {
                var newColor = JColorChooser.showDialog(this, "Choose Background color", settings.backgroundColor, true);
                if (newColor != null) {
                    settings.backgroundColor = newColor;
                    this.bgColorLabel.setBackground(settings.backgroundColor);
                    this.updateAppearance();
                }
            }
            case "chooseBgImage" -> {
                var fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileFilter(new ImageFilter());
                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        settings.backgroundImage = ImageIO.read(fileChooser.getSelectedFile());
                        settings.imagePath = fileChooser.getSelectedFile().getAbsolutePath();
                        this.imageFilePath.setText(settings.imagePath);
                        this.updateAppearance();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error on loading file " +
                                fileChooser.getSelectedFile().getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
            case "chooseTextFont" -> {
                var fontChooser = new FontDialog(this, "Choose timer font", Dialog.ModalityType.APPLICATION_MODAL);
                fontChooser.setSelectedFont(settings.font);
                fontChooser.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                fontChooser.setLocationRelativeTo(this);
                fontChooser.setVisible(true);

                if (!fontChooser.isCancelSelected()) {
                    settings.font = fontChooser.getSelectedFont();
                    this.fontPreviewLabel.setFont(settings.font.deriveFont(20.0f));
                    this.updateAppearance();
                }
            }
        }
    }

    private void timerEvent(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "startTimer" -> {
                this.timerGUI.start();

                this.startBtn.setEnabled(false);
                this.stopBtn.setEnabled(true);
            }
            case "stopTimer" -> {
                this.timerGUI.stop();

                this.startBtn.setEnabled(true);
                this.stopBtn.setEnabled(false);
            }
            case "resetTimer" -> {
                this.timerGUI.reset();
            }
            case "setTimer" -> {
                var text = this.presetTimeField.getText();
                var splits = text.split(":");

                Duration duration = Duration.ZERO;
                duration = duration.plusHours(Integer.parseInt(splits[0]));
                duration = duration.plusMinutes(Integer.parseInt(splits[1]));
                duration = duration.plusSeconds(Integer.parseInt(splits[2]));

                this.timerGUI.setCountdown(duration);
            }
            case "startCountdown" -> {
                try {
                    var daySeconds = Utils.CLOCK_TIME_FORMATTER.parse(this.clockTimeField.getText()).get(ChronoField.SECOND_OF_DAY);

                    var localTime = LocalDate.now().atStartOfDay().plusSeconds(daySeconds);
                    if (localTime.isBefore(LocalDateTime.now())) {
                        localTime = localTime.plusDays(1);
                    }

                    this.timerGUI.setCountdownToTime(localTime);
                    this.timerGUI.start();

                    this.startBtn.setEnabled(false);
                    this.stopBtn.setEnabled(true);
                } catch (DateTimeException ex) {
                    JOptionPane.showMessageDialog(this, "Ungültiger Wert", "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
            // Main page
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        mainTabbedPane = new JTabbedPane();
        contentPane.add(mainTabbedPane, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        mainTabbedPane.addTab("Countdown", panel1);
        final JLabel label1 = new JLabel();
        label1.setText("Set Static Time");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(label1, gbc);
        presetTimeField = new JFormattedTextField();
        presetTimeField.setText("00:00:00");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(presetTimeField, gbc);
        presetTimerBtn = new JButton();
        presetTimerBtn.setActionCommand("setTimer");
        presetTimerBtn.setText("Preset Timer");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(presetTimerBtn, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Countdown to");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(label2, gbc);
        clockTimeField = new JFormattedTextField();
        clockTimeField.setText("12:00 h");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(clockTimeField, gbc);
        startCountdownButton = new JButton();
        startCountdownButton.setActionCommand("startCountdown");
        startCountdownButton.setText("Start Countdown");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(startCountdownButton, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        mainTabbedPane.addTab("Appearance", panel2);
        final JLabel label3 = new JLabel();
        label3.setText("Font Preview");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label3, gbc);
        fontPreviewLabel = new JLabel();
        fontPreviewLabel.setText("00:00:00");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(fontPreviewLabel, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Background Image");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label4, gbc);
        imageFilePath = new JTextField();
        imageFilePath.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(imageFilePath, gbc);
        chooseImageBtn = new JButton();
        chooseImageBtn.setActionCommand("chooseBgImage");
        chooseImageBtn.setText("Choose ...");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(chooseImageBtn, gbc);
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(separator1, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Text Color");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label5, gbc);
        textColorLabel = new JLabel();
        textColorLabel.setBackground(new Color(-1));
        textColorLabel.setMinimumSize(new Dimension(40, 25));
        textColorLabel.setOpaque(true);
        textColorLabel.setPreferredSize(new Dimension(40, 25));
        textColorLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(textColorLabel, gbc);
        chooseTextColorBtn = new JButton();
        chooseTextColorBtn.setActionCommand("chooseTextColor");
        chooseTextColorBtn.setText("Choose ...");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(chooseTextColorBtn, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Background Color");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label6, gbc);
        bgColorLabel = new JLabel();
        bgColorLabel.setBackground(new Color(-1));
        bgColorLabel.setMinimumSize(new Dimension(40, 25));
        bgColorLabel.setOpaque(true);
        bgColorLabel.setPreferredSize(new Dimension(40, 25));
        bgColorLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(bgColorLabel, gbc);
        chooseBgColorBtn = new JButton();
        chooseBgColorBtn.setActionCommand("chooseBgColor");
        chooseBgColorBtn.setText("Choose ...");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(chooseBgColorBtn, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Background Mode");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label7, gbc);
        bgModeSelector = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(bgModeSelector, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Text Alignment");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label8, gbc);
        final JLabel label9 = new JLabel();
        label9.setText("Vertical Padding");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label9, gbc);
        final JLabel label10 = new JLabel();
        label10.setText("Horizontal Padding");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label10, gbc);
        verticalPadField = new JFormattedTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(verticalPadField, gbc);
        horizontalPadField = new JFormattedTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(horizontalPadField, gbc);
        final JLabel label11 = new JLabel();
        label11.setText("px");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label11, gbc);
        final JLabel label12 = new JLabel();
        label12.setText("px");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(label12, gbc);
        alignmentSelector = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(alignmentSelector, gbc);
        chooseFontBtn = new JButton();
        chooseFontBtn.setActionCommand("chooseTextFont");
        chooseFontBtn.setText("Choose ...");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(chooseFontBtn, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        contentPane.add(panel3, BorderLayout.SOUTH);
        timerLabel = new JLabel();
        Font timerLabelFont = this.$$$getFont$$$(null, -1, 18, timerLabel.getFont());
        if (timerLabelFont != null) timerLabel.setFont(timerLabelFont);
        timerLabel.setText("00:00:00");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(timerLabel, gbc);
        stopBtn = new JButton();
        stopBtn.setActionCommand("stopTimer");
        stopBtn.setText("Stop");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(stopBtn, gbc);
        restBtn = new JButton();
        restBtn.setActionCommand("resetTimer");
        restBtn.setText("Reset");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(restBtn, gbc);
        startBtn = new JButton();
        startBtn.setActionCommand("startTimer");
        startBtn.setText("Start");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(startBtn, gbc);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }


}
