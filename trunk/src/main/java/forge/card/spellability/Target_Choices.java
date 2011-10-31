package forge.card.spellability;

import java.util.ArrayList;

import forge.Card;
import forge.Player;

/**
 * <p>
 * Target_Choices class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Target_Choices {
    private int numTargeted = 0;

    /**
     * <p>
     * Getter for the field <code>numTargeted</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getNumTargeted() {
        return this.numTargeted;
    }

    // Card or Player are legal targets.
    private final ArrayList<Card> targetCards = new ArrayList<Card>();
    private final ArrayList<Player> targetPlayers = new ArrayList<Player>();
    private final ArrayList<SpellAbility> targetSAs = new ArrayList<SpellAbility>();

    /**
     * <p>
     * addTarget.
     * </p>
     * 
     * @param o
     *            a {@link java.lang.Object} object.
     * @return a boolean.
     */
    public final boolean addTarget(final Object o) {
        if (o instanceof Player) {
            return this.addTarget((Player) o);
        } else if (o instanceof Card) {
            return this.addTarget((Card) o);
        } else if (o instanceof SpellAbility) {
            return this.addTarget((SpellAbility) o);
        }

        return false;
    }

    /**
     * <p>
     * addTarget.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public final boolean addTarget(final Card c) {
        if (!this.targetCards.contains(c)) {
            this.targetCards.add(c);
            this.numTargeted++;
            return true;
        }
        return false;
    }

    /**
     * <p>
     * addTarget.
     * </p>
     * 
     * @param p
     *            a {@link forge.Player} object.
     * @return a boolean.
     */
    public final boolean addTarget(final Player p) {
        if (!this.targetPlayers.contains(p)) {
            this.targetPlayers.add(p);
            this.numTargeted++;
            return true;
        }
        return false;
    }

    /**
     * <p>
     * addTarget.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public final boolean addTarget(final SpellAbility sa) {
        if (!this.targetSAs.contains(sa)) {
            this.targetSAs.add(sa);
            this.numTargeted++;
            return true;
        }
        return false;
    }

    /**
     * <p>
     * Getter for the field <code>targetCards</code>.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public final ArrayList<Card> getTargetCards() {
        return this.targetCards;
    }

    /**
     * <p>
     * Getter for the field <code>targetPlayers</code>.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public final ArrayList<Player> getTargetPlayers() {
        return this.targetPlayers;
    }

    /**
     * <p>
     * Getter for the field <code>targetSAs</code>.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public final ArrayList<SpellAbility> getTargetSAs() {
        return this.targetSAs;
    }

    /**
     * <p>
     * getTargets.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public final ArrayList<Object> getTargets() {
        final ArrayList<Object> tgts = new ArrayList<Object>();
        tgts.addAll(this.targetPlayers);
        tgts.addAll(this.targetCards);
        tgts.addAll(this.targetSAs);

        return tgts;
    }

    /**
     * <p>
     * getTargetedString.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getTargetedString() {
        final ArrayList<Object> tgts = this.getTargets();
        final StringBuilder sb = new StringBuilder("");
        for (final Object o : tgts) {
            if (o instanceof Player) {
                final Player p = (Player) o;
                sb.append(p.getName());
            }
            if (o instanceof Card) {
                final Card c = (Card) o;
                sb.append(c);
            }
            if (o instanceof SpellAbility) {
                final SpellAbility sa = (SpellAbility) o;
                sb.append(sa);
            }
            sb.append(" ");
        }

        return sb.toString();
    }
}
