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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class TimerGUI extends JDialog  {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private final Timer timer;

    private long resetValue = 0;

    private long currentValue = 0;

    private JLabel timerLabel;

    private TimerPanel timerPanel;

    private Consumer<String> timerUpdateCallback;

    public TimerGUI() {
        super(null, "TimerWindow", ModalityType.MODELESS);
        this.timer = new Timer(1000, this::updateTimer);
    }

    private void updateTimer(ActionEvent actionEvent) {
        if (this.currentValue > 0) {
            this.currentValue--;

            this.updateTimerText();
        }
    }

    public void initialize(Settings settings) {
        this.setUndecorated(true);
        this.setResizable(false);
        this.setBackground(new Color(0, 0, 0, 0));

        // Set up the content of the frame
        this.timerPanel = new TimerPanel(settings);
        this.timerPanel.setOpaque(true);
        this.timerPanel.setLayout(new GridBagLayout());

        this.timerLabel = new JLabel();
        this.updateTimerText();

        var mouseAdapter = new MouseAdapter() {

            final Point point = new Point();

            @Override
            public void mousePressed(MouseEvent e) {
                point.x = e.getX();
                point.y = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = TimerGUI.this.getLocation();
                TimerGUI.this.setLocation(p.x + e.getX() - point.x, p.y + e.getY() - point.y);
            }
        };

        // Add custom dragging functionality
        //this.addMouseListener(mouseAdapter);
        //this.addMouseMotionListener(mouseAdapter);
        this.setContentPane(this.timerPanel);
        this.updateAppearance(settings);
    }

    public void updateAppearance(Settings settings) {
        this.timerPanel.remove(this.timerLabel);

        this.timerLabel.setForeground(settings.textColor);
        this.timerLabel.setFont(settings.font.deriveFont(settings.font.getSize() * 4.0f));

        var gbc = new GridBagConstraints();
        gbc.anchor = settings.alignment.getGbcAlignment();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1d; // fixes the problem
        gbc.weighty = 1d; // fixes the problem
        gbc.insets = new Insets(settings.marginY, settings.marginX, settings.marginY, settings.marginX);

        this.timerPanel.add(this.timerLabel, gbc);
        this.timerPanel.updateSettings(settings);

        this.timerLabel.setText("");
        this.updateTimerText();

        this.setLocation(settings.bounds.x, settings.bounds.y);
        this.setSize(settings.bounds.width, settings.bounds.height);
    }

    public void setSize(Rectangle bounds) {
        this.setLocation(bounds.x, bounds.y);
        this.setSize(bounds.width, bounds.height);
        this.repaint();
    }

    private void updateTimerText() {
        var hours = this.currentValue / 3600;
        var minutes = (this.currentValue % 3600) / 60;
        var seconds = this.currentValue % 60;

        var text = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        this.timerLabel.setText(text);
        if (this.timerUpdateCallback != null)
            this.timerUpdateCallback.accept(text);
    }

    public void start() {
        this.timer.start();
    }

    public void stop() {
        this.timer.stop();
    }

    public void reset() {
        this.currentValue = this.resetValue;
        this.updateTimerText();

        if (this.timer.isRunning()) {
            this.timer.restart();
        }
    }

    public void setCountdown(Duration duration) {
        this.resetValue = duration.getSeconds();
        this.currentValue = duration.getSeconds();

        this.updateTimerText();

        if (this.timer.isRunning()) {
            this.timer.restart();
        }
    }

    public void setCountdownToTime(LocalDateTime target) {
        var now = LocalDateTime.now();
        if (target.isAfter(now)) {
            this.setCountdown(Duration.between(now, target));
        }
    }


    public void setTimerUpdateCallback(Consumer<String> timerUpdateCallback) {
        this.timerUpdateCallback = timerUpdateCallback;
    }
}
