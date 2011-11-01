/**
 * CardDetailPanel.java
 *
 * Created on 17.02.2010
 */

package forge.gui.game;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import forge.AllZone;
import forge.Card;
import forge.CardContainer;
import forge.Constant.Zone;
import forge.Counters;
import forge.GameEntity;
import forge.GuiDisplayUtil;
import forge.Singletons;

/**
 * The class CardDetailPanel. Shows the details of a card.
 * 
 * @author Clemens Koza
 * @version V0.0 17.02.2010
 */
public class CardDetailPanel extends JPanel implements CardContainer {
    /** Constant <code>serialVersionUID=-8461473263764812323L</code>. */
    private static final long serialVersionUID = -8461473263764812323L;

    private static Color purple = new Color(14381203);

    private Card card;

    private final JLabel nameCostLabel;
    private final JLabel typeLabel;
    private final JLabel powerToughnessLabel;
    private final JLabel damageLabel;
    private final JLabel idLabel;
    private final JLabel setInfoLabel;
    private final JTextArea cdArea;

    /**
     * <p>
     * Constructor for CardDetailPanel.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     */
    public CardDetailPanel(final Card card) {
        this.setLayout(new GridLayout(2, 0, 0, 5));
        this.setBorder(new EtchedBorder());

        final JPanel cdLabels = new JPanel(new GridLayout(0, 1, 0, 5));
        this.nameCostLabel = new JLabel();
        this.typeLabel = new JLabel();
        this.powerToughnessLabel = new JLabel();
        this.damageLabel = new JLabel();
        cdLabels.add(this.nameCostLabel);
        cdLabels.add(this.typeLabel);
        cdLabels.add(this.powerToughnessLabel);
        cdLabels.add(this.damageLabel);

        final JPanel idr = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridwidth = 2;
        c.weightx = 1.0;
        this.idLabel = new JLabel();
        idr.add(this.idLabel, c);

        c.gridwidth = 1;
        c.weightx = 0.3;
        this.setInfoLabel = new JLabel();
        idr.add(this.setInfoLabel, c);

        cdLabels.add(idr);

        this.add(cdLabels);
        this.nameCostLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.typeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.powerToughnessLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // cdLabel7.setSize(100, cdLabel7.getHeight());

        this.setInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        this.cdArea = new JTextArea(4, 12);
        this.add(new JScrollPane(this.cdArea));
        this.cdArea.setLineWrap(true);
        this.cdArea.setWrapStyleWord(true);

        if (!Singletons.getModel().getPreferences().lafFonts) {
            this.nameCostLabel.setFont(new java.awt.Font("Dialog", 0, 14));
            this.typeLabel.setFont(new java.awt.Font("Dialog", 0, 14));
            this.powerToughnessLabel.setFont(new java.awt.Font("Dialog", 0, 14));
            this.damageLabel.setFont(new java.awt.Font("Dialog", 0, 14));
            this.idLabel.setFont(new java.awt.Font("Dialog", 0, 14));

            java.awt.Font f = new java.awt.Font("Dialog", 0, 14);
            f = f.deriveFont(java.awt.Font.BOLD);
            this.setInfoLabel.setFont(f);

            this.cdArea.setFont(new java.awt.Font("Dialog", 0, 14));
        }

        this.setCard(card);
    }

