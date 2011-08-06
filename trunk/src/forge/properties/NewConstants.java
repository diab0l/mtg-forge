
package forge.properties;


/**
 * NewConstants.java
 * 
 * Created on 22.08.2009
 */


/**
 * property keys
 * 
 * @version V0.0 22.08.2009
 * @author Clemens Koza
 */
public interface NewConstants {
    //General properties
    public static final String MAIL          = "program/mail";
    public static final String FORUM         = "program/forum";
    public static final String VERSION       = "program/version";
    
    public static final String DECKS         = "decks";
    public static final String BOOSTER_DECKS = "booster-decks";
    public static final String NEW_DECKS     = "decks-dir";
    
    public static final String TOKENS        = "tokens";
    public static final String CARD_PICTURES = "card-pictures";
    public static final String CARDS         = "cards";
    public static final String REMOVED       = "removed-cards";
    public static final String NAME_MUTATOR  = "name-mutator";
    
    public static final String IMAGE_BASE    = "image/base";
    
    /**
     * properties for regular game
     */
    public static interface REGULAR {
        public static final String COMMON   = "regular/common";
        public static final String UNCOMMON = "regular/uncommon";
        public static final String RARE     = "regular/rare";
    }
    
    /**
     * properties for quest game
     */
    public static interface QUEST {
        public static final String COMMON   = "quest/common";
        public static final String UNCOMMON = "quest/uncommon";
        public static final String RARE     = "quest/rare";
        
        public static final String EASY     = "quest/easy";
        public static final String MEDIUM   = "quest/medium";
        public static final String HARD     = "quest/hard";
        
        public static final String DATA     = "quest/data";
    }
    
    /**
     * gui-related properties
     */
    public static interface GUI {
        public static interface GuiDisplay {
            public static final String LAYOUT = "gui/Display";
        }
        
        public static interface GuiDeckEditor {
            public static final String LAYOUT = "gui/DeckEditor";
        }
    }
    
    /**
     * Localization properties
     */
    public static interface LANG {
        public static final String PROGRAM_NAME = "%s/program/name";
        public static final String LANGUAGE     = "lang";
        
        public static interface HowTo {
            public static final String TITLE   = "%s/HowTo/title";
            public static final String MESSAGE = "%s/HowTo/message";
        }
        
        public static interface ErrorViewer {
            public static final String SHOW_ERROR   = "%s/ErrorViewer/show";
            
            public static final String TITLE        = "%s/ErrorViewer/title";
            public static final String MESSAGE      = "%s/ErrorViewer/message";
            public static final String BUTTON_SAVE  = "%s/ErrorViewer/button/save";
            public static final String BUTTON_CLOSE = "%s/ErrorViewer/button/close";
            public static final String BUTTON_EXIT  = "%s/ErrorViewer/button/exit";
            
            public static interface ERRORS {
                public static final String SAVE_MESSAGE = "%s/ErrorViewer/errors/save/message";
                public static final String SHOW_MESSAGE = "%s/ErrorViewer/errors/show/message";
            }
        }
        
        public static interface Gui_BoosterDraft {
            public static final String CLOSE_MESSAGE      = "%s/BoosterDraft/close/message";
            public static final String SAVE_MESSAGE       = "%s/BoosterDraft/save/message";
            public static final String SAVE_TITLE         = "%s/BoosterDraft/save/title";
            public static final String RENAME_MESSAGE     = "%s/BoosterDraft/rename/message";
            public static final String RENAME_TITLE       = "%s/BoosterDraft/rename/title";
            public static final String SAVE_DRAFT_MESSAGE = "%s/BoosterDraft/saveDraft/message";
            public static final String SAVE_DRAFT_TITLE   = "%s/BoosterDraft/saveDraft/title";
        }
        
        public static interface GuiDisplay {
            public static interface MENU_BAR {
                public static interface MENU {
                    public static final String TITLE = "%s/Display/menu/title";
                }
            }
            
            public static final String HUMAN_TITLE = "%s/Display/human/title";
            
            public static interface HUMAN_HAND {
                public static final String TITLE = "%s/Display/human/hand/title";
            }
            
            public static interface HUMAN_LIBRARY {
                public static final String TITLE = "%s/Display/human/library/title";
            }
            
            public static final String HUMAN_GRAVEYARD = "%s/Display/human/graveyard";
            
            public static interface HUMAN_GRAVEYARD {
                public static final String TITLE  = "%s/Display/human/graveyard/title";
                public static final String BUTTON = "%s/Display/human/graveyard/button";
                public static final String MENU   = "%s/Display/human/graveyard/menu";
            }
            
