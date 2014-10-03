package forge.card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import forge.Graphics;
import forge.ImageKeys;
import forge.assets.FImageComplex;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.assets.FTextureRegionImage;
import forge.assets.ImageCache;
import forge.assets.TextRenderer;
import forge.card.CardDetailUtil.DetailColors;
import forge.card.mana.ManaCost;
import forge.item.IPaperCard;
import forge.match.MatchUtil;
import forge.model.FModel;
import forge.properties.ForgePreferences.FPref;
import forge.toolbox.FCardPanel;
import forge.toolbox.FDialog;
import forge.toolbox.FList;
import forge.util.Utils;
import forge.view.CardView;
import forge.view.CardView.CardStateView;
import forge.view.ViewUtil;

public class CardRenderer {
    public enum CardStackPosition {
        Top,
        BehindHorz,
        BehindVert
    }

    private static final FSkinFont NAME_FONT = FSkinFont.get(16);
    private static final FSkinFont TYPE_FONT = FSkinFont.get(14);
    private static final FSkinFont SET_FONT = TYPE_FONT;
    private static final FSkinFont TEXT_FONT = TYPE_FONT;
    private static final FSkinFont ID_FONT = TEXT_FONT;
    private static final FSkinFont PT_FONT = NAME_FONT;
    public static final float NAME_BOX_TINT = 0.2f;
    public static final float TEXT_BOX_TINT = 0.1f;
    public static final float PT_BOX_TINT = 0.2f;
    public static final float MANA_COST_PADDING = Utils.scale(3);
    public static final float SET_BOX_MARGIN = Utils.scale(1);
    public static final float MANA_SYMBOL_SIZE = FSkinImage.MANA_1.getNearestHQWidth(2 * (NAME_FONT.getCapHeight() - MANA_COST_PADDING));
    private static final float NAME_COST_THRESHOLD = Utils.scale(200);
    public static final float BLACK_BORDER_THICKNESS_RATIO = 0.021f;
    private static final float BORDER_THICKNESS = Utils.scale(1);

    private static Color fromDetailColor(DetailColors detailColor) {
        return FSkinColor.fromRGB(detailColor.r, detailColor.g, detailColor.b);
    }

    public static Color getRarityColor(CardRarity rarity) {
        switch(rarity) {
        case Uncommon:
            return fromDetailColor(DetailColors.UNCOMMON);
        case Rare:
            return fromDetailColor(DetailColors.RARE);
        case MythicRare:
            return fromDetailColor(DetailColors.MYTHIC);
        case Special: //"Timeshifted" or other Special Rarity Cards
            return fromDetailColor(DetailColors.SPECIAL);
        default: //case BasicLand: + case Common:
            return fromDetailColor(DetailColors.COMMON);
        }
    }

    public static boolean drawZoom(Graphics g, CardView card, float width, float height) {
        float x = FDialog.INSETS;
        float y = FDialog.INSETS;
        float w = width - 2 * x;
        float h = height - 2 * y;

        final Texture image = ImageCache.getImage(card.getOriginal().getImageKey(false), true);
        if (image == ImageCache.defaultImage) { //support drawing card image manually if card image not found
            float ratio = h / w;
            if (ratio > FCardPanel.ASPECT_RATIO) {
                float oldHeight = h;
                h = w * FCardPanel.ASPECT_RATIO;
                y += (oldHeight - h) / 2;
            }
            else {
                float oldWidth = w;
                w = h / FCardPanel.ASPECT_RATIO;
                x += (oldWidth - w) / 2;
            }
            CardImageRenderer.drawCardImage(g, card, x, y, w, h, CardStackPosition.Top);
            drawFoilEffect(g, card, x, y, w, h);
            return true;
        }

        if (image == null || image == ImageCache.defaultImage) { return false; } //don't support drawing zoom for null or default textures

        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();

        if (imageWidth > w || imageHeight > h) {
            //scale down until image fits on screen
            float widthRatio = w / imageWidth;
            float heightRatio = h / imageHeight;

            if (widthRatio < heightRatio) {
                imageWidth *= widthRatio;
                imageHeight *= widthRatio;
            }
            else {
                imageWidth *= heightRatio;
                imageHeight *= heightRatio;
            }
        }
        else {
            //scale up as long as image fits on screen
            float minWidth = w / 2;
            float minHeight = h / 2;
            while (imageWidth < minWidth && imageHeight < minHeight) {
                imageWidth *= 2;
                imageHeight *= 2;
            }
        }

        x = (width - imageWidth) / 2;
        y = (height - imageHeight) / 2;
        g.drawImage(image, x, y, imageWidth, imageHeight);
        drawFoilEffect(g, card, x, y, w, h);
        return true;
    }

