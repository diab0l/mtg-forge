package forge.card.trigger;

import java.util.HashMap;

import forge.Card;
import forge.card.spellability.SpellAbility;

/**
 * <p>
 * Trigger_Discarded class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Trigger_Discarded extends Trigger {

    /**
     * <p>
     * Constructor for Trigger_Discarded.
     * </p>
     * 
     * @param params
     *            a {@link java.util.HashMap} object.
     * @param host
     *            a {@link forge.Card} object.
     * @param intrinsic
     *            the intrinsic
     */
    public Trigger_Discarded(final HashMap<String, String> params, final Card host, final boolean intrinsic) {
        super(params, host, intrinsic);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean performTest(final java.util.Map<String, Object> runParams2) {
        if (this.getMapParams().containsKey("ValidCard")) {
            if (!this.matchesValid(runParams2.get("Card"), this.getMapParams().get("ValidCard").split(","),
                    this.getHostCard())) {
                return false;
            }
        }

        if (this.getMapParams().containsKey("ValidPlayer")) {
            if (!this.matchesValid(runParams2.get("Player"), this.getMapParams().get("ValidPlayer").split(","),
                    this.getHostCard())) {
                return false;
            }
        }

        if (this.getMapParams().containsKey("ValidCause")) {
            if (runParams2.get("Cause") == null) {
                return false;
            }
            if (!this.matchesValid(runParams2.get("Cause"), this.getMapParams().get("ValidCause").split(","),
                    this.getHostCard())) {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final Trigger getCopy() {
        final Trigger copy = new Trigger_Discarded(this.getMapParams(), this.getHostCard(), this.isIntrinsic());
        if (this.getOverridingAbility() != null) {
            copy.setOverridingAbility(this.getOverridingAbility());
        }
        copy.setName(this.getName());
        copy.setID(this.getId());

        return copy;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTriggeringObjects(final SpellAbility sa) {
        sa.setTriggeringObject("Card", this.getRunParams().get("Card"));
    }
}
