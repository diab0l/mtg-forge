package forge;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import forge.error.ErrorViewer;


public class FileUtil {
    public static boolean doesFileExist(String filename) {
        File f = new File(filename);
        return f.exists();
    }
    
    public static void writeFile(String filename, List<String> data) {
        writeFile(new File(filename), data);
    }
    
    //writes each element of ArrayList on a separate line
    //this is used to write a file of Strings
    //this will create a new file if needed
    //if filename already exists, it is deleted
    public static void writeFile(File file, List<String> data) {
        try {
            Collections.sort(data);
            
            BufferedWriter io = new BufferedWriter(new FileWriter(file));
            for(int i = 0; i < data.size(); i++)
                io.write(data.get(i) + "\r\n");
            
            io.flush();
            io.close();
        } catch(Exception ex) {
            ErrorViewer.showError(ex);
            throw new RuntimeException("FileUtil : writeFile() error, problem writing file - " + file + " : " + ex);
        }
    }//writeFile()
    
    public static ArrayList<String> readFile(String filename) {
        return readFile(new File(filename));
    }
    
    //reads line by line and adds each line to the ArrayList
    //this will return blank lines as well
    //if filename not found, returns an empty ArrayList
    public static ArrayList<String> readFile(File file) {
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader in;
        
        try {
            if(file == null || !file.exists()) return list;
            

            in = new BufferedReader(new FileReader(file));
            
            String line;
            while((line = in.readLine()) != null)
                list.add(line);
        } catch(Exception ex) {
            ErrorViewer.showError(ex);
            throw new RuntimeException("FileUtil : readFile() error, " + ex);
        }
        
        return list;
    }//readFile()
}