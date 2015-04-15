/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.gui.dnd;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

/**
 * This TransferHandler-derived class handles the drag and drop from a JList, JLabel, or 
 * JTextComponent instance to any class that implements the setText method (typically a
 * JLabel or a JTextField instance).
 * @author Mathieu Fortin - September 2012
 */
@SuppressWarnings("serial")
public class ReplaceTextTransferHandler extends TransferHandler {
	
	private Component owner;
	
	public ReplaceTextTransferHandler(Component owner) {
		this.owner = owner;
	}
	
	@Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        return true;
    }

	@Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
        
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            displayDropLocation("The component does not accept a drop of this type.");
            return false;
        }

        Transferable t = info.getTransferable();
        String data;
        try {
            data = (String) t.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) { 
        	return false; 
        }

        Component comp = info.getComponent();
        try {
			Method setTextMethod = comp.getClass().getMethod("setText", String.class);
			setTextMethod.invoke(comp, data);
			return true;
		} catch (NoSuchMethodException e) {
			displayDropLocation("The component does not accept a drop of this type.");
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        return false;
    }
    
	
    private void displayDropLocation(final String string) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(owner, string);
            }
        });
    }


    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }
    
	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
    protected Transferable createTransferable(JComponent c) {
    	if (c instanceof JTextComponent) {
    		return new StringSelection(((JTextComponent) c).getText());
    	} else if (c instanceof JLabel) {
    		return new StringSelection(((JLabel) c).getText());
    	} else if (c instanceof JList) {
    		JList list = (JList) c;
    		Object[] values = list.getSelectedValues();

    		StringBuffer buff = new StringBuffer();

    		for (int i = 0; i < values.length; i++) {
    			Object val = values[i];
    			buff.append(val == null ? "" : val.toString());
    			if (i != values.length - 1) {
    				buff.append("\n");
    			}
    		}

    		return new StringSelection(buff.toString());
    	} else {
    		return null;
    	}
    }
    
}

