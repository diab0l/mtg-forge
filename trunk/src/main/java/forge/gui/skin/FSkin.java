package forge.gui.skin;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import forge.gui.GuiUtils;

/**
 * Assembles settings from selected or default theme as appropriate. Saves in a
 * hashtable, access using .get(settingName) method.
 * 
 */

public class FSkin {
    // ===== Public fields
    /** Primary font used in titles and buttons and most text output. */
    private Font font1 = null;

    /** Secondary font used where a sub-block of text needs it. */
    private Font font2 = null;

    /** Primary texture used in skin. */
    private ImageIcon texture1 = null;

    /** Left side of button, up state. */
    private ImageIcon btnLup = null;

    /** Middle of button, up state. */
    private ImageIcon btnMup = null;

    /** Right side of button, up state. */
    private ImageIcon btnRup = null;

    /** Button, left side, over state. */
    private ImageIcon btnLover = null;

    /** Button, middle, over state. */
    private ImageIcon btnMover = null;

    /** Button, right side, over state. */
    private ImageIcon btnRover = null;

    /** Button, left side, down state. */
    private ImageIcon btnLdown = null;

    /** Button, middle, down state. */
    private ImageIcon btnMdown = null;

    /** Button, right side, down state. */
    private ImageIcon btnRdown = null;

    /** Splash screen image. */
    private ImageIcon splash = null;

    /** Base color used in skin. */
    private Color clrTheme = Color.red;

    /** Border color. */
    private Color clrBorders = Color.red;

    /** Color of zebra striping in grid displays. */
    private Color clrZebra = Color.red;

    /** Color of elements in mouseover state. */
    private Color clrHover = Color.red;

    /** Color of active (currently selected) elements. */
    private Color clrActive = Color.red;

    /** Color of inactive (not currently selected) elements. */
    private Color clrInactive = Color.red;

    /** Color of text in skin. */
    private Color clrText = Color.red;

    /** Name of skin. */
    private String name = "default";

    // ===== Private fields
    private final String paletteFile = "palette.png";
    private final String font1file = "font1.ttf";
    private final String font2file = "font2.ttf";
    private final String texture1file = "texture1.jpg";
    private final String btnLupfile = "btnLup.png";
    private final String btnMupfile = "btnMup.png";
    private final String btnRupfile = "btnRup.png";
    private final String btnLoverfile = "btnLover.png";
    private final String btnMoverfile = "btnMover.png";
    private final String btnRoverfile = "btnRover.png";
    private final String btnLdownfile = "btnLdown.png";
    private final String btnMdownfile = "btnMdown.png";
    private final String btnRdownfile = "btnRdown.png";
    private final String splashfile = "bg_splash.jpg";

    private ImageIcon tempImg;
    private Font tempFont;
    private String skin;
    private final String notfound = "FSkin.java: \"" + this.skin + "\" skin can't find ";

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
        this.loadFontAndImages("default");

