package forge.card.mana;

import org.testng.annotations.Test;

import forge.Card;
import forge.Constant;

/**
 * <p>
 * Mana_PartTest class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
@Test(groups = { "UnitTest" }, timeOut = 1000)
public class ManaPartTest {
    /**
     * <p>
     * testPayManaCost.
     * </p>
     */
    @Test(groups = { "UnitTest", "fast" }, timeOut = 5000)
    public void testPayManaCost() {
        // test constructor
        @SuppressWarnings("unused")
        ManaCostBeingPaid p = new ManaCostBeingPaid("G");
        p = new ManaCostBeingPaid("U");
        p = new ManaCostBeingPaid("W");
        p = new ManaCostBeingPaid("R");
        p = new ManaCostBeingPaid("B");
        p = new ManaCostBeingPaid("0");
        p = new ManaCostBeingPaid("1");
        p = new ManaCostBeingPaid("2");
        p = new ManaCostBeingPaid("3");
        p = new ManaCostBeingPaid("4");
        p = new ManaCostBeingPaid("5");
        p = new ManaCostBeingPaid("6");
        p = new ManaCostBeingPaid("7");
        p = new ManaCostBeingPaid("8");
        p = new ManaCostBeingPaid("9");
        p = new ManaCostBeingPaid("10");

        p = new ManaCostBeingPaid("GW");
        p = new ManaCostBeingPaid("1 G");
        p = new ManaCostBeingPaid("1 GW");
        p = new ManaCostBeingPaid("GW GW");
        p = new ManaCostBeingPaid("GW GW GW");
        p = new ManaCostBeingPaid("GW GW GW GW");

        p = new ManaCostBeingPaid("G G");
        p = new ManaCostBeingPaid("G G G");
        p = new ManaCostBeingPaid("G G G");
        p = new ManaCostBeingPaid("G G G G");

        p = new ManaCostBeingPaid("2 GW GW GW");
        p = new ManaCostBeingPaid("3 G G G");
        p = new ManaCostBeingPaid("12 GW GW GW");
        p = new ManaCostBeingPaid("11 G G G");

        p = new ManaCostBeingPaid("2/U");
        p = new ManaCostBeingPaid("2/B 2/B");
        p = new ManaCostBeingPaid("2/G 2/G 2/G");
        p = new ManaCostBeingPaid("2/R 2/R 2/R 2/R");
        p = new ManaCostBeingPaid("2/W 2/B 2/U 2/R 2/G");

        final ManaCostBeingPaid p1 = new ManaCostBeingPaid("2/U");

        this.check(0.3, p1.isNeeded("G"));
        this.check(0.4, p1.isNeeded("U"));
        this.check(0.5, p1.isNeeded("B"));
        this.check(0.6, p1.isNeeded("W"));
        this.check(0.7, p1.isNeeded("R"));
        this.check(0.8, p1.isNeeded("1"));

        p1.addMana("U");
        this.check(0.9, p1.isPaid());

        this.check(0.91, !p1.isNeeded("R"));

        final ManaCostBeingPaid p2 = new ManaCostBeingPaid("G");
        this.check(1, p2.isNeeded("G"));

        this.check(1.1, !p2.isNeeded("U"));
        this.check(1.2, !p2.isNeeded("B"));
        this.check(1.3, !p2.isNeeded("W"));
        this.check(1.4, !p2.isNeeded("R"));
        this.check(1.5, !p2.isNeeded("1"));

        p2.addMana("G");
        this.check(2, p2.isPaid());

        this.check(2.1, !p2.isNeeded("G"));

        final ManaCostBeingPaid p4 = new ManaCostBeingPaid("1");

        this.check(3, p4.isNeeded("G"));
        this.check(4, p4.isNeeded("U"));
        this.check(5, p4.isNeeded("B"));
        this.check(6, p4.isNeeded("W"));
        this.check(7, p4.isNeeded("R"));
        this.check(8, p4.isNeeded("1"));

        p4.addMana("B");
        this.check(9, p4.isPaid());

        this.check(9.1, !p4.isNeeded("R"));

        final ManaCostBeingPaid p5 = new ManaCostBeingPaid("GW");

        this.check(10, p5.isNeeded("G"));
        this.check(13, p5.isNeeded("W"));

        this.check(11, !p5.isNeeded("U"));
        this.check(12, !p5.isNeeded("B"));
        this.check(14, !p5.isNeeded("R"));
        this.check(15, !p5.isNeeded("1"));

        p5.addMana("W");
        this.check(16, p5.isPaid());

        this.check(17, !p5.isNeeded("W"));

        final ManaCostBeingPaid p6 = new ManaCostBeingPaid("BR");

        this.check(17.1, p6.isNeeded("B"));
        this.check(17.2, p6.isNeeded("R"));

        this.check(17.3, !p6.isNeeded("U"));
        this.check(17.4, !p6.isNeeded("W"));
        this.check(17.5, !p6.isNeeded("G"));
        this.check(17.6, !p6.isNeeded("1"));

        p6.addMana("R");
        this.check(17.7, p6.isPaid());

        this.check(17.8, !p6.isNeeded("R"));

        final ManaCostBeingPaid p7 = new ManaCostBeingPaid("1 G G");

        p7.addMana("G");

        this.check(18.1, p7.isNeeded("G"));
        this.check(18.2, p7.isNeeded("W"));
        this.check(18.3, p7.isNeeded("U"));
        this.check(18.4, p7.isNeeded("B"));
        this.check(18.5, p7.isNeeded("R"));
        this.check(18.6, p7.isNeeded("1"));

        p7.addMana("1");
        p7.addMana("G");

        this.check(18.7, p7.isPaid());

        this.check(18.8, !p7.isNeeded("W"));

        final ManaCostBeingPaid p8 = new ManaCostBeingPaid("0");

        this.check(19.1, !p8.isNeeded("1"));
        this.check(19.2, !p8.isNeeded("G"));
        this.check(19.3, !p8.isNeeded("U"));

        this.check(19.4, p8.isPaid());

        this.check(19.5, !p8.isNeeded("R"));

        final ManaCostBeingPaid p9 = new ManaCostBeingPaid("G G");

        this.check(20.1, !p9.isNeeded("1"));
        this.check(20.2, p9.isNeeded("G"));

        this.check(20.3, !p9.isNeeded("U"));

        p9.addMana("G");
        p9.addMana("G");

        this.check(20.4, p9.isPaid());

        this.check(20.5, !p9.isNeeded("B"));

        final ManaCostBeingPaid p10 = new ManaCostBeingPaid("G G G");

        this.check(21.1, !p10.isNeeded("W"));
        this.check(21.2, p10.isNeeded("G"));

        this.check(21.3, !p10.isNeeded("R"));

        p10.addMana("G");
        p10.addMana("G");
        p10.addMana("G");

        this.check(21.4, p10.isPaid());

        this.check(21.5, !p10.isNeeded("U"));

        final ManaCostBeingPaid p11 = new ManaCostBeingPaid("G G G G");

        this.check(22.1, !p11.isNeeded("W"));
        this.check(22.2, p11.isNeeded("G"));

        this.check(22.3, !p11.isNeeded("R"));

        p11.addMana("G");
        p11.addMana("G");
        p11.addMana("G");
        p11.addMana("G");

        this.check(22.4, p11.isPaid());

        this.check(22.5, !p11.isNeeded("G"));

        final ManaCostBeingPaid p12 = new ManaCostBeingPaid("GW");

        this.check(23.1, p12.isNeeded("W"));
        this.check(23.2, p12.isNeeded("G"));
        this.check(23.3, !p12.isNeeded("R"));

        p12.addMana("G");

        this.check(23.4, p12.isPaid());

        this.check(23.5, !p12.isNeeded("G"));

        final ManaCostBeingPaid p13 = new ManaCostBeingPaid("GW");

        this.check(24.1, p13.isNeeded("W"));
        this.check(24.2, p13.isNeeded("G"));
        this.check(24.3, !p13.isNeeded("U"));

        p13.addMana("W");

        this.check(24.4, p13.isPaid());

        this.check(24.5, !p13.isNeeded("W"));

        final ManaCostBeingPaid p14 = new ManaCostBeingPaid("3 GW GW");

        this.check(25.1, p14.isNeeded("W"));
        this.check(25.2, p14.isNeeded("G"));
        this.check(25.3, p14.isNeeded("U"));

        p14.addMana("1");
        p14.addMana("1");
        p14.addMana("1");

        this.check(25.4, p14.isNeeded("W"));
        this.check(25.5, p14.isNeeded("G"));
        this.check(25.6, !p14.isNeeded("U"));

        p14.addMana("G");
        p14.addMana("W");

        this.check(25.7, p14.isPaid());

        this.check(25.8, !p14.isNeeded("W"));
        this.check(25.9, !p14.isNeeded("G"));
        this.check(25.10, !p14.isNeeded("1"));
        this.check(25.11, !p14.isNeeded("R"));

        final ManaCostBeingPaid p15 = new ManaCostBeingPaid("4");

        this.check(26.1, p15.isNeeded("W"));
        this.check(26.2, p15.isNeeded("G"));
        this.check(26.3, p15.isNeeded("U"));

        p15.addMana("1");
        p15.addMana("1");
        p15.addMana("1");
        p15.addMana("1");

        this.check(26.4, p15.isPaid());

        final ManaCostBeingPaid p16 = new ManaCostBeingPaid("10");

        p16.addMana("G");
        p16.addMana("W");
        p16.addMana("R");
        p16.addMana("U");
        p16.addMana("B");

        p16.addMana("1");

        p16.addMana("W");
        p16.addMana("R");
        p16.addMana("U");
        p16.addMana("B");

        this.check(27, p16.isPaid());

        final ManaCostBeingPaid p17 = new ManaCostBeingPaid("12 G GW");

        for (int i = 0; i < 12; i++) {
            p17.addMana("R");
        }

        p17.addMana("G");
        p17.addMana("W");

        this.check(28, p17.isPaid());

        final ManaCostBeingPaid p18 = new ManaCostBeingPaid("2 W B U R G");

        for (int i = 0; i < 1; i++) {
            p18.addMana("R");
        }

        for (int i = 0; i < 2; i++) {
            p18.addMana("1");
        }

        for (int i = 0; i < 1; i++) {
            p18.addMana("G");
            p18.addMana("W");
            p18.addMana("B");
            p18.addMana("U");

        }
        this.check(29, p18.isPaid());

        final ManaCostBeingPaid p19 = new ManaCostBeingPaid("W B U R G W");

        p19.addMana("R");
        p19.addMana("G");
        p19.addMana("B");
        p19.addMana("U");

        p19.addMana("W");
        p19.addMana("W");

        this.check(30, p19.isPaid());

        final ManaCostBeingPaid p20 = new ManaCostBeingPaid("W B U R G W B U R G");

        for (int i = 0; i < 2; i++) {
            p20.addMana("W");
            p20.addMana("R");
            p20.addMana("G");
            p20.addMana("B");
            p20.addMana("U");
        }

        this.check(31, p20.isPaid());

        final ManaCostBeingPaid p21 = new ManaCostBeingPaid("2 W B U R G W B U R G G");

        for (int i = 0; i < 2; i++) {
            p21.addMana("W");
            p21.addMana("R");
            p21.addMana("G");
            p21.addMana("B");
            p21.addMana("U");
        }

        p21.addMana("1");
        p21.addMana("1");
        p21.addMana("G");

        this.check(32, p21.isPaid());

        final ManaCostBeingPaid p22 = new ManaCostBeingPaid("1 B R");

        p22.addMana("B");
        p22.addMana("1");
        p22.addMana("R");

        this.check(33, p22.isPaid());

        final ManaCostBeingPaid p23 = new ManaCostBeingPaid("B R");

        p23.addMana("B");
        p23.addMana("R");

        this.check(34, p23.isPaid());

        final ManaCostBeingPaid p24 = new ManaCostBeingPaid("2/B 2/B 2/B");

        this.check(35, p24.isNeeded("G"));

        p24.addMana("B");
        this.check(36, p24.toString().equals("2/B 2/B"));

        p24.addMana("B");
        this.check(37, p24.toString().equals("2/B"));

        p24.addMana("B");
        this.check(38, p24.isPaid());

        final ManaCostBeingPaid p25 = new ManaCostBeingPaid("2/G");

        p25.addMana("1");
        this.check(39, p25.toString().equals("1"));

        p25.addMana("W");
        this.check(40, p25.isPaid());

        final ManaCostBeingPaid p27 = new ManaCostBeingPaid("2/R 2/R");

        p27.addMana("1");
        this.check(41, p27.toString().equals("2/R 1"));

        p27.addMana("W");
        this.check(42, p27.toString().equals("2/R"));

        final ManaCostBeingPaid p26 = new ManaCostBeingPaid("2/W 2/W");

        for (int i = 0; i < 4; i++) {
            this.check(43, !p26.isPaid());
            p26.addMana("1");
        }

        this.check(44, p26.isPaid());

        final ManaCostBeingPaid p28 = new ManaCostBeingPaid("2/W 2/B 2/U 2/R 2/G");
        this.check(45, !p28.isPaid());

        p28.addMana("B");
        p28.addMana("R");
        p28.addMana("G");
        p28.addMana("W");
        p28.addMana("U");

        this.check(45.1, p28.isPaid(), p28);

        final ManaCostBeingPaid p29 = new ManaCostBeingPaid("2/W 2/B 2/U 2/R 2/G");
        this.check(46, !p29.isPaid());

        final Card c = new Card();

        p29.addMana(new Mana(Constant.Color.BLACK, c, null));
        p29.addMana(new Mana(Constant.Color.RED, c, null));
        p29.addMana(new Mana(Constant.Color.GREEN, c, null));
        p29.addMana(new Mana(Constant.Color.WHITE, c,  null));
        p29.addMana(new Mana(Constant.Color.BLUE, c, null));

        this.check(46.1, p29.isPaid(), p29);

    } // testPayManaCost()

    /**
     * <p>
     * check.
     * </p>
     * 
     * @param n
     *            a double.
     * @param b
     *            a boolean.
     */
    void check(final double n, final boolean b) {
        if (!b) {
            System.out.println("failed : " + n);
        }
    }

    /**
     * <p>
     * check.
     * </p>
     * 
     * @param n
     *            a double.
     * @param b
     *            a boolean.
     * @param p
     *            a {@link forge.card.mana.ManaCostBeingPaid} object.
     */
    void check(final double n, final boolean b, final ManaCostBeingPaid p) {
        if (!b) {
            System.out.println("failed : " + n);
            System.out.println(p.toString());
        }
    }
}
