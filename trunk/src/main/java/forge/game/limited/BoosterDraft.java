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
package forge.game.limited;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.base.Supplier;
import forge.Card;
import forge.Constant.Preferences;
import forge.Singletons;
import forge.card.CardBlock;
import forge.card.CardEdition;
import forge.card.IUnOpenedProduct;
import forge.card.SealedProductTemplate;
import forge.card.UnOpenedProduct;
import forge.deck.Deck;
import forge.gui.GuiChoose;
import forge.item.PaperCard;
import forge.item.IPaperCard;
import forge.item.ItemPool;
import forge.item.ItemPoolView;
import forge.properties.NewConstants;
import forge.util.FileUtil;
import forge.util.HttpUtil;
import forge.util.storage.IStorageView;

/**
 * 
 * Booster Draft Format.
 * 
 */
public final class BoosterDraft implements IBoosterDraft {
    private final BoosterDraftAI draftAI = new BoosterDraftAI();
    private static final int N_PLAYERS = 8;

    private int nextBoosterGroup = 0;
    private int currentBoosterSize = 0;
    private int currentBoosterPick = 0;
    private List<List<PaperCard>> pack; // size 8

    /** The draft picks. */
    private final Map<String, Float> draftPicks = new TreeMap<String, Float>();
    private final LimitedPoolType draftFormat;

    private final List<Supplier<List<PaperCard>>> product = new ArrayList<Supplier<List<PaperCard>>>();

    /**
     * <p>
     * Constructor for BoosterDraft_1.
     * </p>
     * 
     * @param draftType
     *            a {@link java.lang.String} object.
     */
    public BoosterDraft(final LimitedPoolType draftType) {
        this.draftAI.setBd(this);
        this.draftFormat = draftType;

        switch (draftType) {
        case Full: // Draft from all cards in Forge
            Supplier<List<PaperCard>> s = new UnOpenedProduct(SealedProductTemplate.genericBooster);

            for (int i = 0; i < 3; i++) this.product.add(s);
            IBoosterDraft.LAND_SET_CODE[0] = CardEdition.getRandomSetWithAllBasicLands(Singletons.getModel().getEditions());
            break;

        case Block: case FantasyBlock: // Draft from cards by block or set

            List<CardBlock> blocks = new ArrayList<CardBlock>();
            IStorageView<CardBlock> storage = draftType == LimitedPoolType.Block 
                    ? Singletons.getModel().getBlocks() : Singletons.getModel().getFantasyBlocks();

            for (CardBlock b : storage) {
                if( b.getCntBoostersDraft() > 0)
                    blocks.add(b);
            }

            final CardBlock block = GuiChoose.one("Choose Block", blocks);

            final CardEdition[] cardSets = block.getSets();
            final Stack<String> sets = new Stack<String>();
            for (int k = cardSets.length - 1; k >= 0; --k) {
                sets.add(cardSets[k].getCode());
            }

            for(String setCode : block.getMetaSetNames() ) {
                if ( block.getMetaSet(setCode).isDraftable() )
                    sets.push(setCode); // to the beginning
            }

            final int nPacks = block.getCntBoostersDraft();

            if (sets.size() > 1) {
                final Object p = GuiChoose.one("Choose Set Combination", getSetCombos(sets));
                final String[] pp = p.toString().split("/");
                for (int i = 0; i < nPacks; i++) {
                    this.product.add(block.getBooster(pp[i]));
                }
            } else {
                IUnOpenedProduct product1 = block.getBooster(sets.get(0));

                for (int i = 0; i < nPacks; i++) {
                    this.product.add(product1);
                }
            }

            IBoosterDraft.LAND_SET_CODE[0] = block.getLandSet();
            break;

        case Custom:
            final List<CustomLimited> myDrafts = this.loadCustomDrafts("res/draft/", ".draft");

            if (myDrafts.isEmpty()) {
                JOptionPane
                        .showMessageDialog(null, "No custom draft files found.", "", JOptionPane.INFORMATION_MESSAGE);
            } else {
                final CustomLimited draft = GuiChoose.one("Choose Custom Draft",
                        myDrafts);
                this.setupCustomDraft(draft);
            }
            break;
        default:
            throw new NoSuchElementException("Draft for mode " + draftType + " has not been set up!");
        }

        this.pack = this.get8BoosterPack();
    }

