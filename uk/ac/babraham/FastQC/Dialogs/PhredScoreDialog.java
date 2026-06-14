/**
 * Copyright Copyright 2010-17 Simon Andrews
 *
 *    This file is part of FastQC.
 *
 *    FastQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    FastQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.FastQC.Dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import uk.ac.babraham.FastQC.FastQCApplication;

public class PhredScoreDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public PhredScoreDialog(FastQCApplication a) {
		super(a);
		setTitle("Phred Score Decoder");

		Container cont = getContentPane();
		cont.setLayout(new BorderLayout());
		cont.add(new PhredScorePanel(), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		JButton closeButton = new JButton("Close");
		getRootPane().setDefaultButton(closeButton);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		buttonPanel.add(closeButton);
		cont.add(buttonPanel, BorderLayout.SOUTH);

		setSize(550, 450);
		setLocationRelativeTo(a);
		setResizable(true);
		setVisible(true);
	}
}