    public static void drawDetails(Graphics g, CardView card, float width, float height) {
        float x = FDialog.INSETS;
        float y = FDialog.INSETS;
        float w = width - 2 * x;
        float h = height - 2 * y;

        float ratio = h / w;
        if (ratio > FCardPanel.ASPECT_RATIO) {
            float oldHeight = h;
            h = w * FCardPanel.ASPECT_RATIO;
            y += (oldHeight - h) / 2;
        }
        else {
            float oldWidth = w;
            w = h / FCardPanel.ASPECT_RATIO;
            x += (oldWidth - w) / 2;
        }

        float blackBorderThickness = w * BLACK_BORDER_THICKNESS_RATIO;
        g.fillRect(Color.BLACK, x, y, w, h);
        x += blackBorderThickness;
        y += blackBorderThickness;
        w -= 2 * blackBorderThickness;
        h -= 2 * blackBorderThickness;

        //determine colors for borders
        List<DetailColors> borderColors = CardDetailUtil.getBorderColors(card.getOriginal());
        DetailColors borderColor = borderColors.get(0);
        Color color1 = FSkinColor.fromRGB(borderColor.r, borderColor.g, borderColor.b);
        Color color2 = null;
        if (borderColors.size() > 1) {
            borderColor = borderColors.get(1);
            color2 = FSkinColor.fromRGB(borderColor.r, borderColor.g, borderColor.b);
        }
        if (color2 == null) {
            g.fillRect(color1, x, y, w, h);
        }
        else {
            g.fillGradientRect(color1, color2, false, x, y, w, h);
        }

        Color idForeColor = FSkinColor.getHighContrastColor(color1);

        float outerBorderThickness = 2 * blackBorderThickness;
        x += outerBorderThickness;
        y += outerBorderThickness;
        w -= 2 * outerBorderThickness;
        float cardNameBoxHeight = Math.max(MANA_SYMBOL_SIZE + 2 * MANA_COST_PADDING, 2 * NAME_FONT.getCapHeight()) + 2 * TYPE_FONT.getCapHeight() + 2;

        //draw name/type box
        Color nameBoxColor1 = FSkinColor.tintColor(Color.WHITE, color1, NAME_BOX_TINT);
        Color nameBoxColor2 = color2 == null ? null : FSkinColor.tintColor(Color.WHITE, color2, NAME_BOX_TINT);
        drawCardNameBox(g, card, nameBoxColor1, nameBoxColor2, x, y, w, cardNameBoxHeight);

        float innerBorderThickness = outerBorderThickness / 2;
        float ptBoxHeight = 2 * PT_FONT.getCapHeight();
        float textBoxHeight = h - cardNameBoxHeight - ptBoxHeight - outerBorderThickness - 3 * innerBorderThickness; 

        y += cardNameBoxHeight + innerBorderThickness;
        Color textBoxColor1 = FSkinColor.tintColor(Color.WHITE, color1, TEXT_BOX_TINT);
        Color textBoxColor2 = color2 == null ? null : FSkinColor.tintColor(Color.WHITE, color2, TEXT_BOX_TINT);
        drawCardTextBox(g, card, textBoxColor1, textBoxColor2, x, y, w, textBoxHeight);

        y += textBoxHeight + innerBorderThickness;
        Color ptColor1 = FSkinColor.tintColor(Color.WHITE, color1, PT_BOX_TINT);
        Color ptColor2 = color2 == null ? null : FSkinColor.tintColor(Color.WHITE, color2, PT_BOX_TINT);
        drawCardIdAndPtBox(g, card, idForeColor, ptColor1, ptColor2, x, y, w, ptBoxHeight);
    }

