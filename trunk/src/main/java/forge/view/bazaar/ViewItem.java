package forge.view.bazaar;

import java.awt.Font;

import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import forge.AllZone;
import forge.Command;
import forge.Singletons;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FTextArea;
import forge.quest.data.QuestAssets;
import forge.quest.data.item.IQuestStallPurchasable;

/** An update-able panel instance representing a single item. */
@SuppressWarnings("serial")
public class ViewItem extends FPanel {
    private final FLabel lblIcon, lblName, lblPrice, btnPurchase;
    private final FTextArea tarDesc;
    private IQuestStallPurchasable item;

    /** An update-able panel instance representing a single item. */
    public ViewItem() {
        // Final inits
        this.lblIcon = new FLabel.Builder().iconScaleFactor(1).iconInBackground(true).build();
        this.lblName = new FLabel.Builder().fontStyle(Font.BOLD).build();
        this.lblPrice = new FLabel.Builder().fontStyle(Font.BOLD).fontScaleFactor(0.8).build();
        this.tarDesc = new FTextArea();
        this.btnPurchase = new FLabel.Builder().text("Buy").opaque(true).fontScaleFactor(0.2).hoverable(true).build();

        this.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        // Layout
        this.setLayout(new MigLayout("insets 0, gap 0"));

        this.add(this.lblIcon, "w 100px!, h n:90%:100px, ay center, span 1 3, gap 5px 5px 5px 5px");
        this.add(this.lblName, "pushx, w 60%!, h 22px!, gap 0 0 5px 5px");
        this.add(this.btnPurchase, "w 80px!, h 80px!, ay center, span 1 3, gap 0 15px 0 0, wrap");

        this.add(this.tarDesc, "w 60%!, gap 0 0 0 10px, wrap");
        this.add(this.lblPrice, "w 60%!, h 20px!, gap 0 0 0 5px");

        this.btnPurchase.setCommand(new Command() {
            @Override
            public void execute() {
                final QuestAssets qA = AllZone.getQuest().getAssets();
                final int cost = ViewItem.this.getItem().getBuyingPrice(qA);
                if ((qA.getCredits() - cost) >= 0) {
                    qA.subtractCredits(cost);
                    qA.addCredits(ViewItem.this.getItem().getSellingPrice(qA));
                    ViewItem.this.getItem().onPurchase(qA);
                    AllZone.getQuest().save();
                }
                Singletons.getView().getViewBazaar().refreshLastInstance();
            }
        });
    }

    /**
     * @param i0
     *            &emsp; {@link forge.quest.data.item.IQuestStallPurchasable}
     */
    public void setItem(final IQuestStallPurchasable i0) {
        this.item = i0;
    }

    /** @return {@link forge.quest.data.item.IQuestStallPurchasable} */
    public IQuestStallPurchasable getItem() {
        return this.item;
    }

    /** Updates this panel with current item stats. */
    public void update() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final QuestAssets qA = AllZone.getQuest().getAssets();
                ViewItem.this.lblIcon.setIcon(ViewItem.this.getItem().getIcon());
                ViewItem.this.lblName.setText(ViewItem.this.getItem().getPurchaseName());
                ViewItem.this.lblPrice.setText("Cost: " + String.valueOf(ViewItem.this.getItem().getBuyingPrice(qA))
                        + " credits");
                ViewItem.this.tarDesc.setText(ViewItem.this.getItem().getPurchaseDescription(qA));

                if (qA.getCredits() < ViewItem.this.getItem().getBuyingPrice(qA)) {
                    ViewItem.this.btnPurchase.setEnabled(false);
                }

                ViewItem.this.revalidate();
                ViewItem.this.repaint();
            }
        });
    }
}
