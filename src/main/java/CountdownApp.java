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
import data.Settings;
import gui.CountdownControlGUI;
import gui.TimerGUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.prefs.Preferences;

public class CountdownApp {

    public static void main(String[] args) throws IOException {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Application cannot be run in headless mode.");
            System.exit(-1);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final var env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final var preferences = Preferences.userRoot().node(CountdownApp.class.getName());

        int length = env.getScreenDevices().length;
        var screen = env.getScreenDevices()[length - 1];
        var screenConfig = screen.getDefaultConfiguration();

        var settings = new Settings();
        try {
            settings.loadFrom(preferences, screenConfig.getBounds());
        } catch (Exception e) {
            settings.loadDefaults(screenConfig.getBounds());

            JOptionPane.showMessageDialog(null, "Error occurred on loading preferences. " +
                    "Falling back to default\n\n  Message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            var timerGUI = new TimerGUI();
            timerGUI.initialize(settings);

            var controlGUI = new CountdownControlGUI(timerGUI, preferences);
            controlGUI.init(settings);

            timerGUI.setVisible(true);
            if (settings.fullscreen) {
                screen.setFullScreenWindow(timerGUI);
            }

            timerGUI.setCountdown(Duration.of(5, ChronoUnit.MINUTES));
            controlGUI.setVisible(true);
        });
    }
}