    public static float getCardListItemHeight(boolean compactMode) {
        if (compactMode) {
            return MANA_SYMBOL_SIZE + 2 * FList.PADDING;
        }
        return Math.round(MANA_SYMBOL_SIZE + FSkinFont.get(12).getLineHeight() + 3 * FList.PADDING + 1);
    }

    private static final Map<String, FImageComplex> cardArtCache = new HashMap<String, FImageComplex>();
    public static final float CARD_ART_RATIO = 1.302f;

    //extract card art from the given card
    public static FImageComplex getCardArt(IPaperCard pc) {
        return getCardArt(ImageKeys.getImageKey(pc, false), pc.getRules().getSplitType() == CardSplitType.Split);
    }
    public static FImageComplex getCardArt(CardView card) {
        return getCardArt(card.getOriginal().getImageKey(true), card.isSplitCard());
    }
    public static FImageComplex getCardArt(String imageKey, boolean isSplitCard) {
        FImageComplex cardArt = cardArtCache.get(imageKey);
        if (cardArt == null) {
            Texture image = ImageCache.getImage(imageKey, true);
            if (image != null) {
                if (image == ImageCache.defaultImage) {
                    cardArt = CardImageRenderer.forgeArt;
                }
                else {
                    float x, y;
                    float w = image.getWidth();
                    float h = image.getHeight();
                    if (isSplitCard) { //allow rotated image for split cards
                        x = w * 33f / 250f;
                        y = 0; //delay adjusting y and h until drawn
                        w *= 106f / 250f;
                    }
                    else {
                        x = w * 0.1f;
                        y = h * 0.11f;
                        w -= 2 * x;
                        h *= 0.43f;
                        float ratioRatio = w / h / CARD_ART_RATIO;
                        if (ratioRatio > 1) { //if too wide, shrink width
                            float dw = w * (ratioRatio - 1);
                            w -= dw;
                            x += dw / 2;
                        }
                        else { //if too tall, shrink height
                            float dh = h * (1 - ratioRatio);
                            h -= dh;
                            y += dh / 2;
                        }
                    }
                    cardArt = new FTextureRegionImage(new TextureRegion(image, Math.round(x), Math.round(y), Math.round(w), Math.round(h)));
                }
                cardArtCache.put(imageKey, cardArt);
            }
        }
        return cardArt;
    }

