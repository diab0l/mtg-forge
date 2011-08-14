package forge;


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.*;
import java.util.Arrays;


/**
 * <p>TableModel class.</p>
 *
 * @author Forge
 * @version $Id$
 */
class TableModel extends AbstractTableModel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //holds 1 copy of each card, DOES NOT HOLD multiple cards with the same name
    private CardList dataNoCopies = new CardList();

    //holds multiple card
    //example: if there are 4 Elvish Pipers, dataNoCopies has 1 copy, and dataCopies has 3
    private CardList dataCopies = new CardList();

    //used by sort(), holds old data to compare with sorted data, to see if any change was made
    //private CardList oldList = new CardList();

    private CardContainer cardDetail;
    //private String column[] = {"Qty", "Name", "Cost", "Color", "Type", "Stats", "R", "AI"};
    private String column[] = {"Qty", "Name", "Cost", "Color", "Type", "Stats", "R", "Set", "AI"};

    //used to resort(), used when addCard(Card) is called
    private int recentSortedColumn;
    private boolean recentAscending;

    /**
     * <p>Constructor for TableModel.</p>
     *
     * @param cd a {@link forge.CardContainer} object.
     */
    public TableModel(CardContainer cd) {
        this(new CardList(), cd);
    }

    /**
     * <p>Constructor for TableModel.</p>
     *
     * @param inData a {@link forge.CardList} object.
     * @param in_cardDetail a {@link forge.CardContainer} object.
     */
    public TableModel(CardList inData, CardContainer in_cardDetail) {
        cardDetail = in_cardDetail;
        //intialize dataNoCopies and dataCopies
        addCard(inData);
    }


    /**
     * <p>resizeCols.</p>
     *
     * @param table a {@link javax.swing.JTable} object.
     */
    public void resizeCols(final JTable table) {
        TableColumn column = null;
        for (int i = 0; i < table.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);

            if (i == 0) {
                column.setPreferredWidth(35); // Qty
                column.setMaxWidth(35);
                column.setMinWidth(35);
            } else if (i == 1) {
                column.setPreferredWidth(190); // Name
                column.setMinWidth(170);
                column.setMaxWidth(200);
            } else if (i == 2) {
                column.setPreferredWidth(80); // Cost
                column.setMinWidth(70);
                column.setMaxWidth(90);
            } else if (i == 3) {
                column.setPreferredWidth(70); // Color
                column.setMaxWidth(70);
                column.setMinWidth(70);
            } else if (i == 4) {
                column.setPreferredWidth(130); // Type
            } else if (i == 5) {
                column.setPreferredWidth(50); // Stats 
                column.setMaxWidth(50);
                column.setMinWidth(50);
            } else if (i == 6) {
                column.setPreferredWidth(25); // R
                column.setMaxWidth(25);
                column.setMinWidth(25);
            } else if (i == 7) {
                column.setPreferredWidth(45); // Set
                column.setMaxWidth(45);
                column.setMinWidth(45);
            } else if (i == 8) {
                column.setPreferredWidth(30); // AI
                column.setMaxWidth(30);
                column.setMinWidth(30);
            }
        }//for

        /*for(int j = 0; j < table.getColumnCount(); j++) {
            column = table.getColumnModel().getColumn(j);
            //System.out.println("col Width:" + column.getPreferredWidth());
        }*/
    }

    /**
     * <p>clear.</p>
     */
    public void clear() {
        dataNoCopies.clear();
        dataCopies.clear();
        //fireTableDataChanged();
    }

    /**
     * <p>getCards.</p>
     *
     * @return a {@link forge.CardList} object.
     */
    public CardList getCards() {
        CardList all = new CardList();
        all.addAll(dataCopies);
        all.addAll(dataNoCopies);

        return all;
    }

    /**
     * <p>removeCard.</p>
     *
     * @param c a {@link forge.Card} object.
     */
    public void removeCard(Card c) {
        //remove card from "dataCopies",
        //if not found there, remove card from "dataNoCopies"
        int index = findCardName(c.getName(), dataCopies);

        if (index != -1) //found card name
            dataCopies.remove(index);
        else {
            index = findCardName(c.getName(), dataNoCopies);
            dataNoCopies.remove(index);
        }

        fireTableDataChanged();
    }

    /**
     * <p>findCardName.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param list a {@link forge.CardList} object.
     * @return a int.
     */
    private int findCardName(String name, CardList list) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).getName().equals(name)) return i;

        return -1;
    }

    /**
     * <p>addCard.</p>
     *
     * @param c a {@link forge.Card} object.
     */
    public void addCard(Card c) {
        if (0 == countQuantity(c, dataNoCopies)) dataNoCopies.add(c);
        else dataCopies.add(c);
    }

    /**
     * <p>addCard.</p>
     *
     * @param c a {@link forge.CardList} object.
     */
    public void addCard(CardList c) {
        for (int i = 0; i < c.size(); i++)
            addCard(c.get(i));

        fireTableDataChanged();
    }

    /**
     * <p>rowToCard.</p>
     *
     * @param row a int.
     * @return a {@link forge.Card} object.
     */
    public Card rowToCard(int row) {
        return dataNoCopies.get(row);
    }

    /**
     * <p>countQuantity.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a int.
     */
    private int countQuantity(Card c) {
        return countQuantity(c, dataNoCopies) + countQuantity(c, dataCopies);
    }

    //CardList data is either class members "dataNoCopies" or "dataCopies"
    /**
     * <p>countQuantity.</p>
     *
     * @param c a {@link forge.Card} object.
     * @param data a {@link forge.CardList} object.
     * @return a int.
     */
    private int countQuantity(Card c, CardList data) {
        int count = 0;
        for (int i = 0; i < data.size(); i++) {
            //are the card names and set code the same?
            Card dc = data.get(i);
            if (dc.getName().equals(c.getName()) &&
                    dc.getCurSetCode().equals(c.getCurSetCode())) count++;
        }

        return count;
    }

    /**
     * <p>getRowCount.</p>
     *
     * @return a int.
     */
    public int getRowCount() {
        return dataNoCopies.size();
    }

    /**
     * <p>getColumnCount.</p>
     *
     * @return a int.
     */
    public int getColumnCount() {
        return column.length;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int n) {
        return column[n];
    }

    /** {@inheritDoc} */
    public Object getValueAt(int row, int column) {
        return getColumn(dataNoCopies.get(row), column);
    }

    /**
     * <p>Getter for the field <code>column</code>.</p>
     *
     * @param c a {@link forge.Card} object.
     * @param column a int.
     * @param column a int.
     * @return a {@link java.lang.Object} object.
     */
    private Object getColumn(Card c, int column) {
        switch (column) {
            case 0:
                return Integer.valueOf(countQuantity(c));
            case 1:
                return c.getName();
            case 2:
                return c.getManaCost();
            case 3:
                return TableSorter.getColor(c);
            case 4:
                return GuiDisplayUtil.formatCardType(c);
            case 5:
                //return c.isCreature()? c.getBaseAttackString() + "/" + c.getBaseDefenseString():"";
                if (c.isCreature()) {
                    return c.getBaseAttackString() + "/" + c.getBaseDefenseString();
                } else if (c.isPlaneswalker()) {
                    return Integer.toString(c.getBaseLoyalty());
                }
                return "";
            case 6:
                String rarity = c.getRarity();

                if (rarity.equals("new"))
                    return "n";
                else {
                    if (rarity.length() > 0)
                        rarity = rarity.substring(0, 1);
                }

                if (!c.getCurSetCode().equals("")) {
                    SetInfo si = SetInfoUtil.getSetInfo_Code(c.getSets(), c.getCurSetCode());
                    if (si != null)
                        return si.Rarity.substring(0, 1);
                }
                return rarity;
            case 7:
                String SC = c.getCurSetCode();
                if (!SC.equals(""))
                    return SC;
            case 8:
                if (c.getSVar("RemAIDeck").equals("True")
                        && c.getSVar("RemRandomDeck").equals("True"))
                    return "No ?";
                else if (c.getSVar("RemAIDeck").equals("True"))
                    return "No";
                else if (c.getSVar("RemRandomDeck").equals("True"))
                    return "?";
                else
                    return "";

            default:
                return "error";
        }
    }

    /**
     * <p>addListeners.</p>
     *
     * @param table a {@link javax.swing.JTable} object.
     */
    public void addListeners(final JTable table) {
        //updates card detail, listens to any key strokes
        table.getSelectionModel().addListSelectionListener( new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    cardDetail.setCard(dataNoCopies.get(row));
                }
            }
            
        });

        //sorts
        MouseListener mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TableColumnModel columnModel = table.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = table.convertColumnIndexToModel(viewColumn);


                if (column != -1) {
                    //sort ascending
                    @SuppressWarnings("unused")
                    boolean change = sort(column, true);

                    //if(! change)
                    //  sort(column, false);//sort descending

                    //fireTableDataChanged();
                }
            }//mousePressed()
        };//MouseListener
        table.getTableHeader().addMouseListener(mouse);
    }//addCardListener()

    //called by the GUI when a card is added to re-sort
    /**
     * <p>resort.</p>
     */
    public void resort() {
        sort(recentSortedColumn, recentAscending);
        //this.fireTableDataChanged();
    }

    //returns true if any data changed positions
    // @SuppressWarnings("unchecked")
    // Arrays.sort
    /**
     * <p>sort.</p>
     *
     * @param column a int.
     * @param ascending a boolean.
     * @return a boolean.
     */
    public boolean sort(int column, boolean ascending) {
        //used by addCard() to resort the cards
        recentSortedColumn = column;
        recentAscending = ascending;

        CardList all = new CardList();
        all.addAll(dataNoCopies);
        all.addAll(dataCopies);

        TableSorter sorter = new TableSorter(all, column, ascending, true);
        Card[] array = all.toArray();
        Arrays.sort(array, sorter);

        /*
        //determine if any data changed position
        boolean hasChanged = false;
        CardList check = removeDuplicateNames(array);
        for(int i = 0; i < check.size(); i++)
          //do the card names match?
          if(! check.get(i).getName().equals(dataNoCopies.get(i).getName()))
            hasChanged = true;
        */

        //clear everything, and add sorted data back into the model
        dataNoCopies.clear();
        dataCopies.clear();
        addCard(new CardList(array));

        //this value doesn't seem to matter:
        //return hasChanged;
        return true;
    }//sort()
    /*
    private CardList removeDuplicateNames(Card[] c)
    {
      TreeSet check = new TreeSet();
      CardList list = new CardList();

      for(int i = 0; i < c.length; i++)
      {
        if(! check.contains(c[i].getName()))
        {
          check.add(c[i].getName());
          list.add(c[i]);
        }
      }

      return list;
    }
    */
}//CardTableModel
