package forge.gui.skin;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import forge.gui.GuiUtils;

/** 
 * Assembles settings from selected or default theme as appropriate.
 * Saves in a hashtable, access using .get(settingName) method.
 *
 */

public class FSkin {
    //===== Public fields
    public Font font1                   = null;
    public Font font2                   = null;
    public ImageIcon texture1           = null;
    public ImageIcon texture2           = null;
    public ImageIcon texture3           = null;
    public ImageIcon btnLup             = null;
    public ImageIcon btnMup             = null;
    public ImageIcon btnRup             = null;   
    public ImageIcon btnLover           = null;
    public ImageIcon btnMover           = null;
    public ImageIcon btnRover           = null;  
    public ImageIcon btnLdown           = null;
    public ImageIcon btnMdown           = null;
    public ImageIcon btnRdown           = null;  
    
    public Color bg1a                   = Color.red;
    public Color bg1b                   = Color.red;
    public Color bg2a                   = Color.red;
    public Color bg2b                   = Color.red;
    public Color bg3a                   = Color.red;
    public Color bg3b                   = Color.red;
    
    public Color txt1a                  = Color.red;
    public Color txt1b                  = Color.red;
    public Color txt2a                  = Color.red;
    public Color txt2b                  = Color.red;
    public Color txt3a                  = Color.red;
    public Color txt3b                  = Color.red;
    
    //===== Private fields
    private final String settingsfile   = "settings.txt";
    private final String paletteFile    = "palette.jpg";
    private final String font1file      = "font1.ttf";
    private final String font2file      = "font2.ttf";    
    private final String texture1file   = "texture1.jpg";
    private final String texture2file   = "texture2.jpg";
    private final String texture3file   = "texture3.jpg";
    private final String btnLupfile     = "btnLup.png";
    private final String btnMupfile     = "btnMup.png";
    private final String btnRupfile     = "btnRup.png";
    private final String btnLoverfile   = "btnLover.png";
    private final String btnMoverfile   = "btnMover.png";
    private final String btnRoverfile   = "btnRover.png";
    private final String btnLdownfile   = "btnLdown.png";
    private final String btnMdownfile   = "btnMdown.png";
    private final String btnRdownfile   = "btnRdown.png";
    
    private ImageIcon tempImg;
    private Font tempFont;
    private String skin;
    private String notfound = "FSkin.java: \""+skin+
            "\" skin can't find ";
    private Hashtable<String, String> customSettings = 
            new Hashtable<String, String>();
    private Hashtable<String, String> defaultSettings = 
            new Hashtable<String, String>();

    /**
     * FSkin constructor.  No arguments, will generate default skin settings,
     * fonts, and backgrounds.
     * 
     * @throws Exception
     */
    public FSkin() throws Exception {
        this("default");
    }
    
    /**
     * FSkin constructor, using skin name. Generates custom skin settings,
     * fonts, and backgrounds.
     * 
     * @param themeName
     * @throws Exception
     */
    public FSkin(String skinName) throws Exception {
        defaultSettings = loadSettings("default"); 
        loadFontAndImages("default");  
        
        if(!skinName.equals("default")) {
            customSettings = loadSettings(skinName);
            loadFontAndImages(skinName);
        }
    }
    
    /**
     * Retrieves skin settings from file, returns in hashtable.
     * 
     * @param skinName
     * @return settings hashtable
     * @throws Exception
     */
    private Hashtable<String, String> loadSettings(String skinName) throws Exception {
        String filename = "res/images/skins/"+skinName+"/"+settingsfile;
        
        String notfound = "FSkin > load: Can't find \"" + filename + "\".";
        Hashtable<String, String> settings = new Hashtable<String, String>();
        
        File f = new File(filename);
        customSettings.put("name",skinName);
        
        if (!f.exists()) {
            throw new RuntimeException(notfound);
        }
        
        BufferedReader input = new BufferedReader(new FileReader(f));
        String line = null;
        while ((line = input.readLine()) != null) {
            String[] data = line.split("=");
            if (line.startsWith("#") || line.length() == 0 || data.length != 2) {
                continue;
            }
            
            settings.put(data[0],data[1]);
        }
        
        return settings;
    }
    
