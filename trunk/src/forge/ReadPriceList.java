package forge;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import forge.error.ErrorViewer;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;



public class ReadPriceList implements NewConstants {
    
    final private static String comment = "//";
        
    private HashMap<String, Long>     priceMap;
    
    public ReadPriceList() {
        setup();
    }
  
    private void setup() {
        priceMap = readFile(ForgeProps.getFile(QUEST.PRICE));
    }//setup()
        
    private HashMap<String, Long> readFile(File file) {
        BufferedReader in;
        HashMap<String, Long> map = new HashMap<String, Long>();
        try {
        	
            in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            
            //stop reading if end of file or blank line is read
            while(line != null && (line.trim().length() != 0)) {
                if(!line.startsWith(comment)) {
                	String s[] = line.split("=");
                	String name = s[0].trim();
                	String price = s[1].trim();

                    
                    try {
                        long val = Long.parseLong(price.trim());
                        map.put(name, val);
                     } catch (NumberFormatException nfe) {
                        System.out.println("NumberFormatException: " + nfe.getMessage());
                     }
                }
                line = in.readLine();
            }//if
            
        } catch(Exception ex) {
            ErrorViewer.showError(ex);
            throw new RuntimeException("ReadPriceList : readFile error, " + ex);
        }
        
        return map;
    }//readFile()
    
    public Map<String,Long> getPriceList()
    {
    	return priceMap;
    }
}
