package forge.deck.io;

import forge.ImageCache;
import forge.deck.Deck;
import forge.item.PaperCard;
import forge.properties.NewConstants;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class DeckHtmlSerializer {
        public static void writeDeckHtml(final Deck d, final File f) {
            try {
                final BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                writeDeckHtml(d, writer);
                writer.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * <p>
         * writeDeck.
         * </p>
         * 
         * @param d
         *            a {@link forge.deck.Deck} object.
         * @param out
         *            a {@link java.io.BufferedWriter} object.
         * @throws java.io.IOException
         *             if any.
         */
        private static void writeDeckHtml(final Deck d, final BufferedWriter out) throws IOException {
            Template temp = null;
            final int cardBorder = 0;
            final int height = 319;
            final int width = 222;
    
            /* Create and adjust the configuration */
            final Configuration cfg = new Configuration();
            try {
                cfg.setClassForTemplateLoading(d.getClass(), "/");
                cfg.setObjectWrapper(new DefaultObjectWrapper());
    
                /*
                 * ------------------------------------------------------------------
                 * -
                 */
                /*
                 * You usually do these for many times in the application
                 * life-cycle:
                 */
    
                /* Get or create a template */
                temp = cfg.getTemplate("proxy-template.ftl");
    
                /* Create a data-model */
                final Map<String, Object> root = new HashMap<String, Object>();
                root.put("title", d.getName());
                final List<String> list = new ArrayList<String>();
                for (final Entry<PaperCard, Integer> card : d.getMain()) {
                    // System.out.println(card.getSets().get(card.getSets().size() - 1).URL);
                    for (int i = card.getValue().intValue(); i > 0; --i ) {
                        PaperCard r = card.getKey();
                        String url = NewConstants.URL_PIC_DOWNLOAD + ImageCache.getDownloadUrl(r, false);
                        list.add(url);
                    }
                }
    
                final TreeMap<String, Integer> map = new TreeMap<String, Integer>();
                for (final Entry<PaperCard, Integer> entry : d.getMain()) {
                    map.put(entry.getKey().getName(), entry.getValue());
                    // System.out.println(entry.getValue() + " " +
                    // entry.getKey().getName());
                }
    
                root.put("urls", list);
                root.put("cardBorder", cardBorder);
                root.put("height", height);
                root.put("width", width);
                root.put("cardlistWidth", width - 11);
                root.put("cardList", map);
    
                /* Merge data-model with template */
                temp.process(root, out);
                out.flush();
            } catch (final IOException e) {
                System.out.println(e.toString());
            } catch (final TemplateException e) {
                System.out.println(e.toString());
            }
        }
}