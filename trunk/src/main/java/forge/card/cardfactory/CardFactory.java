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
package forge.card.cardfactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import forge.Card;
import forge.CardCharacteristicName;
import forge.CardUtil;
import forge.Singletons;
import forge.card.CardRules;
import forge.card.cost.Cost;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellPermanent;
import forge.card.spellability.Target;
import forge.error.ErrorViewer;
import forge.game.player.ComputerUtil;
import forge.game.player.Player;
import forge.gui.GuiUtils;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;

/**
 * <p>
 * AbstractCardFactory class.
 * </p>
 * 
 * TODO The map field contains Card instances that have not gone through
 * getCard2, and thus lack abilities. However, when a new Card is requested via
 * getCard, it is this map's values that serve as the templates for the values
 * it returns. This class has another field, allCards, which is another copy of
 * the card database. These cards have abilities attached to them, and are owned
 * by the human player by default. <b>It would be better memory-wise if we had
 * only one or the other.</b> We may experiment in the future with using
 * allCard-type values for the map instead of the less complete ones that exist
 * there today.
 * 
 * @author Forge
 * @version $Id$
 */
public class CardFactory implements CardFactoryInterface {

    /**
     * <p>
     * Constructor for CardFactory.
     * </p>
     * 
     * @param file
     *            a {@link java.io.File} object.
     */
    private final CardReader reader;

    public CardFactory(final File file) {

        GuiUtils.checkEDT("CardFactory$constructor", false);
        reader = new CardReader(ForgeProps.getFile(NewConstants.CARDSFOLDER), true);
        try {
            // this fills in our map of card names to Card instances.
            final List<CardRules> listCardRules = reader.loadCards();
            CardDb.setup(listCardRules.iterator());

        } catch (final Exception ex) {
            ErrorViewer.showError(ex);
        }

    } // constructor


