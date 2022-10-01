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
package esrpatchjava.ui;

import esrpatchjava.ESRPatch;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

public class MyDropTargetListener implements DropTargetListener {

    @Override
    public void drop(DropTargetDropEvent event) {
        try{
            JFrame source = (JFrame) event.getDropTargetContext().getComponent();

            event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

            Transferable transferable = event.getTransferable();
            List<File> filesDropped = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

            event.dropComplete(true);

            Object[] options = {"Patch",
                    "Unpatch"};
            int opt = JOptionPane.showOptionDialog(source,
                    "What would you like to do?",
                    "What to do?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (opt == JOptionPane.YES_OPTION)
                ESRPatch.apply(filesDropped, source);
            else
                ESRPatch.unPatch(filesDropped, source);
        }
        catch (Exception e){
            e.printStackTrace();
            event.rejectDrop();
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {}
    @Override
    public void dragOver(DropTargetDragEvent dropTargetDragEvent) {}
    @Override
    public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {}
    @Override
    public void dragExit(DropTargetEvent dropTargetEvent) {}
}
