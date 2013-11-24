package forge.game.cost;

import java.util.ArrayList;
import java.util.List;

import forge.game.card.Card;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class PaymentDecision {
    public int c = 0;
    public final List<Card> cards = new ArrayList<Card>();
    
    public PaymentDecision(int cnt) {
        c = cnt;
    }

    public PaymentDecision(List<Card> chosen) {
        cards.addAll(chosen);
    }
    
    public PaymentDecision(Card chosen) {
        cards.add(chosen);
    }    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return  String.format("Payment Decision: %d, %s", c, cards);
    }
}
