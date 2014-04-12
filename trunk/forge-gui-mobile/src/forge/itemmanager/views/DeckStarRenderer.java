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
package forge.itemmanager.views;

import com.badlogic.gdx.math.Vector2;

import forge.Forge.Graphics;
import forge.assets.FImage;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.deck.DeckProxy;
import forge.deck.io.DeckPreferences;
import forge.itemmanager.filters.ItemFilter;

/**
 * Displays favorite icons
 */
public class DeckStarRenderer extends ItemCellRenderer {
    @Override
    public void draw(Graphics g, Object value, FSkinFont font, FSkinColor foreColor, Vector2 loc, float itemWidth, float itemHeight) {
        DeckProxy deck;
        if (value instanceof DeckProxy) {
            deck = (DeckProxy) value;
        }
        else {
            return;
        }

        FImage image;
        if (DeckPreferences.getPrefs(deck).getStarCount() == 0) {
            image = FSkinImage.STAR_OUTINE;
        }
        else { //TODO: consider supporting more than 1 star
            image = FSkinImage.STAR_FILLED;
        }

        float size = 15;
        g.drawImage(image, loc.x, loc.y, size, size);

        loc.x += size + ItemFilter.PADDING;
    }
}
