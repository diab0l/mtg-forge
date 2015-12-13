/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Nate
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
package forge.planarconquest;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;

import forge.GuiBase;
import forge.assets.ISkinImage;
import forge.card.CardDb;
import forge.card.CardEdition;
import forge.card.CardEdition.CardInSet;
import forge.card.CardRules;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.deck.generation.DeckGenPool;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.planarconquest.ConquestPreferences.CQPref;
import forge.util.Aggregates;
import forge.util.collect.FCollection;
import forge.util.collect.FCollectionView;


public enum ConquestPlane {
    Alara("Alara", new String[] {
            "ALA", "CON", "ARB"
    }, new String[] {
            "Bant", "Grixis", "Jund", "Naya"
    }, new Region[] {
            new Region("Bant", "Bant Panorama", MagicColor.GREEN | MagicColor.WHITE | MagicColor.BLUE),
            new Region("Esper", "Esper Panorama", MagicColor.WHITE | MagicColor.BLUE | MagicColor.BLACK),
            new Region("Grixis", "Grixis Panorama", MagicColor.BLUE | MagicColor.BLACK | MagicColor.RED),
            new Region("Jund", "Jund Panorama", MagicColor.BLACK | MagicColor.RED | MagicColor.GREEN),
            new Region("Naya", "Naya Panorama", MagicColor.RED | MagicColor.GREEN | MagicColor.WHITE)
    }),
    Dominaria("Dominaria", new String[] {
            "ICE", "ALL", "CSP",
            "MIR", "VIS", "WTH",
            "USG", "ULG", "UDS",
            "INV", "PLS", "APC",
            "ODY", "TOR", "JUD",
            "ONS", "LGN", "SCG",
            "TSP", "PLC", "FUT"
    }, new String[] {
            "Academy at Tolaria West", "Isle of Vesuva", "Krosa", "Llanowar", "Otaria", "Shiv", "Talon Gates"
    }, new Region[] {
            new Region("Ice Age", "Dark Depths", inSet("ICE", "ALL", "CSP")),
            new Region("Mirage", "Teferi's Isle", inSet("MIR", "VIS", "WTH")),
            new Region("Urza's Saga", "Tolarian Academy", inSet("USG", "ULG", "UDS")),
            new Region("Invasion", "Legacy Weapon", inSet("INV", "PLS", "APC")),
            new Region("Odyssey", "Cabal Coffers", inSet("ODY", "TOR", "JUD")),
            new Region("Onslaught", "Grand Coliseum", inSet("ONS", "LGN", "SCG")),
            new Region("Time Spiral", "Vesuva", inSet("TSP", "TSB", "PLC", "FUT"))
    }),
    Innistrad("Innistrad", new String[] {
            "ISD", "DKA", "AVR"
    }, new String[] {
            "Gavony", "Kessig", "Nephalia"
    }, new Region[] {
            new Region("Moorland", "Moorland Haunt", MagicColor.WHITE | MagicColor.BLUE),
            new Region("Nephalia", "Nephalia Drownyard", MagicColor.BLUE | MagicColor.BLACK),
            new Region("Stensia", "Stensia Bloodhall", MagicColor.BLACK | MagicColor.RED),
            new Region("Kessig", "Kessig Wolf Run", MagicColor.RED | MagicColor.GREEN),
            new Region("Gavony", "Gavony Township", MagicColor.GREEN | MagicColor.WHITE),
    }),
    Kamigawa("Kamigawa", new String[] {
            "CHK", "BOK", "SOK"
    }, new String[] {
            "Minamo", "Orochi Colony", "Sokenzan", "Takenuma"
    }, new Region[] {
            new Region("Towabara", "Eiganjo Castle", MagicColor.WHITE),
            new Region("Minamo Academy", "Minamo, School at Water's Edge", MagicColor.BLUE),
            new Region("Takenuma", "Shizo, Death's Storehouse", MagicColor.BLACK),
            new Region("Sokenzan Mountains", "Shinka, the Bloodsoaked Keep", MagicColor.RED),
            new Region("Jukai Forest", "Okina, Temple to the Grandfathers", MagicColor.GREEN),
    }),
    LorwynShadowmoor("Lorwyn-Shadowmoor", new String[] {
            "LRW", "MOR", "SHM", "EVE"
    }, new String[] {
            "Goldmeadow", "The Great Forest", "Velis Vel", "Raven's Run", 
    }, new Region[] {
            new Region("Ancient Amphitheater", "Ancient Amphitheater", MagicColor.RED | MagicColor.WHITE),
            new Region("Auntie's Hovel", "Auntie's Hovel", MagicColor.BLACK | MagicColor.RED),
            new Region("Gilt-Leaf Palace", "Gilt-Leaf Palace", MagicColor.BLACK | MagicColor.GREEN),
            new Region("Murmuring Bosk", "Murmuring Bosk", MagicColor.WHITE | MagicColor.BLACK | MagicColor.GREEN),
            new Region("Primal Beyond", "Primal Beyond", ColorSet.ALL_COLORS.getColor()),
            new Region("Rustic Clachan", "Rustic Clachan", MagicColor.GREEN | MagicColor.WHITE),
            new Region("Secluded Glen", "Secluded Glen", MagicColor.BLUE | MagicColor.BLACK),
            new Region("Wanderwine Hub", "Wanderwine Hub", MagicColor.WHITE | MagicColor.BLUE),
    }),
    Mercadia("Mercadia", new String[] {
            "MMQ", "NEM", "PCY"
    }, new String[] {
            "Cliffside Market"
    }, new Region[] {
            new Region("Fountain of Cho", "Fountain of Cho", MagicColor.WHITE),
            new Region("Saprazzan Cove", "Saprazzan Cove", MagicColor.BLUE),
            new Region("Subterranean Hangar", "Subterranean Hangar", MagicColor.BLACK),
            new Region("Mercadian Bazaar", "Mercadian Bazaar", MagicColor.RED),
            new Region("Rushwood Grove", "Rushwood Grove", MagicColor.GREEN)
    }),
    Mirrodin("Mirrodin", new String[] {
            "MRD", "DST", "5DN", "SOM", "MBS", "NPH"
    }, new String[] {
            "Panopticon", "Quicksilver Sea", "Furnace Layer", "Norn's Dominion"
    }, new Region[] {
            new Region("Panopticon", "Darksteel Citadel", MagicColor.COLORLESS),
            new Region("Taj-Nar", "Ancient Den", MagicColor.WHITE),
            new Region("Lumengrid", "Seat of the Synod", MagicColor.BLUE),
            new Region("Ish Sah", "Vault of Whispers", MagicColor.BLACK),
            new Region("Kuldotha", "Great Furnace", MagicColor.RED),
            new Region("Tel-Jilad", "Tree of Tales", MagicColor.GREEN),
            new Region("Glimmervoid", "Glimmervoid", ColorSet.ALL_COLORS.getColor())
    }),
    Rath("Rath", new String[] {
            "TMP", "STH", "EXO"
    }, new String[] {
            "Stronghold Furnace"
    }, new Region[] {
            new Region("Caldera Lake", "Caldera Lake", MagicColor.BLUE | MagicColor.RED),
            new Region("Cinder Marsh", "Cinder Marsh", MagicColor.BLACK | MagicColor.RED),
            new Region("Mogg Hollows", "Mogg Hollows", MagicColor.RED | MagicColor.GREEN),
            new Region("Pine Barrens", "Pine Barrens", MagicColor.BLACK | MagicColor.GREEN),
            new Region("Rootwater Depths", "Rootwater Depths", MagicColor.BLUE | MagicColor.BLACK),
            new Region("Salt Flats", "Salt Flats", MagicColor.WHITE | MagicColor.BLACK),
            new Region("Scabland", "Scabland", MagicColor.RED | MagicColor.WHITE),
            new Region("Skyshroud Forest", "Skyshroud Forest", MagicColor.GREEN | MagicColor.BLUE),
            new Region("Thalakos Lowlands", "Thalakos Lowlands", MagicColor.WHITE | MagicColor.BLUE),
            new Region("Vec Townships", "Vec Townships", MagicColor.GREEN | MagicColor.WHITE)
    }),
    Ravnica("Ravnica", new String[] {
            "RAV", "GPT", "DIS", "RTR", "GTC", "DGM"
    }, new String[] {
            "Agyrem", "Grand Ossuary", "Izzet Steam Maze", "Orzhova", "Prahv", "Selesnya Loft Gardens", "Undercity Reaches"
    }, new Region[] {
            new Region("Azorius Chancery", "Azorius Chancery", MagicColor.WHITE | MagicColor.BLUE),
            new Region("Boros Garrison", "Boros Garrison", MagicColor.RED | MagicColor.WHITE),
            new Region("Dimir Aqueduct", "Dimir Aqueduct", MagicColor.BLUE | MagicColor.BLACK),
            new Region("Golgari Rot Farm", "Golgari Rot Farm", MagicColor.BLACK | MagicColor.GREEN),
            new Region("Gruul Turf", "Gruul Turf", MagicColor.RED | MagicColor.GREEN),
            new Region("Izzet Boilerworks", "Izzet Boilerworks", MagicColor.BLUE | MagicColor.RED),
            new Region("Orzhov Basilica", "Orzhov Basilica", MagicColor.WHITE | MagicColor.BLACK),
            new Region("Rakdos Carnarium", "Rakdos Carnarium", MagicColor.BLACK | MagicColor.RED),
            new Region("Selesnya Sanctuary", "Selesnya Sanctuary", MagicColor.GREEN | MagicColor.WHITE),
            new Region("Simic Growth Chamber", "Simic Growth Chamber", MagicColor.GREEN | MagicColor.BLUE)
    }),
    Shandalar("Shandalar", new String[] {
            "2ED", "3ED", "4ED", "ARN", "ATQ", "LEG", "DRK"
    }, new String[] {
            "Eloren Wilds", "Onakke Catacomb"
    }, new Region[] {
            new Region("Core", "Black Lotus", inSet("2ED", "3ED", "4ED")),
            new Region("Arabian Nights", "Library of Alexandria", inSet("ARN")),
            new Region("Antiquities", "Mishra's Workshop", inSet("ATQ")),
            new Region("Legends", "Karakas", inSet("LEG")),
            new Region("The Dark", "City of Shadows", inSet("DRK"))
    }),
    Tarkir("Tarkir", new String[] {
            "KTK", "FRF", "DTK"
    }, new String[] {
            "Kharasha Foothills"
    }, new Region[] {
            new Region("Abzan Houses", "Sandsteppe Citadel", MagicColor.WHITE | MagicColor.BLACK | MagicColor.GREEN),
            new Region("Jeskai Way", "Mystic Monastery", MagicColor.BLUE | MagicColor.RED | MagicColor.WHITE),
            new Region("Mardu Horde", "Nomad Outpost", MagicColor.RED | MagicColor.WHITE | MagicColor.BLACK),
            new Region("Sultai Brood", "Opulent Palace", MagicColor.BLACK | MagicColor.GREEN | MagicColor.BLUE),
            new Region("Temur Frontier", "Frontier Bivouac", MagicColor.GREEN | MagicColor.BLUE | MagicColor.RED)
    }),
    Theros("Theros", new String[] {
            "THS", "BNG", "JOU"
    }, new String[] {
            "Lethe Lake"
    }, new Region[] {
            new Region("", "", inSet("THS", "BNG", "JOU"))
    }),
    Ulgrotha("Ulgrotha", new String[] {
            "HML"
    }, new String[] {
            "The Dark Barony"
    }, new Region[] {
            new Region("", "", inSet("HML"))
    }),
    Zendikar("Zendikar", new String[] {
            "ZEN", "WWK", "ROE", "BFZ"
    }, new String[] {
            "Akoum", "Hedron Fields of Agadeem", "Murasa", "Tazeem"
    }, new Region[] {
            new Region("", "", inSet("ZEN", "WWK", "ROE"))
    });

