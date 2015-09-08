package forge.itemmanager.filters;

import java.awt.Component;
import java.awt.Dimension;

import forge.UiCommand;
import forge.interfaces.IButton;
import forge.item.InventoryItem;
import forge.itemmanager.AdvancedSearch;
import forge.itemmanager.ItemManager;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FScrollPanel;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.toolbox.FTextField;
import forge.toolbox.LayoutHelper;

import javax.swing.*;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;


public class AdvancedSearchFilter<T extends InventoryItem> extends ItemFilter<T> {
    private final AdvancedSearch.Model<T> model;
    private FLabel label;
    private EditDialog editDialog;

    public AdvancedSearchFilter(ItemManager<? super T> itemManager0) {
        super(itemManager0);
        model = new AdvancedSearch.Model<T>();
    }

    @Override
    public final boolean isEmpty() {
        return model.isEmpty();
    }

    @Override
    public void reset() {
        model.reset();
    }

    @Override
    public ItemFilter<T> createCopy() {
        return new AdvancedSearchFilter<T>(itemManager);
    }

    @Override
    protected Predicate<T> buildPredicate() {
        return model.getPredicate();
    }

    @Override
    protected final void buildWidget(JPanel widget) {
        label = new FLabel.Builder().fontAlign(SwingConstants.LEFT).fontSize(12).build();
        model.setLabel(label);
        widget.add(label);
    }

    @Override
    protected void doWidgetLayout(LayoutHelper helper) {
        helper.fillLine(label, FTextField.HEIGHT);
    }

    public boolean edit() {
        if (editDialog == null) {
            editDialog = new EditDialog();
        }
        return editDialog.show();
    }

    @Override
    public boolean merge(ItemFilter<?> filter) {
        return true;
    }

    @SuppressWarnings("serial")
    private class EditDialog {
        private static final int WIDTH = 400;
        private static final int HEIGHT = 500;
        
        private final FScrollPanel scroller;
        private FOptionPane optionPane;

        private EditDialog() {
            scroller = new FScrollPanel(null, false, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
                @Override
                public void doLayout() {
                    int x = 0;
                    int y = 0;
                    int w = getWidth();
                    int h = 100;

                    for (Component child : getInnerComponents()) {
                        child.setBounds(x, y, w, h);
                        y += h;
                    }
                }  
            };
            scroller.setMinimumSize(new Dimension(WIDTH, HEIGHT));

            Filter filter = new Filter();
            model.addFilterControl(filter);
            scroller.add(filter);
        }

        private boolean show() {
            optionPane = new FOptionPane(null, "Advanced Search", null, scroller, ImmutableList.of("OK", "Cancel"), 0);
            optionPane.setVisible(true);

            int result = optionPane.getResult();

            optionPane.dispose();
            if (result != 1) {
                model.updateExpression(); //update expression when dialog accepted
                return true;
            }
            return false;
        }

        private void addNewFilter(Filter fromFilter) {
            if (scroller.getComponent(scroller.getComponentCount() - 1) == fromFilter) {
                Filter filter = new Filter();
                model.addFilterControl(filter);
                scroller.add(filter);
                scroller.revalidate();
                scroller.scrollToBottom();
            }
        }

        @SuppressWarnings("unchecked")
        private void removeNextFilter(Filter fromFilter) {
            int index = ArrayUtils.indexOf(scroller.getComponents(), fromFilter);
            if (index < scroller.getComponentCount() - 1) {
                Filter nextFilter = (Filter)scroller.getComponent(index + 1);
                model.removeFilterControl(nextFilter);
                scroller.remove(nextFilter);
                scroller.revalidate();
            }
        }

        private class Filter extends SkinnedPanel implements AdvancedSearch.IFilterControl<T> {
            private final FLabel btnNotBeforeParen, btnOpenParen, btnNotAfterParen;
            private final FLabel btnFilter;
            private final FLabel btnCloseParen, btnAnd, btnOr;
            private AdvancedSearch.Filter<T> filter;

            private Filter() {
                super(null);

                btnNotBeforeParen = new FLabel.Builder().fontAlign(SwingConstants.CENTER).text("NOT").selectable().build();
                btnOpenParen = new FLabel.Builder().fontAlign(SwingConstants.CENTER).text("(").selectable().build();
                btnNotAfterParen = new FLabel.Builder().fontAlign(SwingConstants.CENTER).text("NOT").selectable().build();
                btnFilter = new FLabel.ButtonBuilder().build();
                btnCloseParen = new FLabel.Builder().fontAlign(SwingConstants.CENTER).selectable().text(")").build();
                btnAnd = new FLabel.Builder().fontAlign(SwingConstants.CENTER).text("AND").selectable().cmdClick(new UiCommand() {
                    @Override
                    public void run() {
                        if (btnAnd.isSelected()) {
                            btnOr.setSelected(false);
                            addNewFilter(Filter.this);
                        }
                        else {
                            removeNextFilter(Filter.this);
                        }
                    }
                }).build();
                btnOr = new FLabel.Builder().fontAlign(SwingConstants.CENTER).text("OR").selectable().cmdClick(new UiCommand() {
                    @Override
                    public void run() {
                        if (btnOr.isSelected()) {
                            btnAnd.setSelected(false);
                            addNewFilter(Filter.this);
                        }
                        else {
                            removeNextFilter(Filter.this);
                        }
                    }
                }).build();

                add(btnNotBeforeParen);
                add(btnOpenParen);
                add(btnNotAfterParen);
                add(btnFilter);
                add(btnCloseParen);
                add(btnAnd);
                add(btnOr);
            }

            @Override
            public void doLayout() {
                int padding = 5;
                int width = getWidth();
                int height = getHeight();
                int buttonWidth = (width - padding * 4) / 3;
                int buttonHeight = (height - padding * 3) / 3;

                int x = padding;
                int y = padding;
                int dx = buttonWidth + padding;
                int dy = buttonHeight + padding;

                btnNotBeforeParen.setBounds(x, y, buttonWidth, buttonHeight);
                x += dx;
                btnOpenParen.setBounds(x, y, buttonWidth, buttonHeight);
                x += dx;
                btnNotAfterParen.setBounds(x, y, buttonWidth, buttonHeight);
                x = padding;
                y += dy;
                btnFilter.setBounds(x, y, width - 2 * padding, buttonHeight);
                y += dy;
                btnCloseParen.setBounds(x, y, buttonWidth, buttonHeight);
                x += dx;
                btnAnd.setBounds(x, y, buttonWidth, buttonHeight);
                x += dx;
                btnOr.setBounds(x, y, buttonWidth, buttonHeight);
            }

            @Override
            public IButton getBtnNotBeforeParen() {
                return btnNotBeforeParen;
            }
            @Override
            public IButton getBtnOpenParen() {
                return btnOpenParen;
            }
            @Override
            public IButton getBtnNotAfterParen() {
                return btnNotAfterParen;
            }
            @Override
            public IButton getBtnFilter() {
                return btnFilter;
            }
            @Override
            public IButton getBtnCloseParen() {
                return btnCloseParen;
            }
            @Override
            public IButton getBtnAnd() {
                return btnAnd;
            }
            @Override
            public IButton getBtnOr() {
                return btnOr;
            }
            @Override
            public AdvancedSearch.Filter<T> getFilter() {
                return filter;
            }
            @Override
            public void setFilter(AdvancedSearch.Filter<T> filter0) {
                filter = filter0;
            }
            @Override
            public Class<? super T> getGenericType() {
                return itemManager.getGenericType();
            }
        }
    }
}
