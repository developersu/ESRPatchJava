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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.*;

public class Main extends JFrame implements ActionListener, DropTargetListener {
	private JLabel lblUsage, lblInfo1, lblInfo2;
	private JButton btnPatch, btnUnpatch, btnQuit;
	private JPanel pnlButtons;

	public Main() {
		super("ESRPatchJava v0.2.3");

		Container container = getContentPane();
		container.setLayout(new GridLayout(5, 1));

		btnPatch = new JButton("Patch ISO...");
		btnPatch.setMnemonic('P');
		btnPatch.addActionListener(this);

		btnUnpatch = new JButton("Unpatch ISO...");
		btnUnpatch.setMnemonic('U');
		btnUnpatch.addActionListener(this);
		
		pnlButtons = new JPanel(new GridLayout(1, 2));
		pnlButtons.add(btnPatch);
		pnlButtons.add(btnUnpatch);

		lblUsage  = new JLabel("Select button below or drag and drop files here:", JLabel.LEFT);
		lblInfo1 = new JLabel("(C)06/2008 - bootsector - http://www.brunofreitas.com/", JLabel.CENTER);
		lblInfo2 = new JLabel("ESR Project by ffgriever", JLabel.CENTER);

		btnQuit = new JButton("Close");
		btnQuit.setMnemonic('C');
		btnQuit.addActionListener(this);

		container.add(lblUsage);
		container.add(pnlButtons);
		container.add(lblInfo1);
		container.add(lblInfo2);
		container.add(btnQuit);

		setSize(450, 150);
		setResizable(false);
		centerFrameOnScreen(this);
		new DropTarget(getContentPane(), this);
		setVisible(true);
	}

	public static void main(String args[]) {
		Main app = new Main();

		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void centerFrameOnScreen(JFrame frame) {
		Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
		Dimension f=frame.getSize();

		frame.setBounds(Math.max(0, (d.width - f.width) / 2),
				Math.max(0, (d.height - f.height) / 2),
				f.width, f.height);
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new ISOFileFilter());

		if (e.getSource() == btnQuit)
			System.exit(0);

		if (e.getSource() == btnPatch) {
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				switch(ESRPatch.apply(fc.getSelectedFile().getAbsolutePath())) {
					case ESRPatch.ALREADY_PATCHED: JOptionPane.showMessageDialog(this, "ISO is already patched!", "Attention", JOptionPane.INFORMATION_MESSAGE); break;
					case ESRPatch.ERROR_PATCHING: JOptionPane.showMessageDialog(this, "Error trying to patch ISO!", "Error", JOptionPane.ERROR_MESSAGE); break; 
					case ESRPatch.PATCH_OK: JOptionPane.showMessageDialog(this, "ISO patched successfully! :)", "Ok!", JOptionPane.INFORMATION_MESSAGE); break;
					case ESRPatch.NOT_UDF_ISO: JOptionPane.showMessageDialog(this, "ISO doesn't contain UDF descriptor!", "Error", JOptionPane.ERROR_MESSAGE); break;
				}
			}
		}

		if (e.getSource() == btnUnpatch) {
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				switch(ESRPatch.unPatch(fc.getSelectedFile().getAbsolutePath())) {
					case ESRPatch.NOT_PATCHED: JOptionPane.showMessageDialog(this, "ISO is not patched!", "Attention", JOptionPane.INFORMATION_MESSAGE); break;
					case ESRPatch.ERROR_PATCHING: JOptionPane.showMessageDialog(this, "Error trying to patch ISO!", "Error", JOptionPane.ERROR_MESSAGE); break; 
					case ESRPatch.PATCH_OK: JOptionPane.showMessageDialog(this, "ISO unpatched successfully! :)", "Ok!", JOptionPane.INFORMATION_MESSAGE); break;
					case ESRPatch.NOT_UDF_ISO: JOptionPane.showMessageDialog(this, "ISO doesn't contain UDF descriptor!", "Error", JOptionPane.ERROR_MESSAGE); break;
				}
			}
		}

	}

	public void dragEnter(DropTargetDragEvent dtde) {
		// System.out.println("Drag Enter");
	}

	public void dragExit(DropTargetEvent dte) {
		// System.out.println("Drag Exit");
	}

	public void dragOver(DropTargetDragEvent dtde) {
		// System.out.println("Drag Over");
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
		// System.out.println("Drop Action Changed");
	}

	public void drop(DropTargetDropEvent dtde) {
		try {
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();

			for (int i = 0; i < flavors.length; i++) {

				if (flavors[i].isFlavorJavaFileListType()) {
					//dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					java.util.List list = (java.util.List) tr.getTransferData(flavors[i]);
					ArrayList<Object> filesList = new ArrayList<Object>();


					for (int j = 0; j < list.size(); j++) {
						filesList.add(list.get(j));
					}

					//Custom button text
					Object[] options = {"Patch",
					                    "Unpatch"};
					int opt = JOptionPane.showOptionDialog(this,
					    "What would you like to do?",
					    "What to do?",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[0]);

					if(opt == JOptionPane.YES_OPTION)
						ESRPatch.apply(filesList, this);
					else
						ESRPatch.unPatch(filesList, this);

					dtde.dropComplete(true);
					return;
				}

				//System.out.println("Drop failed: " + dtde);
				dtde.rejectDrop();
			} 
		}catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}

}
