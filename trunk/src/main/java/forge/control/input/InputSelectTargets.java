package forge.control.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import forge.Card;
import forge.GameEntity;
import forge.card.ability.ApiType;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.Player;
import forge.gui.GuiChoose;
import forge.view.ButtonUtil;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public final class InputSelectTargets extends InputSyncronizedBase {
    private final List<Card> choices;
    // some cards can be targeted several times (eg: distribute damage as you choose)
    private final Map<GameEntity, Integer> targetDepth = new HashMap<GameEntity, Integer>();
    private final Target tgt;
    private final SpellAbility sa;
    private boolean bCancel = false;
    private boolean bOk = false;
    private final boolean mandatory;
    private static final long serialVersionUID = -1091595663541356356L;

    public final boolean hasCancelled() { return bCancel; }
    public final boolean hasPressedOk() { return bOk; }
    /**
     * TODO: Write javadoc for Constructor.
     * @param select
     * @param choices
     * @param req
     * @param alreadyTargeted
     * @param targeted
     * @param tgt
     * @param sa
     * @param mandatory
     */
    public InputSelectTargets(List<Card> choices, SpellAbility sa, boolean mandatory) {
        super(sa.getActivatingPlayer());
        this.choices = choices;
        this.tgt = sa.getTarget();
        this.sa = sa;
        this.mandatory = mandatory;
    }

    @Override
    public void showMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Targeted:\n");
        for (final Entry<GameEntity, Integer> o : targetDepth.entrySet()) {
            sb.append(o.getKey());
            if( o.getValue() > 1 )
                sb.append(" (").append(o.getValue()).append(" times)");
           sb.append("\n");
        }
        //sb.append(tgt.getTargetedString()).append("\n");
        sb.append(tgt.getVTSelection());
        
        int maxTargets = tgt.getMaxTargets(sa.getSourceCard(), sa);
        int targeted = tgt.getNumTargeted();
        if(maxTargets > 1)
            sb.append("\n(").append(maxTargets - targeted).append(" more can be targeted)");

        showMessage(sb.toString());

        // If reached Minimum targets, enable OK button
        if (!tgt.isMinTargetsChosen(sa.getSourceCard(), sa) || tgt.isDividedAsYouChoose()) {
            if (mandatory && tgt.hasCandidates(sa, true)) {
                // Player has to click on a target
                ButtonUtil.disableAll();
            } else {
                ButtonUtil.enableOnlyCancel();
            }
        } else {
            if (mandatory && tgt.hasCandidates(sa, true)) {
                // Player has to click on a target or ok
                ButtonUtil.enableOnlyOk();
            } else {
                ButtonUtil.enableAllFocusOk();
            }
        }
    }

    @Override
    public void selectButtonCancel() {
        bCancel = true;
        this.done();
    }

    @Override
    public void selectButtonOK() {
        bOk = true;
        this.done();
    }

    @Override
    public void selectCard(final Card card) {
        if (!tgt.isUniqueTargets() && targetDepth.containsKey(card)) {
            return;
        }
        
        // leave this in temporarily, there some seriously wrong things going on here
        if (!card.canBeTargetedBy(sa)) {
            showMessage("Cannot target this card (Shroud? Protection? Restrictions?).");
            return;
        } 
        if (!choices.contains(card)) {
            showMessage("This card is not a valid choice for some other reason besides (Shroud? Protection? Restrictions?).");
            return;
        }
        
        if (tgt.isDividedAsYouChoose()) {
            final int stillToDivide = tgt.getStillToDivide();
            int allocatedPortion = 0;
            // allow allocation only if the max targets isn't reached and there are more candidates
            if ((tgt.getNumTargeted() + 1 < tgt.getMaxTargets(sa.getSourceCard(), sa))
                    && (tgt.getNumCandidates(sa, true) - 1 > 0) && stillToDivide > 1) {
                final Integer[] choices = new Integer[stillToDivide];
                for (int i = 1; i <= stillToDivide; i++) {
                    choices[i - 1] = i;
                }
                String apiBasedMessage = "Distribute how much to ";
                if (sa.getApi() == ApiType.DealDamage) {
                    apiBasedMessage = "Select how much damage to deal to ";
                } else if (sa.getApi() == ApiType.PreventDamage) {
                    apiBasedMessage = "Select how much damage to prevent to ";
                } else if (sa.getApi() == ApiType.PutCounter) {
                    apiBasedMessage = "Select how many counters to distribute to ";
                }
                final StringBuilder sb = new StringBuilder();
                sb.append(apiBasedMessage);
                sb.append(card.toString());
                Integer chosen = GuiChoose.oneOrNone(sb.toString(), choices);
                if (null == chosen) {
                    return;
                }
                allocatedPortion = chosen;
            } else { // otherwise assign the rest of the damage/protection
                allocatedPortion = stillToDivide;
            }
            tgt.setStillToDivide(stillToDivide - allocatedPortion);
            tgt.addDividedAllocation(card, allocatedPortion);
        }
        addTarget(card);
    } // selectCard()

    @Override
    public void selectPlayer(final Player player) {
        if (!tgt.isUniqueTargets() && targetDepth.containsKey(player)) {
            return;
        }

        if (!sa.canTarget(player)) {
            showMessage("Cannot target this player (Hexproof? Protection? Restrictions?).");
            return;
        }
        
        if (tgt.isDividedAsYouChoose()) {
            final int stillToDivide = tgt.getStillToDivide();
            int allocatedPortion = 0;
            // allow allocation only if the max targets isn't reached and there are more candidates
            if ((tgt.getNumTargeted() + 1 < tgt.getMaxTargets(sa.getSourceCard(), sa)) && (tgt.getNumCandidates(sa, true) - 1 > 0) && stillToDivide > 1) {
                final Integer[] choices = new Integer[stillToDivide];
                for (int i = 1; i <= stillToDivide; i++) {
                    choices[i - 1] = i;
                }
                String apiBasedMessage = "Distribute how much to ";
                if (sa.getApi() == ApiType.DealDamage) {
                    apiBasedMessage = "Select how much damage to deal to ";
                } else if (sa.getApi() == ApiType.PreventDamage) {
                    apiBasedMessage = "Select how much damage to prevent to ";
                }
                final StringBuilder sb = new StringBuilder();
                sb.append(apiBasedMessage);
                sb.append(player.getName());
                Integer chosen = GuiChoose.oneOrNone(sb.toString(), choices);
                if (null == chosen) {
                    return;
                }
                allocatedPortion = chosen;
            } else { // otherwise assign the rest of the damage/protection
                allocatedPortion = stillToDivide;
            }
            tgt.setStillToDivide(stillToDivide - allocatedPortion);
            tgt.addDividedAllocation(player, allocatedPortion);
        }
        addTarget(player);
    }

    private void addTarget(GameEntity ge) {
        tgt.addTarget(ge);
        if(ge instanceof Card) {
            ((Card) ge).setUsedToPay(true);
        }
        Integer val = targetDepth.get(ge);
        targetDepth.put(ge, val == null ? Integer.valueOf(1) : Integer.valueOf(val.intValue() + 1) );
        
        if(hasAllTargets()) {
            bOk = true;
            this.done();
        }
        else
            this.showMessage();
    }
    
    /* (non-Javadoc)
     * @see forge.control.input.InputSyncronizedBase#afterStop()
     */
    @Override
    protected void afterStop() {
        for(GameEntity c : targetDepth.keySet())
            if( c instanceof Card)
                ((Card)c).setUsedToPay(false);
        super.afterStop();

    }
    
    private void done() {
        this.stop();
    }
    
    private boolean hasAllTargets() {
        return tgt.isMaxTargetsChosen(sa.getSourceCard(), sa) || ( tgt.getStillToDivide() == 0 && tgt.isDividedAsYouChoose());
    }
}