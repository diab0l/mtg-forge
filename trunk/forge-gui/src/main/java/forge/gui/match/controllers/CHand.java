/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.gui.match.controllers;

import forge.Command;
import forge.FThreads;
import forge.Singletons;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.framework.ICDoc;
import forge.gui.match.CMatchUI;
import forge.gui.match.views.VField;
import forge.gui.match.views.VHand;
import forge.view.arcane.CardPanel;
import forge.view.arcane.HandArea;
import forge.view.arcane.util.Animation;
import forge.view.arcane.util.CardPanelMouseAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Controls Swing components of a player's hand instance.
 * 
 */
public class CHand implements ICDoc {
    private final Player player;
    private final VHand view;
    private boolean initializedAlready = false;

    /**
     * Controls Swing components of a player's hand instance.
     * 
     * @param p0 &emsp; {@link forge.game.player.Player}
     * @param v0 &emsp; {@link forge.gui.match.views.VHand}
     */
    public CHand(final Player p0, final VHand v0) {
        this.player = p0;
        this.view = v0;
        v0.getHandArea().addCardPanelMouseListener(new CardPanelMouseAdapter() {
            @Override
            public void mouseDragEnd(CardPanel dragPanel, MouseEvent evt) {
                //update index of dragged card in hand zone to match new index within hand area
                int index = CHand.this.view.getHandArea().getCardPanels().indexOf(dragPanel);
                CHand.this.player.getZone(ZoneType.Hand).reposition(dragPanel.getCard(), index);
            }
        });
    }

    @Override
    public void initialize() {
        if (initializedAlready) { return; }
        initializedAlready = true;

//        if (player != null)
//            player.getZone(ZoneType.Hand).addObserver(this);
    }

    public void update(final Observable a, final Object b) {
        FThreads.invokeInEdtNowOrLater(updateRoutine);
    }

    private final Runnable updateRoutine = new Runnable() {
        @Override public void run() { updateHand(); }
    };

    public void updateHand() {
        FThreads.assertExecutedByEdt(true);

        final HandArea p = view.getHandArea();

        VField vf = CMatchUI.SINGLETON_INSTANCE.getFieldViewFor(player);
        if (vf == null) {
            return;
        }
        final Rectangle rctLibraryLabel = vf.getDetailsPanel().getLblLibrary().getBounds();

        // Animation starts from the library label and runs to the hand panel.
        // This check prevents animation running if label hasn't been realized yet.
        if (rctLibraryLabel.isEmpty()) {
            return;
        }

        //update card panels in hand area
        final List<Card> cards = player.getZone(ZoneType.Hand).getCards();
        final List<CardPanel> placeholders = new ArrayList<CardPanel>();
        final List<CardPanel> cardPanels = new ArrayList<CardPanel>();
        
        for (Card card : cards) {
            CardPanel cardPanel = p.getCardPanel(card.getUniqueNumber());
            if (cardPanel == null) { //create placeholders for new cards
                cardPanel = new CardPanel(card);
                cardPanel.setDisplayEnabled(false);
                placeholders.add(cardPanel);
            }
            cardPanels.add(cardPanel);
        }

        p.setCardPanels(cardPanels);

        //animate new cards into positions defined by placeholders
        JLayeredPane layeredPane = Singletons.getView().getFrame().getLayeredPane();
        int fromZoneX = 0, fromZoneY = 0;

        final Point zoneLocation = SwingUtilities.convertPoint(vf.getDetailsPanel().getLblLibrary(),
                Math.round(rctLibraryLabel.width / 2.0f), Math.round(rctLibraryLabel.height / 2.0f), layeredPane);
        fromZoneX = zoneLocation.x;
        fromZoneY = zoneLocation.y;
        int startWidth, startX, startY;
        startWidth = 10;
        startX = fromZoneX - Math.round(startWidth / 2.0f);
        startY = fromZoneY - Math.round(Math.round(startWidth * forge.view.arcane.CardPanel.ASPECT_RATIO) / 2.0f);

        int endWidth, endX, endY;

        for (final CardPanel placeholder : placeholders) {
            endWidth = placeholder.getCardWidth();
            final Point toPos = SwingUtilities.convertPoint(view.getHandArea(), placeholder.getCardLocation(), layeredPane);
            endX = toPos.x;
            endY = toPos.y;

            if (Singletons.getView().getFrame().isShowing()) {
                final CardPanel animationPanel = new CardPanel(placeholder.getCard());
                Animation.moveCard(startX, startY, startWidth, endX, endY, endWidth, animationPanel, placeholder,
                        layeredPane, 500);
            }
            else {
                Animation.moveCard(placeholder);
            }
        }
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }

    @Override
    public void update() {
        updateHand();
    }
}
