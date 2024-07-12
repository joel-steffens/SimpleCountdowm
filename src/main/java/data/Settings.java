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
package data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class Settings {
    public enum BackgroundMode {
        IMAGE,
        COLOR,
        TRANSPARENT;
    }

    public enum Alignment {
        TOP_LEFT(GridBagConstraints.NORTHWEST),
        TOP_CENTER(GridBagConstraints.NORTH),
        TOP_RIGHT(GridBagConstraints.NORTHEAST),
        MIDDLE_LEFT(GridBagConstraints.WEST),
        MIDDLE_CENTER(GridBagConstraints.CENTER),
        MIDDLE_RIGHT(GridBagConstraints.EAST),
        BOTTOM_LEFT(GridBagConstraints.SOUTHWEST),
        BOTTOM_CENTER(GridBagConstraints.SOUTH),
        BOTTOM_RIGHT(GridBagConstraints.SOUTHEAST)
        ;

        private final int gbcAlignment;

        Alignment(int gbcAlignment) {
            this.gbcAlignment = gbcAlignment;
        }

        public int getGbcAlignment() {
            return gbcAlignment;
        }
    }

    public Font font;

    public Color textColor;

    public BackgroundMode mode;

    public String imagePath;

    public Image backgroundImage;

    public Color backgroundColor;

    public Alignment alignment;

    public int marginX;

    public int marginY;

    public Rectangle bounds;

    public boolean fullscreen;

    public void loadFrom(Preferences preferences, Rectangle defaultBounds) throws IOException, IllegalArgumentException {
        var fontSize = preferences.getInt("fontSize", 40);
        var fontName = preferences.get("fontName", Font.SERIF);
        var fontStyle = preferences.getInt("fontStyle", Font.PLAIN);

        this.font = new Font(fontName, fontStyle, fontSize);
        this.alignment = getEnumValue("alignment", preferences, Alignment.class, Alignment.MIDDLE_CENTER);
        this.mode = getEnumValue("mode", preferences, BackgroundMode.class, BackgroundMode.COLOR);

        this.textColor = new Color(preferences.getInt("textColor", Color.WHITE.getRGB()));
        this.backgroundColor = new Color(preferences.getInt("bgColor", Color.BLACK.getRGB()));

        this.marginX = preferences.getInt("marginX", 50);
        this.marginY = preferences.getInt("marginY", 50);

        var x = preferences.getInt("boundsX", defaultBounds.x);
        var y = preferences.getInt("boundsY", defaultBounds.y);
        var w = preferences.getInt("boundsW", defaultBounds.width);
        var h = preferences.getInt("boundsH", defaultBounds.height);

        this.imagePath = preferences.get("imagePath", null);
        if (this.imagePath != null) {
            this.backgroundImage = ImageIO.read(new File(this.imagePath));
        }

        this.bounds = new Rectangle(x, y, w, h);
        this.fullscreen = preferences.getBoolean("fullscreen", true);
    }

    public void saveTo(Preferences preferences) {
        preferences.put("fontName", font.getName());
        preferences.putInt("fontStyle", font.getStyle());
        preferences.putInt("fontSize", font.getSize());

        preferences.put("alignment", alignment.name());
        preferences.put("mode", mode.name());

        preferences.putInt("textColor", textColor.getRGB());
        preferences.putInt("bgColor", backgroundColor.getRGB());

        preferences.putInt("marginX", marginX);
        preferences.putInt("marginY", marginY);

        preferences.putInt("boundsX", bounds.x);
        preferences.putInt("boundsY", bounds.y);
        preferences.putInt("boundsW", bounds.width);
        preferences.putInt("boundsH", bounds.height);

        if (imagePath != null)
            preferences.put("imagePath", imagePath);

        preferences.putBoolean("fullscreen", this.fullscreen);
    }

    public void loadDefaults(Rectangle defaultBounds) {
        this.font = new Font(Font.SERIF, Font.PLAIN, 40);
        this.alignment = Alignment.MIDDLE_CENTER;
        this.mode =  BackgroundMode.COLOR;

        this.textColor =  Color.WHITE;
        this.backgroundColor = Color.BLACK;

        this.marginX = 50;
        this.marginY = 50;

        this.imagePath = null;
        this.backgroundImage = null;
        this.bounds = new Rectangle(defaultBounds);
        this.fullscreen = true;
    }

    private <E extends Enum<E>> E getEnumValue(String key, Preferences preferences, Class<E> enumClass, E defaultValue) {
        var enumConstant = preferences.get(key, defaultValue.name());

        return Enum.valueOf(enumClass, enumConstant);
    }





}
