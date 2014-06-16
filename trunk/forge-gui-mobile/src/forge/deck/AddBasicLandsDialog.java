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
package forge.deck;

import forge.Graphics;
import forge.StaticData;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.assets.ImageCache;
import forge.card.CardEdition;
import forge.card.CardZoom;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.toolbox.FButton;
import forge.toolbox.FCardPanel;
import forge.toolbox.FComboBox;
import forge.toolbox.FContainer;
import forge.toolbox.FDialog;
import forge.toolbox.FDisplayObject;
import forge.toolbox.FEvent;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.util.Callback;
import forge.util.Utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;


public class AddBasicLandsDialog extends FDialog {
    private static final float ADD_BTN_SIZE = Utils.AVG_FINGER_HEIGHT * 0.75f;
    private static final float LAND_PANEL_PADDING = Utils.scaleY(3);

    private final Deck deck;
    private final Callback<CardPool> callback;

    private final FLabel lblLandSet = add(new FLabel.Builder().text("Land Set:").font(FSkinFont.get(12)).textColor(FLabel.INLINE_LABEL_COLOR).build());
    private final FComboBox<CardEdition> cbLandSet = add(new FComboBox<CardEdition>(StaticData.instance().getEditions()));
    private final LandPanel pnlPlains = add(new LandPanel("Plains"));
    private final LandPanel pnlIsland = add(new LandPanel("Island"));
    private final LandPanel pnlSwamp = add(new LandPanel("Swamp"));
    private final LandPanel pnlMountain = add(new LandPanel("Mountain"));
    private final LandPanel pnlForest = add(new LandPanel("Forest"));
    private final FButton btnOK    = add(new FButton("OK"));
    private final FButton btnCancel = add(new FButton("Cancel"));

    private CardEdition landSet;

