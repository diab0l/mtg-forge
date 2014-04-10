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

import java.util.Collections;
import java.util.Map;

import forge.GuiBase;

public final class ForgeConstants {
    private static final String _INSTALL_ROOT        = GuiBase.getInterface().getInstallRoot();
    public static final String PROFILE_FILE          = _INSTALL_ROOT + "forge.profile.properties";
    public static final String PROFILE_TEMPLATE_FILE = PROFILE_FILE + ".example";

    // data that is only in the program dir
    private static final String _ASSETS_ROOT = _INSTALL_ROOT + GuiBase.getInterface().getAssetsDir();

    private static final String _LIST_DIR = _ASSETS_ROOT + "lists/";
    public static final String KEYWORD_LIST_FILE                     = _LIST_DIR + "NonStackingKWList.txt";
    public static final String TYPE_LIST_FILE                        = _LIST_DIR + "TypeLists.txt";
    public static final String IMAGE_LIST_TOKENS_FILE                = _LIST_DIR + "token-images.txt";
    public static final String IMAGE_LIST_QUEST_OPPONENT_ICONS_FILE  = _LIST_DIR + "quest-opponent-icons.txt";
    public static final String IMAGE_LIST_QUEST_PET_SHOP_ICONS_FILE  = _LIST_DIR + "quest-pet-shop-icons.txt";
    public static final String IMAGE_LIST_QUEST_TOKENS_FILE          = _LIST_DIR + "quest-pet-token-images.txt";
    public static final String IMAGE_LIST_QUEST_BOOSTERS_FILE        = _LIST_DIR + "booster-images.txt";
    public static final String IMAGE_LIST_QUEST_FATPACKS_FILE        = _LIST_DIR + "fatpack-images.txt";
    public static final String IMAGE_LIST_QUEST_PRECONS_FILE         = _LIST_DIR + "precon-images.txt";
    public static final String IMAGE_LIST_QUEST_TOURNAMENTPACKS_FILE = _LIST_DIR + "tournamentpack-images.txt";

    public static final String CHANGES_FILE = _INSTALL_ROOT + "CHANGES.txt";
    public static final String LICENSE_FILE = _INSTALL_ROOT + "LICENSE.txt";
    public static final String README_FILE  = _INSTALL_ROOT + "README.txt";
    public static final String HOWTO_FILE   = _ASSETS_ROOT + "howto.txt";

    public static final String DRAFT_DIR           = _ASSETS_ROOT + "draft/";
    public static final String DRAFT_RANKINGS_FILE = DRAFT_DIR + "rankings.txt";
    public static final String SEALED_DIR          = _ASSETS_ROOT + "sealed/";
    public static final String CARD_DATA_DIR       = _ASSETS_ROOT + "cardsfolder/";
    public static final String EDITIONS_DIR        = _ASSETS_ROOT + "editions/";
    public static final String BLOCK_DATA_DIR      = _ASSETS_ROOT + "blockdata/";
    public static final String DECK_CUBE_DIR       = _ASSETS_ROOT + "cube/";
    public static final String AI_PROFILE_DIR      = _ASSETS_ROOT + "ai/";
    public static final String SOUND_DIR           = _ASSETS_ROOT + "sound/";

    private static final String _QUEST_DIR            = _ASSETS_ROOT + "quest/";
    public static final String QUEST_WORLD_DIR        = _QUEST_DIR + "world/";
    public static final String QUEST_PRECON_DIR       = _QUEST_DIR + "precons/";
    public static final String PRICES_BOOSTER_FILE    = _QUEST_DIR + "booster-prices.txt";
    public static final String BAZAAR_DIR             = _QUEST_DIR + "bazaar/";
    public static final String BAZAAR_INDEX_FILE      = BAZAAR_DIR + "index.xml";
    public static final String DEFAULT_DUELS_DIR      = _QUEST_DIR + "duels";
    public static final String DEFAULT_CHALLENGES_DIR = _QUEST_DIR + "challenges";
    public static final String THEMES_DIR             = _QUEST_DIR + "themes";

    public static final String SKINS_DIR         = _ASSETS_ROOT + "skins/";
    public static final String DEFAULT_SKINS_DIR = SKINS_DIR + "default/";
    //don't associate these skin files with a directory since skin directory will be determined later
    public static final String SPRITE_ICONS_FILE     = "sprite_icons.png"; 
    public static final String SPRITE_FOILS_FILE     = "sprite_foils.png";
    public static final String SPRITE_OLD_FOILS_FILE = "sprite_old_foils.png";
    public static final String SPRITE_AVATARS_FILE   = "sprite_avatars.png";
    public static final String FONT_FILE             = "font1.ttf";
    public static final String SPLASH_BG_FILE        = "bg_splash.png";
    public static final String MATCH_BG_FILE         = "bg_match.jpg";
    public static final String TEXTURE_BG_FILE       = "bg_texture.jpg";

    // data tree roots
    public static final String USER_DIR;
    public static final String CACHE_DIR;
    public static final String CACHE_CARD_PICS_DIR;
    public static final Map<String, String> CACHE_CARD_PICS_SUBDIR;
    public static final int SERVER_PORT_NUMBER;
    static {
        ForgeProfileProperties profileProps = new ForgeProfileProperties(PROFILE_FILE);
        USER_DIR           = profileProps.userDir;
        CACHE_DIR          = profileProps.cacheDir;
        CACHE_CARD_PICS_DIR = profileProps.cardPicsDir;
        CACHE_CARD_PICS_SUBDIR = Collections.unmodifiableMap(profileProps.cardPicsSubDir);
        SERVER_PORT_NUMBER = profileProps.serverPort;
    }

