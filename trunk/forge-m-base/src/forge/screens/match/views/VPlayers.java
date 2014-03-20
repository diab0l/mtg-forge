package forge.screens.match.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import forge.game.GameType;
import forge.game.card.Card;
import forge.game.card.CardFactoryUtil;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.model.FModel;
import forge.screens.match.FControl;
import forge.screens.match.views.VHeader.HeaderDropDown;
import forge.toolbox.FLabel;
import forge.utils.ForgePreferences.FPref;

public class VPlayers extends HeaderDropDown {
    private Map<Player, InfoLabel[]> infoLabels;
    private InfoLabel stormLabel;

    public VPlayers() {
        infoLabels = new HashMap<Player, InfoLabel[]>();
        for (final Player p : FControl.getSortedPlayers()) {
            // Create and store labels detailing various non-critical player info.
            final InfoLabel name = add(new InfoLabel());
            final InfoLabel life = add(new InfoLabel());
            final InfoLabel hand = add(new InfoLabel());
            final InfoLabel draw = add(new InfoLabel());
            final InfoLabel prevention = add(new InfoLabel());
            final InfoLabel keywords = add(new InfoLabel());
            final InfoLabel antes = add(new InfoLabel());
            final InfoLabel cmd = add(new InfoLabel());
            infoLabels.put(p, new InfoLabel[] { name, life, hand, draw, prevention, keywords, antes, cmd });

            name.setText(p.getName());
        }

        stormLabel = add(new InfoLabel());
    }

    @Override
    public void update() {
        for (Entry<Player, InfoLabel[]> rr : infoLabels.entrySet()) {
            Player p0 = rr.getKey();
            final InfoLabel[] temp = rr.getValue();
            temp[1].setText("Life: " + String.valueOf(p0.getLife()) + "  |  Poison counters: "
                    + String.valueOf(p0.getPoisonCounters()));
            temp[2].setText("Maximum hand size: " + String.valueOf(p0.getMaxHandSize()));
            temp[3].setText("Cards drawn this turn: " + String.valueOf(p0.getNumDrawnThisTurn()));
            temp[4].setText("Damage Prevention: " + String.valueOf(p0.getPreventNextDamageTotalShields()));
            if (!p0.getKeywords().isEmpty()) {
                temp[5].setText(p0.getKeywords().toString());
            }
            else {
                temp[5].setText("");
            }
            if (FModel.getPreferences().getPrefBoolean(FPref.UI_ANTE)) {
                List<Card> list = p0.getCardsIn(ZoneType.Ante);
                StringBuilder sb = new StringBuilder();
                sb.append("Ante'd: ");
                for (int i = 0; i < list.size(); i++) {
                    sb.append(list.get(i));
                    if (i < (list.size() - 1)) {
                        sb.append(", ");
                    }
                }
                temp[6].setText(sb.toString());
            }
            if (p0.getGame().getRules().getGameType() == GameType.Commander) {
                temp[7].setText(CardFactoryUtil.getCommanderInfo(p0));
            }
        }
        stormLabel.setText("Storm count: " + FControl.getGame().getStack().getCardsCastThisTurn().size());
    }

    @Override
    protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
        return new ScrollBounds(visibleWidth, visibleHeight);
    }

    private class InfoLabel extends FLabel {
        private InfoLabel() {
            super(new FLabel.Builder());
        }
    }

    @Override
    public int getCount() {
        return infoLabels.size();
    }
}