    public AddBasicLandsDialog(Deck deck0, CardEdition defaultLandSet, final Callback<CardPool> callback0) {
        this(deck0, defaultLandSet, null, callback0);
    }
    public AddBasicLandsDialog(Deck deck0, CardEdition defaultLandSet, CardPool restrictedCatalog0, final Callback<CardPool> callback0) {
        super("Add Basic Lands to " + deck0.getName());

        deck = deck0;
        callback = callback0;

        cbLandSet.setFont(lblLandSet.getFont());
        cbLandSet.setChangedHandler(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                landSet = cbLandSet.getSelectedItem();
                pnlPlains.refreshArtChoices();
                pnlIsland.refreshArtChoices();
                pnlSwamp.refreshArtChoices();
                pnlMountain.refreshArtChoices();
                pnlForest.refreshArtChoices();
            }
        });
        cbLandSet.setSelectedItem(defaultLandSet);

        btnOK.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                CardPool landsToAdd = new CardPool();
                pnlPlains.addToCardPool(landsToAdd);
                pnlIsland.addToCardPool(landsToAdd);
                pnlSwamp.addToCardPool(landsToAdd);
                pnlMountain.addToCardPool(landsToAdd);
                pnlForest.addToCardPool(landsToAdd);

                hide();

                if (landsToAdd.countAll() > 0) {
                    callback.run(landsToAdd);
                }
            }
        });
        btnCancel.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                hide();
            }
        });
    }

    @Override
    protected float layoutAndGetHeight(float width, float maxHeight) {
        float padding = FOptionPane.PADDING;
        float x = padding;
        float y = padding;
        float w = width - 2 * padding;

        //layout land set combo box
        float comboBoxHeight = cbLandSet.getHeight();
        lblLandSet.setBounds(x, y, lblLandSet.getAutoSizeBounds().width, comboBoxHeight);
        cbLandSet.setBounds(x + lblLandSet.getWidth(), y, w - lblLandSet.getWidth(), comboBoxHeight);

        //layout card panels
        x = padding;
        y += comboBoxHeight + padding;
        float maxPanelHeight = (maxHeight - y - FOptionPane.BUTTON_HEIGHT - FOptionPane.GAP_BELOW_BUTTONS - 2 * padding) / 2;
        float panelExtraHeight = pnlPlains.cbLandArt.getHeight() + ADD_BTN_SIZE + 2 * LAND_PANEL_PADDING;
        float panelWidth = (w - 2 * padding) / 3;
        float panelHeight = panelWidth * FCardPanel.ASPECT_RATIO + panelExtraHeight;
        if (panelHeight > maxPanelHeight) { //ensure panels short enough that dialog doesn't exceed max height
            panelHeight = maxPanelHeight;
        }

        pnlPlains.setBounds(x, y, panelWidth, panelHeight);
        x += panelWidth + padding;
        pnlIsland.setBounds(x, y, panelWidth, panelHeight);
        x += panelWidth + padding;
        pnlSwamp.setBounds(x, y, panelWidth, panelHeight);

        x = (width - padding) / 2 - panelWidth;
        y += panelHeight + padding;
        pnlMountain.setBounds(x, y, panelWidth, panelHeight);
        x += panelWidth + padding;
        pnlForest.setBounds(x, y, panelWidth, panelHeight);

        //layout buttons
        x = padding;
        y += panelHeight + padding;
        float gapBetweenButtons = padding / 2;
        float buttonWidth = (w - gapBetweenButtons) / 2;
        btnOK.setBounds(x, y, buttonWidth, FOptionPane.BUTTON_HEIGHT);
        x += buttonWidth + gapBetweenButtons;
        btnCancel.setBounds(x, y, buttonWidth, FOptionPane.BUTTON_HEIGHT);

        return y + FOptionPane.BUTTON_HEIGHT + FOptionPane.GAP_BELOW_BUTTONS;
    }

    private class LandPanel extends FContainer {
        private final LandCardPanel cardPanel;
        private final FLabel lblCount, btnSubtract, btnAdd;
        private final FComboBox<String> cbLandArt;
        private final String cardName;
        private PaperCard card;
        private int count, maxCount;

        private LandPanel(String cardName0) {
            cardName = cardName0;
            cardPanel = add(new LandCardPanel());
            cbLandArt = add(new FComboBox<String>());
            cbLandArt.setFont(cbLandSet.getFont());
            cbLandArt.setChangedHandler(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    int artIndex = cbLandArt.getSelectedIndex();
                    if (artIndex < 0) { return; }
                    card = generateCard(artIndex); //generate card for display
                }
            });
            lblCount = add(new FLabel.Builder().text("0").font(FSkinFont.get(18)).align(HAlignment.CENTER).build());
            btnSubtract = add(new FLabel.ButtonBuilder().icon(FSkinImage.MINUS).command(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    if (count > 0) {
                        count--;
                        lblCount.setText(String.valueOf(count));
                    }
                }
            }).build());
            btnAdd = add(new FLabel.ButtonBuilder().icon(FSkinImage.PLUS).command(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    if (maxCount == 0 || count < maxCount) {
                        count++;
                        lblCount.setText(String.valueOf(count));
                    }
                }
            }).build());
        }

        private void addToCardPool(CardPool pool) {
            if (count == 0) { return; }
            int artIndex = cbLandArt.getSelectedIndex();
            if (artIndex < 0) { return; }

            if (artIndex > 0 && card != null) {
                pool.add(card, count); //simplify things if art index specified
            }
            else {
                for (int i = 0; i < count; i++) {
                    pool.add(generateCard(artIndex));
                }
            }
        }

        private PaperCard generateCard(int artIndex) {
            PaperCard c = FModel.getMagicDb().getCommonCards().getCard(cardName, landSet.getCode(), artIndex);
            if (c == null) {
                //if can't find land for this set, fall back to Zendikar lands
                c = FModel.getMagicDb().getCommonCards().getCard(cardName, "ZEN");
            }
            return c;
        }

        private void refreshArtChoices() {
            cbLandArt.clearItems();
            if (landSet == null) { return; }

            int artChoiceCount = FModel.getMagicDb().getCommonCards().getArtCount(cardName, landSet.getCode());
            cbLandArt.addItem("Assorted Art");
            for (int i = 1; i <= artChoiceCount; i++) {
                cbLandArt.addItem("Card Art " + i);
            }
        }

        @Override
        protected void doLayout(float width, float height) {
            float y = height - ADD_BTN_SIZE;
            float buttonWidth = ADD_BTN_SIZE;
            float labelWidth = width - 2 * ADD_BTN_SIZE;
            float minLabelWidth = lblCount.getFont().getBounds("0").width + 2 * lblCount.getInsets().x;
            if (labelWidth < minLabelWidth) { //ensure count label has enough room for display a single digit count at normal font size
                labelWidth = minLabelWidth;
                buttonWidth = (width - labelWidth) / 2;
            }
            btnSubtract.setBounds(0, y, buttonWidth, ADD_BTN_SIZE);
            lblCount.setBounds(buttonWidth, y, labelWidth, ADD_BTN_SIZE);
            btnAdd.setBounds(width - buttonWidth, y, buttonWidth, ADD_BTN_SIZE);

            y -= cbLandArt.getHeight() + LAND_PANEL_PADDING;
            cbLandArt.setBounds(0, y, width, cbLandArt.getHeight());

            float cardPanelHeight = y - LAND_PANEL_PADDING;
            float cardPanelWidth = cardPanelHeight / FCardPanel.ASPECT_RATIO;
            cardPanel.setBounds((width - cardPanelWidth) / 2, 0, cardPanelWidth, cardPanelHeight);
        }

        private class LandCardPanel extends FDisplayObject {
            private LandCardPanel() {
            }

            @Override
            public boolean tap(float x, float y, int count) {
                if (card == null) { return false; }
                CardZoom.show(card);
                return true;
            }

            @Override
            public boolean longPress(float x, float y) {
                if (card == null) { return false; }
                CardZoom.show(card);
                return true;
            }

            @Override
            public void draw(Graphics g) {
                if (card == null) { return; }
                Texture image = ImageCache.getImage(card);
                g.drawImage(image, 0, 0, getWidth(), getHeight());
            }
        }
    }
}