    public static void drawCardListItem(Graphics g, FSkinFont font, FSkinColor foreColor, CardView card, int count, String suffix, float x, float y, float w, float h, boolean compactMode) {
        final CardStateView state = card.getOriginal();
        if (card.getId() > 0) {
            drawCardListItem(g, font, foreColor, getCardArt(card), card, card.getSetCode(),
                    card.getRarity(), state.getPower(), state.getToughness(),
                    state.getLoyalty(), count, suffix, x, y, w, h, compactMode);
        }
        else { //if fake card, just draw card name centered
            String name = state.getName();
            if (count > 0) { //preface name with count if applicable
                name = count + " " + name;
            }
            if (suffix != null) {
                name += suffix;
            }
            g.drawText(name, font, foreColor, x, y, w, h, false, HAlignment.CENTER, true);
        }
    }
    public static void drawCardListItem(Graphics g, FSkinFont font, FSkinColor foreColor, IPaperCard pc, int count, String suffix, float x, float y, float w, float h, boolean compactMode) {
        final CardView card = ViewUtil.getCardForUi(pc);
        final CardStateView state = card.getOriginal();
        drawCardListItem(g, font, foreColor, getCardArt(pc), card, pc.getEdition(),
                pc.getRarity(), state.getPower(), state.getToughness(),
                state.getLoyalty(), count, suffix, x, y, w, h, compactMode);
    }
    public static void drawCardListItem(Graphics g, FSkinFont font, FSkinColor foreColor, FImageComplex cardArt, CardView card, String set, CardRarity rarity, int power, int toughness, int loyalty, int count, String suffix, float x, float y, float w, float h, boolean compactMode) {
        float cardArtHeight = h + 2 * FList.PADDING;
        float cardArtWidth = cardArtHeight * CARD_ART_RATIO;
        if (cardArt != null) {
            float artX = x - FList.PADDING;
            float artY = y - FList.PADDING;
            if (card.isSplitCard()) {
                //draw split art with proper orientation
                float srcY = cardArt.getHeight() * 13f / 354f;
                float srcHeight = cardArt.getHeight() * 150f / 354f;
                float dh = srcHeight * (1 - cardArt.getWidth() / srcHeight / CARD_ART_RATIO);
                srcHeight -= dh;
                srcY += dh / 2;
                g.drawRotatedImage(cardArt.getTexture(), artX, artY, cardArtHeight, cardArtWidth / 2, artX + cardArtWidth / 2, artY + cardArtWidth / 2, cardArt.getRegionX(), (int)srcY, (int)cardArt.getWidth(), (int)srcHeight, -90);
                g.drawRotatedImage(cardArt.getTexture(), artX, artY + cardArtWidth / 2, cardArtHeight, cardArtWidth / 2, artX + cardArtWidth / 2, artY + cardArtWidth / 2, cardArt.getRegionX(), (int)cardArt.getHeight() - (int)(srcY + srcHeight), (int)cardArt.getWidth(), (int)srcHeight, -90);
            }
            else {
                g.drawImage(cardArt, artX, artY, cardArtWidth, cardArtHeight);
            }
        }

        //render card name and mana cost on first line
        float manaCostWidth = 0;
        ManaCost mainManaCost = card.getOriginal().getManaCost();
        if (card.isSplitCard()) {
            //handle rendering both parts of split card
            ManaCost otherManaCost = card.getAlternate().getManaCost();
            manaCostWidth = CardFaceSymbols.getWidth(otherManaCost, MANA_SYMBOL_SIZE) + MANA_COST_PADDING;
            CardFaceSymbols.drawManaCost(g, otherManaCost, x + w - manaCostWidth + MANA_COST_PADDING, y, MANA_SYMBOL_SIZE);
            //draw "//" between two parts of mana cost
            manaCostWidth += font.getBounds("//").width + MANA_COST_PADDING;
            g.drawText("//", font, foreColor, x + w - manaCostWidth + MANA_COST_PADDING, y, w, MANA_SYMBOL_SIZE, false, HAlignment.LEFT, true);
        }
        manaCostWidth += CardFaceSymbols.getWidth(mainManaCost, MANA_SYMBOL_SIZE);
        CardFaceSymbols.drawManaCost(g, mainManaCost, x + w - manaCostWidth, y, MANA_SYMBOL_SIZE);

        x += cardArtWidth;
        String name = card.getOriginal().getName();
        if (count > 0) { //preface name with count if applicable
            name = count + " " + name;
        }
        if (suffix != null) {
            name += suffix;
        }
        g.drawText(name, font, foreColor, x, y, w - manaCostWidth - cardArtWidth - FList.PADDING, MANA_SYMBOL_SIZE, false, HAlignment.LEFT, true);

        if (compactMode) {
            return; //skip second line if rendering in compact mode
        }

        y += MANA_SYMBOL_SIZE + FList.PADDING + SET_BOX_MARGIN;

        //render card type, p/t, and set box on second line
        FSkinFont typeFont = FSkinFont.get(12);
        float availableTypeWidth = w - cardArtWidth;
        float lineHeight = typeFont.getLineHeight();
        if (!StringUtils.isEmpty(set)) {
            float setWidth = getSetWidth(typeFont, set);
            availableTypeWidth -= setWidth;
            drawSetLabel(g, typeFont, set, rarity, x + availableTypeWidth + SET_BOX_MARGIN, y - SET_BOX_MARGIN, setWidth, lineHeight + 2 * SET_BOX_MARGIN);
        }
        String type = CardDetailUtil.formatCardType(card.getOriginal());
        if (card.getOriginal().isCreature()) { //include P/T or Loyalty at end of type
            type += " (" + power + " / " + toughness + ")";
        }
        else if (card.getOriginal().isPlaneswalker()) {
            type += " (" + loyalty + ")";
        }
        g.drawText(type, typeFont, foreColor, x, y, availableTypeWidth, lineHeight, false, HAlignment.LEFT, true);
    }

