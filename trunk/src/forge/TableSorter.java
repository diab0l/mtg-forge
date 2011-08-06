package forge;
import java.util.*;

import forge.properties.ForgeProps;
import forge.properties.NewConstants;
@SuppressWarnings("unchecked") // Comparable needs <type>

public class TableSorter implements Comparator, NewConstants
{
  private final int column;
  private boolean ascending;

  private CardList all;

  //used by compare()
  private Comparable aCom = null;
  private Comparable bCom = null;
  private Card a;
  private Card b;

  //used if in_column is 7, new cards first - the order is based on cards.txt
  //static because this should only be read once
  //static to try to reduce file io operations
  private static HashMap<String, Integer> cardsTxt = null;

  //                             0       1       2       3        4     5          6          7
  //private String column[] = {"Qty", "Name", "Cost", "Color", "Type", "Stats", "Rarity"}; New cards first - the order is based on cards.txt
  public TableSorter(CardList in_all, int in_column, boolean in_ascending)
  {
    all = new CardList(in_all.toArray());
    column = in_column;
    ascending = in_ascending;

    if(cardsTxt == null)
      cardsTxt = readCardsTxt();
  }
  //reads the file "cards.txt"
  private HashMap readCardsTxt()
  {
    HashMap<String, Integer>  map = new HashMap<String, Integer> ();
    ArrayList list = FileUtil.readFile(ForgeProps.getFile(CARDS));
   
    for(int i = 0; i < list.size(); i++)
       map.put(list.get(i).toString().trim(), new Integer(i));
    
    return map;
  }
 
 
  final public int compare(Object in_a, Object in_b)
  {
    a = (Card)in_a;
    b = (Card)in_b;

    if(column == 0)//Qty
    {
      aCom = new Integer(countCardName(a.getName(), all));
      bCom = new Integer(countCardName(b.getName(), all));
    }
    else if (column == 1)//Name
    {
      aCom = a.getName();
      bCom = b.getName();
    }
    else if (column == 2)//Cost
    {
      aCom = new Integer(CardUtil.getConvertedManaCost(a.getManaCost()));
      bCom = new Integer(CardUtil.getConvertedManaCost(b.getManaCost()));

      if(a.isLand())
        aCom = new Integer(-1);
      if(b.isLand())
        bCom = new Integer(-1);
    }
    else if (column == 3)//Color
    {
      aCom = getColor(a);
      bCom = getColor(b);
    }
    else if (column == 4)//Type
    {
      aCom = getType(a);
      bCom = getType(b);
    }
    else if (column == 5)//Stats, attack and defense
    {
      aCom = new Float(-1);
      bCom = new Float(-1);

      if(a.isCreature())
        aCom = new Float(a.getBaseAttack() +"." +a.getBaseDefense());
      if(b.isCreature())
        bCom = new Float(b.getBaseAttack() +"." +b.getBaseDefense());
    }
    else if (column == 6)//Rarity
    {
      aCom = getRarity(a);
      bCom = getRarity(b);
    }
    else if (column == 7)//New First
    {
      aCom = sortNewFirst(a);
      bCom = sortNewFirst(b);
    }

    if(ascending)
      return aCom.compareTo(bCom);
    else
      return bCom.compareTo(aCom);
  }//compare()

  final private int countCardName(String name, CardList c)
  {
    int count = 0;
    for(int i = 0; i < c.size(); i++)
      if(name.equals(c.get(i).getName()))
         count++;

    return count;
  }

  final private Integer getRarity(Card c)
  {
    if(c.getRarity().equals("Common"))
      return new Integer(1);
    else if(c.getRarity().equals("Uncommon"))
      return new Integer(2);
    else if(c.getRarity().equals("Rare"))
      return new Integer(3);
    else if(c.getRarity().equals("Land"))
      return new Integer(4);
    else
      return new Integer(5);
  }

  final public static String getColor(Card c)
  {
    ArrayList list = CardUtil.getColors(c);

    if(list.size() == 1)
      return list.get(0).toString();

    return "multi";
  }

  final private Comparable getType(Card c)
  {
    return c.getType().toString();
  }

  final private Comparable sortNewFirst(Card c)
  {
   if(! cardsTxt.containsKey(c.getName()))
     throw new RuntimeException("TableSorter : sortNewFirst() error, Card not found - " +c.getName() +" in hashmap - " +cardsTxt);

   return cardsTxt.get(c.getName());
  }
}