    // data that is only in the profile dirs
    public static final String USER_QUEST_DIR       = USER_DIR + "quest/";
    public static final String USER_PREFS_DIR       = USER_DIR + "preferences/";
    public static final String USER_GAMES_DIR       = USER_DIR + "games/";
    public static final String LOG_FILE             = USER_DIR + "forge.log";
    public static final String DECK_BASE_DIR        = USER_DIR + "decks/";
    public static final String DECK_CONSTRUCTED_DIR = DECK_BASE_DIR + "constructed/";
    public static final String DECK_DRAFT_DIR       = DECK_BASE_DIR + "draft/";
    public static final String DECK_SEALED_DIR      = DECK_BASE_DIR + "sealed/";
    public static final String DECK_SCHEME_DIR      = DECK_BASE_DIR + "scheme/";
    public static final String DECK_PLANE_DIR       = DECK_BASE_DIR + "planar/";
    public static final String DECK_COMMANDER_DIR   = DECK_BASE_DIR + "commander/";
    public static final String QUEST_SAVE_DIR       = USER_QUEST_DIR + "saves/";
    public static final String MAIN_PREFS_FILE      = USER_PREFS_DIR + "forge.preferences";
    public static final String CARD_PREFS_FILE      = USER_PREFS_DIR + "card.preferences";
    public static final String DECK_PREFS_FILE      = USER_PREFS_DIR + "deck.preferences";
    public static final String QUEST_PREFS_FILE     = USER_PREFS_DIR + "quest.preferences";
    public static final String ITEM_VIEW_PREFS_FILE = USER_PREFS_DIR + "item_view.preferences";

    // data that has defaults in the program dir but overrides/additions in the user dir
    private static final String _DEFAULTS_DIR = _ASSETS_ROOT + "defaults/";
    public static final String NO_CARD_FILE   = _DEFAULTS_DIR + "no_card.jpg";
    public static final FileLocation WINDOW_LAYOUT_FILE      = new FileLocation(_DEFAULTS_DIR, USER_PREFS_DIR, "window.xml");
    public static final FileLocation MATCH_LAYOUT_FILE       = new FileLocation(_DEFAULTS_DIR, USER_PREFS_DIR, "match.xml");
    public static final FileLocation WORKSHOP_LAYOUT_FILE    = new FileLocation(_DEFAULTS_DIR, USER_PREFS_DIR, "workshop.xml");
    public static final FileLocation EDITOR_LAYOUT_FILE      = new FileLocation(_DEFAULTS_DIR, USER_PREFS_DIR, "editor.xml");
    public static final FileLocation GAUNTLET_DIR            = new FileLocation(_DEFAULTS_DIR, USER_DIR,       "gauntlet/");

    // data that is only in the cached dir
    private static final String _PICS_DIR                    = CACHE_DIR + "pics/";
    public static final String DB_DIR                        = CACHE_DIR + "db/";
    public static final String CACHE_TOKEN_PICS_DIR          = _PICS_DIR + "tokens/";
    public static final String CACHE_ICON_PICS_DIR           = _PICS_DIR + "icons/";
    public static final String CACHE_SYMBOLS_DIR             = _PICS_DIR + "symbols/";
    public static final String CACHE_BOOSTER_PICS_DIR        = _PICS_DIR + "boosters/";
    public static final String CACHE_FATPACK_PICS_DIR        = _PICS_DIR + "fatpacks/";
    public static final String CACHE_PRECON_PICS_DIR         = _PICS_DIR + "precons/";
    public static final String CACHE_TOURNAMENTPACK_PICS_DIR = _PICS_DIR + "tournamentpacks/";
    public static final String QUEST_CARD_PRICE_FILE         = DB_DIR + "all-prices.txt";

    public static final String[] PROFILE_DIRS = {
            USER_DIR,
            CACHE_DIR,
            CACHE_CARD_PICS_DIR,
            USER_PREFS_DIR,
            GAUNTLET_DIR.userPrefLoc,
            DB_DIR,
            DECK_CONSTRUCTED_DIR,
            DECK_DRAFT_DIR,
            DECK_SEALED_DIR,
            DECK_SCHEME_DIR,
            DECK_PLANE_DIR,
            QUEST_SAVE_DIR,
            CACHE_TOKEN_PICS_DIR,
            CACHE_ICON_PICS_DIR,
            CACHE_BOOSTER_PICS_DIR,
            CACHE_FATPACK_PICS_DIR,
            CACHE_PRECON_PICS_DIR,
            CACHE_TOURNAMENTPACK_PICS_DIR };

    // URLs
    private static final String _URL_CARDFORGE = "http://cardforge.org";
    public static final String URL_DRAFT_UPLOAD   = _URL_CARDFORGE + "/draftAI/submitDraftData.php";
    public static final String URL_PIC_DOWNLOAD   = _URL_CARDFORGE + "/fpics/";
    public static final String URL_PRICE_DOWNLOAD = _URL_CARDFORGE + "/MagicInfo/pricegen.php";
}
