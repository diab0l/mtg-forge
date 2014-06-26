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
package forge.screens.match;

import forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;
import forge.game.card.Card;
import forge.game.combat.Combat;
import forge.screens.match.views.VCardDisplayArea.CardAreaPanel;
import forge.screens.match.views.VPlayerPanel;
import forge.toolbox.FCardPanel;
import forge.util.Utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetingOverlay {
    private static final float ARROW_THICKNESS = Utils.scaleMax(5);
    private static final float ARROW_SIZE = 3 * ARROW_THICKNESS;

    private final List<FCardPanel> cardPanels = new ArrayList<FCardPanel>();
    private final List<Vector2[]> arcsCombat = new ArrayList<Vector2[]>();
    private final List<Vector2[]> arcsOther = new ArrayList<Vector2[]>();

    public TargetingOverlay() {
    }

    // TODO - this is called every repaint, regardless if card
    // positions have changed or not.  Could perform better if
    // it checked for a state change.
    private void assembleArcs() {
        //List<VField> fields = VMatchUI.SINGLETON_INSTANCE.getFieldViews();
        arcsCombat.clear();
        arcsOther.clear();
        cardPanels.clear();

        for (VPlayerPanel pnl : FControl.getView().getPlayerPanels().values()) {
            for (CardAreaPanel cardPanel : pnl.getField().getCardPanels()) {
                cardPanel.buildCardPanelList(cardPanels);
            }
        }

        // Locations of arc endpoint, per card, with ID as primary key.
        final Map<Integer, Vector2> endpoints = new HashMap<Integer, Vector2>();

        for (FCardPanel c : cardPanels) {
            endpoints.put(c.getCard().getUniqueNumber(), c.getTargetingArrowOrigin());
        }

        final Combat combat = FControl.getGame().getCombat();

        // Work with all card panels currently visible
        List<Card> visualized = new ArrayList<Card>();
        for (FCardPanel c : cardPanels) {
            Card card = c.getCard();
            if (visualized.contains(card)) { continue; }

            visualized.addAll(addArcsForCard(card, endpoints, combat));
        }
    }

    private List<Card> addArcsForCard(final Card c, final Map<Integer, Vector2> endpoints, final Combat combat) {
        List<Card> cardsVisualized = new ArrayList<Card>();
        cardsVisualized.add(c);

        Card paired = c.getPairedWith();
        if (paired != null) {
            arcsOther.add(new Vector2[] {
                endpoints.get(paired.getUniqueNumber()),
                endpoints.get(c.getUniqueNumber())
            });
            cardsVisualized.add(paired);
        }

        if (combat != null) {
            for (Card planeswalker : combat.getDefendingPlaneswalkers()) {
                List<Card> cards = combat.getAttackersOf(planeswalker);
                for (Card pwAttacker : cards) {
                    if (!planeswalker.equals(c) && !pwAttacker.equals(c)) { continue; }
                    arcsCombat.add(new Vector2[] {
                        endpoints.get(planeswalker.getUniqueNumber()),
                        endpoints.get(pwAttacker.getUniqueNumber())
                    });
                }
            }
            for (Card attackingCard : combat.getAttackers()) {
                List<Card> cards = combat.getBlockers(attackingCard);
                for (Card blockingCard : cards) {
                    if (!attackingCard.equals(c) && !blockingCard.equals(c)) { continue; }
                    arcsCombat.add(new Vector2[] {
                        endpoints.get(attackingCard.getUniqueNumber()),
                        endpoints.get(blockingCard.getUniqueNumber())
                    });
                    cardsVisualized.add(blockingCard);
                }
                cardsVisualized.add(attackingCard);
            }
        }

        return cardsVisualized;
    }

    public void drawArcs(Graphics g, FSkinColor color, List<Vector2[]> arcs) {
        for (Vector2[] p : arcs) {
            if (p[0] != null && p[1] != null) {
                g.drawArrow(ARROW_THICKNESS, ARROW_SIZE, color, p[1].x, p[1].y, p[0].x, p[0].y);
            }
        }
    }

    public void draw(final Graphics g) {
        assembleArcs();

        if (arcsCombat.isEmpty() && arcsOther.isEmpty()) { return; }

        // Get arrow colors from the theme or use default colors if the theme does not have them defined
        FSkinColor colorOther = FSkinColor.get(Colors.CLR_NORMAL_TARGETING_ARROW);
        if (colorOther.getAlpha() == 0) {
            colorOther = FSkinColor.get(Colors.CLR_ACTIVE).alphaColor(153f / 255f);
        }
        FSkinColor colorCombat = FSkinColor.get(Colors.CLR_COMBAT_TARGETING_ARROW);
        if (colorCombat.getAlpha() == 0) {
            colorCombat = FSkinColor.getStandardColor(new Color(1, 0, 0, 153 / 255f));
        }

        drawArcs(g, colorOther, arcsOther);
        drawArcs(g, colorCombat, arcsCombat);
    }
}