    /** {@inheritDoc} */
    @Override
    public final void setCard(final Card card) {
        this.nameCostLabel.setText("");
        this.typeLabel.setText("");
        this.powerToughnessLabel.setText("");
        this.damageLabel.setText("");
        this.idLabel.setText("");
        this.setInfoLabel.setText("");
        this.setInfoLabel.setOpaque(false);
        this.setInfoLabel.setBorder(null);
        this.cdArea.setText("");
        this.setBorder(GuiDisplayUtil.getBorder(card));

        this.card = card;
        if (card == null) {
            return;
        }

        final boolean faceDown = card.isFaceDown() && (card.getController() != AllZone.getHumanPlayer());
        if (!faceDown) {
            if (card.getManaCost().equals("") || card.isLand()) {
                this.nameCostLabel.setText(card.getName());
            } else {
                this.nameCostLabel.setText(card.getName() + " - " + card.getManaCost());
            }
        } else {
            this.nameCostLabel.setText("Morph");
        }

        if (!faceDown) {
            this.typeLabel.setText(GuiDisplayUtil.formatCardType(card));
        } else {
            this.typeLabel.setText("Creature");
        }

        if (card.isCreature()) {
            this.powerToughnessLabel.setText(card.getNetAttack() + " / " + card.getNetDefense());
            this.damageLabel.setText("Damage: " + card.getDamage() + " Assigned Damage: "
                    + card.getTotalAssignedDamage());
        }
        if (card.isPlaneswalker()) {
            this.damageLabel.setText("Assigned Damage: " + card.getTotalAssignedDamage());
        }

        this.idLabel.setText("Card ID  " + card.getUniqueNumber());

        // rarity and set of a face down card should not be visible to the
        // opponent
        if (!card.isFaceDown() || card.getController().isHuman()) {
            this.setInfoLabel.setText(card.getCurSetCode());
        }

        if (!this.setInfoLabel.getText().equals("")) {
            this.setInfoLabel.setOpaque(true);
            final String csr = card.getCurSetRarity();
            if (csr.equals("Common") || csr.equals("Land")) {
                this.setInfoLabel.setBackground(Color.BLACK);
                this.setInfoLabel.setForeground(Color.WHITE);
                this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            } else if (csr.equals("Uncommon")) {
                this.setInfoLabel.setBackground(Color.LIGHT_GRAY);
                this.setInfoLabel.setForeground(Color.BLACK);
                this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            } else if (csr.equals("Rare")) {
                this.setInfoLabel.setBackground(Color.YELLOW);
                this.setInfoLabel.setForeground(Color.BLACK);
                this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            } else if (csr.equals("Mythic")) {
                this.setInfoLabel.setBackground(Color.RED);
                this.setInfoLabel.setForeground(Color.BLACK);
                this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            } else if (csr.equals("Special")) {
                // "Timeshifted" or other Special Rarity Cards
                this.setInfoLabel.setBackground(CardDetailPanel.purple);
                this.setInfoLabel.setForeground(Color.BLACK);
                this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }
            // cdLabel7.setText(card.getCurSetCode());
        }

        // fill the card text

        final StringBuilder area = new StringBuilder();

        // Token
        if (card.isToken()) {
            area.append("Token");
        }

        if (!faceDown) {
            // card text
            if (area.length() != 0) {
                area.append("\n");
            }
            String text = card.getText();
            // LEVEL [0-9]+-[0-9]+
            // LEVEL [0-9]+\+

            String regex = "LEVEL [0-9]+-[0-9]+ ";
            text = text.replaceAll(regex, "$0\r\n");

            regex = "LEVEL [0-9]+\\+ ";
            text = text.replaceAll(regex, "\r\n$0\r\n");

            // displays keywords that have dots in them a little better:
            regex = "\\., ";
            text = text.replaceAll(regex, ".\r\n");

            area.append(text);
        }

        if (card.isPhasedOut()) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("Phased Out");
        }

        // counter text
        final Counters[] counters = Counters.values();
        for (final Counters counter : counters) {
            if (card.getCounters(counter) != 0) {
                if (area.length() != 0) {
                    area.append("\n");
                }
                area.append(counter.getName() + " counters: ");
                area.append(card.getCounters(counter));
            }
        }

