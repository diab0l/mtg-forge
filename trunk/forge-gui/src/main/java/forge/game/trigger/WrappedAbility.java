package forge.game.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.card.mana.ManaCost;
import forge.game.Game;
import forge.game.GameObject;
import forge.game.ability.ApiType;
import forge.game.card.Card;
import forge.game.cost.Cost;
import forge.game.player.Player;
import forge.game.spellability.Ability;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.ISpellAbility;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityRestriction;
import forge.game.spellability.TargetChoices;
import forge.game.spellability.TargetRestrictions;

// Wrapper ability that checks the requirements again just before
// resolving, for intervening if clauses.
// Yes, it must wrap ALL SpellAbility methods in order to handle
// possible corner cases.
// (The trigger can have a hardcoded OverridingAbility which can make
// use of any of the methods)
public class WrappedAbility extends Ability implements ISpellAbility {

    private final SpellAbility sa;
    private final Trigger regtrig;
    private final Player decider;

    boolean mandatory = false;

    public WrappedAbility(final Trigger regTrig, final SpellAbility sa0, final Player decider0) {
        super(regTrig.getHostCard(), ManaCost.ZERO);
        regtrig = regTrig;
        sa = sa0;
        decider = decider0;
    }


    @Override
    public boolean isWrapper() {
        return true;
    }

    public Trigger getTrigger(){
        return regtrig;
    }

    public Player getDecider() {
        return decider;
    }

    public final void setMandatory(final boolean mand) {
        this.mandatory = mand;
    }

    /**
     * @return the mandatory
     */
    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public String getParam(String key) { return sa.getParam(key); }

    @Override
    public boolean hasParam(String key) { return sa.hasParam(key); }

    @Override
    public ApiType getApi() {
        return sa.getApi();
    }

    @Override
    public void setPaidHash(final HashMap<String, List<Card>> hash) {
        sa.setPaidHash(hash);
    }

    @Override
    public HashMap<String, List<Card>> getPaidHash() {
        return sa.getPaidHash();
    }

    @Override
    public List<Card> getPaidList(final String str) {
        return sa.getPaidList(str);
    }

    @Override
    public void addCostToHashList(final Card c, final String str) {
        sa.addCostToHashList(c, str);
    }

    @Override
    public void resetPaidHash() {
        sa.resetPaidHash();
    }

    @Override
    public HashMap<String, Object> getTriggeringObjects() {
        return sa.getTriggeringObjects();
    }

    @Override
    public void setAllTriggeringObjects(final HashMap<String, Object> triggeredObjects) {
        sa.setAllTriggeringObjects(triggeredObjects);
    }

    @Override
    public void setTriggeringObject(final String type, final Object o) {
        sa.setTriggeringObject(type, o);
    }

    @Override
    public Object getTriggeringObject(final String type) {
        return sa.getTriggeringObject(type);
    }

    @Override
    public boolean hasTriggeringObject(final String type) {
        return sa.hasTriggeringObject(type);
    }

    @Override
    public void resetTriggeringObjects() {
        sa.resetTriggeringObjects();
    }

    @Override
    public boolean canPlay() {
        return sa.canPlay();
    }

    @Override
    public boolean canPlayAI(Player aiPlayer) {
        return sa.canPlayAI(aiPlayer);
    }

    @Override
    public SpellAbility copy() {
        return sa.copy();
    }

    @Override
    public boolean doTrigger(final boolean mandatory, Player ai) {
        return sa.doTrigger(mandatory, ai);
    }

    @Override
    public Player getActivatingPlayer() {
        return sa.getActivatingPlayer();
    }

    @Override
    public String getDescription() {
        return sa.getDescription();
    }

    @Override
    public ManaCost getMultiKickerManaCost() {
        return sa.getMultiKickerManaCost();
    }

    @Override
    public SpellAbilityRestriction getRestrictions() {
        return sa.getRestrictions();
    }

    @Override
    public Card getSourceCard() {
        return sa.getSourceCard();
    }

    @Override
    public String getStackDescription() {
        final StringBuilder sb = new StringBuilder(regtrig.toString());
        if (this.getTargetRestrictions() != null) {
            sb.append(" (Targeting ");
            for (final GameObject o : this.getTargets().getTargets()) {
                sb.append(o.toString());
                sb.append(", ");
            }
            if (sb.toString().endsWith(", ")) {
                sb.setLength(sb.length() - 2);
            } else {
                sb.append("ERROR");
            }
            sb.append(")");
        }

        return sb.toString();
    }

