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

package forge.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.commons.lang3.StringUtils;

import forge.Card;
import forge.CardUtil;
import forge.CounterType;
import forge.GameEntity;
import forge.Singletons;
import forge.card.CardCharacteristicName;
import forge.card.CardEdition;
import forge.card.CardType;
import forge.card.ColorSet;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FTextArea;
import forge.item.IPaperCard;
import forge.item.InventoryItemFromSet;
import forge.item.OpenablePack;
import forge.item.PreconDeck;
import forge.util.Lang;

/**
 * The class CardDetailPanel. Shows the details of a card.
 * 
 * @author Clemens Koza
 * @version V0.0 17.02.2010
 */
public class CardDetailPanel extends FPanel {
    /** Constant <code>serialVersionUID=-8461473263764812323L</code>. */
    private static final long serialVersionUID = -8461473263764812323L;

    private static Color purple = new Color(14381203);

    private final FLabel nameCostLabel;
    private final FLabel typeLabel;
    private final FLabel powerToughnessLabel;
    private final FLabel idLabel;
    private final JLabel setInfoLabel;
    private final FTextArea cdArea;
    private final FScrollPane scrArea;

    public CardDetailPanel(final Card card) {
        super();
        this.setLayout(new GridBagLayout());
        this.setBorder(new EtchedBorder());
        this.setBorderToggle(false);

        GridBagConstraints labelConstrains = new GridBagConstraints();
        labelConstrains.fill = GridBagConstraints.BOTH;
        labelConstrains.gridx = 0;
        labelConstrains.gridy = 0;
        labelConstrains.weightx = 1.0;

        final JPanel cdLabels = new JPanel(new GridLayout(0, 1, 0, 5));
        final FSkin.JComponentSkin<JPanel> cdLabelsSkin = FSkin.get(cdLabels);
        cdLabelsSkin.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME));
        cdLabelsSkin.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        this.nameCostLabel = new FLabel.Builder().build();
        this.typeLabel = new FLabel.Builder().build();
        this.powerToughnessLabel = new FLabel.Builder().build();
        cdLabels.add(this.nameCostLabel);
        cdLabels.add(this.typeLabel);
        cdLabels.add(this.powerToughnessLabel);

        final JPanel idr = new JPanel(new GridBagLayout());
        FSkin.JComponentSkin<JPanel> idrSkin = FSkin.get(idr);
        idrSkin.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME));
        idrSkin.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        final GridBagConstraints c1 = new GridBagConstraints();
        final GridBagConstraints c2 = new GridBagConstraints();

        c1.fill = GridBagConstraints.HORIZONTAL;

        c1.gridwidth = 2;
        c1.weightx = 1.0;
        this.idLabel = new FLabel.Builder().build();
        idr.add(this.idLabel, c1);

        c2.gridwidth = 1;
        c2.weightx = 0.3;
        c2.fill = GridBagConstraints.HORIZONTAL;
        this.setInfoLabel = new JLabel();
        idr.add(this.setInfoLabel, c2);

        cdLabels.add(idr);

        this.add(cdLabels, labelConstrains);
        this.nameCostLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.typeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.powerToughnessLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // cdLabel7.setSize(100, cdLabel7.getHeight());

        this.setInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        //4, 12
        this.cdArea = new FTextArea();
        this.cdArea.setFont(new java.awt.Font("Dialog", 0, 14));
        this.cdArea.setBorder(new EmptyBorder(4, 4, 4, 4));
        this.cdArea.setOpaque(false);
        this.scrArea = new FScrollPane(this.cdArea);

        GridBagConstraints areaConstraints = new GridBagConstraints();
        areaConstraints.fill = GridBagConstraints.BOTH;
        areaConstraints.gridx = 0;
        areaConstraints.gridy = 1;
        areaConstraints.weightx = 1.0;
        areaConstraints.weighty = 1.0;
        this.add(scrArea, areaConstraints);

        this.nameCostLabel.setFont(new java.awt.Font("Dialog", 0, 14));
        this.typeLabel.setFont(new java.awt.Font("Dialog", 0, 14));
        this.powerToughnessLabel.setFont(new java.awt.Font("Dialog", 0, 14));
        this.idLabel.setFont(new java.awt.Font("Dialog", 0, 14));

        java.awt.Font f = new java.awt.Font("Dialog", 0, 14);
        f = f.deriveFont(java.awt.Font.BOLD);
        this.setInfoLabel.setFont(f);

        this.setCard(card);
    }
    
    public String getItemDescription(InventoryItemFromSet i) {
        if( i instanceof OpenablePack )
            return ((OpenablePack)i).getDescription();
        if( i instanceof PreconDeck)
            return ((PreconDeck) i).getDescription();
        return i.getName(); 
    }

    public final void setItem(InventoryItemFromSet item) {
        nameCostLabel.setText(item.getName());
        typeLabel.setVisible(false);
        powerToughnessLabel.setVisible(false);
        idLabel.setText(null);
        cdArea.setText(getItemDescription(item));
        setBorder(getBorder(item instanceof IPaperCard ? ((IPaperCard)item).getRules().getColor() : null, false));

        String set = item.getEdition();
        setInfoLabel.setText(set);
        setInfoLabel.setToolTipText("");
        if (StringUtils.isEmpty(set)) {
            setInfoLabel.setOpaque(false);
            setInfoLabel.setBorder(null);
        } else {
            CardEdition edition = Singletons.getModel().getEditions().get(set);
            if (null != edition) {
                setInfoLabel.setToolTipText(edition.getName());
            }
            
            this.setInfoLabel.setOpaque(true);
            this.setInfoLabel.setBackground(Color.BLACK);
            this.setInfoLabel.setForeground(Color.WHITE);
            this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrArea.getVerticalScrollBar().setValue(scrArea.getVerticalScrollBar().getMinimum());
            }
        });
    }

    /** {@inheritDoc} */
    public final void setCard(final Card card) {
        this.nameCostLabel.setText("");
        this.typeLabel.setVisible(true);
        this.typeLabel.setText("");
        this.powerToughnessLabel.setVisible(true);
        this.powerToughnessLabel.setText("");
        this.idLabel.setText("");
        this.setInfoLabel.setText("");
        this.setInfoLabel.setToolTipText("");
        this.setInfoLabel.setOpaque(false);
        this.setInfoLabel.setBorder(null);
        this.cdArea.setText("");
        if( card == null ) {
            this.setBorder(getBorder(null, false));
            return;
        }

        this.setBorder(getBorder(CardUtil.getColors(card), card.isFaceDown()));
            
        final boolean canShowThis = Singletons.getControl().mayShowCard(card);
        if (canShowThis) {
            if (card.getManaCost().isNoCost()) {
                this.nameCostLabel.setText(card.getName());
            } else {
                String manaCost = card.getManaCost().toString();
                if ( card.isSplitCard() && card.getCurState() == CardCharacteristicName.Original) {
                    manaCost = card.getRules().getMainPart().getManaCost().toString() + " // " + card.getRules().getOtherPart().getManaCost().toString();
                }
                this.nameCostLabel.setText(card.getName() + " - " + manaCost);
            }
            this.typeLabel.setText(formatCardType(card));
            
            String set = card.getCurSetCode();
            this.setInfoLabel.setText(set);
            if (null != set && !set.isEmpty()) {
                CardEdition edition = Singletons.getModel().getEditions().get(set);
                if (null == edition) {
                    setInfoLabel.setToolTipText(card.getRarity().name());
                } else {
                    setInfoLabel.setToolTipText(String.format("%s (%s)", edition.getName(), card.getRarity().name()));
                }
                
                this.setInfoLabel.setOpaque(true);
                switch(card.getRarity()) {
                case Uncommon:
                    this.setInfoLabel.setBackground(Color.LIGHT_GRAY);
                    this.setInfoLabel.setForeground(Color.BLACK);
                    this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    break;

                case Rare:
                    this.setInfoLabel.setBackground(Color.YELLOW);
                    this.setInfoLabel.setForeground(Color.BLACK);
                    this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    break;

                case MythicRare:
                    this.setInfoLabel.setBackground(Color.RED);
                    this.setInfoLabel.setForeground(Color.BLACK);
                    this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    break; 

                case Special:
                    // "Timeshifted" or other Special Rarity Cards
                    this.setInfoLabel.setBackground(CardDetailPanel.purple);
                    this.setInfoLabel.setForeground(Color.BLACK);
                    this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    break;

                default: //case BasicLand: + case Common:
                    this.setInfoLabel.setBackground(Color.BLACK);
                    this.setInfoLabel.setForeground(Color.WHITE);
                    this.setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                    break;
                }
            }
        } else {
            this.nameCostLabel.setText("Morph");
            this.typeLabel.setText("Creature");
        }
        
        StringBuilder ptText = new StringBuilder();
        if (card.isCreature()) {
            ptText.append(card.getNetAttack()).append(" / ").append(card.getNetDefense());
        }

        if (card.isPlaneswalker()) {
            if (ptText.length() > 0) {
                ptText.insert(0, "P/T: ");
                ptText.append(" - ").append("Loy: ");
            } else {
                ptText.append("Loyalty: ");
            }

            int loyalty = card.getCounters(CounterType.LOYALTY);
            if (loyalty == 0) {
                loyalty = card.getBaseLoyalty();
            }
            ptText.append(loyalty);
        }

        this.powerToughnessLabel.setText(ptText.toString());

        this.idLabel.setText(card.getUniqueNumber() > 0 ? "Card ID  " + card.getUniqueNumber() : null);

        // fill the card text
        this.cdArea.setText(composeCardText(card, canShowThis));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrArea.getVerticalScrollBar().setValue(scrArea.getVerticalScrollBar().getMinimum());
            }
        });
    }
    
    private String composeCardText(final Card card, final boolean canShow) {
        final StringBuilder area = new StringBuilder();

        // Token
        if (card.isToken()) {
            area.append("Token");
        }

        if (canShow) {
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
        final CounterType[] counters = CounterType.values();
        for (final CounterType counter : counters) {
            if (card.getCounters(counter) != 0) {
                if (area.length() != 0) {
                    area.append("\n");
                }
                area.append(counter.getName() + " counters: ");
                area.append(card.getCounters(counter));
            }
        }

        if (card.isCreature()) {
            int damage = card.getDamage();
            if (damage > 0) {
                if (area.length() != 0) {
                    area.append("\n");
                }
                area.append("Damage: " + damage);
            }
            int assigned = card.getTotalAssignedDamage();
            if (assigned > 0) {
                if (area.length() != 0) {
                    area.append("\n");
                }
                area.append("Assigned Damage: " + assigned);
            }
        }
        if (card.isPlaneswalker()) {
            int assigned = card.getTotalAssignedDamage();
            if (assigned > 0) {
                if (area.length() != 0) {
                    area.append("\n");
                }
                area.append("Assigned Damage: " + assigned);
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
        final int preventNextDamage = card.getPreventNextDamageTotalShields();
        if (preventNextDamage > 0) {
            area.append("\n");
            area.append("Prevent the next ").append(preventNextDamage).append(" damage that would be dealt to ");
            area.append(card.getName()).append(" this turn.");
        }

        // top revealed
        if ((card.hasKeyword("Play with the top card of your library revealed.") || card
                .hasKeyword("Players play with the top card of their libraries revealed."))
                && card.getController() != null
                && (card.isInZone(ZoneType.Battlefield) || (card.isInZone(ZoneType.Command) && !card.isCommander()))
                && !card.getController().getZone(ZoneType.Library).isEmpty()) {
            area.append("\r\nTop card of your library: ");
            area.append(card.getController().getCardsIn(ZoneType.Library, 1));
            if (card.hasKeyword("Players play with the top card of their libraries revealed.")) {
                for (final Player p : card.getController().getAllOtherPlayers()) {
                    if (p.getZone(ZoneType.Library).isEmpty()) {
                        area.append(p.getName());
                        area.append("'s library is empty.");
                    } else {
                        area.append("\r\nTop card of ");
                        area.append(p.getName());
                        area.append("'s library: ");
                        area.append(p.getCardsIn(ZoneType.Library, 1));
                    }
                }
            }
        }

        // chosen type
        if (!card.getChosenType().equals("")) {
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

        // chosen player
        if (card.getChosenPlayer() != null) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("(chosen player: " + card.getChosenPlayer() + ")");
        }

        // named card
        if (!card.getNamedCard().equals("")) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("(named card: ");
            area.append(card.getNamedCard());
            area.append(")");
        }

        // equipping
        if (!card.getEquipping().isEmpty()) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("=Equipping ");
            area.append(card.getEquipping().get(0));
            area.append("=");
        }

        // equipped by
        if (!card.getEquippedBy().isEmpty()) {
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
                if (!Singletons.getControl().mayShowCard(c)) {
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
        if (!card.getEnchantedBy().isEmpty()) {
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
            area.append("Imprinting: ");
            for (final Iterator<Card> it = card.getImprinted().iterator(); it.hasNext();) {
                area.append(it.next());
                if (it.hasNext()) {
                    area.append(", ");
                }
            }
        }

        // Haunt
        if (!card.getHauntedBy().isEmpty()) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("Haunted by: ");
            for (final Iterator<Card> it = card.getHauntedBy().iterator(); it.hasNext();) {
                area.append(it.next());
                if (it.hasNext()) {
                    area.append(", ");
                }
            }
        }
        if (card.getHaunting() != null) {
            if (area.length() != 0) {
                area.append("\n");
            }
            area.append("Haunting " + card.getHaunting());
        }

        // must block
        if (card.getMustBlockCards() != null) {
            if (area.length() != 0) {
                area.append("\n");
            }
            String mustBlockThese = Lang.joinHomogenous(card.getMustBlockCards());
            area.append("Must block " + mustBlockThese);
        }
        return area.toString();
    }

    /** @return JLabel */
    public JLabel getNameCostLabel() {
        return this.nameCostLabel;
    }

    /** @return JLabel */
    public JLabel getTypeLabel() {
        return this.typeLabel;
    }

    /** @return JLabel */
    public JLabel getPowerToughnessLabel() {
        return this.powerToughnessLabel;
    }

    /** @return JLabel */
    public JLabel getSetInfoLabel() {
        return this.setInfoLabel;
    }

    /** @return JLabel */
    public JTextArea getCDArea() {
        return this.cdArea;
    }

    public static String formatCardType(final Card card) {
        final ArrayList<String> list = card.getType();
        final StringBuilder sb = new StringBuilder();
    
        final ArrayList<String> superTypes = new ArrayList<String>();
        final ArrayList<String> cardTypes = new ArrayList<String>();
        final ArrayList<String> subTypes = new ArrayList<String>();
        final boolean allCreatureTypes = list.contains("AllCreatureTypes");
    
        for (final String t : list) {
            if (allCreatureTypes && t.equals("AllCreatureTypes")) {
                continue;
            }
            if (CardType.isASuperType(t) && !superTypes.contains(t)) {
                superTypes.add(t);
            }
            if (CardType.isACardType(t) && !cardTypes.contains(t)) {
                cardTypes.add(t);
            }
            if (CardType.isASubType(t) && !subTypes.contains(t) && (!allCreatureTypes || !CardType.isACreatureType(t))) {
                subTypes.add(t);
            }
        }
    
        for (final String type : superTypes) {
            sb.append(type).append(" ");
        }
        for (final String type : cardTypes) {
            sb.append(type).append(" ");
        }
        if (!subTypes.isEmpty() || allCreatureTypes) {
            sb.append("- ");
        }
        if (allCreatureTypes) {
            sb.append("All creature types ");
        }
        for (final String type : subTypes) {
            sb.append(type).append(" ");
        }
    
        return sb.toString();
    }
    

    public static Border getBorder(ColorSet list, boolean faceDown) {
        // color info
        if (list == null) {
            return BorderFactory.createEmptyBorder(2, 2, 2, 2);
        }
        java.awt.Color color;

        if (faceDown) {
            color = Color.gray;
        } else if (list.isMulticolor()) {
            color = Color.orange;
        } else if (list.hasBlack()) {
            color = Color.black;
        } else if (list.hasGreen()) {
            color = new Color(0, 220, 39);
        } else if (list.hasWhite()) {
            color = Color.white;
        } else if (list.hasRed()) {
            color = Color.red;
        } else if (list.hasBlue()) {
            color = Color.blue;
        } else if (list.isColorless()) {
            color = Color.gray;
        } else {
            color = new Color(200, 0, 230); // If your card has a violet border,
                                            // something is wrong
        }

        if (color != Color.gray) {

            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();

            final int shade = 10;

            r -= shade;
            g -= shade;
            b -= shade;

            r = Math.max(0, r);
            g = Math.max(0, g);
            b = Math.max(0, b);

            color = new Color(r, g, b);

            return BorderFactory.createLineBorder(color, 2);
        } else {
            return BorderFactory.createLineBorder(Color.gray, 2);
        }
    }

}
