package forge.screens.match.input;

import forge.game.GameEntity;
import forge.game.ability.ApiType;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.screens.match.FControl;
import forge.toolbox.GuiChoose;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class InputSelectTargets extends InputSyncronizedBase {
    private final List<Card> choices;
    // some cards can be targeted several times (eg: distribute damage as you choose)
    private final Map<GameEntity, Integer> targetDepth = new HashMap<GameEntity, Integer>();
    private final TargetRestrictions tgt;
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
        this.choices = choices;
        this.tgt = sa.getTargetRestrictions();
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
        if (!sa.getUniqueTargets().isEmpty()) {
            sb.append("Parent Targeted:");
            sb.append(sa.getUniqueTargets()).append("\n");
        }
        sb.append(sa.getHostCard() + " - " + tgt.getVTSelection());
        
        int maxTargets = tgt.getMaxTargets(sa.getHostCard(), sa);
        int targeted = sa.getTargets().getNumTargeted();
        if(maxTargets > 1)
            sb.append("\n(").append(maxTargets - targeted).append(" more can be targeted)");

        showMessage(sb.toString());

        // If reached Minimum targets, enable OK button
        if (!tgt.isMinTargetsChosen(sa.getHostCard(), sa) || tgt.isDividedAsYouChoose()) {
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
                ButtonUtil.enableAll();
            }
        }
    }

    @Override
    protected final void onCancel() {
        bCancel = true;
        this.done();
    }

    @Override
    protected final void onOk() {
        bOk = true;
        this.done();
    }

    @Override
    protected final void onCardSelected(final Card card, final List<Card> orderedCardOptions) {
        if (!tgt.isUniqueTargets() && targetDepth.containsKey(card)) {
            return;
        }
        
        // leave this in temporarily, there some seriously wrong things going on here
        // Can be targeted doesn't check if the target is a valid type, only if a card is generally "targetable"
        if (!card.canBeTargetedBy(sa)) {
            showMessage(sa.getHostCard() + " - Cannot target this card (Shroud? Protection? Restrictions).");
            return;
        } 
        if (!choices.contains(card)) {
            if (card.isPlaneswalker() && sa.getApi() == ApiType.DealDamage) {
                showMessage(sa.getHostCard() + " - To deal an opposing Planeswalker direct damage, target its controller and then redirect the damage on resolution.");
            } else {
                showMessage(sa.getHostCard() + " - The selected card is not a valid choice to be targeted.");
            }
            return;
        }
        
        if (tgt.isDividedAsYouChoose()) {
            final int stillToDivide = tgt.getStillToDivide();
            int allocatedPortion = 0;
            // allow allocation only if the max targets isn't reached and there are more candidates
            if ((sa.getTargets().getNumTargeted() + 1 < tgt.getMaxTargets(sa.getHostCard(), sa))
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
    protected final void onPlayerSelected(Player player) {
        if (!tgt.isUniqueTargets() && targetDepth.containsKey(player)) {
            return;
        }

        if (!sa.canTarget(player)) {
            showMessage(sa.getHostCard() + " - Cannot target this player (Hexproof? Protection? Restrictions?).");
            return;
        }
        
        if (tgt.isDividedAsYouChoose()) {
            final int stillToDivide = tgt.getStillToDivide();
            int allocatedPortion = 0;
            // allow allocation only if the max targets isn't reached and there are more candidates
            if ((sa.getTargets().getNumTargeted() + 1 < tgt.getMaxTargets(sa.getHostCard(), sa)) && (tgt.getNumCandidates(sa, true) - 1 > 0) && stillToDivide > 1) {
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
        sa.getTargets().add(ge);
        if (ge instanceof Card) {
            FControl.setUsedToPay((Card) ge, true);
        }
        Integer val = targetDepth.get(ge);
        targetDepth.put(ge, val == null ? Integer.valueOf(1) : Integer.valueOf(val.intValue() + 1) );
        
        if (hasAllTargets()) {
            bOk = true;
            this.done();
        }
        else {
            this.showMessage();
        }
    }
    
    private void done() {
        for (GameEntity c : targetDepth.keySet()) {
            if (c instanceof Card) {
                FControl.setUsedToPay((Card)c, false);
            }
        }
        this.stop();
    }
    
    private boolean hasAllTargets() {
        return tgt.isMaxTargetsChosen(sa.getHostCard(), sa) || ( tgt.getStillToDivide() == 0 && tgt.isDividedAsYouChoose());
    }
}