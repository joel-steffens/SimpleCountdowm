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

public class TimerPanel extends JPanel {
    private Settings settings;

    // Constructor to set the background image
    public TimerPanel(Settings settings) {
        this.settings = settings;
        this.setBackground(TimerGUI.TRANSPARENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.clearRect(0, 0, getWidth(), getHeight());

        // Draw the background image
        switch (settings.mode) {
            case COLOR:
                g.setColor(settings.backgroundColor);
                g.fillRect(0, 0, getWidth(), getHeight());
                break;
            case IMAGE:
                g.drawImage(this.settings.backgroundImage, 0, 0, getWidth(), getHeight(), this);
                break;
            case TRANSPARENT:
                /*g.setColor(TimerGUI.TRANSPARENT);
                g.fillRect(0, 0, getWidth(), getHeight());*/
                break;
        }
    }

    public void updateSettings(Settings settings) {
        this.settings = settings;
        this.repaint();
    }

}
