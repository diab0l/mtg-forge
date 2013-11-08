package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.List;

import forge.Card;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.GuiDialog;

public class RearrangeTopOfLibraryEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */

    @Override
    protected String getStackDescription(SpellAbility sa) {
        int numCards = 0;
        final List<Player> tgtPlayers = getTargetPlayers(sa);
        boolean shuffle = false;
        Card host = sa.getSourceCard();

        numCards = AbilityUtils.calculateAmount(host, sa.getParam("NumCards"), sa);
        shuffle = sa.hasParam("MayShuffle");

        final StringBuilder ret = new StringBuilder();
        ret.append("Look at the top ");
        ret.append(numCards);
        ret.append(" cards of ");
        for (final Player p : tgtPlayers) {
            ret.append(p.getName());
            ret.append("s");
            ret.append(" & ");
        }
        ret.delete(ret.length() - 3, ret.length());

        ret.append(" library. Then put them back in any order.");

        if (shuffle) {
            ret.append("You may have ");
            if (tgtPlayers.size() > 1) {
                ret.append("those");
            } else {
                ret.append("that");
            }

            ret.append(" player shuffle his or her library.");
        }

        return ret.toString();
    }

    /**
     * <p>
     * rearrangeTopOfLibraryResolve.
     * </p>
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param af
     *            a {@link forge.card.ability.AbilityFactory} object.
     */

    @Override
    public void resolve(SpellAbility sa) {
        int numCards = 0;
        Card host = sa.getSourceCard();
        boolean shuffle = false;

        if (sa.getActivatingPlayer().isHuman()) {
            final TargetRestrictions tgt = sa.getTargetRestrictions();


            numCards = AbilityUtils.calculateAmount(host, sa.getParam("NumCards"), sa);
            shuffle = sa.hasParam("MayShuffle");

            for (final Player p : getTargetPlayers(sa)) {
                if ((tgt == null) || p.canBeTargetedBy(sa)) {
                    rearrangeTopOfLibrary(host, p, numCards, shuffle, sa);
                }
            }
        }
    }

    /**
     * use this when Human needs to rearrange the top X cards in a player's
     * library. You may also specify a shuffle when done
     * 
     * @param src
     *            the source card
     * @param player
     *            the player to target
     * @param numCards
     *            the number of cards from the top to rearrange
     * @param mayshuffle
     *            a boolean.
     */
    private void rearrangeTopOfLibrary(final Card src, final Player player, final int numCards,
            final boolean mayshuffle, final SpellAbility sa) {
        final PlayerZone lib = player.getZone(ZoneType.Library);
        int maxCards = lib.size();
        // If library is smaller than N, only show that many cards
        maxCards = Math.min(maxCards, numCards);
        if (maxCards == 0) {
            return;
        }
        final List<Card> topCards = new ArrayList<Card>();
        // show top n cards:
        for (int j = 0; j < maxCards; j++) {
            topCards.add(lib.get(j));
        }

        List<Card> orderedCards = GuiChoose.order("Select order to Rearrange", "Top of Library", 0, topCards, null, src);
        for (int i = maxCards - 1; i >= 0; i--) {
            Card next = orderedCards.get(i);
            player.getGame().getAction().moveToLibrary(next, 0);
        }
        if (mayshuffle) {
            if (GuiDialog.confirm(src, "Do you want to shuffle the library?")) {
                player.shuffle(sa);
            }
        }
    }

}
