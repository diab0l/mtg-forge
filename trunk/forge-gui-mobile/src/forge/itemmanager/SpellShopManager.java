package forge.itemmanager;

import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.google.common.base.Function;

import forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.assets.ImageCache;
import forge.card.CardRenderer;
import forge.card.CardZoom;
import forge.item.InventoryItem;
import forge.item.PaperCard;
import forge.itemmanager.filters.ItemFilter;
import forge.menu.FPopupMenu;
import forge.model.FModel;
import forge.quest.QuestSpellShop;
import forge.toolbox.FList;


public final class SpellShopManager extends ItemManager<InventoryItem> {
    private final Function<Entry<? extends InventoryItem, Integer>, Object> fnGetPrice;

    public SpellShopManager(boolean isShop0) {
        super(InventoryItem.class, false);

        fnGetPrice = isShop0 ? QuestSpellShop.fnPriceGet : QuestSpellShop.fnPriceSellGet;
        if (!isShop0) {
            setCaption("Cards");
        }
    }

    @Override
    protected void addDefaultFilters() {
        CardManager.addDefaultFilters(this);
    }

    @Override
    protected ItemFilter<? extends InventoryItem> createSearchFilter() {
        return CardManager.createSearchFilter(this);
    }

    @Override
    protected void buildAddFilterMenu(FPopupMenu menu) {
        CardManager.buildAddFilterMenu(menu, this);
    }

    @Override
    public ItemRenderer getListItemRenderer() {
        return new ItemRenderer() {
            @Override
            public float getItemHeight() {
                return CardRenderer.getCardListItemHeight();
            }

            @Override
            public void drawValue(Graphics g, Entry<InventoryItem, Integer> value, FSkinFont font, FSkinColor foreColor, FSkinColor backColor, boolean pressed, float x, float y, float w, float h) {
                float totalHeight = h + 2 * FList.PADDING;
                float cardArtWidth = totalHeight * CardRenderer.CARD_ART_RATIO;

                if (value.getKey() instanceof PaperCard) {
                    String suffix = getConfig().getCols().containsKey(ColumnDef.NEW) && FModel.getQuest().getCards().isNew(value.getKey()) ? " *NEW*" : null;
                    CardRenderer.drawCardListItem(g, font, foreColor, (PaperCard)value.getKey(), value.getValue(), suffix, x, y, w, h);
                }
                else {
                    g.drawText(value.getValue().toString() + " " + value.getKey().toString(), font, foreColor, x + cardArtWidth, y, w - cardArtWidth, h, false, HAlignment.LEFT, true);
                    Texture image = ImageCache.getImage(value.getKey());
                    if (image != null) {
                        float imageRatio = (float)image.getWidth() / (float)image.getHeight();
                        float imageHeight = totalHeight;
                        float imageWidth = imageHeight * imageRatio;
                        if (imageWidth > cardArtWidth) {
                            imageWidth = cardArtWidth;
                            imageHeight = imageWidth / imageRatio;
                        }
                        g.drawImage(image, x - FList.PADDING + (cardArtWidth - imageWidth) / 2, y - FList.PADDING + (totalHeight - imageHeight) / 2, imageWidth, imageHeight);
                    }
                }

                //render price on top of card art
                float priceHeight = font.getLineHeight();
                y += totalHeight - priceHeight - FList.PADDING;
                g.fillRect(backColor, x - FList.PADDING, y, cardArtWidth, priceHeight);
                g.drawImage(FSkinImage.QUEST_COINSTACK, x, y, priceHeight, priceHeight);
                float offset = priceHeight * 1.1f;
                g.drawText(fnGetPrice.apply(value).toString(), font, foreColor, x + offset, y, cardArtWidth - offset - 2 * FList.PADDING, priceHeight, false, HAlignment.LEFT, true);
            }

            @Override
            public boolean tap(Entry<InventoryItem, Integer> value, float x, float y, int count) {
                if (value.getKey() instanceof PaperCard) {
                    return CardRenderer.cardListItemTap((PaperCard)value.getKey(), x, y, count);
                }
                return false;
            }

            @Override
            public boolean longPress(Entry<InventoryItem, Integer> value, float x, float y) {
                if (value.getKey() instanceof PaperCard) {
                    CardZoom.show((PaperCard)value.getKey());
                    return true;
                }
                return false;
            }
        };
    }
}
