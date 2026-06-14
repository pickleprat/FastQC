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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class PhredScorePanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final String[] ENCODINGS = {
		"Illumina 1.8+ (Base 33)",
		"Sanger (Base 33)",
		"Ion Torrent (Base 33)",
		"PacBio (Base 33)",
		"Old Illumina 1.3-1.7 (Base 64)"
	};

	private JTextField inputField;
	private JComboBox<String> encodingBox;
	private DefaultTableModel tableModel;
	private JTable table;

	public PhredScorePanel() {
		setLayout(new BorderLayout(5, 5));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// --- top: encoding selector + input row ---
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));

		JPanel encodingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JLabel encodingLabel = new JLabel("Encoding: ");
		encodingLabel.setFont(new Font("Dialog", Font.BOLD, 13));
		encodingBox = new JComboBox<String>(ENCODINGS);
		encodingBox.setFont(new Font("Dialog", Font.PLAIN, 13));
		encodingPanel.add(encodingLabel);
		encodingPanel.add(encodingBox);
		topPanel.add(encodingPanel, BorderLayout.NORTH);

		JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
		JLabel prompt = new JLabel("Quality string: ");
		prompt.setFont(new Font("Dialog", Font.BOLD, 13));
		inputPanel.add(prompt, BorderLayout.WEST);

		inputField = new JTextField();
		inputField.setFont(new Font("Monospaced", Font.PLAIN, 13));
		inputField.addActionListener(this);
		inputPanel.add(inputField, BorderLayout.CENTER);

		JButton decodeButton = new JButton("Decode");
		decodeButton.addActionListener(this);
		inputPanel.add(decodeButton, BorderLayout.EAST);

		topPanel.add(inputPanel, BorderLayout.SOUTH);
		add(topPanel, BorderLayout.NORTH);

		// --- centre: results table ---
		String[] columns = {"Position", "Character", "ASCII Value", "Q Score", "P_error"};
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int col) { return false; }
		};

		table = new JTable(tableModel);
		table.setFont(new Font("Monospaced", Font.PLAIN, 13));
		table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 13));
		table.setRowHeight(22);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
		table.setDefaultRenderer(Object.class, new PhredCellRenderer());

		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	private int getBase() {
		String selected = (String) encodingBox.getSelectedItem();
		return selected.contains("64") ? 64 : 33;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String text = inputField.getText().trim();
		tableModel.setRowCount(0);
		int base = getBase();

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			int ascii = (int) c;
			int q = ascii - base;
			double pError = Math.pow(10.0, -q / 10.0);
			String pErrorStr = String.format("%.5f", pError);
			tableModel.addRow(new Object[]{i + 1, String.valueOf(c), ascii, q, pErrorStr});
		}
	}

	/** Colours rows by Q score: red < 20, amber 20-27, green >= 28. */
	private static class PhredCellRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public PhredCellRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {

			setText(value != null ? value.toString() : "");
			setHorizontalAlignment(column == 1 ? CENTER : LEFT);

			int q = (Integer) table.getModel().getValueAt(row, 3);

			Color bg;
			if (q < 20)       bg = new Color(255, 200, 200); // red
			else if (q < 28)  bg = new Color(255, 240, 180); // amber
			else              bg = new Color(200, 240, 200); // green

			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(bg);
				setForeground(Color.BLACK);
			}

			return this;
		}
	}
}