            public static final String HUMAN_REMOVED = "%s/Display/human/removed";
            
            public static interface HUMAN_REMOVED {
                public static final String TITLE  = "%s/Display/human/removed/title";
                public static final String BUTTON = "%s/Display/human/removed/button";
                public static final String MENU   = "%s/Display/human/removed/menu";
            }
            
            public static final String COMBAT          = "%s/Display/combat/title";
            
            public static final String HUMAN_FLASHBACK = "%s/Display/human/flashback";
            
            public static interface HUMAN_FLASHBACK {
                public static final String TITLE  = "%s/Display/human/flashback/title";
                public static final String BUTTON = "%s/Display/human/flashback/button";
                public static final String MENU   = "%s/Display/human/flashback/menu";
            }
            
            public static final String COMPUTER_TITLE = "%s/Display/computer/title";
            
            public static interface COMPUTER_HAND {
                public static final String TITLE = "%s/Display/computer/hand/title";
            }
            
            public static interface COMPUTER_LIBRARY {
                public static final String TITLE = "%s/Display/computer/library/title";
            }
            
            
            public static final String COMPUTER_GRAVEYARD = "%s/Display/human/graveyard";
            
            public static interface COMPUTER_GRAVEYARD {
                public static final String TITLE  = "%s/Display/computer/graveyard/title";
                public static final String BUTTON = "%s/Display/computer/graveyard/button";
                public static final String MENU   = "%s/Display/computer/graveyard/menu";
            }
            
            
            public static final String COMPUTER_REMOVED = "%s/Display/human/removed";
            
            public static interface COMPUTER_REMOVED {
                public static final String TITLE  = "%s/Display/computer/removed/title";
                public static final String BUTTON = "%s/Display/computer/removed/button";
                public static final String MENU   = "%s/Display/computer/removed/menu";
            }
            
            public static final String CONCEDE = "%s/Display/concede";
            
            public static interface CONCEDE {
                public static final String BUTTON = "%s/Display/concede/button";
                public static final String MENU   = "%s/Display/concede/menu";
            }
        }
        
        public static interface Gui_DownloadPictures {
            public static final String TITLE            = "%s/DownloadPictures/title";
            
            public static final String PROXY_ADDRESS    = "%s/DownloadPictures/proxy/address";
            public static final String PROXY_PORT       = "%s/DownloadPictures/proxy/port";
            
            public static final String NO_PROXY         = "%s/DownloadPictures/proxy/type/none";
            public static final String HTTP_PROXY       = "%s/DownloadPictures/proxy/type/http";
            public static final String SOCKS_PROXY      = "%s/DownloadPictures/proxy/type/socks";
            
            public static final String NO_MORE          = "%s/DownloadPictures/no-more";
            
            public static final String BAR_BEFORE_START = "%s/DownloadPictures/bar/before-start";
            public static final String BAR_WAIT         = "%s/DownloadPictures/bar/wait";
            public static final String BAR_CLOSE        = "%s/DownloadPictures/bar/close";
            
            public static interface BUTTONS {
                public static final String START  = "%s/DownloadPictures/button/start";
                public static final String CANCEL = "%s/DownloadPictures/button/cancel";
                public static final String CLOSE  = "%s/DownloadPictures/button/close";
            }
            
            public static interface ERRORS {
                public static final String PROXY_CONNECT = "%s/DownloadPictures/errors/proxy/connect";
                public static final String OTHER         = "%s/DownloadPictures/errors/other";
            }
        }
        
        public static interface Gui_NewGame {
            public static interface MENU_BAR {
                public static interface MENU {
                    public static final String TITLE      = "%s/NewGame/menu/title";
                    public static final String LF         = "%s/NewGame/menu/lookAndFeel";
                    public static final String DOWNLOAD   = "%s/NewGame/menu/download";
                    public static final String CARD_SIZES = "%s/NewGame/menu/cardSizes";
                    public static final String ABOUT      = "%s/NewGame/menu/about";
                }
                
                public static interface OPTIONS {
                    public static final String TITLE = "%s/NewGame/options/title";
                    
                    public static interface GENERATE {
                        public static final String TITLE            = "%s/NewGame/options/generate/title";
                        public static final String REMOVE_SMALL     = "%s/NewGame/options/generate/removeSmall";
                        public static final String REMOVE_ARTIFACTS = "%s/NewGame/options/generate/removeArtifacts";
                    }
                }
            }
            
            public static interface ERRORS {}
        }
    }
}
