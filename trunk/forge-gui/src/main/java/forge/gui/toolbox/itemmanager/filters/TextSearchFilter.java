package forge.gui.toolbox.itemmanager.filters;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.Timer;

import forge.gui.toolbox.FTextField;
import forge.gui.toolbox.LayoutHelper;
import forge.gui.toolbox.itemmanager.ItemManager;
import forge.item.InventoryItem;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public abstract class TextSearchFilter<T extends InventoryItem> extends ItemFilter<T> {
    protected FTextField txtSearch;

    protected TextSearchFilter(ItemManager<T> itemManager0) {
        super(itemManager0);
    }

    @Override
    public boolean isEmpty() {
        return txtSearch.isEmpty();
    }

    @Override
    public void reset() {
        txtSearch.setText("");
    }

    @Override
    public Component getMainComponent() {
        return txtSearch;
    }

    /**
     * Merge the given filter with this filter if possible
     * @param filter
     * @return true if filter merged in or to suppress adding a new filter, false to allow adding new filter
     */
    @Override
    @SuppressWarnings("rawtypes")
    public boolean merge(ItemFilter filter) {
        return false;
    }

    @Override
    protected void buildWidget(JPanel widget) {
        txtSearch = new FTextField.Builder().ghostText("Search").build();
        widget.add(txtSearch);

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_PAGE_UP:
                    case KeyEvent.VK_PAGE_DOWN:
                        //set focus to item manager when certain keys pressed
                        if (changeTimer.isRunning()) {
                            applyChange(); //apply change now if currently delayed
                        }
                        itemManager.focus();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (e.getModifiers() == 0) {
                            if (changeTimer.isRunning()) {
                                applyChange(); //apply change now if currently delayed
                            }
                        }
                        break;
                }
            }
        });

        txtSearch.addChangeListener(new FTextField.ChangeListener() {
            @Override
            public void textChanged() {
                changeTimer.restart();
            }
        });
    }

    @Override
    protected void doWidgetLayout(LayoutHelper helper) {
        helper.offset(0, 3); //add padding above text field
        helper.fillLine(txtSearch, FTextField.HEIGHT);
    }

    @Override
    protected void applyChange() {
        changeTimer.stop(); //ensure change timer stopped before applying change
        super.applyChange();
    }

    private Timer changeTimer = new Timer(200, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            applyChange();
        }
    });
}