    /**
     * Loads objects from skin folder, prints brief error if not found.
     * 
     * @param skinName
     */
    private void loadFontAndImages(String skinName) {
        String dirName = "res/images/skins/"+skinName+"/";
       
        // Fonts
        font1 = retrieveFont(dirName + font1file);
        font2 = retrieveFont(dirName + font2file);
        
        // Images
        texture1 = retrieveImage(dirName + texture1file);
        texture2 = retrieveImage(dirName + texture2file);
        texture3 = retrieveImage(dirName + texture3file);
        btnLup = retrieveImage(dirName + btnLupfile);
        btnMup = retrieveImage(dirName + btnMupfile);
        btnRup = retrieveImage(dirName + btnRupfile);
        btnLover = retrieveImage(dirName + btnLoverfile);
        btnMover = retrieveImage(dirName + btnMoverfile);
        btnRover = retrieveImage(dirName + btnRoverfile);
        btnLdown = retrieveImage(dirName + btnLdownfile);
        btnMdown = retrieveImage(dirName + btnMdownfile);
        btnRdown = retrieveImage(dirName + btnRdownfile);
        
        // Color palette
        File file= new File(dirName + paletteFile);
        BufferedImage image;
        try {
            image = ImageIO.read(file);
            bg1a    = getColorFromPixel(image.getRGB(10,30));
            bg1b    = getColorFromPixel(image.getRGB(30,30));
            bg2a    = getColorFromPixel(image.getRGB(50,30));
            bg2b    = getColorFromPixel(image.getRGB(70,30));
            bg3a    = getColorFromPixel(image.getRGB(90,30));
            bg3b    = getColorFromPixel(image.getRGB(110,30)); 
            
            txt1a   = getColorFromPixel(image.getRGB(10,70));
            txt1b   = getColorFromPixel(image.getRGB(30,70));
            txt2a   = getColorFromPixel(image.getRGB(50,70));
            txt2b   = getColorFromPixel(image.getRGB(70,70));
            txt3a   = getColorFromPixel(image.getRGB(90,70));
            txt3b   = getColorFromPixel(image.getRGB(110,70)); 
        } catch (IOException e) {
            System.err.println(notfound + paletteFile);
        }
        
    }
    
    /**
     * <p>retrieveImage.</p>
     * Tries to instantiate an image icon from a filename.
     * Error reported if not found.
     * 
     * @param {@link java.lang.String} address
     * @return a ImageIcon
     */
    private ImageIcon retrieveImage(String address) {
        tempImg = new ImageIcon(address);
        if(tempImg.getIconWidth()==-1) {
            System.err.println(notfound + address);
        }

        return tempImg;
    }
    
    /**
     * <p>retrieveFont.</p>
     * Uses GuiUtils to grab a font file at an address.
     * Error will be reported by GuiUtils if not found.
     * Error also reported by this method if not found.
     * 
     * @param {@link java.lang.String} address
     * @return a Font
     */
    private Font retrieveFont(String address) {
        tempFont = GuiUtils.newFont(address);

        return tempFont;
    }
    
    /**
     * <p>getColorFromPixel.</p>
     * 
     * @param {@link java.lang.Integer} pixel information
     */
    private Color getColorFromPixel(int pixel) {
        return new Color(
                (pixel & 0x00ff0000) >> 16,
                (pixel & 0x0000ff00) >> 8,
                (pixel & 0x000000ff)
                );
    }
       
    /**
     * <p>getSetting.</p>
     * Retrieves a specific skin setting.  If skin setting is not defined, 
     * will attempt to use default value.
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSetting(String key) {
        String val = null;
        
        if(customSettings.size() > 1) {  
            val = customSettings.get(key);
            if(val==null) {
                System.err.println("FSkin > getSetting: Could not find "+key+
                        " setting for "+customSettings.get("name")+" skin!");
            }
        }
        
        if(val==null) {
            val = defaultSettings.get(key);
        }
        
        if(val==null) {
            System.err.println("FSkin > getSetting: Could not find "+key+
                    " in default settings!");
        }
        
        return val;
    }
}
