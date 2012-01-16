/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.view.toolbox;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import forge.gui.GuiUtils;

/**
 * Assembles settings from selected or default theme as appropriate. Saves in a
 * hashtable, access using .get(settingName) method.
 * 
 */

public class FSkin {
    private Map<String, ImageIcon> icons;
    private Map<String, Color> colors;
    private Map<String, Image> images;
    private Map<Integer, Font> plainFonts;
    private Map<Integer, Font> boldFonts;
    private Map<Integer, Font> italicFonts;

    private Font font = null;
    private String name = "default";
    private final String spriteFile = "";
    private final String font1file = "font1.ttf";
    private Font tempFont;
    private final String notfound = "FSkin.java: Can't find ";

    /**
     * Gets the skins.
     *
     * @return the skins
     */
    public static ArrayList<String> getSkins() {
        final ArrayList<String> mySkins = new ArrayList<String>();

        File dir = new File("res/images/skins/");
        String[] children = dir.list();
        if (children == null) {
            System.err.println("FSkin > can't find skins directory!");
        } else {
            for (int i = 0; i < children.length; i++) {
                if (children[i].equalsIgnoreCase(".svn")) { continue; }
                if (children[i].equalsIgnoreCase(".DS_Store")) { continue; }
                mySkins.add(children[i]);
            }
        }

        return mySkins;
    }

    /**
     * FSkin constructor. No arguments, will generate default skin settings,
     * fonts, and backgrounds.
     * 
     * @throws Exception
     *             the exception
     */
    public FSkin() throws Exception {
        this("default");
    }

    /**
     * FSkin constructor, using skin name. Generates custom skin settings,
     * fonts, and backgrounds.
     * 
     * @param skinName
     *            the skin name
     * @throws Exception
     *             the exception
     */
    public FSkin(final String skinName) throws Exception {
        this.name = skinName;
        this.loadFontAndImages(skinName);
    }