    /**
     * <p>
     * copyCard.
     * </p>
     * 
     * @param in
     *            a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    @Override
    public final Card copyCard(final Card in) {
        final CardCharacteristicName curState = in.getCurState();
        if (in.isInAlternateState()) {
            in.setState(CardCharacteristicName.Original);
        }
        final Card out = this.getCard(CardDb.instance().getCard(in), in.getOwner());
        out.setUniqueNumber(in.getUniqueNumber());

        CardFactoryUtil.copyCharacteristics(in, out);
        if (in.hasAlternateState()) {
            for (final CardCharacteristicName state : in.getStates()) {
                in.setState(state);
                if (state == CardCharacteristicName.Cloner) {
                    out.addAlternateState(state);
                }
                out.setState(state);
                CardFactoryUtil.copyCharacteristics(in, out);
            }
            in.setState(curState);
            out.setState(curState);
        }

        // I'm not sure if we really should be copying enchant/equip stuff over.
        out.setEquipping(in.getEquipping());
        out.setEquippedBy(in.getEquippedBy());
        out.setEnchantedBy(in.getEnchantedBy());
        out.setEnchanting(in.getEnchanting());
        out.setClones(in.getClones());
        for (final Object o : in.getRemembered()) {
            out.addRemembered(o);
        }
        for (final Card o : in.getImprinted()) {
            out.addImprinted(o);
        }

        return out;

    }

    /**
     * <p>
     * copySpellontoStack.
     * </p>
     * 
     * @param source
     *            a {@link forge.Card} object.
     * @param original
     *            a {@link forge.Card} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param bCopyDetails
     *            a boolean.
     */
    @Override
    public final void copySpellontoStack(final Card source, final Card original, final SpellAbility sa,
            final boolean bCopyDetails) {
        Player originalController = original.getController();
        Player controller = sa.getActivatingPlayer();
        final Card c = Singletons.getModel().getCardFactory().copyCard(original);

        // change the color of the copy (eg: Fork)
        // Currently won't work for abilities, only for spells
        if (null != source.getFirstSpellAbility() && null != source.getFirstSpellAbility().getAbilityFactory()) {
            final SpellAbility sourceSA = source.getFirstSpellAbility();
            if (sourceSA.getAbilityFactory().getMapParams().containsKey("CopyIsColor")) {
                String tmp = "";
                final Map<String, String> params = sourceSA.getAbilityFactory().getMapParams();
                final String newColor = params.get("CopyIsColor");
                if (newColor.equals("ChosenColor")) {
                    tmp = CardUtil.getShortColorsString(source.getChosenColor());
                } else {
                    tmp = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(newColor.split(","))));
                }
                final String finalColors = tmp;

                c.addColor(finalColors, c, !params.containsKey("OverwriteColors"), true);
            }
        }

        c.addController(controller);
        c.setCopiedSpell(true);
        c.refreshUniqueNumber();

        final SpellAbility copySA = sa.copy();
        //remove all costs
        copySA.setPayCosts(new Cost(c, "", sa.isAbility()));
        if (sa.getTarget() != null) {
            Target target = new Target(sa.getTarget());
            target.setSourceCard(c);
            copySA.setTarget(target);
        }
        copySA.setSourceCard(c);

        if (bCopyDetails) {
            c.addXManaCostPaid(original.getXManaCostPaid());
            c.addMultiKickerMagnitude(original.getMultiKickerMagnitude());
            if (original.getOptionalAdditionalCostsPaid() != null) {
                for (String cost : original.getOptionalAdditionalCostsPaid()) {
                    c.addOptionalAdditionalCostsPaid(cost);
                }
            }
            c.addReplicateMagnitude(original.getReplicateMagnitude());
            if (sa.isReplicate()) {
                copySA.setIsReplicate(true);
            }
        }

        if (controller.isHuman()) {
            Singletons.getModel().getGame().getAction().playSpellAbilityForFree(copySA);
        } else if (copySA instanceof Spell) {
            Spell spell = (Spell) copySA;
            if (spell.canPlayFromEffectAI(false, true)) {
                ComputerUtil.playStackFree(controller, copySA);
            }
        } else if (copySA.canPlayAI()) {
            ComputerUtil.playStackFree(controller, copySA);
        }

        c.addController(originalController);
    }

    /**
     * <p>
     * getCard.
     * </p>
     * 
     * @param cardName
     *            a {@link java.lang.String} object.
     * @param owner
     *            a {@link forge.game.player.Player} object.
     * @return a {@link forge.Card} instance, owned by owner; or the special
     *         blankCard
     */
    @Override
    public final Card getCard(final CardPrinted cp, final Player owner) {

        //System.out.println(cardName);
        Card c = this.getCard2(cp.getCard().getCardScript(), owner);

        if (c != null) {
            c.setCurSetCode(cp.getEdition());
            c.setRandomPicture(cp.getArtIndex() + 1);
            c.setImageFilename(cp.getImageFilename());

            if (c.hasAlternateState()) {
                if (c.isFlipCard()) {
                    c.setState(CardCharacteristicName.Flipped);
                }
                if (c.isDoubleFaced()) {
                    c.setState(CardCharacteristicName.Transformed);
                }
                c.setCurSetCode(cp.getEdition());
                c.setImageFilename(CardUtil.buildFilename(c));
                c.setState(CardCharacteristicName.Original);
            }
        }
        // else throw "Unsupported card";
        return c;

    }

    protected Card getCard2(final Iterable<String> script, final Player owner) {
        final Card card = CardReader.readCard(script);
        card.setOwner(owner);
        buildAbilities(card);
        return card;
    }

    public static Card getCard2(final Card o, final Player owner) {
        final Card copy = CardFactoryUtil.copyStats(o);
        copy.setOwner(owner);
        buildAbilities(copy);
        return copy;
    }

    private static void buildAbilities(final Card card) {
        final String cardName = card.getName();

        if (!card.isCardColorsOverridden()) {
            card.addColor(card.getManaCost().toString());
        }
        // may have to change the spell

        // this is so permanents like creatures and artifacts have a "default"
        // spell
        if (card.isPermanent() && !card.isLand() && !card.isAura()) {
            card.addSpellAbility(new SpellPermanent(card));
        }

        CardFactoryUtil.parseKeywords(card, cardName);

        for (final CardCharacteristicName state : card.getStates()) {
            if (card.isDoubleFaced() && state == CardCharacteristicName.FaceDown) {
                continue; // Ignore FaceDown for DFC since they have none.
            }
            card.setState(state);
            CardFactoryUtil.addAbilityFactoryAbilities(card);
            final ArrayList<String> stAbs = card.getStaticAbilityStrings();
            if (stAbs.size() > 0) {
                for (int i = 0; i < stAbs.size(); i++) {
                    card.addStaticAbility(stAbs.get(i));
                }
            }
        }

        card.setState(CardCharacteristicName.Original);

        // ******************************************************************
        // ************** Link to different CardFactories *******************

        if (card.isCreature()) {
            CardFactoryCreatures.buildCard(card, cardName);
        } else if (card.isAura()) {
            CardFactoryAuras.buildCard(card, cardName);
        } else if (card.isEquipment()) {
            CardFactoryEquipment.buildCard(card, cardName);
        } else if (card.isPlaneswalker()) {
            CardFactoryPlaneswalkers.buildCard(card, cardName);
        } else if (card.isLand()) {
            CardFactoryLands.buildCard(card, cardName);
        } else if (card.isInstant()) {
            CardFactoryInstants.buildCard(card, cardName);
        } else if (card.isSorcery()) {
            CardFactorySorceries.buildCard(card, cardName);
        } else if (card.isEnchantment()) {
            CardFactoryEnchantments.buildCard(card, cardName);
        } else if (card.isArtifact()) {
            CardFactoryArtifacts.buildCard(card, cardName);
        }

        CardFactoryUtil.postFactoryKeywords(card);
    } // getCard2
} // end class AbstractCardFactory
