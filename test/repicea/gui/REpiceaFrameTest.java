/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2022 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui;

import java.awt.event.ActionEvent;

import javax.swing.JButton;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;

public class REpiceaFrameTest {

	
	public static class FakeFrame extends REpiceaFrame {

		FakeFrame() {
			this.askUserBeforeExit = true;
			JButton b = new JButton("Hello world!");
			getContentPane().add(b);
			pack();
			setVisible(true);
		}
		
		
		@Override
		public void listenTo() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void doNotListenToAnymore() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static void main(String[] args) {
		REpiceaTranslator.setCurrentLanguage(Language.French);
		new FakeFrame();
	}
	
	
}
