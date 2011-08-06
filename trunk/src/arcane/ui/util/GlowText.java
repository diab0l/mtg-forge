
package arcane.ui.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.Locale;

import javax.swing.JLabel;

public class GlowText extends JLabel {
	private static final long serialVersionUID = -2868833097364223352L;
	private int glowSize;
	@SuppressWarnings("unused")
	private float glowIntensity;
	private Color glowColor;
	private boolean wrap;

	public void setGlow (Color glowColor, int size, float intensity) {
		this.glowColor = glowColor;
		this.glowSize = size;
		this.glowIntensity = intensity;
	}

	public void setWrap (boolean wrap) {
		this.wrap = wrap;
	}

	public Dimension getPreferredSize () {
		Dimension size = super.getPreferredSize();
		size.width += glowSize;
		size.height += glowSize / 2;
		return size;
	}

	public void setText (String text) {
		super.setText(text);
	}

	public void paint (Graphics g) {
		if (getText().length() == 0) return;

		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Dimension size = getSize();
		int textX = 0, textY = 0;
		int wrapWidth = Math.max(0, wrap ? size.width - glowSize : Integer.MAX_VALUE);

		AttributedString attributedString = new AttributedString(getText());
		attributedString.addAttribute(TextAttribute.FONT, getFont());
		AttributedCharacterIterator charIterator = attributedString.getIterator();
		FontRenderContext fontContext = g2d.getFontRenderContext();

		LineBreakMeasurer measurer = new LineBreakMeasurer(charIterator, BreakIterator.getWordInstance(Locale.ENGLISH), fontContext);
		int lineCount = 0;
		while (measurer.getPosition() < charIterator.getEndIndex()) {
		    	measurer.nextLayout(wrapWidth);
			lineCount++;
			if (lineCount > 2) break;
		}
		charIterator.first();
		// Use char wrap if word wrap would cause more than two lines of text.
		if (lineCount > 2)
			measurer = new LineBreakMeasurer(charIterator, BreakIterator.getCharacterInstance(Locale.ENGLISH), fontContext);
		else
			measurer.setPosition(0);
		while (measurer.getPosition() < charIterator.getEndIndex()) {
			TextLayout textLayout = measurer.nextLayout(wrapWidth);
			float ascent = textLayout.getAscent();
			textY += ascent; // Move down to baseline.

			g2d.setColor(glowColor);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
			textLayout.draw(g2d, textX + glowSize / 2 + 1, textY + glowSize / 2 - 1);
			textLayout.draw(g2d, textX + glowSize / 2 + 1, textY + glowSize / 2 + 1);
			textLayout.draw(g2d, textX + glowSize / 2 - 1, textY + glowSize / 2 - 1);
			textLayout.draw(g2d, textX + glowSize / 2 - 1, textY + glowSize / 2 + 1);

			g2d.setColor(getForeground());
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			textLayout.draw(g2d, textX + glowSize / 2, textY + glowSize / 2);

			textY += textLayout.getDescent() + textLayout.getLeading(); // Move down to top of next line.
		}
	}
}