    public static boolean cardListItemTap(CardView card, float x, float y, int count, boolean compactMode) {
        if (x <= getCardListItemHeight(compactMode) * CARD_ART_RATIO) {
            CardZoom.show(card);
            return true;
        }
        return false;
    }
    public static boolean cardListItemTap(IPaperCard pc, float x, float y, int count, boolean compactMode) {
        float cardArtHeight = getCardListItemHeight(compactMode);
        float cardArtWidth = cardArtHeight * CARD_ART_RATIO;
        if (x <= cardArtWidth && y <= cardArtHeight) {
            CardZoom.show(ViewUtil.getCardForUi(pc));
            return true;
        }
        return false;
    }

    private static void drawCardNameBox(Graphics g, CardView card, Color color1, Color color2, float x, float y, float w, float h) {
        if (color2 == null) {
            g.fillRect(color1, x, y, w, h);
        }
        else {
            g.fillGradientRect(color1, color2, false, x, y, w, h);
        }
        g.drawRect(BORDER_THICKNESS, Color.BLACK, x, y, w, h);

        float padding = h / 8;
        final CardStateView state = card.getOriginal();

        //make sure name/mana cost row height is tall enough for both
        h = Math.max(MANA_SYMBOL_SIZE + 2 * MANA_COST_PADDING, 2 * NAME_FONT.getCapHeight());

        //draw mana cost for card
        float manaCostWidth = 0;
        ManaCost mainManaCost = state.getManaCost();
        if (card.isSplitCard() && card.hasAltState()) {
            //handle rendering both parts of split card
            mainManaCost = state.getManaCost();
            ManaCost otherManaCost = card.getAlternate().getManaCost();
            manaCostWidth = CardFaceSymbols.getWidth(otherManaCost, MANA_SYMBOL_SIZE) + MANA_COST_PADDING;
            CardFaceSymbols.drawManaCost(g, otherManaCost, x + w - manaCostWidth, y + (h - MANA_SYMBOL_SIZE) / 2, MANA_SYMBOL_SIZE);
            //draw "//" between two parts of mana cost
            manaCostWidth += NAME_FONT.getBounds("//").width + MANA_COST_PADDING;
            g.drawText("//", NAME_FONT, Color.BLACK, x + w - manaCostWidth, y, w, h, false, HAlignment.LEFT, true);
        }
        manaCostWidth += CardFaceSymbols.getWidth(mainManaCost, MANA_SYMBOL_SIZE) + MANA_COST_PADDING;
        CardFaceSymbols.drawManaCost(g, mainManaCost, x + w - manaCostWidth, y + (h - MANA_SYMBOL_SIZE) / 2, MANA_SYMBOL_SIZE);

        //draw name for card
        x += padding;
        w -= 2 * padding;
        g.drawText(state.getName(), NAME_FONT, Color.BLACK, x, y, w - manaCostWidth - padding, h, false, HAlignment.LEFT, true);

        //draw type and set label for card
        y += h;
        h = 2 * TYPE_FONT.getCapHeight();

        String set = card.getSetCode();
        if (!StringUtils.isEmpty(set)) {
            float setWidth = getSetWidth(SET_FONT, set);
            drawSetLabel(g, SET_FONT, set, card.getRarity(), x + w + padding - setWidth - SET_BOX_MARGIN, y + SET_BOX_MARGIN, setWidth, h - SET_BOX_MARGIN);
            w -= setWidth; //reduce available width for type
        }

        g.drawText(CardDetailUtil.formatCardType(state), TYPE_FONT, Color.BLACK, x, y, w, h, false, HAlignment.LEFT, true);
    }

    public static float getSetWidth(FSkinFont font, String set) {
        return font.getBounds(set).width + font.getCapHeight();
    }

    public static void drawSetLabel(Graphics g, FSkinFont font, String set, CardRarity rarity, float x, float y, float w, float h) {
        Color backColor = getRarityColor(rarity);
        Color foreColor = FSkinColor.getHighContrastColor(backColor);
        g.fillRect(backColor, x, y, w, h);
        g.drawText(set, font, foreColor, x, y, w, h, false, HAlignment.CENTER, true);
    }

    //use text renderer to handle mana symbols and reminder text
    private static final TextRenderer cardTextRenderer = new TextRenderer(true);

