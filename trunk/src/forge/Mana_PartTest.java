package forge;
public class Mana_PartTest
{
  static void testPayManaCost()
  {
    {
      //test constructor
      @SuppressWarnings("unused")
	  Mana_PayCost p = new Mana_PayCost("G");
      p = new Mana_PayCost("U");
      p = new Mana_PayCost("W");
      p = new Mana_PayCost("R");
      p = new Mana_PayCost("B");
      p = new Mana_PayCost("0");
      p = new Mana_PayCost("1");
      p = new Mana_PayCost("2");
      p = new Mana_PayCost("3");
      p = new Mana_PayCost("4");
      p = new Mana_PayCost("5");
      p = new Mana_PayCost("6");
      p = new Mana_PayCost("7");
      p = new Mana_PayCost("8");
      p = new Mana_PayCost("9");
      p = new Mana_PayCost("10");

      p = new Mana_PayCost("GW");
      p = new Mana_PayCost("1 G");
      p = new Mana_PayCost("1 GW");
      p = new Mana_PayCost("GW GW");
      p = new Mana_PayCost("GW GW GW");
      p = new Mana_PayCost("GW GW GW GW");

      p = new Mana_PayCost("G G");
      p = new Mana_PayCost("G G G");
      p = new Mana_PayCost("G G G");
      p = new Mana_PayCost("G G G G");

      p = new Mana_PayCost("2 GW GW GW");
      p = new Mana_PayCost("3 G G G");
      p = new Mana_PayCost("12 GW GW GW");
      p = new Mana_PayCost("11 G G G");

      p = new Mana_PayCost("2/U");
      p = new Mana_PayCost("2/B 2/B");
      p = new Mana_PayCost("2/G 2/G 2/G");
      p = new Mana_PayCost("2/R 2/R 2/R 2/R");
      p = new Mana_PayCost("2/W 2/B 2/U 2/R 2/G");
    }

    {
      Mana_PayCost p = new Mana_PayCost("2/U");

      check(0.3, p.isNeeded("G"));
      check(0.4, p.isNeeded("U"));
      check(0.5, p.isNeeded("B"));
      check(0.6, p.isNeeded("W"));
      check(0.7, p.isNeeded("R"));
      check(0.8, p.isNeeded("1"));

      p.addMana("U");
      check(0.9, p.isPaid());

      check(0.91, !p.isNeeded("R"));
    }


    {
      Mana_PayCost p = new Mana_PayCost("G");
      check(1, p.isNeeded("G"));

      check(1.1, !p.isNeeded("U"));
      check(1.2, !p.isNeeded("B"));
      check(1.3, !p.isNeeded("W"));
      check(1.4, !p.isNeeded("R"));
      check(1.5, !p.isNeeded("1"));

      p.addMana("G");
      check(2, p.isPaid());

      check(2.1, !p.isNeeded("G"));

    }

    {
      Mana_PayCost p = new Mana_PayCost("1");

      check(3, p.isNeeded("G"));
      check(4, p.isNeeded("U"));
      check(5, p.isNeeded("B"));
      check(6, p.isNeeded("W"));
      check(7, p.isNeeded("R"));
      check(8, p.isNeeded("1"));

      p.addMana("B");
      check(9, p.isPaid());

      check(9.1, !p.isNeeded("R"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("GW");

      check(10, p.isNeeded("G"));
      check(13, p.isNeeded("W"));

      check(11, !p.isNeeded("U"));
      check(12, !p.isNeeded("B"));
      check(14, !p.isNeeded("R"));
      check(15, !p.isNeeded("1"));

      p.addMana("W");
      check(16, p.isPaid());

      check(17, !p.isNeeded("W"));
    }


    {
      Mana_PayCost p = new Mana_PayCost("BR");

      check(17.1, p.isNeeded("B"));
      check(17.2, p.isNeeded("R"));

      check(17.3, !p.isNeeded("U"));
      check(17.4, !p.isNeeded("W"));
      check(17.5, !p.isNeeded("G"));
      check(17.6, !p.isNeeded("1"));

      p.addMana("R");
      check(17.7, p.isPaid());

      check(17.8, !p.isNeeded("R"));
    }


    {
      Mana_PayCost p = new Mana_PayCost("1 G G");

      p.addMana("G");

      check(18.1, p.isNeeded("G"));
      check(18.2, p.isNeeded("W"));
      check(18.3, p.isNeeded("U"));
      check(18.4, p.isNeeded("B"));
      check(18.5, p.isNeeded("R"));
      check(18.6, p.isNeeded("1"));

      p.addMana("1");
      p.addMana("G");

      check(18.7, p.isPaid());

      check(18.8, !p.isNeeded("W"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("0");

      check(19.1, !p.isNeeded("1"));
      check(19.2, !p.isNeeded("G"));
      check(19.3, !p.isNeeded("U"));

      check(19.4, p.isPaid());

      check(19.5, !p.isNeeded("R"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("G G");

      check(20.1, !p.isNeeded("1"));
      check(20.2, p.isNeeded("G"));

      check(20.3, !p.isNeeded("U"));

      p.addMana("G");
      p.addMana("G");

      check(20.4, p.isPaid());

      check(20.5, !p.isNeeded("B"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("G G G");

      check(21.1, !p.isNeeded("W"));
      check(21.2, p.isNeeded("G"));

      check(21.3, !p.isNeeded("R"));

      p.addMana("G");
      p.addMana("G");
      p.addMana("G");

      check(21.4, p.isPaid());

      check(21.5, !p.isNeeded("U"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("G G G G");

      check(22.1, !p.isNeeded("W"));
      check(22.2, p.isNeeded("G"));

      check(22.3, !p.isNeeded("R"));

      p.addMana("G");
      p.addMana("G");
      p.addMana("G");
      p.addMana("G");

      check(22.4, p.isPaid());

      check(22.5, !p.isNeeded("G"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("GW");

      check(23.1, p.isNeeded("W"));
      check(23.2, p.isNeeded("G"));
      check(23.3, !p.isNeeded("R"));

      p.addMana("G");

      check(23.4, p.isPaid());

      check(23.5, !p.isNeeded("G"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("GW");

      check(24.1, p.isNeeded("W"));
      check(24.2, p.isNeeded("G"));
      check(24.3, !p.isNeeded("U"));

      p.addMana("W");

      check(24.4, p.isPaid());

      check(24.5, !p.isNeeded("W"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("3 GW GW");

      check(25.1, p.isNeeded("W"));
      check(25.2, p.isNeeded("G"));
      check(25.3, p.isNeeded("U"));

      p.addMana("1");
      p.addMana("1");
      p.addMana("1");

      check(25.4, p.isNeeded("W"));
      check(25.5, p.isNeeded("G"));
      check(25.6, !p.isNeeded("U"));

      p.addMana("G");
      p.addMana("W");

      check(25.7, p.isPaid());

      check(25.8, !p.isNeeded("W"));
      check(25.9, !p.isNeeded("G"));
      check(25.10, !p.isNeeded("1"));
      check(25.11, !p.isNeeded("R"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("4");

      check(26.1, p.isNeeded("W"));
      check(26.2, p.isNeeded("G"));
      check(26.3, p.isNeeded("U"));

      p.addMana("1");
      p.addMana("1");
      p.addMana("1");
      p.addMana("1");

      check(26.4, p.isPaid());
    }

    {
      Mana_PayCost p = new Mana_PayCost("10");

      p.addMana("G");
      p.addMana("W");
      p.addMana("R");
      p.addMana("U");
      p.addMana("B");

      p.addMana("1");

      p.addMana("W");
      p.addMana("R");
      p.addMana("U");
      p.addMana("B");

      check(27, p.isPaid());
    }

    {
      Mana_PayCost p = new Mana_PayCost("12 G GW");

      for (int i = 0; i < 12; i++)
        p.addMana("R");

      p.addMana("G");
      p.addMana("W");

      check(28, p.isPaid());
    }

    {
      Mana_PayCost p = new Mana_PayCost("2 W B U R G");

      for (int i = 0; i < 1; i++)
        p.addMana("R");

      for (int i = 0; i < 2; i++)
        p.addMana("1");

      for (int i = 0; i < 1; i++)
      {
        p.addMana("G");
        p.addMana("W");
        p.addMana("B");
        p.addMana("U");

      }
      check(29, p.isPaid());
    }

    {
      Mana_PayCost p = new Mana_PayCost("W B U R G W");

      p.addMana("R");
      p.addMana("G");
      p.addMana("B");
      p.addMana("U");

      p.addMana("W");
      p.addMana("W");

      check(30, p.isPaid());
    }

    {
      Mana_PayCost p = new Mana_PayCost("W B U R G W B U R G");

      for (int i = 0; i < 2; i++)
      {
        p.addMana("W");
        p.addMana("R");
        p.addMana("G");
        p.addMana("B");
        p.addMana("U");
      }

      check(31, p.isPaid());
    }

    {
      Mana_PayCost p = new Mana_PayCost("2 W B U R G W B U R G G");

      for (int i = 0; i < 2; i++)
      {
        p.addMana("W");
        p.addMana("R");
        p.addMana("G");
        p.addMana("B");
        p.addMana("U");
      }

      p.addMana("1");
      p.addMana("1");
      p.addMana("G");

      check(32, p.isPaid());
    }

    {
      Mana_PayCost p = new Mana_PayCost("1 B R");

      p.addMana("B");
      p.addMana("1");
      p.addMana("R");

      check(33, p.isPaid());
    }

    {
      Mana_PayCost p = new Mana_PayCost("B R");

      p.addMana("B");
      p.addMana("R");

      check(34, p.isPaid());
    }


    {
      Mana_PayCost p = new Mana_PayCost("2/B 2/B 2/B");

      check(35, p.isNeeded("G"));

      p.addMana("B");
      check(36, p.toString().equals("2/B 2/B"));

      p.addMana("B");
      check(37, p.toString().equals("2/B"));

      p.addMana("B");
      check(38, p.isPaid());
    }


    {
      Mana_PayCost p = new Mana_PayCost("2/G");

      p.addMana("1");
      check(39, p.toString().equals("1"));

      p.addMana("W");
      check(40, p.isPaid());
    }

    {
      Mana_PayCost p = new Mana_PayCost("2/R 2/R");

      p.addMana("1");
      check(41, p.toString().equals("2/R 1"));

      p.addMana("W");
      check(42, p.toString().equals("2/R"));
    }

    {
      Mana_PayCost p = new Mana_PayCost("2/W 2/W");

      for(int i = 0; i < 4; i++)
      {
        check(43, p.isPaid() == false);
        p.addMana("1");
      }

      check(44, p.isPaid());
    }

  } //testPayManaCost()


  static void check(double n, boolean b)
  {
    if (!b)
      System.out.println("failed : " + n);
  }

  public static void main(String[] args)
  {
    try
    {

//      magic.core.ErrorReport.setThrowException(true);

      testPayManaCost();
      System.out.println("all tests passed");
    }
    catch (Exception ex)
    {
      System.out.println("failed : exception " + ex);
      ex.printStackTrace();
    }
  }
}
