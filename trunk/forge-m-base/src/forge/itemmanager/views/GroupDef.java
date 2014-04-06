package forge.itemmanager.views;

import com.google.common.base.Function;

import forge.card.CardType;
import forge.card.ColorSet;
import forge.deck.DeckProxy;
import forge.item.InventoryItem;
import forge.item.PaperCard;

public enum GroupDef {
    COLOR("Color", getColorGroups(),
            new Function<Integer, ColumnDef>() {
                @Override
                public ColumnDef apply(final Integer groupIndex) {
                    return null;
                }
            },
            new Function<InventoryItem, Integer>() {
                @Override
                public Integer apply(final InventoryItem item) {
                    if (item instanceof PaperCard) {
                        return getColorGroup(((PaperCard) item).getRules().getColor());
                    }
                    else if (item instanceof DeckProxy) {
                        return getColorGroup(((DeckProxy) item).getColor());
                    }
                    return -1;
                }
            }),
    COLOR_IDENTITY("Color Identity", getColorGroups(),
            new Function<Integer, ColumnDef>() {
                @Override
                public ColumnDef apply(final Integer groupIndex) {
                    return null;
                }
            },
            new Function<InventoryItem, Integer>() {
                @Override
                public Integer apply(final InventoryItem item) {
                    if (item instanceof PaperCard) {
                        return getColorGroup(((PaperCard) item).getRules().getColorIdentity());
                    }
                    else if (item instanceof DeckProxy) {
                        return getColorGroup(((DeckProxy) item).getColorIdentity());
                    }
                    return -1;
                }
            }),
    CREATURE_SPELL_LAND("Creatures/Spells/Lands",
            new String[] { "Creatures", "Spells", "Lands" },
            new Function<Integer, ColumnDef>() {
                @Override
                public ColumnDef apply(final Integer groupIndex) {
                    if (groupIndex == 2) {
                        return ColumnDef.NAME; //pile lands by name regardless
                    }
                    return null;
                }
            },
            new Function<InventoryItem, Integer>() {
                @Override
                public Integer apply(final InventoryItem item) {
                    if (item instanceof PaperCard) {
                        CardType type = ((PaperCard) item).getRules().getType();
                        if (type.isCreature()) {
                            return 0;
                        }
                        if (type.isLand()) { //make Artifact Lands appear in Lands group
                            return 2;
                        }
                        if (type.isArtifact() || type.isEnchantment() || type.isPlaneswalker() || type.isInstant() || type.isSorcery()) {
                            return 1;
                        }
                    }
                    return -1;
                }
            }),
    CARD_TYPE("Type",
            new String[] { "Creatures", "Artifacts", "Enchantments", "Planeswalkers", "Instants", "Sorceries", "Lands" },
            new Function<Integer, ColumnDef>() {
                @Override
                public ColumnDef apply(final Integer groupIndex) {
                    if (groupIndex == 6) {
                        return ColumnDef.NAME; //pile lands by name regardless
                    }
                    return null;
                }
            },
            new Function<InventoryItem, Integer>() {
                @Override
                public Integer apply(final InventoryItem item) {
                    if (item instanceof PaperCard) {
                        CardType type = ((PaperCard) item).getRules().getType();
                        if (type.isCreature()) { //make Artifact and Land Creatures appear in Creatures group
                            return 0;
                        }
                        if (type.isLand()) { //make Artifact Lands appear in Lands group
                            return 6;
                        }
                        if (type.isArtifact()) {
                            return 1;
                        }
                        if (type.isEnchantment()) {
                            return 2;
                        }
                        if (type.isPlaneswalker()) {
                            return 3;
                        }
                        if (type.isInstant()) {
                            return 4;
                        }
                        if (type.isSorcery()) {
                            return 5;
                        }
                    }
                    return -1;
                }
            }),
    CARD_RARITY("Rarity",
            new String[] { "Mythic Rares", "Rares", "Uncommons", "Commons", "Basic Lands" },
            new Function<Integer, ColumnDef>() {
                @Override
                public ColumnDef apply(final Integer groupIndex) {
                    return null;
                }
            },
            new Function<InventoryItem, Integer>() {
                @Override
                public Integer apply(final InventoryItem item) {
                    if (item instanceof PaperCard) {
                        switch (((PaperCard) item).getRarity()) {
                        case MythicRare:
                            return 0;
                        case Rare:
                            return 1;
                        case Uncommon:
                            return 2;
                        case Common:
                            return 3;
                        case BasicLand:
                            return 4;
                        default:
                            return -1; //show Special and Unknown in "Other" group
                        }
                    }
                    return -1;
                }
            });

    GroupDef(String name0, String[] groups0, Function<Integer, ColumnDef> fnGetPileByOverride0, Function<InventoryItem, Integer> fnGroupItem0) {
        this.name = name0;
        this.groups = groups0;
        this.fnGetPileByOverride = fnGetPileByOverride0;
        this.fnGroupItem = fnGroupItem0;
    }

    private final String name;
    private final String[] groups;
    private final Function<Integer, ColumnDef> fnGetPileByOverride;
    private final Function<InventoryItem, Integer> fnGroupItem;

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String[] getGroups() {
        return this.groups;
    }

    public ColumnDef getGroupPileBy(int groupIndex, ColumnDef defaultPileBy) {
        ColumnDef pileBy = this.fnGetPileByOverride.apply(groupIndex);
        if (pileBy == null) {
            return defaultPileBy;
        }
        return pileBy;
    }

    public int getItemGroupIndex(InventoryItem item) {
        return this.fnGroupItem.apply(item);
    }

    private static String[] getColorGroups() {
        //TODO: Support breaking up Multicolor into separate groups for each color combination
        return new String[] { "White", "Blue", "Black", "Red", "Green", "Multicolor", "Colorless" };
    }

    private static Integer getColorGroup(ColorSet color) {
        if (color.isColorless()) {
            return 6;
        }
        if (color.isMulticolor()) {
            return 5;
        }
        if (color.hasWhite()) {
            return 0;
        }
        if (color.hasBlue()) {
            return 1;
        }
        if (color.hasBlack()) {
            return 2;
        }
        if (color.hasRed()) {
            return 3;
        }
        if (color.hasGreen()) {
            return 4;
        }
        return -1; //shouldn't happen
    }
}
