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

import java.io.*;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

public class ESRPatch {
	public static final int LBA_SIZE = 2048;

	public static final int PATCH_OK = 0;
	public static final int ALREADY_PATCHED = 1;
	public static final int ERROR_PATCHING = 2;
	public static final int NOT_UDF_ISO = 3;
	public static final int NOT_PATCHED = 4;

	private static RandomAccessFile iso;
	private static final byte[] buffer = new byte[LBA_SIZE];

	public static int apply(String fileName) {
		byte[] b;
		int desc_crc, desc_crc_len, tag_checksum;

		try {
			iso = new RandomAccessFile(fileName, "rw");
			
			// Checks if image is a UDF ISO
			if(!isUDFISO()) {
				iso.close();
				return NOT_UDF_ISO;
			}

			// Checks if image is already patched
			if(isAlreadyPatched()) {
				iso.close();
				return ALREADY_PATCHED;
			}

			// Backups LBA 34 into 14
			iso.seek(34 * LBA_SIZE);
			iso.read(buffer);

			iso.seek(14 * LBA_SIZE);
			iso.write(buffer);

			// Backups LBA 50 into 15
			iso.seek(50 * LBA_SIZE);
			iso.read(buffer);

			iso.seek(15 * LBA_SIZE);
			iso.write(buffer);

			// Updates LBA 34
			iso.seek(34 * LBA_SIZE);
			iso.read(buffer);

			// Updates new referenced LBA: Now it points to a DVD_VIDEO structure
			buffer[0xBC] = (byte) 0x80;
			buffer[0xBD] = (byte) 0x00;

			// Calculates new checksums
			desc_crc_len = (((int)buffer[11] & 0xFF) << 8 & 0xFF00)+ ((int) buffer[10] & 0xFF);
			b = new byte[desc_crc_len];

			System.arraycopy(buffer, 16, b, 0, desc_crc_len);

			desc_crc = CRC.CRC16CCITT(b);

			buffer[8] = (byte) (desc_crc & 0xFF);
			buffer[9] = (byte) ((desc_crc >> 8) & 0xFF);

			tag_checksum = 0;

			for(int i = 0; i < 16; i++) {
				if(i != 4) {
					tag_checksum += buffer[i];
				}
			}

			buffer[4] = (byte) tag_checksum;

			// Save modified LBA
			iso.seek(34 * LBA_SIZE);
			iso.write(buffer);

			// Updates LBA 50
			iso.seek(50 * LBA_SIZE);
			iso.read(buffer);

			// Updates new referenced LBA: Now it points to a DVD_VIDEO structure
			buffer[0xBC] = (byte) 0x80;
			buffer[0xBD] = (byte) 0x00;

			// Calculates new checksums
			desc_crc_len = (((int)buffer[11] & 0xFF) << 8 & 0xFF00)+ ((int) buffer[10] & 0xFF);
			b = new byte[desc_crc_len];

			System.arraycopy(buffer, 16, b, 0, desc_crc_len);

			desc_crc = CRC.CRC16CCITT(b);

			buffer[8] = (byte) (desc_crc & 0xFF);
			buffer[9] = (byte) ((desc_crc >> 8) & 0xFF);

			tag_checksum = 0;

			for(int i = 0; i < 16; i++) {
				if(i != 4) {
					tag_checksum += buffer[i];
				}
			}

			buffer[4] = (byte) tag_checksum;

			// Saves modified LBA
			iso.seek(50 * LBA_SIZE);
			iso.write(buffer);

			// Writes esrpatchjava.DVDV data
			DVDV.write(iso);

			return PATCH_OK;

		} catch(Exception ex) {
			ex.printStackTrace();
			return ERROR_PATCHING;

		} finally {
			try {
				iso.close();
			}
			catch(Exception ignore) {}
		}
	}

