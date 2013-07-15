package forge.gui.home.settings;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import forge.GameLogEntryType;
import forge.Singletons;
import forge.control.KeyboardShortcuts;
import forge.control.KeyboardShortcuts.Shortcut;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.gui.home.VHomeUI;
import forge.gui.toolbox.FCheckBox;
import forge.gui.toolbox.FComboBoxPanel;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;
import forge.properties.ForgePreferences.FPref;

/** 
 * Assembles Swing components of preferences submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public enum VSubmenuPreferences implements IVSubmenu<CSubmenuPreferences> {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Preferences");

    /** */
    private final JPanel pnlPrefs = new JPanel();
    private final FScrollPane scrContent = new FScrollPane(pnlPrefs,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    private final FLabel btnReset = new FLabel.Builder().opaque(true).hoverable(true).text("Reset to Default Settings").build();
    private final FLabel btnDeleteMatchUI = new FLabel.Builder().opaque(true).hoverable(true).text("Reset Match Layout").build();
    private final FLabel btnDeleteEditorUI = new FLabel.Builder().opaque(true).hoverable(true).text("Reset Editor Layout").build();
    
    private final JCheckBox cbRemoveSmall = new OptionsCheckBox("Remove Small Creatures");
    private final JCheckBox cbSingletons = new OptionsCheckBox("Singleton Mode");
    private final JCheckBox cbRemoveArtifacts = new OptionsCheckBox("Remove Artifacts");
    private final JCheckBox cbAnte = new OptionsCheckBox("Play for Ante");
    private final JCheckBox cbUploadDraft = new OptionsCheckBox("Upload Draft Picks");
    private final JCheckBox cbStackLand = new OptionsCheckBox("Stack AI Land");
    private final JCheckBox cbManaBurn = new OptionsCheckBox("Mana Burn");
    private final JCheckBox cbDevMode = new OptionsCheckBox("Developer Mode");
    private final JCheckBox cbEnforceDeckLegality = new OptionsCheckBox("Deck Conformance");
    private final JCheckBox cbCloneImgSource = new OptionsCheckBox("Clones use original card art");
    private final JCheckBox cbScaleLarger = new OptionsCheckBox("Scale Image Larger");
    private final JCheckBox cbRandomFoil = new OptionsCheckBox("Random Foil");
    private final JCheckBox cbRandomizeArt = new OptionsCheckBox("Randomize Card Art");
    private final JCheckBox cbEnableSounds = new OptionsCheckBox("Enable Sounds");
    private final JCheckBox cbAltSoundSystem = new OptionsCheckBox("Use Alternate Sound System");
    private final JCheckBox cbUiForTouchScreen = new OptionsCheckBox("Enchance UI for touchscreens");
    private final JCheckBox cbOverlayCardName = new OptionsCheckBox("Card Name");
    private final JCheckBox cbOverlayCardPower = new OptionsCheckBox("Power/Toughness");
    private final JCheckBox cbOverlayCardManaCost = new OptionsCheckBox("Mana Cost");
    private final JCheckBox cbCompactMainMenu = new OptionsCheckBox("Use Compact Main Sidebar Menu");
    private final JCheckBox cbShowMatchBackgroundImage = new OptionsCheckBox("Show Background Image on Match Screen");    

    private final Map<FPref, KeyboardShortcutField> shortcutFields = new HashMap<FPref, KeyboardShortcutField>();

    // ComboBox items are added in CSubmenuPreferences since this is just the View.
    private final FComboBoxPanel<String> cbpSkin = new FComboBoxPanel<String>("Choose Skin:");    
    private final FComboBoxPanel<GameLogEntryType> cbpGameLogEntryType = new FComboBoxPanel<GameLogEntryType>("Game Log Verbosity:");
    private final FComboBoxPanel<String> cbpAiProfiles = new FComboBoxPanel<String>("AI Personality:");
    
    /**
     * Constructor.
     */
    private VSubmenuPreferences() {
        
        pnlPrefs.setOpaque(false);
        pnlPrefs.setLayout(new MigLayout("insets 0, gap 0, wrap 2"));

        // Spacing between components is defined here.
        final String sectionConstraints = "w 80%!, h 42px!, gap 10% 0 10px 10px, span 2 1";
        final String regularConstraints = "w 80%!, h 22px!, gap 10% 0 0 10px, span 2 1";
        
        
        // Troubleshooting
        pnlPrefs.add(new SectionLabel("Troubleshooting"), sectionConstraints);
        
        //pnlPrefs.add(new SectionLabel(" "), sectionConstraints);
        pnlPrefs.add(btnReset, regularConstraints + ", h 30px!");
                
        final String twoButtonConstraints = "w 38%!, h 30px!, gap 10% 0 0 10px";
        pnlPrefs.add(btnDeleteMatchUI, twoButtonConstraints);
        pnlPrefs.add(btnDeleteEditorUI, "w 38%!, h 30px!, gap 0 0 0 10px");
        // Reset button

          
        // General Configuration
        pnlPrefs.add(new SectionLabel("General Configuration"), sectionConstraints + ", gaptop 2%");
        
        pnlPrefs.add(cbCompactMainMenu, regularConstraints);
        pnlPrefs.add(new NoteLabel("Enable for a space efficient sidebar that displays only one menu group at a time (RESTART REQUIRED)."), regularConstraints);
        
                 
        // Gameplay Options
        pnlPrefs.add(new SectionLabel("Gameplay"), sectionConstraints + ", gaptop 2%");
        
        pnlPrefs.add(cbpAiProfiles, "w 80%!, gap 10% 0 0 10px, span 2 1");
        pnlPrefs.add(new NoteLabel("Choose your AI opponent."), regularConstraints);        

        pnlPrefs.add(cbAnte, regularConstraints);
        pnlPrefs.add(new NoteLabel("Determines whether or not the game is played for ante."), regularConstraints);

        pnlPrefs.add(cbUploadDraft, regularConstraints);
        pnlPrefs.add(new NoteLabel("Sends draft picks to Forge servers for analysis, to improve draft AI."), regularConstraints);

        pnlPrefs.add(cbStackLand, regularConstraints);
        pnlPrefs.add(new NoteLabel("Minimizes mana lock in AI hands, giving a slight advantage to computer."), regularConstraints);

        pnlPrefs.add(cbManaBurn, regularConstraints);
        pnlPrefs.add(new NoteLabel("Play with mana burn (from pre-Magic 2010 rules)."), regularConstraints);

        pnlPrefs.add(cbEnforceDeckLegality, regularConstraints);
        pnlPrefs.add(new NoteLabel("Enforces deck legality relevant to each environment (minimum deck sizes, max card count etc)"), regularConstraints);

        pnlPrefs.add(cbCloneImgSource, regularConstraints);
        pnlPrefs.add(new NoteLabel("When enabled clones will use their original art instead of the cloned card's art"), regularConstraints);
        
        // Deck building options
        pnlPrefs.add(new SectionLabel("Random Deck Generation"), sectionConstraints);

        pnlPrefs.add(cbRemoveSmall, regularConstraints);
        pnlPrefs.add(new NoteLabel("Disables 1/1 and 0/X creatures in generated decks."), regularConstraints);

        pnlPrefs.add(cbSingletons, regularConstraints);
        pnlPrefs.add(new NoteLabel("Disables non-land duplicates in generated decks."), regularConstraints);

        pnlPrefs.add(cbRemoveArtifacts, regularConstraints);
        pnlPrefs.add(new NoteLabel("Disables artifact cards in generated decks."), regularConstraints);
        
        // Advanced
        pnlPrefs.add(new SectionLabel("Advanced Settings"), sectionConstraints);

        pnlPrefs.add(cbDevMode, regularConstraints);
        pnlPrefs.add(new NoteLabel("Enables menu with functions for testing during development."), regularConstraints);

        pnlPrefs.add(cbpGameLogEntryType, "w 80%!, gap 10% 0 0 10px, span 2 1");
        pnlPrefs.add(new NoteLabel("Changes how much information is displayed in the game log. Sorted by least to most verbose."), regularConstraints);

        
        // Themes
        pnlPrefs.add(new SectionLabel("Visual Themes"), sectionConstraints + ", gaptop 2%");
        
        pnlPrefs.add(cbpSkin, "w 80%!, gap 10% 0 0 10px, span 2 1");
        pnlPrefs.add(new NoteLabel("Change the overall look and feel of Forge (RESTART REQUIRED)."), regularConstraints);

        pnlPrefs.add(cbShowMatchBackgroundImage, regularConstraints);
        pnlPrefs.add(new NoteLabel("Toggle the visibility of the background image on the match screen."), regularConstraints);


        // Graphic Options
        pnlPrefs.add(new SectionLabel("Graphic Options"), sectionConstraints + ", gaptop 2%");

        pnlPrefs.add(cbRandomFoil, regularConstraints);
        pnlPrefs.add(new NoteLabel("Adds foiled effects to random cards."), regularConstraints);

        pnlPrefs.add(cbRandomizeArt, regularConstraints);
        pnlPrefs.add(new NoteLabel("Randomize the card art for cards in the human's deck"), regularConstraints);

        pnlPrefs.add(cbScaleLarger, regularConstraints);
        pnlPrefs.add(new NoteLabel("Allows card pictures to be expanded larger than their original size."), regularConstraints);

        pnlPrefs.add(cbUiForTouchScreen, regularConstraints);
        pnlPrefs.add(new NoteLabel("Increases some UI elements to provide a better experience on touchscreen devices. (Needs restart)"), regularConstraints);

        // Card Overlay options
        pnlPrefs.add(new SectionLabel("Card Overlay Options"), sectionConstraints);
        pnlPrefs.add(new NoteLabel("Show text overlays which are easier to read when cards are reduced in size to fit the play area."), regularConstraints);
        pnlPrefs.add(cbOverlayCardName, regularConstraints);
        pnlPrefs.add(cbOverlayCardPower, regularConstraints);
        pnlPrefs.add(cbOverlayCardManaCost, regularConstraints);

        
        // Sound options
        pnlPrefs.add(new SectionLabel("Sound Options"), sectionConstraints + ", gaptop 2%");

        pnlPrefs.add(cbEnableSounds, regularConstraints);
        pnlPrefs.add(new NoteLabel("Enable sound effects during the game."), regularConstraints);

        pnlPrefs.add(cbAltSoundSystem, regularConstraints);
        pnlPrefs.add(new NoteLabel("Use the alternate sound system (only use in case your have issues with sound not playing or disappearing)"), regularConstraints);
	
        
        // Keyboard shortcuts
        final JLabel lblShortcuts = new SectionLabel("Keyboard Shortcuts");
        pnlPrefs.add(lblShortcuts, sectionConstraints + ", gaptop 2%");

        final List<Shortcut> shortcuts = Singletons.getControl().getShortcuts();

        for (final Shortcut s : shortcuts) {
            pnlPrefs.add(new FLabel.Builder().text(s.getDescription())
                    .fontAlign(SwingConstants.RIGHT).build(), "w 50%!, h 22px!, gap 0 2% 0 1%");
            KeyboardShortcutField field = new KeyboardShortcutField(s);
            pnlPrefs.add(field, "w 25%!");
            shortcutFields.put(s.getPrefKey(), field);
        }

        scrContent.setBorder(null);
    }
    
    public void reloadShortcuts() {
        for (Map.Entry<FPref, KeyboardShortcutField> e : shortcutFields.entrySet()) {
            e.getValue().reload(e.getKey());
        }
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#populate()
     */
    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0"));
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(scrContent, "w 98%!, h 98%!, gap 1% 0 1% 0");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getGroup()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.SETTINGS;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Preferences";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getItemEnum()
     */
    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_PREFERENCES;
    }

    /** Consolidates checkbox styling in one place. */
    @SuppressWarnings("serial")
    private class OptionsCheckBox extends FCheckBox {
        public OptionsCheckBox(final String txt0) {
            super(txt0);
            setFont(FSkin.getBoldFont(12));
        }
    }

    /** Consolidates section title label styling in one place. */
    @SuppressWarnings("serial")
    private class SectionLabel extends JLabel {
        public SectionLabel(final String txt0) {
            super(txt0);
            setBorder(new MatteBorder(0, 0, 1, 0, FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(FSkin.getBoldFont(16));
            setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        }
    }

    /** Consolidates notation label styling in one place. */
    @SuppressWarnings("serial")
    private class NoteLabel extends JLabel {
        public NoteLabel(final String txt0) {
            super(txt0);
            setFont(FSkin.getItalicFont(12));
            setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        }
    }

    /**
     * A JTextField plus a "codeString" property, that stores keycodes for the
     * shortcut. Also, an action listener that handles translation of keycodes
     * into characters and (dis)assembly of keycode stack.
     */
    @SuppressWarnings("serial")
    public class KeyboardShortcutField extends JTextField {
        private String codeString;

        /**
         * A JTextField plus a "codeString" property, that stores keycodes for
         * the shortcut. Also, an action listener that handles translation of
         * keycodes into characters and (dis)assembly of keycode stack.
         * 
         * @param shortcut0 &emsp; Shortcut object
         */
        public KeyboardShortcutField(final Shortcut shortcut0) {
            super();
            this.setEditable(false);
            this.setFont(FSkin.getFont(14));
            final FPref prefKey = shortcut0.getPrefKey();
            reload(prefKey);

            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent evt) {
                    KeyboardShortcuts.addKeyCode(evt);
                }
            });

            this.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(final FocusEvent evt) {
                    KeyboardShortcutField.this.setBackground(FSkin.getColor(FSkin.Colors.CLR_ACTIVE));
                }

                @Override
                public void focusLost(final FocusEvent evt) {
                    Singletons.getModel().getPreferences().setPref(prefKey, getCodeString());
                    Singletons.getModel().getPreferences().save();
                    shortcut0.attach();
                    KeyboardShortcutField.this.setBackground(Color.white);
                }
            });
        }

        public void reload(FPref prefKey) {
            this.setCodeString(Singletons.getModel().getPreferences().getPref(prefKey));
        }

        /**
         * Gets the code string.
         * 
         * @return String
         */
        public final String getCodeString() {
            return this.codeString;
        }

        /**
         * Sets the code string.
         * 
         * @param str0
         *            &emsp; The new code string (space delimited)
         */
        public final void setCodeString(final String str0) {
            if ("null".equals(str0)) {
                return;
            }

            this.codeString = str0.trim();

            final List<String> codes = new ArrayList<String>(Arrays.asList(this.codeString.split(" ")));
            final List<String> displayText = new ArrayList<String>();

            for (final String s : codes) {
                if (!s.isEmpty()) {
                    displayText.add(KeyEvent.getKeyText(Integer.valueOf(s)));
                }
            }

            this.setText(StringUtils.join(displayText, ' '));
        }
    }
    
    /** @return {@link javax.swing.JCheckBox} */
    public final JCheckBox getCbCompactMainMenu() {
        return cbCompactMainMenu;
    }    

    /** @return {@link javax.swing.JCheckBox} */
    public final JCheckBox getCbRemoveSmall() {
        return cbRemoveSmall;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public final JCheckBox getCbSingletons() {
        return cbSingletons;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbRemoveArtifacts() {
        return cbRemoveArtifacts;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbUploadDraft() {
        return cbUploadDraft;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbStackLand() {
        return cbStackLand;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbOverlayCardName() {
        return cbOverlayCardName;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbOverlayCardPower() {
        return cbOverlayCardPower;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbOverlayCardManaCost() {
        return cbOverlayCardManaCost;
    }
    
    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbRandomFoil() {
        return cbRandomFoil;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbRandomizeArt() {
        return cbRandomizeArt;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbAnte() {
        return cbAnte;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbManaBurn() {
        return cbManaBurn;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbScaleLarger() {
        return cbScaleLarger;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbDevMode() {
        return cbDevMode;
    }
        
    public FComboBoxPanel<String> getAiProfilesComboBoxPanel() {
        return cbpAiProfiles;
    }

    public FComboBoxPanel<GameLogEntryType> getGameLogVerbosityComboBoxPanel() {
        return cbpGameLogEntryType;
    }
  
    public FComboBoxPanel<String> getSkinsComboBoxPanel() {
        return cbpSkin;
    }
               
    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbEnforceDeckLegality() {
        return cbEnforceDeckLegality;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbCloneImgSource() {
        return cbCloneImgSource;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbEnableSounds() {
        return cbEnableSounds;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbAltSoundSystem() {
	return cbAltSoundSystem;
    }

    public final JCheckBox getCbUiForTouchScreen() {
        return cbUiForTouchScreen;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public FLabel getBtnReset() {
        return btnReset;
    }
    
    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbShowMatchBackgroundImage() {
        return cbShowMatchBackgroundImage;
    }    

    //========== Overridden from IVDoc

    public final FLabel getBtnDeleteMatchUI() {
        return btnDeleteMatchUI;
    }

    public final FLabel getBtnDeleteEditorUI() {
        return btnDeleteEditorUI;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_PREFERENCES;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getTabLabel()
     */
    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getLayoutControl()
     */
    @Override
    public CSubmenuPreferences getLayoutControl() {
        return CSubmenuPreferences.SINGLETON_INSTANCE;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#setParentCell(forge.gui.framework.DragCell)
     */
    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getParentCell()
     */
    @Override
    public DragCell getParentCell() {
        return parentCell;
    }
}
