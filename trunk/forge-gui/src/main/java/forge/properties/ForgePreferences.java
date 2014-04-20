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

import forge.ai.AiProfileUtil;
import forge.game.GameLogEntryType;

public class ForgePreferences extends PreferencesStore<ForgePreferences.FPref> {
    /**
     * Preference identifiers, and their default values.
     */
    public static enum FPref {
        PLAYER_NAME (""),
        CONSTRUCTED_P1_DECK_STATE(""),
        CONSTRUCTED_P2_DECK_STATE(""),
        CONSTRUCTED_P3_DECK_STATE(""),
        CONSTRUCTED_P4_DECK_STATE(""),
        CONSTRUCTED_P5_DECK_STATE(""),
        CONSTRUCTED_P6_DECK_STATE(""),
        CONSTRUCTED_P7_DECK_STATE(""),
        CONSTRUCTED_P8_DECK_STATE(""),
        UI_COMPACT_MAIN_MENU ("false"),
        UI_USE_OLD ("false"),
        UI_RANDOM_FOIL ("false"),
        UI_ENABLE_AI_CHEATS ("false"),
        UI_AVATARS ("0,1"),
        UI_SHOW_CARD_OVERLAYS ("true"),
        UI_OVERLAY_CARD_NAME ("true"),
        UI_OVERLAY_CARD_POWER ("true"),
        UI_OVERLAY_CARD_MANA_COST ("true"),
        UI_OVERLAY_CARD_ID ("true"),
        UI_OVERLAY_FOIL_EFFECT ("true"),
        UI_HIDE_REMINDER_TEXT ("false"),
        UI_UPLOAD_DRAFT ("false"),
        UI_SCALE_LARGER ("true"),
        UI_RANDOM_ART_IN_POOLS ("true"),
        UI_COMPACT_PROMPT ("false"),
        UI_CARD_SIZE ("small"),
        UI_BUGZ_NAME (""),
        UI_BUGZ_PWD (""),
        UI_ANTE ("false"),
        UI_ANTE_MATCH_RARITY ("false"),
        UI_MANABURN("false"),
        UI_SKIN ("Default"),
        UI_PREFERRED_AVATARS_ONLY ("false"),
        UI_TARGETING_OVERLAY ("false"),
        UI_ENABLE_SOUNDS ("true"),
        UI_ALT_SOUND_SYSTEM ("false"),
        UI_CURRENT_AI_PROFILE (AiProfileUtil.AI_PROFILE_RANDOM_MATCH),
        UI_CLONE_MODE_SOURCE ("false"), /** */
        UI_MATCH_IMAGE_VISIBLE ("true"),
        UI_THEMED_COMBOBOX ("true"),                // Now applies to all theme settings, not just Combo.
        UI_LOCK_TITLE_BAR ("false"),
        UI_HIDE_GAME_TABS ("false"),                // Visibility of tabs in match screen.
        UI_CLOSE_ACTION ("NONE"),

        UI_FOR_TOUCHSCREN("false"),

        MATCHPREF_PROMPT_FREE_BLOCKS("false"),

        SUBMENU_CURRENTMENU ("CONSTRUCTED"),
        SUBMENU_SANCTIONED ("true"),
        SUBMENU_GAUNTLET ("false"),
        SUBMENU_VARIANT ("false"),
        SUBMENU_QUEST ("false"),
        SUBMENU_SETTINGS ("false"),
        SUBMENU_UTILITIES ("false"),

        ENFORCE_DECK_LEGALITY ("true"),

        DEV_MODE_ENABLED ("false"),
//        DEV_MILLING_LOSS ("true"),
        DEV_UNLIMITED_LAND ("false"),
        DEV_LOG_ENTRY_TYPE (GameLogEntryType.DAMAGE.toString()),

        DECK_DEFAULT_CARD_LIMIT ("4"),
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
        super(ForgeConstants.MAIN_PREFS_FILE, FPref.class);
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

    // was not used anywhere else
    public static boolean NET_CONN = false;

    /** The Constant DevMode. */
    // one for normal mode, one for quest mode
    public static boolean DEV_MODE;
    /** The Constant UpldDrft. */
    public static boolean UPLOAD_DRAFT;
}