	private static boolean isAlreadyPatched() {
		try {
			iso.seek(14 * LBA_SIZE);
			iso.read(buffer);

			// Checks for "+NSR" magic string
			if(buffer[25] == (byte) 0x2B && buffer[26] == (byte) 0x4E && buffer[27] == (byte) 0x53 && buffer[28] == (byte) 0x52) {
				return true;
			}
		} catch(Exception ex) {
			return false;
		}

		return false;
	}

	private static boolean isUDFISO() {
		try {
			for(int i = 1; i < 64; i++) {
				iso.seek(LBA_SIZE * i + 32768);
				iso.read(buffer);

				// "NSR"
				if (buffer[1] == (byte) 0x4E && buffer[2] == (byte) 0x53 && buffer[3] == (byte) 0x52)
					return true;
			}
		}
		catch (Exception ex) {
			return false;
		}
		return false;
	}
	
	public static int apply(List files, JFrame parent) {
		StringBuilder results = new StringBuilder();

		for (Object file : files) {
			File f = new File(file.toString());

			if (f.isFile() && f.exists()) {
				results.append(f.getName());

				switch (apply(f.getPath())) {
					case ESRPatch.ALREADY_PATCHED:
						results.append(" - ISO is already patched!\n");
						break;
					case ESRPatch.ERROR_PATCHING:
						results.append(" - Error trying to patch ISO!\n");
						break;
					case ESRPatch.PATCH_OK:
						results.append(" - ISO patched successfully! :)\n");
						break;
					case ESRPatch.NOT_UDF_ISO:
						results.append(" - Error: ISO doesn't contain UDF descriptor!\n");
						break;
				}
			}
		}
		
		JOptionPane.showMessageDialog(parent, results.toString(), "Results:", JOptionPane.INFORMATION_MESSAGE);

		return 0;
	}
	
	public static int unPatch(String fileName) {

		try {
			iso = new RandomAccessFile(fileName, "rw");
			
			// Checks if image is a UDF ISO
			if(!isUDFISO()) {
				iso.close();
				return NOT_UDF_ISO;
			}

			// Checks if image is not patched
			if(!isAlreadyPatched()) {
				iso.close();
				return NOT_PATCHED;
			}

			// Restore LBA 14 into 34
			iso.seek(14 * LBA_SIZE);
			iso.read(buffer);

			iso.seek(34 * LBA_SIZE);
			iso.write(buffer);

			// Restore LBA 15 into 50
			iso.seek(15 * LBA_SIZE);
			iso.read(buffer);

			iso.seek(50 * LBA_SIZE);
			iso.write(buffer);
			
			// Clear backups
			Arrays.fill(buffer, (byte) 0);
			
			iso.seek(14 * LBA_SIZE);
			iso.write(buffer);

			iso.seek(15 * LBA_SIZE);
			iso.write(buffer);

			return PATCH_OK;

		} catch(Exception ex) {
			ex.printStackTrace();
			return ERROR_PATCHING;

		}
		finally {
			try {
				iso.close();
			}
			catch(Exception ignore) {}
		}

	}
	
	public static int unPatch(List files, JFrame parent) {
		StringBuilder results = new StringBuilder();

		for (Object file : files) {

			File f = new File(file.toString());

			if (f.isFile() && f.exists()) {
				results.append(f.getName());

				switch (unPatch(f.getPath())) {
					case ESRPatch.NOT_PATCHED:
						results.append(" - ISO is not patched!\n");
						break;
					case ESRPatch.ERROR_PATCHING:
						results.append(" - Error trying to unpatch ISO!\n");
						break;
					case ESRPatch.PATCH_OK:
						results.append(" - ISO unpatched successfully! :)\n");
						break;
					case ESRPatch.NOT_UDF_ISO:
						results.append(" - Error: ISO doesn't contain UDF descriptor!\n");
						break;
				}
			}
		}
		
		JOptionPane.showMessageDialog(parent, results.toString(), "Results:", JOptionPane.INFORMATION_MESSAGE);

		return 0;
	}
}
