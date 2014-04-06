package forge.itemmanager.filters;

import com.google.common.base.Predicate;

import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.deck.DeckProxy;
import forge.itemmanager.ItemManager;
import forge.itemmanager.SItemManagerUtil.StatTypes;
import forge.util.BinaryUtil;
import forge.util.ItemPool;


public class DeckColorFilter extends StatTypeFilter<DeckProxy> {
    public DeckColorFilter(ItemManager<? super DeckProxy> itemManager0) {
        super(itemManager0);
    }

    @Override
    public ItemFilter<DeckProxy> createCopy() {
        return new DeckColorFilter(itemManager);
    }

    @Override
    protected void buildWidget(Widget widget) {
        addToggleButton(widget, StatTypes.DECK_WHITE);
        addToggleButton(widget, StatTypes.DECK_BLUE);
        addToggleButton(widget, StatTypes.DECK_BLACK);
        addToggleButton(widget, StatTypes.DECK_RED);
        addToggleButton(widget, StatTypes.DECK_GREEN);
        addToggleButton(widget, StatTypes.DECK_COLORLESS);
        addToggleButton(widget, StatTypes.DECK_MULTICOLOR);
    }

    @Override
    protected final Predicate<DeckProxy> buildPredicate() {
        return new Predicate<DeckProxy>() {
            @Override
            public boolean apply(DeckProxy input) {
                byte colorProfile = input.getColor().getColor();
                if (colorProfile == 0) {
                    return buttonMap.get(StatTypes.DECK_COLORLESS).isSelected();
                }

                boolean wantMulticolor = buttonMap.get(StatTypes.DECK_MULTICOLOR).isSelected();
                if (!wantMulticolor && BinaryUtil.bitCount(colorProfile) > 1) {
                    return false;
                }

                byte colors = 0;
                if (buttonMap.get(StatTypes.DECK_WHITE).isSelected()) {
                    colors |= MagicColor.WHITE;
                }
                if (buttonMap.get(StatTypes.DECK_BLUE).isSelected()) {
                    colors |= MagicColor.BLUE;
                }
                if (buttonMap.get(StatTypes.DECK_BLACK).isSelected()) {
                    colors |= MagicColor.BLACK;
                }
                if (buttonMap.get(StatTypes.DECK_RED).isSelected()) {
                    colors |= MagicColor.RED;
                }
                if (buttonMap.get(StatTypes.DECK_GREEN).isSelected()) {
                    colors |= MagicColor.GREEN;
                }

                if (colors == 0 && wantMulticolor && BinaryUtil.bitCount(colorProfile) > 1) {
                    return true;
                }

                return (colorProfile & colors) == colorProfile;
            }
        };
    }

    private static final Predicate<DeckProxy> IS_WHITE = new Predicate<DeckProxy>() {
        @Override
        public boolean apply(final DeckProxy deck) {
            ColorSet cs = deck.getColor();
            return cs != null && cs.hasAnyColor(MagicColor.WHITE);
        }
    };
    private static final Predicate<DeckProxy> IS_BLUE = new Predicate<DeckProxy>() {
        @Override
        public boolean apply(final DeckProxy deck) {
            ColorSet cs = deck.getColor();
            return cs != null && cs.hasAnyColor(MagicColor.BLUE);
        }
    };
    public static final Predicate<DeckProxy> IS_BLACK = new Predicate<DeckProxy>() {
        @Override
        public boolean apply(final DeckProxy deck) {
            ColorSet cs = deck.getColor();
            return cs != null && cs.hasAnyColor(MagicColor.BLACK);
        }
    };
    public static final Predicate<DeckProxy> IS_RED = new Predicate<DeckProxy>() {
        @Override
        public boolean apply(final DeckProxy deck) {
            ColorSet cs = deck.getColor();
            return cs != null && cs.hasAnyColor(MagicColor.RED);
        }
    };
    public static final Predicate<DeckProxy> IS_GREEN = new Predicate<DeckProxy>() {
        @Override
        public boolean apply(final DeckProxy deck) {
            ColorSet cs = deck.getColor();
            return cs != null && cs.hasAnyColor(MagicColor.GREEN);
        }
    };
    private static final Predicate<DeckProxy> IS_COLORLESS = new Predicate<DeckProxy>() {
        @Override
        public boolean apply(final DeckProxy deck) {
            ColorSet cs = deck.getColor();
            return cs != null && cs.getColor() == 0;
        }
    };
    private static final Predicate<DeckProxy> IS_MULTICOLOR = new Predicate<DeckProxy>() {
        @Override
        public boolean apply(final DeckProxy deck) {
            ColorSet cs = deck.getColor();
            return cs != null && BinaryUtil.bitCount(cs.getColor()) > 1;
        }
    };

    @Override
    public void afterFiltersApplied() {
        final ItemPool<? super DeckProxy> items = itemManager.getFilteredItems();

        buttonMap.get(StatTypes.DECK_WHITE).setText(String.valueOf(items.countAll(IS_WHITE, DeckProxy.class)));
        buttonMap.get(StatTypes.DECK_BLUE).setText(String.valueOf(items.countAll(IS_BLUE, DeckProxy.class)));
        buttonMap.get(StatTypes.DECK_BLACK).setText(String.valueOf(items.countAll(IS_BLACK, DeckProxy.class)));
        buttonMap.get(StatTypes.DECK_RED).setText(String.valueOf(items.countAll(IS_RED, DeckProxy.class)));
        buttonMap.get(StatTypes.DECK_GREEN).setText(String.valueOf(items.countAll(IS_GREEN, DeckProxy.class)));
        buttonMap.get(StatTypes.DECK_COLORLESS).setText(String.valueOf(items.countAll(IS_COLORLESS, DeckProxy.class)));
        buttonMap.get(StatTypes.DECK_MULTICOLOR).setText(String.valueOf(items.countAll(IS_MULTICOLOR, DeckProxy.class)));

        getWidget().revalidate();
    }
}
