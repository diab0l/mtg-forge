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
package forge.properties;

import java.util.List;

import forge.Constant;
import forge.Constant.Preferences;
import forge.game.ai.AiProfileUtil;
import forge.gui.home.EMenuItem;
import forge.gui.match.VMatchUI;
import forge.gui.match.nonsingleton.VField;
import forge.gui.match.views.VDev;

public class ForgePreferences extends PreferencesStore<ForgePreferences.FPref> {
    /** 
     * Preference identifiers, and their default values.
     */
    public static enum FPref {
        UI_USE_OLD ("false"),
        UI_RANDOM_FOIL ("false"),
        UI_SMOOTH_LAND ("false"),
        UI_AVATARS ("0,1"),
        UI_CARD_OVERLAY ("true"),
        UI_UPLOAD_DRAFT ("false"),
        UI_SCALE_LARGER ("true"),
        UI_MAX_STACK ("3"),
        UI_STACK_OFFSET ("tiny"),
        UI_CARD_SIZE ("small"),
        UI_BUGZ_NAME (""),
        UI_BUGZ_PWD (""),
        UI_ANTE ("false"),
        UI_MANABURN("false"),
        UI_SKIN ("default"),
        UI_PREFERRED_AVATARS_ONLY ("false"),
        UI_TARGETING_OVERLAY ("false"),
        UI_ENABLE_SOUNDS ("true"),
        UI_ALT_SOUND_SYSTEM ("false"),
        UI_RANDOM_CARD_ART ("false"),
        UI_CURRENT_AI_PROFILE (AiProfileUtil.AI_PROFILE_RANDOM_MATCH),
        UI_CLONE_MODE_SOURCE ("false"), /** */

        SUBMENU_CURRENTMENU (EMenuItem.CONSTRUCTED.toString()),
        SUBMENU_SANCTIONED ("false"),
        SUBMENU_GAUNTLET ("false"),
        SUBMENU_VARIANT ("false"),
        SUBMENU_QUEST ("false"),
        SUBMENU_SETTINGS ("false"),
        SUBMENU_UTILITIES ("false"),

        ENFORCE_DECK_LEGALITY ("true"),

        DEV_MODE_ENABLED ("false"),
        DEV_MILLING_LOSS ("true"),
        DEV_UNLIMITED_LAND ("false"),

        DECKGEN_SINGLETONS ("false"),
        DECKGEN_ARTIFACTS ("false"),
        DECKGEN_NOSMALL ("false"),

        PHASE_AI_UPKEEP ("true"),
        PHASE_AI_DRAW ("true"),
        PHASE_AI_MAIN1 ("true"),
        PHASE_AI_BEGINCOMBAT ("true"),
        PHASE_AI_DECLAREATTACKERS ("true"),
        PHASE_AI_DECLAREBLOCKERS ("true"),
        PHASE_AI_FIRSTSTRIKE ("true"),
        PHASE_AI_COMBATDAMAGE ("true"),
        PHASE_AI_ENDCOMBAT ("true"),
        PHASE_AI_MAIN2 ("true"),
        PHASE_AI_EOT ("true"),
        PHASE_AI_CLEANUP ("true"),

        PHASE_HUMAN_UPKEEP ("true"),
        PHASE_HUMAN_DRAW ("true"),
        PHASE_HUMAN_MAIN1 ("true"),
        PHASE_HUMAN_BEGINCOMBAT ("true"),
        PHASE_HUMAN_DECLAREATTACKERS ("true"),
        PHASE_HUMAN_DECLAREBLOCKERS ("true"),
        PHASE_HUMAN_FIRSTSTRIKE ("true"),
        PHASE_HUMAN_COMBATDAMAGE ("true"),
        PHASE_HUMAN_ENDCOMBAT ("true"),
        PHASE_HUMAN_MAIN2 ("true"),
        PHASE_HUMAN_EOT ("true"),
        PHASE_HUMAN_CLEANUP ("true"),

