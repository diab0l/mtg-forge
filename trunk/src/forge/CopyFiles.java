
package forge;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import forge.properties.ForgeProps;
import forge.properties.NewConstants;


public class CopyFiles extends SwingWorker<Void, Integer> implements NewConstants {
    
    private List<File> FileList;
    JLabel             jLb;
    JProgressBar       jB;
    JCheckBox          jCheck;
    JButton            jSource;
    int                count;
    
    public CopyFiles(List<File> FileList, JLabel jLabelTotalFiles, JProgressBar Jbar, JCheckBox jCheckBox, JButton jButtonSource) {
        this.FileList = FileList;
        jLb = jLabelTotalFiles;
        jB = Jbar;
        jCheck = jCheckBox;
        jSource = jButtonSource;
    }
    
    @Override
    protected Void doInBackground() {
        for(int i = 0; i < this.FileList.size(); i++) {
            publish();
            String cName, name, source;
            name = this.FileList.get(i).getName();
            source = this.FileList.get(i).getAbsolutePath();
            cName = name.substring(0, name.length() - 8);
            cName = GuiDisplayUtil.cleanString(cName) + ".jpg";
            File sourceFile = new File(source);
            File base = ForgeProps.getFile(IMAGE_BASE);
            File reciever = new File(base, cName);
            reciever.delete();
            
            try {
                reciever.createNewFile();
                FileOutputStream fos = new FileOutputStream(reciever);
                FileInputStream fis = new FileInputStream(sourceFile);
                byte[] buff = new byte[32 * 1024];
                int length;
                while(fis.available() > 0) {
                    length = fis.read(buff);
                    if(length > 0) fos.write(buff, 0, length);
                }
                fos.flush();
                fis.close();
                fos.close();
                count = i * 100 / this.FileList.size() + 1;
                setProgress(count);
                
            } catch(IOException e1) {
                e1.printStackTrace();
            }
            
        }
        return null;
        
    }
    
    @Override
    protected void done() {
        jLb.setText("All files were copied successfully.");
        jB.setIndeterminate(false);
        jCheck.setEnabled(true);
        jSource.setEnabled(true);
        
    }
    
}
