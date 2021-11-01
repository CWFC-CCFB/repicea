/*
  DBFReader
  Class for reading the records assuming that the given
	InputStream contains DBF data.

  This file is part of JavaDBF package.

  Author: anil@linuxense.com
  License: LGPL (http://www.gnu.org/copyleft/lesser.html)

  $Id: DBFReader.java,v 1.8 2004/03/31 10:54:03 anil Exp $
*/

package repicea.io.javadbf;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.GregorianCalendar;

import repicea.io.FormatReader;

/**
	DBFReader class can creates objects to represent DBF data.

	This Class is used to read data from a DBF file. Meta data and
	records can be queried against this document.

	<p>
	DBFReader cannot write anythng to a DBF file. For creating DBF files 
	use DBFWriter.

	<p>
	Fetching rocord is possible only in the forward direction and 
	cannot re-wound. In such situation, a suggested approach is to reconstruct the object.

	<p>
	The nextRecord() method returns an array of Objects and the types of these
	Object are as follows:

	<table>
	<tr>
		<th>xBase Type</th><th>Java Type</th>
	</tr>

	<tr>
		<td>C</td><td>String</td>
	</tr>
	<tr>
		<td>N</td><td>Integer</td>
	</tr>
	<tr>
		<td>F</td><td>Double</td>
	</tr>
	<tr>
		<td>L</td><td>Boolean</td>
	</tr>
	<tr>
		<td>D</td><td>java.util.Date</td>
	</tr>
	</table>
	
*/
public class DBFReader extends FormatReader<DBFHeader> {

	protected DataInputStream dataInputStream;

	private final int END_OF_DATA = 0x1A;

	private String characterSetName = "8859_1";

	
	/**
		Initializes a DBFReader object.
		When this constructor returns the object 
		will have completed reading the hader (meta date) and 
		header information can be quried there on. And it will 
		be ready to return the first row.
		@param filename the filename where the data is read from.	
	 * @throws IOException 
	 */
	public DBFReader(String filename) throws IOException {
		super(filename);
		try {
			reset();
		} catch (IOException e) {
			close();
			throw new DBFException(e.getMessage());	
		}
	}
	
	public void reset() throws IOException {
		if (dataInputStream != null) {
			close();
		}
		dataInputStream = new DataInputStream(openStream());
		setFormatHeader(new DBFHeader());
		getHeader().read(this.dataInputStream);

		/* it might be required to leap to the start of records at times */
		int t_dataStartIndex = getHeader().headerLength - (32 + (32 * getFieldCount())) - 1;
		if(t_dataStartIndex > 0) {
			dataInputStream.skip( t_dataStartIndex);
		}
		linePointer = 0;
		isClosed = false;
	}

	/* 
	 If the library is used in a non-latin environment use this method to set 
	 corresponding character set. More information: 
	 http://www.iana.org/assignments/character-sets
	 Also see the documentation of the class java.nio.charset.Charset
	*/
	public String getCharactersetName() {return this.characterSetName;}

	public void setCharactersetName(String characterSetName) {this.characterSetName = characterSetName;}

	
	
	public String toString() {

		StringBuffer sb = new StringBuffer(getHeader().year + "/" + getHeader().month + "/" + getHeader().day + "\n"
		+ "Total records: " + getHeader().getNumberOfRecords() + 
		"\nHEader length: " + getHeader().headerLength +
		"");

		try {
			for (int i = 0; i < getFieldCount(); i++) {
				sb.append(getHeader().getField(i).getName());
				sb.append("\n");
			}
		} catch (Exception e) {}

		return sb.toString();
	}


	/**
		Returns the asked Field. In case of an invalid index,
		it returns a ArrayIndexOutofboundsException.

		@param index Index of the field. Index of the first field is zero.
	*/
	@Override
	public DBFField getField(int index) {
		return getHeader().getField(index);
	}

	