        SHORTCUT_SHOWSTACK ("83"),
        SHORTCUT_SHOWCOMBAT ("67"),
        SHORTCUT_SHOWCONSOLE ("76"),
        SHORTCUT_SHOWPLAYERS ("80"),
        SHORTCUT_SHOWDEV ("68"),
        SHORTCUT_CONCEDE ("17"),
        SHORTCUT_ENDTURN ("69"),
        SHORTCUT_ALPHASTRIKE ("65"),
        SHORTCUT_SHOWTARGETING ("84");

        private final String strDefaultVal;

        /** @param s0 &emsp; {@link java.lang.String} */
        FPref(String s0) {
            this.strDefaultVal = s0;
        }

        /** @return {@link java.lang.String} */
        public String getDefault() {
            return strDefaultVal;
        }
    }

    public static enum CardSizeType {
        tiny, smaller, small, medium, large, huge
    }

   
    public static enum StackOffsetType {
        tiny, small, medium, large
    }

   
    public static enum HomeMenus {
        constructed, draft, sealed, quest, settings, utilities
    }

    /** Instantiates a ForgePreferences object. */
    public ForgePreferences() {
        super(NewConstants.MAIN_PREFS_FILE, FPref.class);
    }

    /**
     * TODO: Needs to be reworked for efficiency with rest of prefs saves in
     * codebase.
     */
    public void writeMatchPreferences() {
        final List<VField> fieldViews = VMatchUI.SINGLETON_INSTANCE.getFieldViews();

        // AI field is at index [1]
        this.setPref(FPref.PHASE_AI_UPKEEP, String.valueOf(fieldViews.get(1).getLblUpkeep().getEnabled()));
        this.setPref(FPref.PHASE_AI_DRAW, String.valueOf(fieldViews.get(1).getLblDraw().getEnabled()));
        this.setPref(FPref.PHASE_AI_MAIN1, String.valueOf(fieldViews.get(1).getLblMain1().getEnabled()));
        this.setPref(FPref.PHASE_AI_BEGINCOMBAT, String.valueOf(fieldViews.get(1).getLblBeginCombat().getEnabled()));
        this.setPref(FPref.PHASE_AI_DECLAREATTACKERS,
                String.valueOf(fieldViews.get(1).getLblDeclareAttackers().getEnabled()));
        this.setPref(FPref.PHASE_AI_DECLAREBLOCKERS,
                String.valueOf(fieldViews.get(1).getLblDeclareBlockers().getEnabled()));
        this.setPref(FPref.PHASE_AI_FIRSTSTRIKE, String.valueOf(fieldViews.get(1).getLblFirstStrike().getEnabled()));
        this.setPref(FPref.PHASE_AI_COMBATDAMAGE, String.valueOf(fieldViews.get(1).getLblCombatDamage().getEnabled()));
        this.setPref(FPref.PHASE_AI_ENDCOMBAT, String.valueOf(fieldViews.get(1).getLblEndCombat().getEnabled()));
        this.setPref(FPref.PHASE_AI_MAIN2, String.valueOf(fieldViews.get(1).getLblMain2().getEnabled()));
        this.setPref(FPref.PHASE_AI_EOT, String.valueOf(fieldViews.get(1).getLblEndTurn().getEnabled()));
        this.setPref(FPref.PHASE_AI_CLEANUP, String.valueOf(fieldViews.get(1).getLblCleanup().getEnabled()));

        // Human field is at index [0]
        this.setPref(FPref.PHASE_HUMAN_UPKEEP, String.valueOf(fieldViews.get(0).getLblUpkeep().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_DRAW, String.valueOf(fieldViews.get(0).getLblDraw().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_MAIN1, String.valueOf(fieldViews.get(0).getLblMain1().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_BEGINCOMBAT, String.valueOf(fieldViews.get(0).getLblBeginCombat().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_DECLAREATTACKERS,
                String.valueOf(fieldViews.get(0).getLblDeclareAttackers().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_DECLAREBLOCKERS,
                String.valueOf(fieldViews.get(0).getLblDeclareBlockers().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_FIRSTSTRIKE, String.valueOf(fieldViews.get(0).getLblFirstStrike().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_COMBATDAMAGE, String.valueOf(fieldViews.get(0).getLblCombatDamage().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_ENDCOMBAT, String.valueOf(fieldViews.get(0).getLblEndCombat().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_MAIN2, String.valueOf(fieldViews.get(0).getLblMain2().getEnabled()));
        this.setPref(FPref.PHASE_HUMAN_EOT, fieldViews.get(0).getLblEndTurn().getEnabled());
        this.setPref(FPref.PHASE_HUMAN_CLEANUP, fieldViews.get(0).getLblCleanup().getEnabled());

        final VDev v = VDev.SINGLETON_INSTANCE;

        this.setPref(FPref.DEV_MILLING_LOSS, v.getLblMilling().getEnabled());
        this.setPref(FPref.DEV_UNLIMITED_LAND, v.getLblUnlimitedLands().getEnabled());
    }

    /**
     * TODO: Needs to be reworked for efficiency with rest of prefs saves in
     * codebase.
     */
    public void actuateMatchPreferences() {
        final List<VField> fieldViews = VMatchUI.SINGLETON_INSTANCE.getFieldViews();

        Preferences.DEV_MODE = this.getPrefBoolean(FPref.DEV_MODE_ENABLED);
        Preferences.UPLOAD_DRAFT = Constant.Runtime.NET_CONN && this.getPrefBoolean(FPref.UI_UPLOAD_DRAFT);

        // AI field is at index [0]
        fieldViews.get(1).getLblUpkeep().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_UPKEEP));
        fieldViews.get(1).getLblDraw().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_DRAW));
        fieldViews.get(1).getLblMain1().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_MAIN1));
        fieldViews.get(1).getLblBeginCombat().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_BEGINCOMBAT));
        fieldViews.get(1).getLblDeclareAttackers().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_DECLAREATTACKERS));
        fieldViews.get(1).getLblDeclareBlockers().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_DECLAREBLOCKERS));
        fieldViews.get(1).getLblFirstStrike().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_FIRSTSTRIKE));
        fieldViews.get(1).getLblCombatDamage().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_COMBATDAMAGE));
        fieldViews.get(1).getLblEndCombat().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_ENDCOMBAT));
        fieldViews.get(1).getLblMain2().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_MAIN2));
        fieldViews.get(1).getLblEndTurn().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_EOT));
        fieldViews.get(1).getLblCleanup().setEnabled(this.getPrefBoolean(FPref.PHASE_AI_CLEANUP));

        // Human field is at index [1]
        fieldViews.get(0).getLblUpkeep().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_UPKEEP));
        fieldViews.get(0).getLblDraw().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_DRAW));
        fieldViews.get(0).getLblMain1().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_MAIN1));
        fieldViews.get(0).getLblBeginCombat().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_BEGINCOMBAT));
        fieldViews.get(0).getLblDeclareAttackers().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_DECLAREATTACKERS));
        fieldViews.get(0).getLblDeclareBlockers().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_DECLAREBLOCKERS));
        fieldViews.get(0).getLblFirstStrike().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_FIRSTSTRIKE));
        fieldViews.get(0).getLblCombatDamage().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_COMBATDAMAGE));
        fieldViews.get(0).getLblEndCombat().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_ENDCOMBAT));
        fieldViews.get(0).getLblMain2().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_MAIN2));
        fieldViews.get(0).getLblEndTurn().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_EOT));
        fieldViews.get(0).getLblCleanup().setEnabled(this.getPrefBoolean(FPref.PHASE_HUMAN_CLEANUP));

        //Singletons.getView().getViewMatch().setLayoutParams(this.getPref(FPref.UI_LAYOUT_PARAMS));
    }

    protected FPref[] getEnumValues() {
        return FPref.values();
    }
    
    protected FPref valueOf(String name) {
        try {
            return FPref.valueOf(name);
        }
        catch (Exception e) {
            return null;
        }
    }

    protected String getPrefDefault(FPref key) {
        return key.getDefault();
    }
}