    @Override
    public AbilitySub getSubAbility() {
        return sa.getSubAbility();
    }

    @Override
    public TargetRestrictions getTargetRestrictions() {
        return sa.getTargetRestrictions();
    }

    @Override
    public Card getTargetCard() {
        return sa.getTargetCard();
    }

    @Override
    public TargetChoices getTargets() {
        return sa.getTargets();
    }

    @Override
    public boolean isAbility() {
        return sa.isAbility();
    }

    @Override
    public boolean isBuyBackAbility() {
        return sa.isBuyBackAbility();
    }

    @Override
    public boolean isCycling() {
        return sa.isCycling();
    }

    @Override
    public boolean isFlashBackAbility() {
        return sa.isFlashBackAbility();
    }

    @Override
    public boolean isMultiKicker() {
        return sa.isMultiKicker();
    }

    @Override
    public boolean isSpell() {
        return sa.isSpell();
    }

    @Override
    public boolean isXCost() {
        return sa.isXCost();
    }

    @Override
    public void resetOnceResolved() {
        // Fixing an issue with Targeting + Paying Mana
        // sa.resetOnceResolved();
    }

    @Override
    public void setActivatingPlayer(final Player player) {
        sa.setActivatingPlayer(player);
    }

    @Override
    public void setDescription(final String s) {
        sa.setDescription(s);
    }

    @Override
    public void setFlashBackAbility(final boolean flashBackAbility) {
        sa.setFlashBackAbility(flashBackAbility);
    }

    @Override
    public void setMultiKickerManaCost(final ManaCost cost) {
        sa.setMultiKickerManaCost(cost);
    }

    @Override
    public void setPayCosts(final Cost abCost) {
        sa.setPayCosts(abCost);
    }

    @Override
    public void setRestrictions(final SpellAbilityRestriction restrict) {
        sa.setRestrictions(restrict);
    }

    @Override
    public void setSourceCard(final Card c) {
        sa.setSourceCard(c);
    }

    @Override
    public void setStackDescription(final String s) {
        sa.setStackDescription(s);
    }

    @Override
    public void setSubAbility(final AbilitySub subAbility) {
        sa.setSubAbility(subAbility);
    }

    @Override
    public void setTargetRestrictions(final TargetRestrictions tgt) {
        sa.setTargetRestrictions(tgt);
    }

    @Override
    public void setTargetCard(final Card card) {
        sa.setTargetCard(card);
    }

    @Override
    public void setSourceTrigger(final int id) {
        sa.setSourceTrigger(id);
    }

    @Override
    public int getSourceTrigger() {
        return sa.getSourceTrigger();
    }

    @Override
    public void setOptionalTrigger(final boolean b) {
        sa.setOptionalTrigger(b);
    }

    @Override
    public boolean isOptionalTrigger() {
        return sa.isOptionalTrigger();
    }

    @Override
    public boolean usesTargeting() {
        return sa.usesTargeting();
    }

    // //////////////////////////////////////
    // THIS ONE IS ALL THAT MATTERS
    // //////////////////////////////////////
    @Override
    public void resolve() {
        final Game game = sa.getActivatingPlayer().getGame();

        if (!(regtrig instanceof TriggerAlways)) {
            // State triggers don't have "Intervening If"
            if (!regtrig.requirementsCheck(game)) {
                return;
            }
        }

        TriggerHandler th = game.getTriggerHandler();
        Map<String, String> triggerParams = regtrig.getMapParams();

        if (decider != null && !decider.getController().confirmTrigger(sa, regtrig, triggerParams, this.isMandatory())) {
            return;
        }

        getActivatingPlayer().getController().playSpellAbilityNoStack(sa, false);

        // Add eventual delayed trigger.
        if (triggerParams.containsKey("DelayedTrigger")) {
            final String sVarName = triggerParams.get("DelayedTrigger");
            final Trigger deltrig = TriggerHandler.parseTrigger(regtrig.getHostCard().getSVar(sVarName),
                    regtrig.getHostCard(), true);
            deltrig.setStoredTriggeredObjects(this.getTriggeringObjects());
            th.registerDelayedTrigger(deltrig);
        }
    }
}