    private void setupCustomDraft(final CustomLimited draft) {
        final ItemPoolView<PaperCard> dPool = draft.getCardPool();
        if (dPool == null) {
            throw new RuntimeException("BoosterGenerator : deck not found");
        }

        final SealedProductTemplate tpl = draft.getSealedProductTemplate();

        UnOpenedProduct toAdd = new UnOpenedProduct(tpl, dPool);
        toAdd.setLimitedPool(draft.isSingleton());
        for (int i = 0; i < draft.getNumPacks(); i++) {
            this.product.add(toAdd);
        }

        IBoosterDraft.LAND_SET_CODE[0] = Singletons.getModel().getEditions().get(draft.getLandSetCode());
    }

    /** Looks for res/draft/*.draft files, reads them, returns a list. */
    private List<CustomLimited> loadCustomDrafts(final String lookupFolder, final String fileExtension) {
        String[] dList;
        final ArrayList<CustomLimited> customs = new ArrayList<CustomLimited>();

        // get list of custom draft files
        final File dFolder = new File(lookupFolder);
        if (!dFolder.exists()) {
            throw new RuntimeException("BoosterDraft : folder not found -- folder is " + dFolder.getAbsolutePath());
        }

        if (!dFolder.isDirectory()) {
            throw new RuntimeException("BoosterDraft : not a folder -- " + dFolder.getAbsolutePath());
        }

        dList = dFolder.list();

        for (final String element : dList) {
            if (element.endsWith(fileExtension)) {
                final List<String> dfData = FileUtil.readFile(lookupFolder + element);
                customs.add(CustomLimited.parse(dfData, Singletons.getModel().getDecks().getCubes()));
            }
        }
        return customs;
    }

    /**
     * <p>
     * nextChoice.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    @Override
    public ItemPoolView<PaperCard> nextChoice() {
        if (this.pack.get(this.getCurrentBoosterIndex()).size() == 0) {
            this.pack = this.get8BoosterPack();
        }

        this.computerChoose();
        return ItemPool.createFrom(this.pack.get(this.getCurrentBoosterIndex()), PaperCard.class);
    }

    /**
     * <p>
     * get8BoosterPack.
     * </p>
     * 
     * @return an array of {@link forge.CardList} objects.
     */
    public List<List<PaperCard>> get8BoosterPack() {
        if (this.nextBoosterGroup >= this.product.size()) {
            return null;
        }

        final List<List<PaperCard>> list = new ArrayList<List<PaperCard>>();
        for (int i = 0; i < 8; i++) {
            list.add(this.product.get(this.nextBoosterGroup).get());
        }

        this.nextBoosterGroup++;
        this.currentBoosterSize = list.get(0).size();
        this.currentBoosterPick = 0;
        return list;
    }

    // size 7, all the computers decks

    /**
     * <p>
     * getDecks.
     * </p>
     * 
     * @return an array of {@link forge.deck.Deck} objects.
     */
    @Override
    public Deck[] getDecks() {
        return this.draftAI.getDecks();
    }

    private void computerChoose() {

        final int iHumansBooster = this.getCurrentBoosterIndex();
        int iPlayer = 0;
        for (int i = 1; i < this.pack.size(); i++) {

            final List<Card> forAi = new ArrayList<Card>();
            final List<PaperCard> booster = this.pack.get((iHumansBooster + i) % this.pack.size());
            for (final IPaperCard cr : booster) {
                forAi.add(cr.getMatchingForgeCard());
            }

            final PaperCard aiPick = this.draftAI.choose(booster, iPlayer++);
            booster.remove(aiPick);
        }
    } // computerChoose()

    /**
     * 
     * Get the current booster index.
     * @return int
     */
    public int getCurrentBoosterIndex() {
        return this.currentBoosterPick % BoosterDraft.N_PLAYERS;
    }

    /**
     * <p>
     * hasNextChoice.
     * </p>
     * 
     * @return a boolean.
     */
    @Override
    public boolean hasNextChoice() {
        final boolean isLastGroup = this.nextBoosterGroup >= this.product.size();
        final boolean isBoosterDepleted = this.currentBoosterPick >= this.currentBoosterSize;
        final boolean noMoreCards = isLastGroup && isBoosterDepleted;
        return !noMoreCards;
    }

