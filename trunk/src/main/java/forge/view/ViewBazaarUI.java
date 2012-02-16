package forge.view;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import forge.Command;
import forge.control.ControlBazaarUI;
import forge.quest.data.bazaar.QuestStallManager;
import forge.view.bazaar.ViewStall;
import forge.view.toolbox.FLabel;
import forge.view.toolbox.FPanel;
import forge.view.toolbox.FSkin;


/** Lays out containers and borders for resizeable layout and
 *  instantiates top-level controller for bazaar UI. */
@SuppressWarnings("serial")
public class ViewBazaarUI extends FPanel {
    private final JPanel pnlAllStalls;
    private final ViewStall pnlSingleStall;
    private final ControlBazaarUI control;
    private FLabel previousSelected;

    /** Lays out containers and borders for resizeable layout and
     *  instantiates top-level controller for bazaar UI. */
    public ViewBazaarUI() {
        super();

        // Final inits
        this.pnlAllStalls = new JPanel();
        this.pnlSingleStall = new ViewStall(this);

        // Component styling
        this.setCornerDiameter(0);
        this.setBorderToggle(false);
        this.setBackgroundTexture(FSkin.getIcon(FSkin.Backgrounds.BG_TEXTURE));
        this.setLayout(new MigLayout("insets 0, gap 0"));
        pnlAllStalls.setOpaque(false);
        pnlAllStalls.setLayout(new MigLayout("insets 0, gap 0, wrap, align center"));

        // Layout
        this.add(pnlAllStalls, "w 25%!, h 100%!");
        this.add(pnlSingleStall, "w 75%!, h 100%!");

        // Instantiate control
        control = new ControlBazaarUI(this);
        control.initBazaar();
        previousSelected = ((FLabel) pnlAllStalls.getComponent(0));
    }

    /** */
    public void populateStalls() {
        for (final String s : QuestStallManager.getStallNames()) {
            final FLabel lbl = new FLabel.Builder().text(s + "  ")
                    .fontAlign(SwingConstants.RIGHT).iconInBackground(true)
                    .fontScaleFactor(0.3).opaque(true).hoverable(true)
                    .icon(QuestStallManager.getStall(s).getIcon()).selectable(true).build();

            pnlAllStalls.add(lbl, "h 80px!, w 90%!, gap 0 0 10px 10px");

            lbl.setCommand(new Command() {
                @Override
                public void execute() {
                    if (previousSelected != null) { previousSelected.setSelected(false); }
                    previousSelected = lbl;
                    control.showStall(s);
                }
            });
        }
    }

    /** */
    public void refreshLastInstance() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                pnlSingleStall.updateStall();
            }
        });
    }

    /**
     * TODO: Write javadoc for this method.
     * @return {@link javax.swing.JPanel}
     */
    public JPanel getPnlAllStalls() {
        return this.pnlAllStalls;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return {@link forge.view.bazaar.ViewStall}
     */
    public ViewStall getPnlSingleStall() {
        return this.pnlSingleStall;
    }
}
