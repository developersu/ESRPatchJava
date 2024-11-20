/*
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package esrpatchjava;

import esrpatchjava.ui.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.dnd.*;
import java.util.*;

public class Main extends JFrame implements ActionListener {
	private final JButton btnPatch;
	private final JButton btnUnpatch;
	private final JButton btnQuit;

	public static void main(String[] args) {
		Main app = new Main();

		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public Main() {
		super("ESRPatchJava "+ResourceBundle.getBundle("app").getString("_version"));

		Container container = getContentPane();
		container.setLayout(new GridLayout(5, 1));

		btnPatch = new JButton("Patch ISO...");
		btnPatch.setMnemonic('P');
		btnPatch.addActionListener(this);
		btnPatch.setBackground(Color.getHSBColor(0.5f, 0.12156863f, 1));

		btnUnpatch = new JButton("Unpatch ISO...");
		btnUnpatch.setMnemonic('U');
		btnUnpatch.addActionListener(this);
		btnUnpatch.setBackground(Color.getHSBColor(0.5f, 0.12156863f, 1));

		GridLayout pnlButtonsLayout = new GridLayout(1, 2);
		pnlButtonsLayout.setHgap(5);
		JPanel pnlButtons = new JPanel(pnlButtonsLayout);
		pnlButtons.add(btnPatch);
		pnlButtons.add(btnUnpatch);

		JLabel lblUsage = new JLabel("Select button below or drag and drop files here:", JLabel.LEFT);
		JLabel lblInfo1 = new JLabel("(C)06/2008 - bootsector - http://www.brunofreitas.com/", JLabel.CENTER);
		JLabel lblInfo2 = new JLabel("ESR Project by ffgriever", JLabel.CENTER);

		btnQuit = new JButton("Close");
		btnQuit.setMnemonic('C');
		btnQuit.addActionListener(this);
		btnQuit.setBackground(Color.getHSBColor(0.90f, 0.12156863f, 1));

		container.add(lblUsage);
		container.add(pnlButtons);
		container.add(lblInfo1);
		container.add(lblInfo2);
		container.add(btnQuit);
		((JPanel) container).setBorder(new EmptyBorder(5, 5, 5, 5));
		setSize(450, 250);
		setResizable(false);
		setLocationRelativeTo(null);
		setDropTarget(new DropTarget(getContentPane(), new MyDropTargetListener()));
		setVisible(true);
	}

	public void actionPerformed(ActionEvent event){
		if (event.getSource() == btnQuit)
			System.exit(0);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new ISOFileFilter());

		if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;

		String fileLocation = fileChooser.getSelectedFile().getAbsolutePath();

		if (event.getSource() == btnPatch) {
			doPatch(fileLocation);
			return;
		}
		if (event.getSource() == btnUnpatch)
			doUnPatch(fileLocation);
	}
	private void doPatch(String fileLocation){
		switch(ESRPatch.apply(fileLocation)) {
			case ESRPatch.ALREADY_PATCHED:
				JOptionPane.showMessageDialog(this, "ISO is already patched!", "Attention", JOptionPane.INFORMATION_MESSAGE);
				break;
			case ESRPatch.ERROR_PATCHING:
				JOptionPane.showMessageDialog(this, "Error trying to patch ISO!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
			case ESRPatch.PATCH_OK:
				JOptionPane.showMessageDialog(this, "ISO patched successfully! :)", "Ok!", JOptionPane.INFORMATION_MESSAGE);
				break;
			case ESRPatch.NOT_UDF_ISO:
				JOptionPane.showMessageDialog(this, "ISO doesn't contain UDF descriptor!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	private void doUnPatch(String fileLocation){
		switch(ESRPatch.unPatch(fileLocation)) {
			case ESRPatch.NOT_PATCHED:
				JOptionPane.showMessageDialog(this, "ISO is not patched!", "Attention", JOptionPane.INFORMATION_MESSAGE);
				break;
			case ESRPatch.ERROR_PATCHING:
				JOptionPane.showMessageDialog(this, "Error trying to patch ISO!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
			case ESRPatch.PATCH_OK:
				JOptionPane.showMessageDialog(this, "ISO unpatched successfully! :)", "Ok!", JOptionPane.INFORMATION_MESSAGE);
				break;
			case ESRPatch.NOT_UDF_ISO:
				JOptionPane.showMessageDialog(this, "ISO doesn't contain UDF descriptor!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