    private final String name;
    private final FCollection<CardEdition> editions = new FCollection<CardEdition>();
    private final FCollection<Region> regions;
    private final FCollection<String> bannedCards = new FCollection<String>();
    private final DeckGenPool cardPool = new DeckGenPool();
    private final FCollection<PaperCard> planeCards = new FCollection<PaperCard>();
    private final FCollection<PaperCard> commanders = new FCollection<PaperCard>();
    private AwardPool awardPool;

    private ConquestPlane(String name0, String[] setCodes0, String[] planeCards0, Region[] regions0) {
        this(name0, setCodes0, planeCards0, regions0, null);
    }
    private ConquestPlane(String name0, String[] setCodes0, String[] planeCards0, Region[] regions0, String[] bannedCards0) {
        name = name0;
        regions = new FCollection<Region>(regions0);
        if (bannedCards0 != null) {
            bannedCards.addAll(bannedCards0);
        }
        for (String setCode : setCodes0) {
            CardEdition edition = FModel.getMagicDb().getEditions().get(setCode);
            if (edition != null) {
                editions.add(edition);
                for (CardInSet card : edition.getCards()) {
                    if (!bannedCards.contains(card.name)) {
                        PaperCard pc = FModel.getMagicDb().getCommonCards().getCard(card.name, setCode);
                        if (pc != null) {
                            CardRules rules = pc.getRules();
                            boolean isCommander = pc.getRules().canBeCommander();
                            cardPool.add(pc);
                            if (isCommander) {
                                commanders.add(pc);
                            }
                            int count = 0;
                            if (!rules.getType().isBasicLand()) { //add all basic lands to all regions below
                                for (Region region : regions) {
                                    if (region.pred.apply(pc)) {
                                        region.cardPool.add(pc);
                                        if (isCommander) {
                                            region.commanders.add(pc);
                                        }
                                        count++;
                                    }
                                }
                            }
                            //if card doesn't match any region's predicate,
                            //make card available to all regions
                            if (count == 0) {
                                for (Region region : regions) {
                                    region.cardPool.add(pc);
                                    if (isCommander) {
                                        region.commanders.add(pc);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        CardDb variantCards = FModel.getMagicDb().getVariantCards();
        for (String planeCard : planeCards0) {
            PaperCard pc = variantCards.getCard(planeCard);
            if (pc == null) {
                System.out.println("\"" + planeCard + "\" does not correspond to a valid Plane card");
                continue;
            }
            planeCards.add(pc);
        }
    }

    public String getName() {
        return name;
    }

    public FCollectionView<CardEdition> getEditions() {
        return editions;
    }

    public FCollectionView<String> getBannedCards() {
        return bannedCards;
    }

    public FCollectionView<Region> getRegions() {
        return regions;
    }

    public DeckGenPool getCardPool() {
        return cardPool;
    }

    public FCollectionView<PaperCard> getCommanders() {
        return commanders;
    }

    public FCollectionView<PaperCard> getPlaneCards() {
        return planeCards;
    }

    public int getEventCount() {
        return regions.size() * Region.ROWS_PER_REGION * Region.COLS_PER_REGION;
    }

    public String toString() {
        return name;
    }

    public static class Region {
        public static final int ROWS_PER_REGION = 3;
        public static final int COLS_PER_REGION = 3;
        public static final int START_COL = (COLS_PER_REGION - 1) / 2;

        private final String name, artCardName;
        private final ColorSet colorSet;
        private final Predicate<PaperCard> pred;
        private final DeckGenPool cardPool = new DeckGenPool();
        private final FCollection<PaperCard> commanders = new FCollection<PaperCard>();

        private ISkinImage art;

        private Region(String name0, String artCardName0, final int colorMask) {
            name = name0;
            artCardName = artCardName0;
            pred = new Predicate<PaperCard>() {
                @Override
                public boolean apply(PaperCard pc) {
                    return pc.getRules().getColorIdentity().hasNoColorsExcept(colorMask);
                }
            };
            colorSet = ColorSet.fromMask(colorMask);
        }
        private Region(String name0, String artCardName0, Predicate<PaperCard> pred0) {
            name = name0;
            artCardName = artCardName0;
            pred = pred0;
            colorSet = ColorSet.fromMask(ColorSet.ALL_COLORS.getColor());
        }

        public String getName() {
            return name;
        }

        public ISkinImage getArt() {
            if (art == null) {
                art = GuiBase.getInterface().getCardArt(cardPool.getCard(artCardName));
            }
            return art;
        }

        public ColorSet getColorSet() {
            return colorSet;
        }

        public DeckGenPool getCardPool() {
            return cardPool;
        }

        public FCollectionView<PaperCard> getCommanders() {
            return commanders;
        }

        public String toString() {
            return name;
        }
    }

    private static Predicate<PaperCard> inSet(final String... sets) {
        return new Predicate<PaperCard>() {
            @Override
            public boolean apply(PaperCard pc) {
                for (String set : sets) {
                    if (pc.getEdition().equals(set)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public AwardPool getAwardPool() {
        if (awardPool == null) { //delay initializing until needed
            awardPool = new AwardPool();
        }
        return awardPool;
    }

    public class AwardPool {
        private final BoosterPool commons, uncommons, rares, mythics;
        private final int commonValue, uncommonValue, rareValue, mythicValue;

        private AwardPool() {
            Iterable<PaperCard> cards = cardPool.getAllCards();

            ConquestPreferences prefs = FModel.getConquestPreferences();

            commons = new BoosterPool();
            uncommons = new BoosterPool();
            rares = new BoosterPool();
            mythics = new BoosterPool();

            for (PaperCard c : cards) {
                switch (c.getRarity()) {
                case Common:
                    commons.add(c);
                    break;
                case Uncommon:
                    uncommons.add(c);
                    break;
                case Rare:
                case Special: //lump special cards in with rares for simplicity
                    rares.add(c);
                    break;
                case MythicRare:
                    mythics.add(c);
                    break;
                default:
                    break;
                }
            }

            //calculate odds of each rarity
            float commonOdds = commons.getOdds(prefs.getPrefInt(CQPref.BOOSTER_COMMONS));
            float uncommonOdds = uncommons.getOdds(prefs.getPrefInt(CQPref.BOOSTER_UNCOMMONS));
            int raresPerBooster = prefs.getPrefInt(CQPref.BOOSTER_RARES);
            float rareOdds = rares.getOdds(raresPerBooster);
            float mythicOdds = mythics.getOdds((float)raresPerBooster / (float)prefs.getPrefInt(CQPref.BOOSTERS_PER_MYTHIC));

            //determine value of each rarity based on the base value of a common
            commonValue = prefs.getPrefInt(CQPref.AETHER_BASE_VALUE);
            uncommonValue = Math.round(commonValue / (uncommonOdds / commonOdds));
            rareValue = Math.round(commonValue / (rareOdds / commonOdds));
            mythicValue = mythics.isEmpty() ? 0 : Math.round(commonValue / (mythicOdds / commonOdds));
        }

        public int getShardValue(PaperCard card) {
            switch (card.getRarity()) {
            case Common:
                return commonValue;
            case Uncommon:
                return uncommonValue;
            case Rare:
            case Special:
                return rareValue;
            case MythicRare:
                return mythicValue;
            default:
                return 0;
            }
        }

        public BoosterPool getCommons() {
            return commons;
        }
        public BoosterPool getUncommons() {
            return uncommons;
        }
        public BoosterPool getRares() {
            return rares;
        }
        public BoosterPool getMythics() {
            return mythics;
        }

        public class BoosterPool {
            private final List<PaperCard> cards = new ArrayList<PaperCard>();

            private BoosterPool() {
            }

            public boolean isEmpty() {
                return cards.isEmpty();
            }

            private float getOdds(float perBoosterCount) {
                int count = cards.size();
                if (count == 0) { return 0; }
                return (float)perBoosterCount / (float)count;
            }

            private void add(PaperCard c) {
                cards.add(c);
            }

            public void rewardCard(List<PaperCard> rewards) {
                int index = Aggregates.randomInt(0, cards.size() - 1);
                PaperCard c = cards.get(index);
                cards.remove(index);
                rewards.add(c);
            }
        }
    }
}