	/**
		Reads the returns the next row in the DBF stream.
		@return The next row as an Object array. Types of the elements 
		these arrays follow the convention mentioned in the class description.
	*/
	public Object[] nextRecord(int skipThisNumberOfLines) throws DBFException {
		try {
			int rowToReachBeforeReading = linePointer + skipThisNumberOfLines;
			Object recordObjects[] = new Object[getFieldCount()];
			boolean isDeleted = false;
			boolean wentThroughLoopOnce = false;
//			int lineCddount = 0;
			do {
				if (isDeleted || wentThroughLoopOnce) {
					if (isDeleted)
						dataInputStream.skip(getHeader().recordLength - 1);
					else if (linePointer < rowToReachBeforeReading) {
						dataInputStream.skip(getHeader().recordLength - 1);
						linePointer++;
					}
				}
	
				int t_byte = dataInputStream.readByte();
				if( t_byte == END_OF_DATA) {
					return null;
				}

				wentThroughLoopOnce = true;						// make sure the process went through this loop before checking 
				isDeleted = (t_byte == '*');
			} while (isDeleted || linePointer < rowToReachBeforeReading);
	
			for (int i = 0; i < getFieldCount(); i++) {
				switch (getHeader().getField(i).getDataType()) {
				case 'C':
					byte b_array[] = new byte[getHeader().getField(i).getFieldLength()];
					dataInputStream.read( b_array);
					recordObjects[i] = new String( b_array, characterSetName);
					break;
				case 'D':
					byte t_byte_year[] = new byte[ 4];
					dataInputStream.read( t_byte_year);
					byte t_byte_month[] = new byte[ 2];
					dataInputStream.read( t_byte_month);
					byte t_byte_day[] = new byte[ 2];
					dataInputStream.read( t_byte_day);
					try {
						GregorianCalendar calendar = new GregorianCalendar( 
								Integer.parseInt(new String(t_byte_year)),
								Integer.parseInt(new String(t_byte_month)) - 1,
								Integer.parseInt(new String(t_byte_day))
								);
						recordObjects[i] = calendar.getTime();
					} catch (NumberFormatException e) {
						/* this field may be empty or may have improper value set */
						recordObjects[i] = null;
					}

					break;
	
				case 'F':
					try {

						byte t_float[] = new byte[getHeader().getField(i).getFieldLength()];
						dataInputStream.read( t_float);
						t_float = Utils.trimLeftSpaces( t_float);
						if (t_float.length > 0 && !Utils.contains( t_float, (byte)'?')) {
							recordObjects[i] = new Float( new String( t_float));
						} else {

							recordObjects[i] = null;
						}
					} catch (NumberFormatException e) {
						throw new DBFException( "Failed to parse Float: " + e.getMessage());
					}
					break;
	
				case 'N':
					try {
						byte t_numeric[] = new byte[getHeader().getField(i).getFieldLength()];
						dataInputStream.read( t_numeric);
						t_numeric = Utils.trimLeftSpaces( t_numeric);

						if( t_numeric.length > 0 && !Utils.contains( t_numeric, (byte)'?')) {
							recordObjects[i] = new Double( new String( t_numeric));
						} else {
							recordObjects[i] = null;
						}
					} catch (NumberFormatException e) {
						throw new DBFException( "Failed to parse Number: " + e.getMessage());
					}

					break;

				case 'L':
					byte t_logical = dataInputStream.readByte();
					if (t_logical == 'Y' || t_logical == 't' || t_logical == 'T' || t_logical == 't') {

						recordObjects[i] = Boolean.TRUE;
					} else {

						recordObjects[i] = Boolean.FALSE;
					}
					break;

				case 'M':
					// TODO Later
					recordObjects[i] = new String( "null");
					break;

				default:
					recordObjects[i] = new String( "null");
				}
			}
			return recordObjects;
		} catch (EOFException e) {
			close();
			return null;
		} catch( IOException e) {
			close();
			throw new DBFException( e.getMessage());
		}

	}
	
	
	@Override
	public void closeInternalStream() {
		try {
			dataInputStream.close(); 
		} catch (IOException e) {}
	}

}