    /** {@inheritDoc} */
    @Override
    public void setChoice(final PaperCard c) {
        final List<PaperCard> thisBooster = this.pack.get(this.getCurrentBoosterIndex());

        if (!thisBooster.contains(c)) {
            throw new RuntimeException("BoosterDraft : setChoice() error - card not found - " + c
                    + " - booster pack = " + thisBooster);
        }

        if (Preferences.UPLOAD_DRAFT) {
            for (int i = 0; i < thisBooster.size(); i++) {
                final PaperCard cc = thisBooster.get(i);
                final String cnBk = cc.getName() + "|" + cc.getEdition();

                float pickValue = 0;
                if (cc.equals(c)) {
                    pickValue = thisBooster.size()
                            * (1f - (((float) this.currentBoosterPick / this.currentBoosterSize) * 2f));
                } else {
                    pickValue = 0;
                }

                if (!this.draftPicks.containsKey(cnBk)) {
                    this.draftPicks.put(cnBk, pickValue);
                } else {
                    final float curValue = this.draftPicks.get(cnBk);
                    final float newValue = (curValue + pickValue) / 2;
                    this.draftPicks.put(cnBk, newValue);
                }
            }
        }

        thisBooster.remove(c);
        this.currentBoosterPick++;
    } // setChoice()

    /** This will upload drafting picks to cardforge HQ. */
    @Override
    public void finishedDrafting() {
        if (!Preferences.UPLOAD_DRAFT || 1 >= draftPicks.size()) {
            return;
        }
        
        ArrayList<String> outDraftData = new ArrayList<String>();
        for (Entry<String, Float> key : draftPicks.entrySet()) {
            outDraftData.add(key.getValue() + "|" + key.getKey());
        }
        Collections.sort(outDraftData);
        HttpUtil.upload(NewConstants.URL_DRAFT_UPLOAD + "?fmt=" + draftFormat, outDraftData);
    }

    private List<String> getSetCombos(final List<String> setz) {
        String[] sets = setz.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        List<String> setCombos = new ArrayList<String>();
        if (sets.length >= 2) {
            setCombos.add(String.format("%s/%s/%s", sets[0], sets[0], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[0], sets[0], sets[1]));
            setCombos.add(String.format("%s/%s/%s", sets[0], sets[1], sets[1]));
            if (sets.length >= 3) {
                setCombos.add(String.format("%s/%s/%s", sets[0], sets[1], sets[2]));
                setCombos.add(String.format("%s/%s/%s", sets[0], sets[2], sets[2]));
            }
            setCombos.add(String.format("%s/%s/%s", sets[1], sets[0], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[1], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[1], sets[1], sets[1]));
            if (sets.length >= 3) {
                setCombos.add(String.format("%s/%s/%s", sets[1], sets[1], sets[2]));
                setCombos.add(String.format("%s/%s/%s", sets[1], sets[2], sets[2]));
            }
        }
        if (sets.length >= 3) {
            setCombos.add(String.format("%s/%s/%s", sets[2], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[2], sets[2], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[2], sets[2], sets[1]));
            setCombos.add(String.format("%s/%s/%s", sets[2], sets[2], sets[2]));
        } // Beyond 3, skimp on the choice configurations, or the list will be enormous!
        if (sets.length >= 4) {
            setCombos.add(String.format("%s/%s/%s", sets[3], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[3], sets[2], sets[1]));
        }
        if (sets.length >= 5) {
            setCombos.add(String.format("%s/%s/%s", sets[4], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[4], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[4], sets[2], sets[0]));
        }
        if (sets.length >= 6) {
            setCombos.add(String.format("%s/%s/%s", sets[5], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[5], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[5], sets[4], sets[3]));
            setCombos.add(String.format("%s/%s/%s", sets[5], sets[2], sets[0]));
        }
        if (sets.length >= 7) {
            setCombos.add(String.format("%s/%s/%s", sets[6], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[6], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[6], sets[5], sets[4]));
            setCombos.add(String.format("%s/%s/%s", sets[6], sets[3], sets[0]));
        }
        if (sets.length >= 8) {
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[5], sets[4]));
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[6], sets[5]));
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[3], sets[0]));
        }
        if (sets.length >= 9) {
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[5], sets[4]));
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[7], sets[6]));
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[4], sets[0]));
        }
        return setCombos;
    }
}
