package forge.gui.deckchooser;

import forge.gui.MouseUtil;
import forge.gui.deckchooser.DecksComboBox.DeckType;
import forge.gui.toolbox.FComboBox.TextAlignment;
import forge.gui.toolbox.FComboBoxWrapper;
import forge.gui.toolbox.FSkin;

import javax.swing.*;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DecksComboBox extends FComboBoxWrapper<DeckType> {

    public enum DeckType {
        CUSTOM_DECK ("Custom User Decks"),
        PRECONSTRUCTED_DECK("Preconstructed Decks"),
        QUEST_OPPONENT_DECK ("Quest Opponent Decks"),
        COLOR_DECK ("Random Color Decks"),
        THEME_DECK ("Random Theme Decks");

        private String value;
        private DeckType(String value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return value;
        }
        public static DeckType fromString(String value){
            for (final DeckType d : DeckType.values()) {
                if (d.toString().equalsIgnoreCase(value)) {
                    return d;
                }
            }
            throw new IllegalArgumentException("No Enum specified for this string");
        }
    };

    private List<IDecksComboBoxListener> _listeners = new ArrayList<>();
    private DeckType selectedDeckType = null;

    public DecksComboBox() {
        setSkinFont(FSkin.getBoldFont(14));
        setTextAlignment(TextAlignment.CENTER);
        addActionListener(getDeckTypeComboListener());
    }

    public void refresh(DeckType deckType) {
        setModel(new DefaultComboBoxModel<DeckType>(DeckType.values()));
        setSelectedItem(deckType);
    }

    private ActionListener getDeckTypeComboListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MouseUtil.setCursor(Cursor.WAIT_CURSOR);
                DeckType newDeckType = (DeckType)getSelectedItem();
                if (newDeckType != selectedDeckType) {
                    notifyDeckTypeSelected(newDeckType);
                    selectedDeckType = newDeckType;
                }
                MouseUtil.resetCursor();
            }
        };
    }

    public synchronized void addListener(IDecksComboBoxListener obj) {
        _listeners.add(obj);
    }

    public synchronized void removeListener(IDecksComboBoxListener obj) {
        _listeners.remove(obj);
    }

    private synchronized void notifyDeckTypeSelected(DeckType deckType) {
        if (deckType != null) {
            for (IDecksComboBoxListener listener : _listeners) {
                listener.deckTypeSelected(new DecksComboBoxEvent(this, deckType));
            }
        }
    }

    public DeckType getDeckType() {
        return selectedDeckType;
    }

    public void setDeckType(DeckType valueOf) {
        selectedDeckType = valueOf;
        setSelectedItem(selectedDeckType);
    }
}