        if (!skinName.equals("default")) {
            this.loadFontAndImages(skinName);
        }
    }

    /**
     * Loads objects from skin folder, prints brief error if not found.
     * 
     * @param skinName
     */
    private void loadFontAndImages(final String skinName) {
        final String dirName = "res/images/skins/" + skinName + "/";

        // Fonts
        this.setFont1(this.retrieveFont(dirName + this.font1file));
        this.setFont2(this.retrieveFont(dirName + this.font2file));

        // Images
        this.setTexture1(this.retrieveImage(dirName + this.texture1file));
        this.setBtnLup(this.retrieveImage(dirName + this.btnLupfile));
        this.setBtnMup(this.retrieveImage(dirName + this.btnMupfile));
        this.setBtnRup(this.retrieveImage(dirName + this.btnRupfile));
        this.setBtnLover(this.retrieveImage(dirName + this.btnLoverfile));
        this.setBtnMover(this.retrieveImage(dirName + this.btnMoverfile));
        this.setBtnRover(this.retrieveImage(dirName + this.btnRoverfile));
        this.setBtnLdown(this.retrieveImage(dirName + this.btnLdownfile));
        this.setBtnMdown(this.retrieveImage(dirName + this.btnMdownfile));
        this.setBtnRdown(this.retrieveImage(dirName + this.btnRdownfile));
        this.setSplash(this.retrieveImage(dirName + this.splashfile));

        // Color palette
        final File file = new File(dirName + this.paletteFile);
        BufferedImage image;
        try {
            image = ImageIO.read(file);
            this.setClrTheme(this.getColorFromPixel(image.getRGB(60, 10)));
            this.setClrBorders(this.getColorFromPixel(image.getRGB(60, 30)));
            this.setClrZebra(this.getColorFromPixel(image.getRGB(60, 50)));
            this.setClrHover(this.getColorFromPixel(image.getRGB(60, 70)));
            this.setClrActive(this.getColorFromPixel(image.getRGB(60, 90)));
            this.setClrInactive(this.getColorFromPixel(image.getRGB(60, 110)));
            this.setClrText(this.getColorFromPixel(image.getRGB(60, 130)));
        } catch (final IOException e) {
            System.err.println(this.notfound + this.paletteFile);
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
    private ImageIcon retrieveImage(final String address) {
        this.tempImg = new ImageIcon(address);
        if (this.tempImg.getIconWidth() == -1) {
            System.err.println(this.notfound + address);
        }

        return this.tempImg;
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
        
        return new Color(r,g,b,a);
    }

    /**
     * Primary font used in titles and buttons and most text output.
     * @return {@link java.awt.font} font1
     */
    public Font getFont1() {
        return font1;
    }

    /**
     * Primary font used in titles and buttons and most text output.
     * @param {@link java.awt.font} font1
     */
    public void setFont1(Font font10) {
        this.font1 = font10;
    }

    /**
     * Secondary font used where a sub-block of text needs it.
     * @return {@link java.awt.Font} font2
     */
    public Font getFont2() {
        return font2;
    }

    /**
     * Secondary font used where a sub-block of text needs it.
     * @param {@link java.awt.Font} font2
     */
    public void setFont2(Font font20) {
        this.font2 = font20; 
    }

    /**
     * Splash screen image.
     * @return {@link javax.swing.ImageIcon} splash
     */
    public ImageIcon getSplash() {
        return splash;
    }

    /**
     * Splash screen image.
     * @param {@link javax.swing.ImageIcon} splash
     */
    public void setSplash(ImageIcon splash0) {
        this.splash = splash0; 
    }
    
    /**
     * Base color used in skin.
     * @return {@link java.awt.Color} clrTheme
     */
    public Color getClrTheme() {
        return clrTheme;
    }

    /**
     * Base color used in skin.
     * @param {@link java.awt.Color} clrTheme
     */
    public void setClrTheme(Color clrTheme0) {
        this.clrTheme = clrTheme0; 
    }

    /**
     * Border color.
     * @return {@link java.awt.Color} clrBorders
     */
    public Color getClrBorders() {
        return clrBorders;
    }

    /**
     * Border color.
     * @param {@link java.awt.Color} clrBorders
     */
    public void setClrBorders(Color clrBorders0) {
        this.clrBorders = clrBorders0; 
    }

    /**
     * Primary texture used in skin.
     * @return {@link javax.swing.ImageIcon} texture1
     */
    public ImageIcon getTexture1() {
        return texture1;
    }

    /**
     * Primary texture used in skin.
     * @param {@link javax.swing.ImageIcon} texture1
     */
    public void setTexture1(ImageIcon texture10) {
        this.texture1 = texture10;
    }

    /**
     * Color of zebra striping in grid displays.
     * @return {@link java.awt.Color} clrZebra
     */
    public Color getClrZebra() {
        return clrZebra;
    }

    /**
     * Color of zebra striping in grid displays.
     * @param {@link java.awt.Color} clrZebra
     */
    public void setClrZebra(Color clrZebra0) {
        this.clrZebra = clrZebra0; 
    }
    
    /**
     * Color of elements in mouseover state.
     * @return {@link java.awt.Color} clrHover
     */
    public Color getClrHover() {
        return clrHover;
    }

    /**
     * Color of elements in mouseover state.
     * @param {@link java.awt.Color} clrHover
     */
    public void setClrHover(Color clrHover0) {
        this.clrHover = clrHover0; 
    }
    
    /**
     * Color of active (currently selected) elements.
     * @return {@link java.awt.Color} clrActive
     */
    public Color getClrActive() {
        return clrActive;
    }

    /**
     * Color of active (currently selected) elements.
     * @param {@link java.awt.Color} clrActive
     */
    public void setClrActive(Color clrActive0) {
        this.clrActive = clrActive0; 
    }
    
    /**
     * Color of inactive (not currently selected) elements.
     * @return {@link java.awt.Color} clrHover
     */
    public Color getClrInactive() {
        return clrInactive;
    }

    /**
     * Color of inactive (not currently selected) elements.
     * @param {@link java.awt.Color} clrHover
     */
    public void setClrInactive(Color clrInactive0) {
        this.clrInactive = clrInactive0; 
    }
    
    /**
     * Color of text in skin.
     * @return {@link java.awt.Color} clrText
     */
    public Color getClrText() {
        return clrText;
    }

    /**
     * Color of text in skin.
     * @param {@link java.awt.Color} clrHover
     */
    public void setClrText(Color clrText0) {
        this.clrText = clrText0; 
    }

    /**
     * Left side of button, up state.
     * @return {@link javax.swing.ImageIcon} btnLup
     */
    public ImageIcon getBtnLup() {
        return btnLup;
    }

    /**
     * Left side of button, up state.
     * @param {@link javax.swing.ImageIcon} btnLup
     */
    public void setBtnLup(ImageIcon btnLup0) {
        this.btnLup = btnLup0; 
    }

    /**
     * Middle of button, up state.
     * @return {@link javax.swing.ImageIcon} btnMup
     */
    public ImageIcon getBtnMup() {
        return btnMup;
    }

    /**
     * Middle of button, up state.
     * @param {@link javax.swing.ImageIcon} btnMup
     */
    public void setBtnMup(ImageIcon btnMup0) {
        this.btnMup = btnMup0; 
    }
    
    /**
     * Right side of button, up state.
     * @return {@link javax.swing.ImageIcon} btnRup
     */
    public ImageIcon getBtnRup() {
        return btnRup;
    }

    /**
     * Right side of button, up state.
     * @param {@link javax.swing.ImageIcon} btnRup
     */
    public void setBtnRup(ImageIcon btnRup0) {
        this.btnRup = btnRup0; 
    }

    /**
     * Left side of button, over state.
     * @return {@link javax.swing.ImageIcon} btnLover
     */
    public ImageIcon getBtnLover() {
        return btnLover;
    }

    /**
     * Left side of button, over state.
     * @param {@link javax.swing.ImageIcon} btnLover
     */
    public void setBtnLover(ImageIcon btnLover0) {
        this.btnLover = btnLover0; 
    }

    /**
     * Middle of button, over state.
     * @return {@link javax.swing.ImageIcon}  btnMover
     */
    public ImageIcon getBtnMover() {
        return btnMover;
    }

    /**
     * Middle of button, over state.
     * @param {@link javax.swing.ImageIcon} btnMover
     */
    public void setBtnMover(ImageIcon btnMover0) {
        this.btnMover = btnMover0;
    }

    /**
     * Right side of button, over state.
     * @return {@link javax.swing.ImageIcon}  btnRover
     */
    public ImageIcon getBtnRover() {
        return btnRover;
    }

    /**
     * Right side of button, over state.
     * @param {@link javax.swing.ImageIcon} btnRover
     */
    public void setBtnRover(ImageIcon btnRover0) {
        this.btnRover = btnRover0; 
    }

    /**
     * Left side of button, down state.
     * @return {@link javax.swing.ImageIcon} btnLdown
     */
    public ImageIcon getBtnLdown() {
        return btnLdown;
    }

    /**
     * Left side of button, down state.
     * @param {@link javax.swing.ImageIcon} btnLdown
     */
    public void setBtnLdown(ImageIcon btnLdown0) {
        this.btnLdown = btnLdown0;
    }
    
    /**
     * Right side of button, down state.
     * @return {@link javax.swing.ImageIcon} btnRdown
     */
    public ImageIcon getBtnRdown() {
        return btnRdown;
    }

    /**
     * Right side of button, down state.
     * @param {@link javax.swing.ImageIcon} btnRdown
     */
    public void setBtnRdown(ImageIcon btnRdown0) {
        this.btnRdown = btnRdown0;
    }

    /**
     * Middle of button, down state.
     * @return {@link javax.swing.ImageIcon}  btnMdown
     */
    public ImageIcon getBtnMdown() {
        return btnMdown;
    }

    /**
     * Middle of button, down state.
     * @param {@link javax.swing.ImageIcon} btnMdown
     */
    public void setBtnMdown(ImageIcon btnMdown0) {
        this.btnMdown = btnMdown0;
    }
    
    /**
     * Name of skin.
     * @return {@link java.lang.String} name
     */
    public String getName() {
        return name;
    }
}