    private static void drawCardTextBox(Graphics g, CardView card, Color color1, Color color2, float x, float y, float w, float h) {
        if (color2 == null) {
            g.fillRect(color1, x, y, w, h);
        }
        else {
            g.fillGradientRect(color1, color2, false, x, y, w, h);
        }
        g.drawRect(BORDER_THICKNESS, Color.BLACK, x, y, w, h);

        float padX = TEXT_FONT.getCapHeight() / 2;
        float padY = padX + Utils.scale(2); //add a little more vertical padding
        x += padX;
        y += padY;
        w -= 2 * padX;
        h -= 2 * padY;
        cardTextRenderer.drawText(g, CardDetailUtil.composeCardText(card.getOriginal()), TEXT_FONT, Color.BLACK, x, y, w, h, y, h, true, HAlignment.LEFT, false);
    }

    private static void drawCardIdAndPtBox(Graphics g, CardView card, Color idForeColor, Color color1, Color color2, float x, float y, float w, float h) {
        final CardStateView state = card.getOriginal();

        String idText = CardDetailUtil.formatCardId(state);
        g.drawText(idText, ID_FONT, idForeColor, x, y + ID_FONT.getCapHeight() / 2, w, h, false, HAlignment.LEFT, false);

        String ptText = CardDetailUtil.formatPowerToughness(state);
        if (StringUtils.isEmpty(ptText)) { return; }

        float padding = PT_FONT.getCapHeight() / 2;
        float boxWidth = Math.min(PT_FONT.getBounds(ptText).width + 2 * padding,
                w - ID_FONT.getBounds(idText).width - padding); //prevent box overlapping ID
        x += w - boxWidth;
        w = boxWidth;

        if (color2 == null) {
            g.fillRect(color1, x, y, w, h);
        }
        else {
            g.fillGradientRect(color1, color2, false, x, y, w, h);
        }
        g.drawRect(BORDER_THICKNESS, Color.BLACK, x, y, w, h);
        g.drawText(ptText, PT_FONT, Color.BLACK, x, y, w, h, false, HAlignment.CENTER, true);
    }

    public static void drawCard(Graphics g, IPaperCard pc, float x, float y, float w, float h, CardStackPosition pos) {
        Texture image = ImageCache.getImage(pc);
        if (image != null) {
            if (image == ImageCache.defaultImage) {
                CardImageRenderer.drawCardImage(g, ViewUtil.getCardForUi(pc), x, y, w, h, pos);
            }
            else {
                g.drawImage(image, x, y, w, h);
            }
            if (pc.isFoil()) { //draw foil effect if needed
                final CardView card = ViewUtil.getCardForUi(pc);
                if (card.getOriginal().getFoilIndex() == 0) { //if foil finish not yet established, assign a random one
                    card.getOriginal().setRandomFoil();
                }
                drawFoilEffect(g, card, x, y, w, h);
            }
        }
        else { //draw cards without textures as just a black rectangle
            g.fillRect(Color.BLACK, x, y, w, h);
        }
    }
    public static void drawCard(Graphics g, CardView card, float x, float y, float w, float h, CardStackPosition pos) {
        Texture image = ImageCache.getImage(card);
        if (image != null) {
            if (image == ImageCache.defaultImage) {
                CardImageRenderer.drawCardImage(g, card, x, y, w, h, pos);
            }
            else {
                g.drawImage(image, x, y, w, h);
            }
            drawFoilEffect(g, card, x, y, w, h);
        }
        else { //draw cards without textures as just a black rectangle
            g.fillRect(Color.BLACK, x, y, w, h);
        }
    }

