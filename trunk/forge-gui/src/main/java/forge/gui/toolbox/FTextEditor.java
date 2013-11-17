package forge.gui.toolbox;

import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class FTextEditor extends JScrollPane {
	private final JTextArea tarEditor;
	private final FUndoManager undoManager;
	
	public FTextEditor() {
		tarEditor = new JTextArea();
        FSkin.JTextComponentSkin<JTextArea> skin = FSkin.get(tarEditor);
        skin.setFont(FSkin.getFixedFont(16));
        skin.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        skin.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        skin.setCaretColor(FSkin.getColor(FSkin.Colors.CLR_TEXT));

        undoManager = new FUndoManager(tarEditor);
 
        tarEditor.setMargin(new Insets(3, 3, 3, 3));
        tarEditor.addKeyListener(new KeyAdapter() {
        	@Override
            public void keyPressed(KeyEvent e) {
        		if (e.isControlDown() && !e.isMetaDown()) {
        			switch (e.getKeyCode()) {
        			case KeyEvent.VK_Z:
        				if (e.isShiftDown()) {
        					undoManager.redo();
        				}
        				else {
        					undoManager.undo();
        				}
        				break;
        			case KeyEvent.VK_Y:
        				if (!e.isShiftDown()) {
        					undoManager.redo();
        				}
        				break;
        			}
        		}
            }
        });

        this.setViewportView(tarEditor);
        this.setBorder(null);
        this.setOpaque(false);
	}
	
	//Mapped functions to JTextArea
	@Override
	public boolean isEnabled() {
		return tarEditor.isEnabled();
	}
	@Override
	public void setEnabled(boolean enabled) {
		tarEditor.setEnabled(enabled);
	}
	public String getText() {
		return tarEditor.getText();
	}
	public void setText(String t) {
		tarEditor.setText(t);
		undoManager.discardAllEdits();
	}
	public boolean isEditable() {
		return tarEditor.isEditable();
	}
	public void setEditable(boolean b) {
		tarEditor.setEditable(b);
	}
	public int getCaretPosition() {
		return tarEditor.getCaretPosition();
	}
	public void setCaretPosition(int position) {
		tarEditor.setCaretPosition(position);
	}
	public void addDocumentListener(DocumentListener listener) {
		tarEditor.getDocument().addDocumentListener(listener);
	}
}
