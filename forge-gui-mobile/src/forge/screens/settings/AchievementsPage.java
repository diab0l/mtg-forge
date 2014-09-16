package forge.screens.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.math.Rectangle;

import forge.Graphics;
import forge.achievement.Achievement;
import forge.achievement.AchievementCollection;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.assets.FSkinTexture;
import forge.menu.FDropDown;
import forge.screens.FScreen;
import forge.screens.TabPageScreen.TabPage;
import forge.toolbox.FComboBox;
import forge.toolbox.FEvent;
import forge.toolbox.FLabel;
import forge.toolbox.FScrollPane;
import forge.toolbox.FEvent.FEventHandler;
import forge.util.Utils;

public class AchievementsPage extends TabPage<SettingsScreen> {
    private static final float PADDING = Utils.scaleMin(5);
    private static final float SELECTED_BORDER_THICKNESS = Utils.scaleMin(1);
    private static final int MIN_SHELVES = 4;
    private static final int TROPHIES_PER_SHELVE = 4;
    private static final FSkinFont NAME_FONT = FSkinFont.get(14);
    private static final FSkinFont DESC_FONT = FSkinFont.get(12);
    private static final FSkinColor TEXT_COLOR = FLabel.DEFAULT_TEXT_COLOR;
    private static final FSkinColor NOT_EARNED_COLOR = TEXT_COLOR.alphaColor(0.5f);

    private final FComboBox<AchievementCollection> cbCollections = add(new FComboBox<AchievementCollection>());
    private final TrophyCase trophyCase = add(new TrophyCase());

