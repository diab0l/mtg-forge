package forge.game.ability.effects;

import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;

import java.util.ArrayList;
import java.util.List;

public class RearrangeTopOfLibraryEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */

    @Override
    protected String getStackDescription(SpellAbility sa) {
        int numCards = 0;
        final List<Player> tgtPlayers = getTargetPlayers(sa);
        boolean shuffle = false;
        Card host = sa.getHostCard();

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
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param af
     *            a {@link forge.game.ability.AbilityFactory} object.
     */

    @Override
    public void resolve(SpellAbility sa) {
        int numCards = 0;
        Card host = sa.getHostCard();
        boolean shuffle = false;

        final TargetRestrictions tgt = sa.getTargetRestrictions();


        numCards = AbilityUtils.calculateAmount(host, sa.getParam("NumCards"), sa);
        shuffle = sa.hasParam("MayShuffle");

        for (final Player p : getTargetPlayers(sa)) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                rearrangeTopOfLibrary(host, p, numCards, shuffle, sa);
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
    private void rearrangeTopOfLibrary(final Card src, final Player player, final int numCards, final boolean mayshuffle, final SpellAbility sa) {
        final Player activator = sa.getActivatingPlayer();
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

        List<Card> orderedCards = activator.getController().orderMoveToZoneList(topCards, ZoneType.Library);
        for (int i = maxCards - 1; i >= 0; i--) {
            Card next = orderedCards.get(i);
            player.getGame().getAction().moveToLibrary(next, 0);
        }
        if (mayshuffle && activator.getController().confirmAction(sa, null, "Do you want to shuffle the library?")) {
            player.shuffle(sa);
        }
    }

}
