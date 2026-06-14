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

	// Each entry: { display name, base offset, min ASCII, max ASCII }
	private static final Object[][] ENCODING_DEFS = {
		{ "Illumina 1.8+ (Base 33)",      33, 33, 74  },  // Q0-Q41  '!' to 'J'
		{ "Sanger (Base 33)",             33, 33, 73  },  // Q0-Q40  '!' to 'I'
		{ "Ion Torrent (Base 33)",        33, 33, 73  },  // Q0-Q40  '!' to 'I'
		{ "PacBio (Base 33)",             33, 33, 126 },  // Q0-Q93  '!' to '~'
		{ "Old Illumina 1.3-1.7 (Base 64)", 64, 64, 104 } // Q0-Q40  '@' to 'h'
	};

	private static final String[] ENCODING_NAMES;
	static {
		ENCODING_NAMES = new String[ENCODING_DEFS.length];
		for (int i = 0; i < ENCODING_DEFS.length; i++) {
			ENCODING_NAMES[i] = (String) ENCODING_DEFS[i][0];
		}
	}

	private JTextField inputField;
	private JComboBox<String> encodingBox;
	private DefaultTableModel tableModel;
	private JTable table;

	public PhredScorePanel() {
		setLayout(new BorderLayout(5, 5));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel topPanel = new JPanel(new BorderLayout(5, 5));

		JPanel encodingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JLabel encodingLabel = new JLabel("Encoding: ");
		encodingLabel.setFont(new Font("Dialog", Font.BOLD, 13));
		encodingBox = new JComboBox<String>(ENCODING_NAMES);
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

		String[] columns = {"Position", "Character", "ASCII Value", "Q Score", "P_error"};
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int col) { return false; }

			@Override
			public Class<?> getColumnClass(int col) {
				// keep Q Score as Object so we can store "Invalid" strings
				return Object.class;
			}
		};

		table = new JTable(tableModel);
		table.setFont(new Font("Monospaced", Font.PLAIN, 13));
		table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 13));
		table.setRowHeight(22);
		table.getColumnModel().getColumn(4).setPreferredWidth(110);
		table.setDefaultRenderer(Object.class, new PhredCellRenderer());

		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	private Object[] getEncodingDef() {
		return ENCODING_DEFS[encodingBox.getSelectedIndex()];
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String text = inputField.getText().trim();
		tableModel.setRowCount(0);

		Object[] def = getEncodingDef();
		int base   = (Integer) def[1];
		int minAsc = (Integer) def[2];
		int maxAsc = (Integer) def[3];

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			int ascii = (int) c;

			if (ascii < minAsc || ascii > maxAsc) {
				// Character is outside the valid range for this encoding
				tableModel.addRow(new Object[]{i + 1, String.valueOf(c), ascii, "Invalid", "—"});
			} else {
				int q = ascii - base;
				double pError = Math.pow(10.0, -q / 10.0);
				tableModel.addRow(new Object[]{i + 1, String.valueOf(c), ascii, q, String.format("%.5f", pError)});
			}
		}
	}

	/** Colours rows: grey = invalid, red Q<20, amber Q 20-27, green Q>=28. */
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

			Object qVal = table.getModel().getValueAt(row, 3);
			boolean invalid = !(qVal instanceof Integer);

			Color bg;
			if (invalid) {
				bg = new Color(220, 220, 220); // grey
			} else {
				int q = (Integer) qVal;
				if (q < 20)      bg = new Color(255, 200, 200); // red
				else if (q < 28) bg = new Color(255, 240, 180); // amber
				else             bg = new Color(200, 240, 200); // green
			}

			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(bg);
				setForeground(invalid ? new Color(120, 120, 120) : Color.BLACK);
			}

			return this;
		}
	}
}