    public static void drawCardWithOverlays(Graphics g, CardView card, float x, float y, float w, float h, CardStackPosition pos) {
        drawCard(g, card, x, y, w, h, pos);

        float padding = w * 0.021f; //adjust for card border
        x += padding;
        y += padding;
        w -= 2 * padding;
        h -= 2 * padding;

        CardStateView details = card.getOriginal();
        DetailColors borderColor = CardDetailUtil.getBorderColor(details);
        Color color = FSkinColor.fromRGB(borderColor.r, borderColor.g, borderColor.b);
        color = FSkinColor.tintColor(Color.WHITE, color, CardRenderer.PT_BOX_TINT);

        //draw name and mana cost overlays if card is small or default card image being used
        if (h <= NAME_COST_THRESHOLD && card.mayBeShown()) {
            if (showCardNameOverlay(card)) {
                g.drawOutlinedText(details.getName(), FSkinFont.forHeight(h * 0.18f), Color.WHITE, Color.BLACK, x + padding, y + padding, w - 2 * padding, h * 0.4f, true, HAlignment.LEFT, false);
            }
            if (showCardManaCostOverlay(card)) {
                float manaSymbolSize = w / 4;
                if (card.isSplitCard() && card.hasAltState()) {
                    float dy = manaSymbolSize / 2 + Utils.scale(5);
                    drawManaCost(g, card.getOriginal().getManaCost(), x, y + dy, w, h, manaSymbolSize);
                    drawManaCost(g, card.getAlternate().getManaCost(), x, y - dy, w, h, manaSymbolSize);
                }
                else {
                    drawManaCost(g, card.getOriginal().getManaCost(), x, y, w, h, manaSymbolSize);
                }
            }
        }

        if (pos == CardStackPosition.BehindVert) { return; } //remaining rendering not needed if card is behind another card in a vertical stack
        boolean onTop = (pos == CardStackPosition.Top);

        if (showCardIdOverlay(card)) {
            FSkinFont idFont = FSkinFont.forHeight(h * 0.12f);
            float idHeight = idFont.getCapHeight();
            g.drawOutlinedText(String.valueOf(card.getId()), idFont, Color.WHITE, Color.BLACK, x + padding, y + h - idHeight - padding, w, h, false, HAlignment.LEFT, false);
        }

        int number = 0;
        for (final Integer i : card.getCounters().values()) {
            number += i.intValue();
        }

        final int counters = number;

        float countersSize = w / 2;
        final float xCounters = x - countersSize / 2;
        final float yCounters = y + h * 2 / 3 - countersSize;

        if (counters == 1) {
            CardFaceSymbols.drawSymbol("counters1", g, xCounters, yCounters, countersSize, countersSize);
        }
        else if (counters == 2) {
            CardFaceSymbols.drawSymbol("counters2", g, xCounters, yCounters, countersSize, countersSize);
        }
        else if (counters == 3) {
            CardFaceSymbols.drawSymbol("counters3", g, xCounters, yCounters, countersSize, countersSize);
        }
        else if (counters > 3) {
            CardFaceSymbols.drawSymbol("countersMulti", g, xCounters, yCounters, countersSize, countersSize);
        }

        float otherSymbolsSize = w / 2;
        final float combatXSymbols = (x + (w / 4)) - otherSymbolsSize / 2;
        final float stateXSymbols = (x + (w / 2)) - otherSymbolsSize / 2;
        final float ySymbols = (y + h) - (h / 8) - otherSymbolsSize / 2;

        if (card.isAttacking()) {
            CardFaceSymbols.drawSymbol("attack", g, combatXSymbols, ySymbols, otherSymbolsSize, otherSymbolsSize);
        }
        else if (card.isBlocking()) {
            CardFaceSymbols.drawSymbol("defend", g, combatXSymbols, ySymbols, otherSymbolsSize, otherSymbolsSize);
        }

        if (onTop && card.isSick()) {
            //only needed if on top since otherwise symbol will be hidden
            CardFaceSymbols.drawSymbol("summonsick", g, stateXSymbols, ySymbols, otherSymbolsSize, otherSymbolsSize);
        }

        if (card.isPhasedOut()) {
            CardFaceSymbols.drawSymbol("phasing", g, stateXSymbols, ySymbols, otherSymbolsSize, otherSymbolsSize);
        }

        if (MatchUtil.isUsedToPay(card)) {
            float sacSymbolSize = otherSymbolsSize * 1.2f;
            CardFaceSymbols.drawSymbol("sacrifice", g, (x + (w / 2)) - sacSymbolSize / 2, (y + (h / 2)) - sacSymbolSize / 2, otherSymbolsSize, otherSymbolsSize);
        }

        if (onTop && showCardPowerOverlay(card) && (card.mayBeShown() || card.isFaceDown())) { //make sure card p/t box appears on top
            //only needed if on top since otherwise P/T will be hidden
            drawPtBox(g, card, details, color, x, y, w, h);
        }
    }