    protected AchievementsPage() {
        super("Achievements", FSkinImage.GOLD_TROPHY);

        AchievementCollection.buildComboBox(cbCollections);

        cbCollections.setSelectedIndex(0);
        cbCollections.setAlignment(HAlignment.CENTER);
        cbCollections.setChangedHandler(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                setAchievements(cbCollections.getSelectedItem());
            }
        });
        setAchievements(cbCollections.getSelectedItem());
    }

    @Override
    protected void doLayout(float width, float height) {
        float x = PADDING;
        float y = PADDING;
        width -= 2 * x;

        cbCollections.setBounds(x, y, width, cbCollections.getHeight());
        y += cbCollections.getHeight() + PADDING;
        trophyCase.setBounds(x, y, width, height - PADDING - y);
    }

    private void setAchievements(AchievementCollection achievements0) {
        trophyCase.achievements = achievements0;
        trophyCase.selectedAchievement = null;
        trophyCase.shelfCount = Math.max(achievements0.getCount() % TROPHIES_PER_SHELVE, MIN_SHELVES);
        trophyCase.revalidate();
    }

    private static class TrophyCase extends FScrollPane {
        private static final Color FORE_COLOR = new Color(239f / 255f, 220f / 255f, 144f / 255f, 1f);

        private AchievementCollection achievements;
        private int shelfCount;
        private float extraWidth = 0;
        private Achievement selectedAchievement;

        @Override
        protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
            float scrollWidth = visibleWidth + extraWidth;
            float scale = scrollWidth / FSkinImage.TROPHY_CASE_TOP.getWidth();
            float scrollHeight = (FSkinImage.TROPHY_CASE_TOP.getHeight() +
                    shelfCount * FSkinImage.TROPHY_CASE_SHELF.getHeight()) * scale;
            return new ScrollBounds(scrollWidth, scrollHeight);
        }

        private Achievement getAchievementAt(float x0, float y0) {
            float w = getScrollWidth();
            float scale = w / FSkinImage.TROPHY_CASE_TOP.getWidth();
            float trophyScale = scale * 1.8f;

            float shelfHeight = FSkinImage.TROPHY_CASE_SHELF.getHeight() * scale;
            float trophyWidth = FSkinImage.GOLD_TROPHY.getWidth() * trophyScale;
            float trophyHeight = FSkinImage.GOLD_TROPHY.getHeight() * trophyScale;
            float x = -getScrollLeft() + (w - TROPHIES_PER_SHELVE * trophyWidth) / 2;
            float y = -getScrollTop() + FSkinImage.TROPHY_CASE_TOP.getHeight() * scale + (shelfHeight - trophyHeight - 12 * scale) / 2;

            int trophyCount = 0;
            float startX = x;

            for (Achievement achievement : achievements) {
                if (trophyCount == TROPHIES_PER_SHELVE) {
                    trophyCount = 0;
                    x = startX;
                    y += shelfHeight;

                    if (y >= getHeight()) {
                        return null;
                    }
                }

                if (x <= x0 && x0 < x + trophyWidth && y <= y0 && y0 < y + shelfHeight) {
                    return achievement;
                }

                trophyCount++;
                x += trophyWidth;
            }
            return null;
        }

        @Override
        public boolean tap(float x, float y, int count) {
            Achievement achievement = getAchievementAt(x, y);
            if (achievement == selectedAchievement) {
                achievement = null; //unselect if selected achievement tapped again
            }
            selectedAchievement = achievement;
            return true;
        }

        @Override
        public boolean zoom(float x, float y, float amount) {
            selectedAchievement = null; //unselect when zooming

            float oldScrollLeft = getScrollLeft();
            float oldScrollTop = getScrollTop();
            float oldScrollWidth = getScrollWidth();
            float oldScrollHeight = getScrollHeight();

            x += oldScrollLeft;
            y += oldScrollTop;

            float zoom = oldScrollWidth / getWidth();
            extraWidth += amount * zoom; //scale amount by current zoom
            if (extraWidth < 0) {
                extraWidth = 0;
            }
            revalidate(); //apply change in height to all scroll panes

            //adjust scroll positions to keep x, y in the same spot
            float newScrollWidth = getScrollWidth();
            float xAfter = x * newScrollWidth / oldScrollWidth;
            setScrollLeft(oldScrollLeft + xAfter - x);

            float newScrollHeight = getScrollHeight();
            float yAfter = y * newScrollHeight / oldScrollHeight;
            setScrollTop(oldScrollTop + yAfter - y);
            return true;
        }

        @Override
        protected void drawBackground(Graphics g) {
            float x = -getScrollLeft();
            float y = -getScrollTop();
            float w = getScrollWidth();
            float scale = w / FSkinImage.TROPHY_CASE_TOP.getWidth();
            float trophyScale = scale * 1.8f;

            float topHeight = FSkinImage.TROPHY_CASE_TOP.getHeight() * scale;
            float shelfHeight = FSkinImage.TROPHY_CASE_SHELF.getHeight() * scale;
            float trophyWidth = FSkinImage.GOLD_TROPHY.getWidth() * trophyScale;
            float trophyHeight = FSkinImage.GOLD_TROPHY.getHeight() * trophyScale;
            float plateWidth = FSkinImage.TROPHY_PLATE.getWidth() * scale;
            float plateHeight = FSkinImage.TROPHY_PLATE.getHeight() * scale;

            float titleHeight = plateHeight * 0.55f;
            float subTitleHeight = plateHeight * 0.35f;
            FSkinFont titleFont = FSkinFont.forHeight(titleHeight);
            FSkinFont subTitleFont = FSkinFont.forHeight(subTitleHeight);

            float plateY = y + topHeight + shelfHeight - plateHeight;
            float trophyStartY = y + topHeight + (shelfHeight - trophyHeight - 12 * scale) / 2;
            float plateOffset = (trophyWidth - plateWidth) / 2;

            if (y + topHeight > 0) {
                g.drawImage(FSkinImage.TROPHY_CASE_TOP, x, y, w, topHeight);
            }
            y += topHeight;

            for (int i = 0; i < shelfCount; i++) {
                if (y + shelfHeight > 0) {
                    g.drawImage(FSkinImage.TROPHY_CASE_SHELF, x, y, w, shelfHeight);
                }
                y += shelfHeight;
                if (y >= getHeight()) {
                    break;
                }
            }

            x += (w - TROPHIES_PER_SHELVE * trophyWidth) / 2;
            y = trophyStartY;

            int trophyCount = 0;
            float startX = x;
            Rectangle selectRect = null;

            for (Achievement achievement : achievements) {
                if (trophyCount == TROPHIES_PER_SHELVE) {
                    trophyCount = 0;
                    y += shelfHeight;
                    plateY += shelfHeight;
                    x = startX;

                    if (y >= getHeight()) {
                        return;
                    }
                }

                if (plateY + plateHeight > 0) {
                    if (achievement.earnedGold()) {
                        g.drawImage(FSkinImage.GOLD_TROPHY, x, y, trophyWidth, trophyHeight);
                    }
                    else if (achievement.earnedSilver()) {
                        g.drawImage(FSkinImage.SILVER_TROPHY, x, y, trophyWidth, trophyHeight);
                    }
                    else if (achievement.earnedBronze()) {
                        g.drawImage(FSkinImage.BRONZE_TROPHY, x, y, trophyWidth, trophyHeight);
                    }
                    g.drawImage(FSkinImage.TROPHY_PLATE, x + plateOffset, plateY, plateWidth, plateHeight);
    
                    g.drawText(achievement.getDisplayName(), titleFont, FORE_COLOR, x + plateOffset + plateWidth * 0.075f, plateY + plateHeight * 0.05f, plateWidth * 0.85f, titleHeight, false, HAlignment.CENTER, true);
    
                    String subTitle = achievement.getSubTitle();
                    if (subTitle != null) {
                        g.drawText(subTitle, subTitleFont, FORE_COLOR, x + plateOffset + plateWidth * 0.075f, plateY + plateHeight * 0.6f, plateWidth * 0.85f, subTitleHeight, false, HAlignment.CENTER, true);
                    }

                    if (achievement == selectedAchievement) {
                        g.drawRect(SELECTED_BORDER_THICKNESS, Color.GREEN, x, y, trophyWidth, shelfHeight);
                        selectRect = new Rectangle(x, y, trophyWidth, shelfHeight);
                    }
                }

                trophyCount++;
                x += trophyWidth;
            }

            //draw tooltip for selected achievement if needed
            if (selectRect != null) {
                String subTitle = selectedAchievement.getSubTitle();
                String goldDesc = selectedAchievement.getGoldDesc() == null ? null : "(Gold) " + selectedAchievement.getGoldDesc();
                String silverDesc = selectedAchievement.getSilverDesc() == null ? null : "(Silver) " + selectedAchievement.getSilverDesc();
                String bronzeDesc = selectedAchievement.getBronzeDesc() == null ? null : "(Bronze) " + selectedAchievement.getBronzeDesc();

                w = getWidth() - 2 * PADDING;
                float h = NAME_FONT.getLineHeight() + 2.5f * PADDING;
                if (subTitle != null) {
                    h += DESC_FONT.getLineHeight();
                }
                if (goldDesc != null) {
                    h += DESC_FONT.getLineHeight();
                }
                if (silverDesc != null) {
                    h += DESC_FONT.getLineHeight();
                }
                if (bronzeDesc != null) {
                    h += DESC_FONT.getLineHeight();
                }

                x = PADDING;
                y = selectRect.y + selectRect.height + PADDING;
                if (y + h > getHeight()) {
                    if (selectRect.y - PADDING > h) {
                        y = selectRect.y - h - PADDING;
                    }
                    else {
                        y = getHeight() - h;
                    }
                }

                g.drawImage(FSkinTexture.BG_TEXTURE, x, y, w, h);
                g.fillRect(FScreen.TEXTURE_OVERLAY_COLOR, x, y, w, h);
                g.drawRect(SELECTED_BORDER_THICKNESS, FDropDown.BORDER_COLOR, x, y, w, h);

                x += PADDING;
                y += PADDING;
                w -= 2 * PADDING;
                h -= 2 * PADDING;
                g.drawText(selectedAchievement.getDisplayName(), NAME_FONT, TEXT_COLOR, x, y, w, h, false, HAlignment.LEFT, false);
                y += NAME_FONT.getLineHeight();
                if (subTitle != null) {
                    g.drawText(subTitle, DESC_FONT, TEXT_COLOR, x, y, w, h, false, HAlignment.LEFT, false);
                    y += DESC_FONT.getLineHeight();
                }
                y += PADDING;
                if (goldDesc != null) {
                    g.drawText(goldDesc, DESC_FONT,
                            selectedAchievement.earnedGold() ? TEXT_COLOR : NOT_EARNED_COLOR,
                            x, y, w, h, false, HAlignment.LEFT, false);
                    y += DESC_FONT.getLineHeight();
                }
                if (silverDesc != null) {
                    g.drawText(silverDesc, DESC_FONT,
                            selectedAchievement.earnedSilver() ? TEXT_COLOR : NOT_EARNED_COLOR,
                            x, y, w, h, false, HAlignment.LEFT, false);
                    y += DESC_FONT.getLineHeight();
                }
                if (bronzeDesc != null) {
                    g.drawText(bronzeDesc, DESC_FONT,
                            selectedAchievement.earnedBronze() ? TEXT_COLOR : NOT_EARNED_COLOR,
                            x, y, w, h, false, HAlignment.LEFT, false);
                }
            }
        }
    }
}
