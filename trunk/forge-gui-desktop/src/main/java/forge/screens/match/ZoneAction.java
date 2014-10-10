package forge.screens.match;

import java.awt.event.ActionEvent;
import java.util.List;

import com.google.common.collect.Lists;

import forge.game.card.CardView;
import forge.game.card.CardView.CardStateView;
import forge.game.player.PlayerView;
import forge.game.zone.ZoneType;
import forge.gui.ForgeAction;
import forge.gui.GuiChoose;
import forge.match.MatchConstants;

/**
 * Receives click and programmatic requests for viewing data stacks in the
 * "zones" of a player field: hand, library, etc.
 * 
 */
public class ZoneAction extends ForgeAction {
    private static final long serialVersionUID = -5822976087772388839L;
    private final PlayerView player;
    private final ZoneType zone;
    private final String title;

    /**
     * Receives click and programmatic requests for viewing data stacks in
     * the "zones" of a player field: hand, graveyard, etc. The library
     * "zone" is an exception to the rule; it's handled in DeckListAction.
     * 
     * @param zone
     *            &emsp; PlayerZone obj
     * @param property
     *            &emsp; String obj
     */
    public ZoneAction(final PlayerView player, final ZoneType zone, final MatchConstants property) {
        super(property);
        this.title = property.title;
        this.player = player;
        this.zone = zone;
    }

    /**
     * @param e
     *            &emsp; ActionEvent obj
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final Iterable<CardView> choices = this.getCardsAsIterable();
        if (!choices.iterator().hasNext()) {
            GuiChoose.reveal(this.title, "no cards");
            return;
        } 

        final List<CardStateView> choices2 = Lists.newLinkedList();
        for (final CardView crd : choices) {
            final CardStateView toAdd = crd.getOriginal();
            choices2.add(toAdd);
        }

        final CardStateView choice = GuiChoose.oneOrNone(this.title, choices2);
        if (choice != null) {
            this.doAction(choice.getCard());
        }
    }

    protected Iterable<CardView> getCardsAsIterable() {
        return player.getCards(zone);
    }

    protected void doAction(final CardView c) {
    }
} // End ZoneAction