        // Regeneration Shields
        final int regenShields = card.getShield();
        if (regenShields > 0) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("Regeneration Shield(s): ").append(regenShields);
        }

        // Damage Prevention
        final int preventNextDamage = card.getPreventNextDamage();
        if (preventNextDamage > 0) {
            area.append("\n");
            area.append("Prevent the next ").append(preventNextDamage).append(" damage that would be dealt to ");
            area.append(card.getName()).append(" it this turn.");
        }

        // top revealed
        if (card.hasKeyword("Play with the top card of your library revealed.") && (card.getController() != null)
                && !card.getController().getZone(Zone.Library).isEmpty()) {
            area.append("\r\nTop card: ");
            area.append(card.getController().getCardsIn(Zone.Library, 1));
        }

        // chosen type
        if (card.getChosenType() != "") {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("(chosen type: ");
            area.append(card.getChosenType());
            area.append(")");
        }

        // chosen color
        if (!card.getChosenColor().isEmpty()) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("(chosen colors: ");
            area.append(card.getChosenColor());
            area.append(")");
        }

        // named card
        if (card.getNamedCard() != "") {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("(named card: ");
            area.append(card.getNamedCard());
            area.append(")");
        }

        // equipping
        if (card.getEquipping().size() > 0) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("=Equipping ");
            area.append(card.getEquipping().get(0));
            area.append("=");
        }

        // equipped by
        if (card.getEquippedBy().size() > 0) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("=Equipped by ");
            for (final Iterator<Card> it = card.getEquippedBy().iterator(); it.hasNext();) {
                area.append(it.next());
                if (it.hasNext()) {
                    area.append(", ");
                }
            }
            area.append("=");
        }

        // enchanting
        final GameEntity entity = card.getEnchanting();
        if (entity != null) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("*Enchanting ");

            if (entity instanceof Card) {
                final Card c = (Card) entity;
                if (c.isFaceDown() && c.getController().isComputer()) {
                    area.append("Morph (");
                    area.append(card.getUniqueNumber());
                    area.append(")");
                } else {
                    area.append(entity);
                }
            } else {
                area.append(entity);
            }
            area.append("*");
        }

        // enchanted by
        if (card.getEnchantedBy().size() > 0) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("*Enchanted by ");
            for (final Iterator<Card> it = card.getEnchantedBy().iterator(); it.hasNext();) {
                area.append(it.next());
                if (it.hasNext()) {
                    area.append(", ");
                }
            }
            area.append("*");
        }

        // controlling
        if (card.getGainControlTargets().size() > 0) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("+Controlling: ");
            for (final Iterator<Card> it = card.getGainControlTargets().iterator(); it.hasNext();) {
                area.append(it.next());
                if (it.hasNext()) {
                    area.append(", ");
                }
            }
            area.append("+");
        }

        // cloned via
        if (card.getCloneOrigin() != null) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("^Cloned via: ");
            area.append(card.getCloneOrigin().getName());
            area.append("^");
        }

        // Imprint
        if (!card.getImprinted().isEmpty()) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("^Imprinting: ");
            for (final Iterator<Card> it = card.getImprinted().iterator(); it.hasNext();) {
                area.append(it.next());
                if (it.hasNext()) {
                    area.append(", ");
                }
            }
            area.append("^");
        }

        // uncastable
        if (card.isUnCastable()) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("This card can't be cast.");
        }

        // must block
        if (!card.getMustBlockCards().isEmpty()) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("Must block an attacker");
        }

        if (card.hasAttachedCardsByMindsDesire()) {
            if (area.length() != 0) {
                area.append("\n");
            }
            final Card[] cards = card.getAttachedCardsByMindsDesire();
            area.append("=Attached: ");
            for (final Card c : cards) {
                area.append(c.getName());
                area.append(" ");
            }
            area.append("=");
        }

        this.cdArea.setText(area.toString());
    }

    /**
     * <p>
     * Getter for the field <code>card</code>.
     * </p>
     * 
     * @return a {@link forge.Card} object.
     */
    @Override
    public final Card getCard() {
        return this.card;
    }
}