    /**
     * Loads objects from skin folder, prints brief error if not found.
     * 
     * @param skinName
     */
    private void loadFontAndImages(final String skinName) {
        final String dirName = "res/images/skins/" + skinName + "/";

        this.icons = new HashMap<String, ImageIcon>();
        this.colors = new HashMap<String, Color>();
        this.images = new HashMap<String, Image>();

        // Fonts
        this.setFont(this.retrieveFont(dirName + this.font1file));
        plainFonts = new HashMap<Integer, Font>();
        plainFonts.put(10, font.deriveFont(Font.PLAIN, 10));
        plainFonts.put(11, font.deriveFont(Font.PLAIN, 11));
        plainFonts.put(12, font.deriveFont(Font.PLAIN, 12));
        plainFonts.put(13, font.deriveFont(Font.PLAIN, 13));
        plainFonts.put(14, font.deriveFont(Font.PLAIN, 14));
        plainFonts.put(15, font.deriveFont(Font.PLAIN, 15));
        plainFonts.put(16, font.deriveFont(Font.PLAIN, 16));
        plainFonts.put(18, font.deriveFont(Font.PLAIN, 18));
        plainFonts.put(20, font.deriveFont(Font.PLAIN, 20));
        plainFonts.put(22, font.deriveFont(Font.PLAIN, 22));

        boldFonts = new HashMap<Integer, Font>();
        boldFonts.put(12, font.deriveFont(Font.BOLD, 12));
        boldFonts.put(14, font.deriveFont(Font.BOLD, 14));
        boldFonts.put(16, font.deriveFont(Font.BOLD, 16));
        boldFonts.put(18, font.deriveFont(Font.BOLD, 18));
        boldFonts.put(20, font.deriveFont(Font.BOLD, 20));

        italicFonts = new HashMap<Integer, Font>();
        italicFonts.put(12, font.deriveFont(Font.ITALIC, 12));
        italicFonts.put(14, font.deriveFont(Font.ITALIC, 14));

        // Images
        this.setImage("bg.texture", this.retrieveImage(dirName + "bg_texture.jpg"));
        this.setImage("bg.match", this.retrieveImage(dirName + "bg_match.jpg"));
        this.setImage("bg.splash", this.retrieveImage(dirName + "bg_splash.jpg"));

        // Sprite
        final File file = new File(dirName + "sprite.png");
        BufferedImage image;
        try {
            image = ImageIO.read(file);
            this.setColor("theme", this.getColorFromPixel(image.getRGB(70, 10)));
            this.setColor("borders", this.getColorFromPixel(image.getRGB(70, 30)));
            this.setColor("zebra", this.getColorFromPixel(image.getRGB(70, 50)));
            this.setColor("hover", this.getColorFromPixel(image.getRGB(70, 70)));
            this.setColor("active", this.getColorFromPixel(image.getRGB(70, 90)));
            this.setColor("inactive", this.getColorFromPixel(image.getRGB(70, 110)));
            this.setColor("text", this.getColorFromPixel(image.getRGB(70, 130)));
            this.setColor("progress1", this.getColorFromPixel(image.getRGB(65, 145)));
            this.setColor("progress2", this.getColorFromPixel(image.getRGB(75, 145)));
            this.setColor("progress3", this.getColorFromPixel(image.getRGB(65, 155)));
            this.setColor("progress4", this.getColorFromPixel(image.getRGB(75, 155)));

            // All icons should eventually be set and retrieved using this
            // method.
            // Doublestrike 6-12-11
            this.setIcon("zone.hand", image.getSubimage(280, 40, 40, 40));
            this.setIcon("zone.library", image.getSubimage(280, 0, 40, 40));
            this.setIcon("zone.graveyard", image.getSubimage(320, 0, 40, 40));
            this.setIcon("zone.exile", image.getSubimage(320, 40, 40, 40));
            this.setIcon("zone.flashback", image.getSubimage(320, 120, 40, 40));
            this.setIcon("zone.poison", image.getSubimage(320, 80, 40, 40));

            this.setIcon("mana.black", image.getSubimage(240, 0, 40, 40));
            this.setIcon("mana.blue", image.getSubimage(240, 40, 40, 40));
            this.setIcon("mana.green", image.getSubimage(240, 120, 40, 40));
            this.setIcon("mana.red", image.getSubimage(240, 80, 40, 40));
            this.setIcon("mana.white", image.getSubimage(280, 120, 40, 40));
            this.setIcon("mana.colorless", image.getSubimage(280, 80, 40, 40));

            this.setIcon("dock.shortcuts", image.getSubimage(160, 0, 80, 80));
            this.setIcon("dock.settings", image.getSubimage(80, 0, 80, 80));
            this.setIcon("dock.endturn", image.getSubimage(160, 80, 80, 80));
            this.setIcon("dock.concede", image.getSubimage(80, 80, 80, 80));
            this.setIcon("dock.decklist", image.getSubimage(80, 160, 80, 80));

            this.setImage("image.logo", image.getSubimage(280, 240, 200, 200));

            this.setImage("button.startUP", image.getSubimage(0, 240, 160, 80));
            this.setImage("button.startOVER", image.getSubimage(0, 320, 160, 80));
            this.setImage("button.startDOWN", image.getSubimage(0, 400, 160, 80));

            this.setImage("button.upLEFT", image.getSubimage(360, 0, 40, 40));
            this.setImage("button.upCENTER", image.getSubimage(400, 0, 1, 40));
            this.setImage("button.upRIGHT", image.getSubimage(440, 0, 40, 40));

            this.setImage("button.overLEFT", image.getSubimage(360, 40, 40, 40));
            this.setImage("button.overCENTER", image.getSubimage(400, 40, 1, 40));
            this.setImage("button.overRIGHT", image.getSubimage(440, 40, 40, 40));

            this.setImage("button.downLEFT", image.getSubimage(360, 80, 40, 40));
            this.setImage("button.downCENTER", image.getSubimage(400, 80, 1, 40));
            this.setImage("button.downRIGHT", image.getSubimage(440, 80, 40, 40));

            this.setImage("button.focusLEFT", image.getSubimage(360, 120, 40, 40));
            this.setImage("button.focusCENTER", image.getSubimage(400, 120, 1, 40));
            this.setImage("button.focusRIGHT", image.getSubimage(440, 120, 40, 40));

            this.setImage("button.toggleLEFT", image.getSubimage(360, 160, 40, 40));
            this.setImage("button.toggleCENTER", image.getSubimage(400, 160, 1, 40));
            this.setImage("button.toggleRIGHT", image.getSubimage(440, 160, 40, 40));

            this.setImage("button.disabledLEFT", image.getSubimage(360, 200, 40, 40));
            this.setImage("button.disabledCENTER", image.getSubimage(400, 200, 1, 40));
            this.setImage("button.disabledRIGHT", image.getSubimage(440, 200, 40, 40));
        } catch (final IOException e) {
            System.err.println(this.notfound + this.spriteFile);
        }
    }

    /**
     * <p>
     * retrieveImage.
     * </p>
     * Tries to instantiate an image icon from a filename. Error reported if not
     * found.
     * 
     * @param {@link java.lang.String} address
     * @return a ImageIcon
     */
    private Image retrieveImage(final String address) {
        final ImageIcon tempImg = new ImageIcon(address);

        if (tempImg.getIconWidth() == -1) {
            System.err.println(this.notfound + address);
        }

        return tempImg.getImage();
    }

