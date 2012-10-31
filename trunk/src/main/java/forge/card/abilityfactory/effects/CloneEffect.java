package forge.card.abilityfactory.effects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import forge.Card;
import forge.CardCharacteristicName;
import forge.CardUtil;
import forge.GameActionUtil;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.AbilityFactoryAttach;
import forge.card.abilityfactory.SpellEffect;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;

public class CloneEffect extends SpellEffect {
    // TODO update this method

    @Override
    protected String getStackDescription(java.util.Map<String,String> params, SpellAbility sa) {
         final StringBuilder sb = new StringBuilder();
    
        if (sa instanceof AbilitySub) {
            sb.append(" ");
        } else {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        }
    
        final Target tgt = sa.getTarget();
        ArrayList<Card> tgts;
        if (tgt != null) {
            tgts = tgt.getTargetCards();
        } else {
            tgts = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);
        }
    
        sb.append(sa.getSourceCard());
        sb.append(" becomes a copy of ");
        if (!tgts.isEmpty()) {
          sb.append(tgts.get(0)).append(".");
        }
        else {
          sb.append("target creature.");
        }
        return sb.toString();
    } // end cloneStackDescription()

    /**
     * <p>
     * cloneResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    @Override
    public void resolve(java.util.Map<String,String> params, SpellAbility sa) {
        Card tgtCard;
        final Card host = sa.getSourceCard();
        Map<String, String> origSVars = host.getSVars();


        // find cloning source i.e. thing to be copied
        Card cardToCopy = null;
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            cardToCopy = tgt.getTargetCards().get(0);
        }
        else if (params.containsKey("Defined")) {
            ArrayList<Card> cloneSources = AbilityFactory.getDefinedCards(host, params.get("Defined"), sa);
            if (!cloneSources.isEmpty()) {
                cardToCopy = cloneSources.get(0);
            }
        }
        if (cardToCopy == null) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("Do you want to copy " + cardToCopy + "?");
        boolean optional = params.containsKey("Optional");
        if (host.getController().isHuman() && optional
                && !GameActionUtil.showYesNoDialog(host, sb.toString())) {
            return;
        }

        // find target of cloning i.e. card becoming a clone
        ArrayList<Card> cloneTargets = AbilityFactory.getDefinedCards(host, params.get("CloneTarget"), sa);
        if (!cloneTargets.isEmpty()) {
            tgtCard = cloneTargets.get(0);
        }
        else {
            tgtCard = host;
        }

        String imageFileName = host.getImageFilename();

        boolean keepName = params.containsKey("KeepName");
        String originalName = tgtCard.getName();
        boolean copyingSelf = (tgtCard == cardToCopy);

        if (!copyingSelf) {
            if (tgtCard.isCloned()) { // cloning again
                tgtCard.switchStates(CardCharacteristicName.Cloner, CardCharacteristicName.Original);
                tgtCard.setState(CardCharacteristicName.Original);
                tgtCard.clearStates(CardCharacteristicName.Cloner);
            }
            // add "Cloner" state to clone
            tgtCard.addAlternateState(CardCharacteristicName.Cloner);
            tgtCard.switchStates(CardCharacteristicName.Original, CardCharacteristicName.Cloner);
            tgtCard.setState(CardCharacteristicName.Original);
        }
        else {
            //copy Original state to Cloned
            tgtCard.addAlternateState(CardCharacteristicName.Cloned);
            tgtCard.switchStates(CardCharacteristicName.Original, CardCharacteristicName.Cloned);
            if (tgtCard.isFlipCard()) {
                tgtCard.setState(CardCharacteristicName.Original);
            }
        }

        CardCharacteristicName stateToCopy = null;
        if (copyingSelf) {
            stateToCopy = CardCharacteristicName.Cloned;
        }
        else if (cardToCopy.isFlipCard()) {
            stateToCopy = CardCharacteristicName.Original;
        }
        else {
            stateToCopy = cardToCopy.getCurState();
        }

        CardFactoryUtil.copyState(cardToCopy, stateToCopy, tgtCard);
        // must call this before addAbilityFactoryAbilities so cloned added abilities are handled correctly
        addExtraCharacteristics(tgtCard, params, origSVars);
        CardFactoryUtil.addAbilityFactoryAbilities(tgtCard);
        for (int i = 0; i < tgtCard.getStaticAbilityStrings().size(); i++) {
            tgtCard.addStaticAbility(tgtCard.getStaticAbilityStrings().get(i));
        }
        if (keepName) {
            tgtCard.setName(originalName);
        }

        // If target is a flipped card, also copy the flipped
        // state.
        if (cardToCopy.isFlipCard()) {
            if (!copyingSelf) {
                tgtCard.addAlternateState(CardCharacteristicName.Flipped);
                tgtCard.setState(CardCharacteristicName.Flipped);
            }
            CardFactoryUtil.copyState(cardToCopy, CardCharacteristicName.Flipped, tgtCard);
            addExtraCharacteristics(tgtCard, params, origSVars);
            CardFactoryUtil.addAbilityFactoryAbilities(tgtCard);
            for (int i = 0; i < tgtCard.getStaticAbilityStrings().size(); i++) {
                tgtCard.addStaticAbility(tgtCard.getStaticAbilityStrings().get(i));
            }
            if (keepName) {
                tgtCard.setName(originalName);
            }
            tgtCard.setFlipCard(true);
            //keep the Clone card image for the cloned card
            tgtCard.setImageFilename(imageFileName);

            if (!tgtCard.isFlipped()) {
              tgtCard.setState(CardCharacteristicName.Original);
            }
        } else {
            tgtCard.setFlipCard(false);
        }

        //Clean up copy of cloned state
        if (copyingSelf) {
            tgtCard.clearStates(CardCharacteristicName.Cloned);
        }

        //Clear Remembered and Imprint lists
        tgtCard.clearRemembered();
        tgtCard.clearImprinted();

        //keep the Clone card image for the cloned card
        tgtCard.setImageFilename(imageFileName);

        // check if clone is now an Aura that needs to be attached
        if (tgtCard.isAura()) {
            AbilityFactoryAttach.attachAuraOnIndirectEnterBattlefield(tgtCard);
        }

    } // cloneResolve

    private void addExtraCharacteristics(final Card tgtCard, final Map<String, String> params, final Map<String, String> origSVars) {
        // additional types to clone
        if (params.containsKey("AddTypes")) {
           for (final String type : Arrays.asList(params.get("AddTypes").split(","))) {
               tgtCard.addType(type);
           }
        }

        // triggers to add to clone
        final ArrayList<String> triggers = new ArrayList<String>();
        if (params.containsKey("AddTriggers")) {
            triggers.addAll(Arrays.asList(params.get("AddTriggers").split(",")));
            for (final String s : triggers) {
                if (origSVars.containsKey(s)) {
                    final String actualTrigger = origSVars.get(s);
                    final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, tgtCard, true);
                    tgtCard.addTrigger(parsedTrigger);
                }
            }
        }

        // SVars to add to clone
        if (params.containsKey("AddSVars")) {
            for (final String s : Arrays.asList(params.get("AddSVars").split(","))) {
                if (origSVars.containsKey(s)) {
                    final String actualsVar = origSVars.get(s);
                    tgtCard.setSVar(s, actualsVar);
                }
            }
        }

        // abilities to add to clone
        if (params.containsKey("AddAbilities")) {
            for (final String s : Arrays.asList(params.get("AddAbilities").split(","))) {
                if (origSVars.containsKey(s)) {
                    //final AbilityFactory newAF = new AbilityFactory();
                    final String actualAbility = origSVars.get(s);
                    // final SpellAbility grantedAbility = newAF.getAbility(actualAbility, tgtCard);
                    // tgtCard.addSpellAbility(grantedAbility);
                    tgtCard.getIntrinsicAbilities().add(actualAbility);
                }
            }
        }

        // keywords to add to clone
        final ArrayList<String> keywords = new ArrayList<String>();
        if (params.containsKey("AddKeywords")) {
            keywords.addAll(Arrays.asList(params.get("AddKeywords").split(" & ")));
            // allow SVar substitution for keywords
            for (int i = 0; i < keywords.size(); i++) {
                final String k = keywords.get(i);
                if (origSVars.containsKey(k)) {
                    keywords.add("\"" + k + "\"");
                    keywords.remove(k);
                }
                if (keywords.get(i).startsWith("HIDDEN")) {
                    tgtCard.addExtrinsicKeyword(keywords.get(i));
                }
                else {
                    tgtCard.addIntrinsicKeyword(keywords.get(i));
                }
            }
        }

        // set power of clone
        if (params.containsKey("IntoPlayTapped")) {
            tgtCard.setTapped(true);
        }

        // set power of clone
        if (params.containsKey("SetPower")) {
            String rhs = params.get("SetPower");
            int power = -1;
            try {
                power = Integer.parseInt(rhs);
            } catch (final NumberFormatException e) {
                power = CardFactoryUtil.xCount(tgtCard, tgtCard.getSVar(rhs));
            }
            tgtCard.setBaseAttack(power);
        }

        // set toughness of clone
        if (params.containsKey("SetToughness")) {
            String rhs = params.get("SetToughness");
            int toughness = -1;
            try {
                toughness = Integer.parseInt(rhs);
            } catch (final NumberFormatException e) {
                toughness = CardFactoryUtil.xCount(tgtCard, tgtCard.getSVar(rhs));
            }
            tgtCard.setBaseDefense(toughness);
        }

        // colors to be added or changed to
        String shortColors = "";
        if (params.containsKey("Colors")) {
            final String colors = params.get("Colors");
            if (colors.equals("ChosenColor")) {
                shortColors = CardUtil.getShortColorsString(tgtCard.getChosenColor());
            } else {
                shortColors = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(","))));
            }
        }
        tgtCard.addColor(shortColors, tgtCard, !params.containsKey("OverwriteColors"), true);

    }

 } // end class AbilityFactoryClone