package forge.gui.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import forge.Card;
import forge.CardUtil;
import forge.FThreads;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.card.ability.ApiType;
import forge.card.mana.ManaCostBeingPaid;
import forge.card.mana.ManaCostShard;
import forge.card.replacement.ReplacementEffect;
import forge.card.spellability.AbilityManaPart;
import forge.card.spellability.SpellAbility;
import forge.game.Game;
import forge.game.player.HumanPlay;
import forge.game.player.Player;
import forge.gui.GuiChoose;
import forge.view.ButtonUtil;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public abstract class InputPayMana extends InputSyncronizedBase {

    private static final long serialVersionUID = -9133423708688480255L;

    protected int phyLifeToLose = 0;
    
    protected final Player player;
    protected final Game game;
    protected ManaCostBeingPaid manaCost;
    protected final SpellAbility saPaidFor;
    
    boolean bPaid = false;
    
    protected InputPayMana(SpellAbility saToPayFor) {
        this.player = saToPayFor.getActivatingPlayer();
        this.game = player.getGame();
        this.saPaidFor = saToPayFor;
    }
    
    

    @Override
    protected void onCardSelected(Card card, boolean isRmb) {
        if (card.getManaAbility().isEmpty()) {
            flashIncorrectAction();
            return;
        }
        // only tap card if the mana is needed
        activateManaAbility(card, this.manaCost);
    }
    
    public void selectManaPool(byte colorCode) {
        useManaFromPool(colorCode);
    }

    /**
     * <p>
     * activateManaAbility.
     * </p>
     * @param color a String that represents the Color the mana is coming from
     * @param saBeingPaidFor a SpellAbility that is being paid for
     * @param manaCost the amount of mana remaining to be paid
     * 
     * @return ManaCost the amount of mana remaining to be paid after the mana is activated
     */
    protected void useManaFromPool(byte colorCode) { useManaFromPool(colorCode, manaCost); } 
    protected void useManaFromPool(byte colorCode, ManaCostBeingPaid manaCost) {
        // Convert Color to short String
        player.getManaPool().payManaFromPool(saPaidFor, manaCost, ManaCostShard.parseNonGeneric(MagicColor.toShortString(colorCode))); 
    
        onManaAbilityPlayed(null);
        showMessage();
    }

    /**
     * <p>
     * activateManaAbility.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param card
     *            a {@link forge.Card} object.
     * @param manaCost
     *            a {@link forge.card.mana.ManaCostBeingPaid} object.
     * @return a {@link forge.card.mana.ManaCostBeingPaid} object.
     */
    protected void activateManaAbility(final Card card, ManaCostBeingPaid manaCost) {
        // make sure computer's lands aren't selected
        if (card.getController() != player) {
            return;
        }

        byte colorCanUse = 0;
        byte colorNeeded = 0;

        for (final byte color : MagicColor.WUBRG) {
            if (manaCost.isAnyPartPayableWith(color)) colorCanUse |= color;
            if (manaCost.needsColor(color))           colorNeeded |= color;
        }
        boolean canUseColorless = manaCost.isAnyPartPayableWith((byte)0);

        List<SpellAbility> abilities = new ArrayList<SpellAbility>();
        // you can't remove unneeded abilities inside a for(am:abilities) loop :(
    
        final String typeRes = manaCost.getSourceRestriction();
        if( StringUtils.isNotBlank(typeRes) && !card.isType(typeRes))
            return;

        boolean guessAbilityWithRequiredColors = true;
        for (SpellAbility ma : card.getManaAbility()) {
            ma.setActivatingPlayer(player);

            AbilityManaPart m = ma.getManaPartRecursive();
            if (m == null || !ma.canPlay())                                     continue;
            if (!canUseColorless && !abilityProducesManaColor(ma, m, colorCanUse)) continue;
            if (ma.isAbility() && ma.getRestrictions().isInstantSpeed())        continue;
            if (!m.meetsManaRestrictions(saPaidFor))                            continue;

            abilities.add(ma);

            // skip express mana if the ability is not undoable or reusable
            if (!ma.isUndoable() || !ma.getPayCosts().isRenewableResource() || ma.getSubAbility() != null)
                guessAbilityWithRequiredColors = false;
        }
        
        if (abilities.isEmpty()) {
            return;
        }

        // Store some information about color costs to help with any mana choices
        if (colorNeeded == 0) {  // only colorless left
            if (saPaidFor.getSourceCard() != null && saPaidFor.getSourceCard().hasSVar("ManaNeededToAvoidNegativeEffect")) {
                String[] negEffects = saPaidFor.getSourceCard().getSVar("ManaNeededToAvoidNegativeEffect").split(",");
                for (String negColor : negEffects) {
                    byte col = MagicColor.fromName(negColor);
                    colorCanUse |= col;
                }
            }
        }        
    

    
        // If the card has sunburst or any other ability that tracks mana spent,
        // skip express Mana choice
        if (saPaidFor.getSourceCard() != null && saPaidFor.getSourceCard().hasKeyword("Sunburst") && saPaidFor.isSpell()) {
            colorCanUse = MagicColor.ALL_COLORS;
            guessAbilityWithRequiredColors = false;
        }
    
        boolean choice = true;
        if (guessAbilityWithRequiredColors) {
            // express Mana Choice
            final ArrayList<SpellAbility> colorMatches = new ArrayList<SpellAbility>();
            for (SpellAbility sa : abilities) {
                if (colorNeeded != 0 && abilityProducesManaColor(sa, sa.getManaPartRecursive(), colorNeeded))
                    colorMatches.add(sa);
            }
            
    
            if (colorMatches.isEmpty()) {
                // can only match colorless just grab the first and move on.
                // This is wrong. Sometimes all abilities aren't created equal
                choice = false;
            } else if (colorMatches.size() < abilities.size()) {
                // leave behind only color matches
                abilities = colorMatches;
            }
        }
    
        final SpellAbility chosen = abilities.size() > 1 && choice ? GuiChoose.one("Choose mana ability", abilities) : abilities.get(0);
        ColorSet colors = ColorSet.fromMask(0 == colorNeeded ? colorCanUse : colorNeeded);
        chosen.getManaPartRecursive().setExpressChoice(colors);
        
        // System.out.println("Chosen sa=" + chosen + " of " + chosen.getSourceCard() + " to pay mana");
        Runnable proc = new Runnable() {
            @Override
            public void run() {
                HumanPlay.playSpellAbility(chosen.getActivatingPlayer(), chosen);
                onManaAbilityPlayed(chosen);
                onStateChanged();
            }
        };
        game.getAction().invoke(proc);
    }


    /**
     * <p>
     * canMake.  color is like "G", returns "Green".
     * </p>
     * 
     * @param am
     *            a {@link forge.card.spellability.AbilityMana} object.
     * @param mana
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    private static boolean abilityProducesManaColor(final SpellAbility am, AbilityManaPart m, final byte neededColor) {
        if (neededColor == 0) {
            return true;
        }
    
        if (m.isAnyMana()) {
            return true;
        }
        
        // check for produce mana replacement effects - they mess this up, so just use the mana ability
        final Card source = am.getSourceCard();
        final Player activator = am.getActivatingPlayer();
        final Game g = source.getGame();
        final HashMap<String, Object> repParams = new HashMap<String, Object>();
        repParams.put("Event", "ProduceMana");
        repParams.put("Mana", m.getOrigProduced());
        repParams.put("Affected", source);
        repParams.put("Player", activator);
        repParams.put("AbilityMana", am);
        
        for (final Player p : g.getPlayers()) {
            for (final Card crd : p.getAllCards()) {
                for (final ReplacementEffect replacementEffect : crd.getReplacementEffects()) {
                    if (replacementEffect.requirementsCheck(g)
                            && replacementEffect.canReplace(repParams)
                            && replacementEffect.getMapParams().containsKey("ManaReplacement")
                            && replacementEffect.zonesCheck(g.getZoneOf(crd))) {
                        return true;
                    }
                }
            }
        }
        
        if (am.getApi() == ApiType.ManaReflected) {
            final Iterable<String> reflectableColors = CardUtil.getReflectableManaColors(am);
            for (final String color : reflectableColors) {
                if (0 != (neededColor & MagicColor.fromName(color))) {
                    return true;
                }
            }
        } else {
            String colorsProduced = m.isComboMana() ? m.getComboColors() : m.getOrigProduced();
            for (final String color : colorsProduced.split(" ")) {
                if (0 != (neededColor & MagicColor.fromName(color))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void onManaAbilityPlayed(final SpellAbility saPaymentSrc) { 
        if ( saPaymentSrc != null) // null comes when they've paid from pool
            player.getManaPool().payManaFromAbility(saPaidFor, manaCost, saPaymentSrc);

        onManaAbilityPaid();
    }
    
    protected boolean isAlreadyPaid() {
        if (manaCost.isPaid()) {
            bPaid = true;
        }
        return bPaid;
    }
    
    
    /** {@inheritDoc} */
    @Override
    public void showMessage() {
        if ( isFinished() ) return;
        ButtonUtil.enableOnlyCancel();
        onStateChanged();
    }
    
    protected void onStateChanged() { 
        if( isAlreadyPaid() ) {
            done();
            stop();
        } else 
            FThreads.invokeInEdtNowOrLater(new Runnable() { @Override public void run(){ updateMessage(); }});
    }
    
    protected void onManaAbilityPaid() {} // some inputs overload it
    protected abstract void done();
    protected abstract void updateMessage();

    @Override
    public String toString() {
        return String.format("PayManaBase %s left", manaCost.toString() );
    }

    public boolean isPaid() { return bPaid; }
}