    /**
     * <p>
     * retrieveFont.
     * </p>
     * Uses GuiUtils to grab a font file at an address. Error will be reported
     * by GuiUtils if not found.
     * 
     * @param {@link java.lang.String} address
     * @return a Font
     */
    private Font retrieveFont(final String address) {
        this.tempFont = GuiUtils.newFont(address);

        return this.tempFont;
    }

    /**
     * <p>
     * getColorFromPixel.
     * </p>
     * 
     * @param {@link java.lang.Integer} pixel information
     */
    private Color getColorFromPixel(final int pixel) {
        int r, g, b, a;
        a = (pixel >> 24) & 0x000000ff;
        r = (pixel >> 16) & 0x000000ff;
        g = (pixel >> 8) & 0x000000ff;
        b = (pixel) & 0x000000ff;
        return new Color(r, g, b, a);
    }

    /** @return {@link java.awt.font} font */
    public Font getFont() {
        return this.font;
    }


    /**
     * Primary font used in titles and buttons and most text output.
     * 
     * @param font0 - an image icon
     */
    public void setFont(final Font font0) {
        this.font = font0;
    }

    /**
     * @param size - integer, pixel size
     * @return {@link java.awt.font} font1
     */
    public Font getFont(int size) {
        if (plainFonts.get(size) == null) {
            plainFonts.put(size, getFont().deriveFont(Font.PLAIN, size));
        }
        return plainFonts.get(size);
    }

    /**
     * @param size - integer, pixel size
     * @return {@link java.awt.font} font1
     */
    public Font getBoldFont(int size) {
        if (boldFonts.get(size) == null) {
            boldFonts.put(size, getFont().deriveFont(Font.BOLD, size));
        }
        return boldFonts.get(size);
    }

    /**
     * @param size - integer, pixel size
     * @return {@link java.awt.font} font1
     */
    public Font getItalicFont(int size) {
        if (boldFonts.get(size) == null) {
            italicFonts.put(size, getFont().deriveFont(Font.ITALIC, size));
        }
        return italicFonts.get(size);
    }

    /**
     * Gets the name.
     * 
     * @return Name of skin.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the icon.
     *
     * @param s0 &emsp; String address
     * @return ImageIcon
     */
    public ImageIcon getIcon(final String s0) {
        return this.icons.get(s0);
    }

    /**
     * Gets a scaled version of an icon from this skin's icon map.
     * 
     * @param s0
     *            String icon address
     * @param w0
     *            int new width
     * @param h0
     *            int new height
     * @return ImageIcon
     */
    public ImageIcon getIcon(final String s0, int w0, int h0) {
        w0 = (w0 < 1) ? 1 : w0;
        h0 = (h0 < 1) ? 1 : h0;

        final BufferedImage original = (BufferedImage) this.icons.get(s0).getImage();
        final BufferedImage scaled = new BufferedImage(w0, h0, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, w0, h0, 0, 0, original.getWidth(), original.getHeight(), null);
        g2d.dispose();

        return new ImageIcon(scaled);
    }

    /**
     * Sets an icon in this skin's icon map from a BufferedImage.
     * 
     * @param s0
     *            &emsp; String address
     * @param bi0
     *            &emsp; BufferedImage
     */
    public void setIcon(final String s0, final BufferedImage bi0) {
        this.icons.put(s0, new ImageIcon(bi0));
    }

    /**
     * Sets an icon in this skin's icon map from an ImageIcon.
     * 
     * @param s0
     *            &emsp; String address
     * @param i0
     *            &emsp; ImageIcon
     */
    public void setIcon(final String s0, final ImageIcon i0) {
        this.icons.put(s0, i0);
    }

    /**
     * Retrieves a color from this skin's color map.
     * 
     * @param s0
     *            &emsp; String color address
     * @return Color
     */
    public Color getColor(final String s0) {
        return this.colors.get(s0);
    }

    /**
     * Sets a color in this skin's color map.
     * 
     * @param s0
     *            &emsp; String address
     * @param c0
     *            &emsp; Color
     */
    public void setColor(final String s0, final Color c0) {
        this.colors.put(s0, c0);
    }

    /**
     * Retrieves an image from this skin's image map.
     * 
     * @param s0
     *            &emsp; String color address
     * @return BufferedImage
     */
    public Image getImage(final String s0) {
        return this.images.get(s0);
    }

    /**
     * Sets an image in this skin's image map.
     * 
     * @param s0
     *            &emsp; String address
     * @param bi0
     *            &emsp; ImageIcon
     */
    public void setImage(final String s0, final Image bi0) {
        this.images.put(s0, bi0);
    }
}