    private static void drawPtBox(Graphics g, CardView card, CardStateView details, Color color, float x, float y, float w, float h) {
        //use array of strings to render separately with a tiny amount of space in between
        //instead of using actual spaces which are too wide
        List<String> pieces = new ArrayList<String>();
        if (details.isCreature()) {
            pieces.add(String.valueOf(details.getPower()));
            pieces.add("/");
            pieces.add(String.valueOf(details.getToughness()));
        }
        if (details.isPlaneswalker()) {
            if (pieces.isEmpty()) {
                pieces.add(String.valueOf(details.getLoyalty()));
            }
            else {
                pieces.add("(" + details.getLoyalty() + ")");
            }
        }
        if (pieces.isEmpty()) { return; }

        FSkinFont font = FSkinFont.forHeight(h * 0.15f);
        float padding = Math.round(font.getCapHeight() / 4);
        float boxWidth = padding;
        List<Float> pieceWidths = new ArrayList<Float>();
        for (String piece : pieces) {
            float pieceWidth = font.getBounds(piece).width + padding;
            pieceWidths.add(pieceWidth);
            boxWidth += pieceWidth;
        }
        float boxHeight = font.getCapHeight() + font.getAscent() + 2 * padding;

        x += w - boxWidth;
        y += h - boxHeight;
        w = boxWidth;
        h = boxHeight;

        //draw card damage about P/T box if needed
        if (card.getDamage() > 0) {
            g.drawOutlinedText(">" + card.getDamage() + "<", font, Color.RED, Color.WHITE, x, y - h + padding, w, h, false, HAlignment.CENTER, true);
        }

        g.fillRect(color, x, y, w, h);
        g.drawRect(BORDER_THICKNESS, Color.BLACK, x, y, w, h);

        x += padding;
        for (int i = 0; i < pieces.size(); i++) {
            g.drawText(pieces.get(i), font, Color.BLACK, x, y, w, h, false, HAlignment.LEFT, true);
            x += pieceWidths.get(i);
        }
    }

    private static void drawManaCost(Graphics g, ManaCost cost, float x, float y, float w, float h, float manaSymbolSize) {
        float manaCostWidth = CardFaceSymbols.getWidth(cost, manaSymbolSize);
        CardFaceSymbols.drawManaCost(g, cost, x + (w - manaCostWidth) / 2, y + (h - manaSymbolSize) / 2, manaSymbolSize);
    }

    public static void drawFoilEffect(Graphics g, CardView card, float x, float y, float w, float h) {
        if (isPreferenceEnabled(FPref.UI_OVERLAY_FOIL_EFFECT) && card.mayBeShown()) {
            int foil = card.getOriginal().getFoilIndex();
            if (foil > 0) {
                CardFaceSymbols.drawOther(g, String.format("foil%02d", foil), x, y, w, h);
            }
        }
    }

    private static boolean isPreferenceEnabled(FPref preferenceName) {
        return FModel.getPreferences().getPrefBoolean(preferenceName);
    }

    private static boolean isShowingOverlays(CardView card) {
        return isPreferenceEnabled(FPref.UI_SHOW_CARD_OVERLAYS) && card != null;
    }

    private static boolean showCardNameOverlay(CardView card) {
        return isShowingOverlays(card) && isPreferenceEnabled(FPref.UI_OVERLAY_CARD_NAME);
    }

    private static boolean showCardPowerOverlay(CardView card) {
        return isShowingOverlays(card) && isPreferenceEnabled(FPref.UI_OVERLAY_CARD_POWER);
    }

    private static boolean showCardManaCostOverlay(CardView card) {
        return isShowingOverlays(card) &&
                isPreferenceEnabled(FPref.UI_OVERLAY_CARD_MANA_COST);
    }

    private static boolean showCardIdOverlay(CardView card) {
        return card.getId() > 0 && isShowingOverlays(card) && isPreferenceEnabled(FPref.UI_OVERLAY_CARD_ID);
    }
}
