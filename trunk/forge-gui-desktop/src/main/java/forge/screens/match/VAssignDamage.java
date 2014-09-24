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

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import forge.gui.SOverlayUtils;
import forge.match.MatchUtil;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FScrollPane;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.view.CardView;
import forge.view.FDialog;
import forge.view.GameEntityView;
import forge.view.PlayerView;
import forge.view.arcane.CardPanel;

/**
 * Assembles Swing components of assign damage dialog.
 * 
 * This needs a JDialog to maintain a modal state.
 * Without the modal state, the PhaseHandler automatically
 * moves forward to phase Main2 without assigning damage.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public class VAssignDamage {
    // Width and height of blocker dialog
    private final int wDlg = 700;
    private final int hDlg = 500;
    private final FDialog dlg = new FDialog();

    // Damage storage
    private final int totalDamageToAssign;

    private boolean attackerHasDeathtouch = false;
    private boolean attackerHasTrample = false;
    private boolean attackerHasInfect = false;
    private boolean overrideCombatantOrder = false;

    private final GameEntityView defender;

    private final JLabel lblTotalDamage = new FLabel.Builder().text("Available damage points: Unknown").build();
    private final JLabel lblAssignRemaining = new FLabel.Builder().text("Distribute the remaining damage points among lethally wounded entities").build();
    //  Label Buttons
    private final FButton btnOK    = new FButton("OK");
    private final FButton btnReset = new FButton("Reset");
    private final FButton btnAuto  = new FButton("Auto");

    
    private static class DamageTarget {
        public final CardView card;
        public final JLabel label;
        public int damage;

        public DamageTarget(final CardView c, final JLabel lbl) {
            card = c;
            label = lbl;
        }
    }

    // Indexes of defenders correspond to their indexes in the damage list and labels.
    private final List<DamageTarget> defenders = Lists.newArrayList(); // NULL in this map means defender
    private final Map<CardView, DamageTarget> damage = Maps.newHashMap(); // NULL in this map means defender

    private boolean canAssignTo(final CardView card) {
        for (DamageTarget dt : defenders) {
            if (dt.card == card ) return true;
            if (getDamageToKill(dt.card) > dt.damage )
                return false;
        }
        throw new RuntimeException("Asking to assign damage to object which is not present in defenders list");
    }

    // Mouse actions
    private final MouseAdapter mad = new MouseAdapter() {
        @Override
        public void mouseEntered(final MouseEvent evt) {
            CardView source = ((CardPanel) evt.getSource()).getCard();
            if (!damage.containsKey(source)) source = null; // to get player instead of fake card

            final FSkin.Colors brdrColor = VAssignDamage.this.canAssignTo(source) ? FSkin.Colors.CLR_ACTIVE : FSkin.Colors.CLR_INACTIVE;
            ((CardPanel) evt.getSource()).setBorder(new FSkin.LineSkinBorder(FSkin.getColor(brdrColor), 2));
        }

        @Override
        public void mouseExited(final MouseEvent evt) {
            ((CardPanel) evt.getSource()).setBorder((Border)null);
        }

        @Override
        public void mousePressed(final MouseEvent evt) {
            CardView source = ((CardPanel) evt.getSource()).getCard(); // will be NULL for player

            boolean meta = evt.isControlDown();
            boolean isLMB = SwingUtilities.isLeftMouseButton(evt);
            boolean isRMB = SwingUtilities.isRightMouseButton(evt);
            
            if ( isLMB || isRMB)
                assignDamageTo(source, meta, isLMB);
        }
    };

    /** Constructor.
     * 
     * @param attacker {@link forge.game.card.Card}
     * @param blockers List<{@link forge.game.card.Card}>
     * @param damage0 int
     * @param defender GameEntity that's bein attacked
     * @param overrideOrder override combatant order

     */
    public VAssignDamage(final CardView attacker, final List<CardView> blockers, final int damage0, final GameEntityView defender, boolean overrideOrder) {
        dlg.setTitle("Assign damage dealt by " + attacker);

        // Set damage storage vars
        this.totalDamageToAssign = damage0;
        this.defender = defender;
        this.attackerHasDeathtouch = attacker.getOriginal().hasDeathtouch();
        this.attackerHasInfect = attacker.getOriginal().hasInfect();
        this.attackerHasTrample = defender != null && attacker.getOriginal().hasTrample();
        this.overrideCombatantOrder = overrideOrder;

        // Top-level UI stuff
        final JPanel overlay = SOverlayUtils.genericOverlay();
        final SkinnedPanel pnlMain = new SkinnedPanel();
        pnlMain.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        // Attacker area
        final CardPanel pnlAttacker = new CardPanel(attacker);
        pnlAttacker.setOpaque(false);
        pnlAttacker.setCardBounds(0, 0, 105, 150);

        final JPanel pnlInfo = new JPanel(new MigLayout("insets 0, gap 0, wrap"));
        pnlInfo.setOpaque(false);
        pnlInfo.add(lblTotalDamage, "gap 0 0 20px 5px");
        pnlInfo.add(new FLabel.Builder().text("Left click: Assign 1 damage. (Left Click + Control): Assign remaining damage up to lethal").build(), "gap 0 0 0 5px");
        pnlInfo.add(new FLabel.Builder().text("Right click: Unassign 1 damage. (Right Click + Control): Unassign all damage.").build(), "gap 0 0 0 5px");

        // Defenders area
        final JPanel pnlDefenders = new JPanel();
        pnlDefenders.setOpaque(false);
        int cols = attackerHasTrample ? blockers.size() + 1 : blockers.size();
        final String wrap = "wrap " +  Integer.toString(cols);
        pnlDefenders.setLayout(new MigLayout("insets 0, gap 0, ax center, " + wrap));

        final FScrollPane scrDefenders = new FScrollPane(pnlDefenders, false);

        // Top row of cards...
        for (final CardView c : blockers) {
            DamageTarget dt = new DamageTarget(c, new FLabel.Builder().text("0").fontSize(18).fontAlign(SwingConstants.CENTER).build());
            this.damage.put(c, dt);
            this.defenders.add(dt);
            addPanelForDefender(pnlDefenders, c);
        }

        if (attackerHasTrample) {
            DamageTarget dt = new DamageTarget(null, new FLabel.Builder().text("0").fontSize(18).fontAlign(SwingConstants.CENTER).build());
            this.damage.put(null, dt);
            this.defenders.add(dt);
            CardView fakeCard = null;
            if (defender instanceof CardView) 
                fakeCard = (CardView)defender;
            else if (defender instanceof PlayerView) { 
                fakeCard = new CardView(true);
                fakeCard.getOriginal().setName(this.defender.toString());
                final PlayerView p = (PlayerView)defender;
                fakeCard.setOwner(p);
                fakeCard.setController(p);
                fakeCard.getOriginal().setImageKey(CMatchUI.SINGLETON_INSTANCE.avatarImages.get(p.getLobbyPlayer()));
            }

            addPanelForDefender(pnlDefenders, fakeCard);
        }        

        // Add "opponent placeholder" card if trample allowed
        // If trample allowed, make card placeholder

        // ... bottom row of labels.
        for (DamageTarget l : defenders) {
            pnlDefenders.add(l.label, "w 145px!, h 30px!, gap 5px 5px 0 5px");
        }

        btnOK.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent arg0) { finish(); } });
        btnReset.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent arg0) { resetAssignedDamage(); initialAssignDamage(false); } });
        btnAuto.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent arg0) { resetAssignedDamage(); initialAssignDamage(true); finish(); } });

        // Final UI layout
        pnlMain.setLayout(new MigLayout("insets 0, gap 0, wrap 2, ax center"));
        pnlMain.add(pnlAttacker, "w 125px!, h 160px!, gap 50px 0 0 15px");
        pnlMain.add(pnlInfo, "gap 20px 0 0 15px");
        pnlMain.add(scrDefenders, "w 96%!, gap 2% 0 0 0, pushy, growy, ax center, span 2");
        pnlMain.add(lblAssignRemaining, "w 96%!, gap 2% 0 0 0, ax center, span 2");

        JPanel pnlButtons = new JPanel(new MigLayout("insets 0, gap 0, ax center"));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnAuto, "w 110px!, h 30px!, gap 0 10px 0 0");
        pnlButtons.add(btnOK, "w 110px!, h 30px!, gap 0 10px 0 0");
        pnlButtons.add(btnReset, "w 110px!, h 30px!");

        pnlMain.add(pnlButtons, "ax center, w 350px!, gap 10px 10px 10px 10px, span 2");
        overlay.add(pnlMain);
        
        pnlMain.getRootPane().setDefaultButton(btnOK);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                btnAuto.requestFocusInWindow();
            }
        });

        initialAssignDamage(false);
        SOverlayUtils.showOverlay();

        this.dlg.setUndecorated(true);
        this.dlg.setContentPane(pnlMain);
        this.dlg.setSize(new Dimension(wDlg, hDlg));
        this.dlg.setLocation((overlay.getWidth() - wDlg) / 2, (overlay.getHeight() - hDlg) / 2);
        this.dlg.setModalityType(ModalityType.APPLICATION_MODAL);
        this.dlg.setVisible(true);
    }

    /**
     * TODO: Write javadoc for this method.
     * @param pnlDefenders
     * @param defender
     */
    private void addPanelForDefender(final JPanel pnlDefenders, final CardView defender) {
        final CardPanel cp = new CardPanel(defender);
        cp.setCardBounds(0, 0, 105, 150);
        cp.setOpaque(true);
        pnlDefenders.add(cp, "w 145px!, h 170px!, gap 5px 5px 3px 3px, ax center");
        cp.addMouseListener(mad);
    }

    /**
     * TODO: Write javadoc for this method.
     * @param source
     * @param meta
     * @param isLMB
     */
    private void assignDamageTo(CardView source, final boolean meta, final boolean isAdding) {
        if ( !damage.containsKey(source) ) 
            source = null;

        // If trying to assign to the defender, follow the normal assignment rules
        // No need to check for "active" creature assignee when overiding combatant order
        if ((source == null || source == this.defender || !this.overrideCombatantOrder) && isAdding && 
                !VAssignDamage.this.canAssignTo(source)) {
            return;
        }

        // If lethal damage has already been assigned just act like it's 0.
        int lethalDamage = getDamageToKill(source);
        int damageItHad = damage.get(source).damage;
        int leftToKill = Math.max(0, lethalDamage - damageItHad);
    
        int damageToAdd = isAdding ? 1 : -1;
    
        int leftToAssign = getRemainingDamage();
        // Left click adds damage, right click substracts damage.
        // Hold Ctrl to assign lethal damage, Ctrl-click again on a creature with lethal damage to assign all available damage to it
        if ( meta )  {
            if (isAdding) {
                damageToAdd = leftToKill > 0 ? leftToKill : leftToAssign;
            } else {
                damageToAdd = damageItHad > lethalDamage ? lethalDamage - damageItHad : -damageItHad;
            }
        }
        
        if ( damageToAdd > leftToAssign )
            damageToAdd = leftToAssign;
        
        // cannot assign first blocker less than lethal damage except when overriding order
        boolean isFirstBlocker = defenders.get(0).card == source;
        if (!this.overrideCombatantOrder && isFirstBlocker && damageToAdd + damageItHad < lethalDamage )
            return;

        if ( 0 == damageToAdd || damageToAdd + damageItHad < 0) 
            return;
        
        addDamage(source, damageToAdd);
        checkDamageQueue();
        updateLabels();
    }

    private void checkDamageQueue() {
        // Clear out any Damage that shouldn't be assigned to other combatants
        boolean hasAliveEnemy = false;
        for(DamageTarget dt : defenders) {
            int lethal = getDamageToKill(dt.card);
            int damage = dt.damage;
            // If overriding combatant order, make sure everything has lethal if defender has damage assigned to it
            // Otherwise, follow normal combatant order
            if ( hasAliveEnemy && (!this.overrideCombatantOrder || dt.card == null || dt.card == this.defender))
                dt.damage = 0;
            else
                hasAliveEnemy |= damage < lethal;
        }
    }

    // will assign all damage to defenders and rest to player, if present
    private void initialAssignDamage(boolean toAllBlockers) {
        if (!toAllBlockers && this.overrideCombatantOrder) {
            // Don't auto assign the first damage when overriding combatant order
            updateLabels();
            return;
        }

        int dmgLeft = totalDamageToAssign;
        DamageTarget dtLast = null;
        for(DamageTarget dt : defenders) { // MUST NOT RUN WITH EMPTY collection
            int lethal = getDamageToKill(dt.card);
            int damage = Math.min(lethal, dmgLeft);
            addDamage(dt.card, damage);
            dmgLeft -= damage;
            dtLast = dt;
            if ( dmgLeft <= 0 || !toAllBlockers ) break;
        }
        if ( dmgLeft < 0 )
            throw new RuntimeException("initialAssignDamage managed to assign more damage than it could");
        if (toAllBlockers && dmgLeft > 0) { 
            // flush the remaining damage into last defender if assigning all damage
            addDamage(dtLast.card, dmgLeft );
        }
        updateLabels();
    }

    /** Reset Assign Damage back to how it was at the beginning. */
    private void resetAssignedDamage() {
        for(DamageTarget dt : defenders)
            dt.damage = 0;
    }
    
    private void addDamage(final CardView card, int addedDamage) {
        // If we don't have enough left or we're trying to unassign too much return
        final int canAssign = getRemainingDamage();
        if (canAssign < addedDamage) {
            addedDamage = canAssign;
        }

        final DamageTarget dt = damage.get(card);
        dt.damage = Math.max(0, addedDamage + dt.damage); 
    }


    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    private int getRemainingDamage() {
        int spent = 0;
        for(DamageTarget dt : defenders) {
            spent += dt.damage;
        }
        return totalDamageToAssign - spent;
    }

    /** Updates labels and other UI elements.
     * @param index index of the last assigned damage*/
    private void updateLabels() {

        int damageLeft = totalDamageToAssign;
        boolean allHaveLethal = true;
        
        for ( DamageTarget dt : defenders )
        {
            int dmg = dt.damage;
            damageLeft -= dmg;
            int lethal = getDamageToKill(dt.card);
            int overkill = dmg - lethal;
            StringBuilder sb = new StringBuilder();
            sb.append(dmg);
            if( overkill >= 0 ) { 
                sb.append(" (Lethal");
                if( overkill > 0 ) 
                    sb.append(" +").append(overkill);
                sb.append(")");
            }
            allHaveLethal &= dmg >= lethal;
            dt.label.setText(sb.toString());
        }

        this.lblTotalDamage.setText(String.format("Available damage points: %d (of %d)", damageLeft, this.totalDamageToAssign));
        btnOK.setEnabled(damageLeft == 0);
        lblAssignRemaining.setVisible(allHaveLethal && damageLeft > 0);
    }

    // Dumps damage onto cards. Damage must be stored first, because if it is
    // assigned dynamically, the cards die off and further damage to them can't
    // be modified.
    private void finish() {
        if ( getRemainingDamage() > 0 ) 
            return;
        
        dlg.dispose();
        SOverlayUtils.hideOverlay();
    }
    
    /**
     * TODO: Write javadoc for this method.
     * @param card
     * @return
     */
    private int getDamageToKill(final CardView card) {
        int lethalDamage = 0;
        if (card == null) {
            if (defender instanceof PlayerView) {
                final PlayerView p = (PlayerView)defender;
                lethalDamage = attackerHasInfect ? MatchUtil.getGameView().getPoisonCountersToLose() - p.getPoisonCounters() : p.getLife();
            } else if (defender instanceof CardView) { // planeswalker
                final CardView pw = (CardView)defender;
                lethalDamage = pw.getOriginal().getLoyalty();
            }
        } else {
            lethalDamage = VAssignDamage.this.attackerHasDeathtouch ? 1 : Math.max(0, card.getLethalDamage());
        }
        return lethalDamage;
    }

    public Map<CardView, Integer> getDamageMap() {
        Map<CardView, Integer> result = Maps.newHashMapWithExpectedSize(defenders.size());
        for (DamageTarget dt : defenders)
            result.put(dt.card, dt.damage);
        return result;
    }
}
