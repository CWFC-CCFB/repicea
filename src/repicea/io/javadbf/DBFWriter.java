/*
	DBFWriter
	Class for defining a DBF structure and addin data to that structure and 
	finally writing it to an OutputStream.

	This file is part of JavaDBF packege.

	author: anil@linuxense.com
	license: LGPL (http://www.gnu.org/copyleft/lesser.html)

	$Id: DBFWriter.java,v 1.9 2004/03/31 10:57:16 anil Exp $
*/
package repicea.io.javadbf;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import repicea.io.FormatField;
import repicea.io.FormatWriter;
import repicea.io.GExportFieldDetails;
import repicea.io.GFileFilter;
import repicea.io.GFileFilter.FileType;

/**
	An object of this class can create a DBF file.

	Create an object, <br>
	then define fields by creating DBFField objects and<br>
	add them to the DBFWriter object<br>
	add records using the addRecord() method and then<br>
	call write() method.
*/
public class DBFWriter extends FormatWriter<DBFHeader> {

	/* other class variables */
	private List<Object> v_records = new ArrayList<Object>();
	private int recordCount = 0;
	private RandomAccessFile raf = null; /* Open and append records to an existing DBF */

	private final int END_OF_DATA = 0x1A;

	private String characterSetName = "8859_1";

	
	
	/**
		Creates an empty Object.
	*/
	@Deprecated
	public DBFWriter() {
		setFormatHeader(new DBFHeader());
	}

	/**
	 *	Creates a DBFWriter which can append to records to an existing DBF file.
	 *	@param dbfFile The file passed in should be a valid DBF file.
	 *	@throws DBFException if the passed in file does exist but not a valid DBF file, or if an IO error occurs.
	 *	@deprecated The append option is automatically determined in this writer depending on whether the file exists. This 
	 * feature is unsafe. The constructor with the append parameter is preferable to this one.
	 */
	@Deprecated
	public DBFWriter(File dbfFile) throws IOException {
		super(dbfFile, false);
		try {
			this.raf = new RandomAccessFile(dbfFile, "rw");

			/* before proceeding check whether the passed in File object 
		 is an empty/non-existent file or not.
			 */

			if (!dbfFile.exists() || dbfFile.length() == 0) {
				setFormatHeader(new DBFHeader());
				appendFile = false;
				return;
			}

			setFormatHeader(new DBFHeader());
			getHeader().read(raf);
			/* position file pointer at the end of the raf */
			this.raf.seek(this.raf.length() - 1 /* to ignore the END_OF_DATA byte at EoF */);
			appendFile = true;
		} catch( FileNotFoundException e) {
			throw new DBFException( "Specified file is not found. " + e.getMessage());
		} catch( IOException e) {
			throw new DBFException( e.getMessage() + " while reading header");
		}
		this.recordCount = getHeader().getNumberOfRecords();
	}

	
	
	
	/**
	 	Creates a DBFWriter which can append to records to an existing DBF file.
		@param dbfFile The file passed in should be a valid DBF file.
		@param append true if the file is to be appended
		@throws IOException if the passed in file does exist but not a valid DBF file, or if an IO error occurs.
	 */
	public DBFWriter(File dbfFile, boolean append) throws IOException {
		super(dbfFile, append);
		if (GFileFilter.getFileType(getFilename()) != FileType.DBF) {
			throw new IOException("DBFWriter.c. The file is not a .dbf file");
		}

		setFormatHeader(new DBFHeader());

		File outputFile = new File(getFilename());
		if (outputFile.exists() && appendFile) {
			raf = new RandomAccessFile(outputFile, "rw");
			getHeader().read(raf);
			raf.seek(raf.length() - 1);		/* to ignore the END_OF_DATA byte at EoF */
		} else {
			if (outputFile.exists() && !outputFile.delete()) {
				throw new IOException("Java has been unable to delete file : " + outputFile.getAbsolutePath());
			}
			outputFile.createNewFile();
			raf = new RandomAccessFile(outputFile, "rw");
		}

		
/*		
		try {
			this.raf = new RandomAccessFile(dbfFile, "rw");

			if (!dbfFile.exists() || dbfFile.length() == 0 || !appendFile) {
				setFormatHeader(new DBFHeader());
				return;
			}
			
			setFormatHeader(new DBFHeader());
			getHeader().read(raf);

			 position file pointer at the end of the raf 
			this.raf.seek(this.raf.length() - 1  to ignore the END_OF_DATA byte at EoF );
			
			
			
			
			
		} catch( FileNotFoundException e) {
			throw new DBFException( "Specified file is not found. " + e.getMessage());
		} catch( IOException e) {
			throw new DBFException( e.getMessage() + " while reading header");
		}
*/		this.recordCount = getHeader().getNumberOfRecords();
	}

	
	/* 
	 If the library is used in a non-latin environment use this method to set 
	 corresponding character set. More information: 
	 http://www.iana.org/assignments/character-sets
	 Also see the documentation of the class java.nio.charset.Charset
	*/
	public String getCharactersetName() {return this.characterSetName;}

	public void setCharactersetName( String characterSetName) {this.characterSetName = characterSetName;}

	
	
	/**
	 * Sets fields. 
	 * @param fields an array of DBFField instances
	 * @throws IOException if an I/O error has occurred
	 * @deprecated Use the setFields(Vector&#60;FormatField&#62; fields) method instead
	 */
	@Deprecated
	public void setFields(DBFField[] fields) throws IOException {
		List<FormatField> fieldArray = new ArrayList<FormatField>();
		for (int i=0; i<fields.length; i++) {
			if (fields[i] == null) {
				throw new DBFException( "Field " + (i+1) + " is null");
			}
			fieldArray.add(fields[i]);
		}
		
		setFields(fieldArray);
		
//		getHeader().setFieldArray(fieldArray);

	}
	
	@Override
	public void setFields(List<FormatField> fields) throws IOException {
		for (FormatField field : fields) {
			if (!(field instanceof DBFField)) {
				throw new IOException("DBFWriter.setFields(). Some fields are not DBFField instances!");
			}
		}
		super.setFields(fields);
		
		try {
//			if (this.raf != null && this.raf.length() == 0) {
			if (this.raf != null && this.raf.length() == 0 || !appendFile) {		// new file or appendFile is set to false
				getHeader().write( this.raf);
			}
		} catch( IOException e) {
			throw new IOException("DBFWriter.setFields(). Error while accessing file!");
		}
	}

	/**
	 * Add a record.
	 */
	public void addRecord(Object[] values) throws DBFException {
		try {
			validateRecord(values);
		} catch (IOException e) {
			throw new DBFException(e.getMessage());
		}
//		if (getHeader().getFieldArray() == null || getHeader().getFieldArray().isEmpty()) {
//			throw new DBFException( "Fields should be set before adding records");
//		}
//
//		if (values == null) {
//			throw new DBFException( "Null cannot be added as row");
//		}
//
//		if (values.length != getHeader().getFieldArray().size()) {
//			throw new DBFException( "Invalid record. Invalid number of fields in row");
//		}

		for (int i = 0; i < getHeader().getNumberOfFields(); i++) {

			if (values[i] == null) {
				continue;
			}

			switch (getHeader().getField(i).getDataType()) {

				case 'C':
					if( !(values[i] instanceof String)) {
						throw new DBFException( "Invalid value for field " + i);
					}
					break;

				case 'L':
					if( !( values[i] instanceof Boolean)) {
					  throw new DBFException( "Invalid value for field " + i);
					}
					break;

				case 'N':
					if( !( values[i] instanceof Double)) {
						throw new DBFException( "Invalid value for field " + i);
					}
					break;

				case 'D':
					if( !( values[i] instanceof Date)) {
						throw new DBFException( "Invalid value for field " + i);
					}
					break;

				case 'F':
					if( !(values[i] instanceof Double)) {

						throw new DBFException( "Invalid value for field " + i);
					}
					break;
			}
		}

		if (this.raf == null) {
//			v_records.addElement(values);
			v_records.add(values);
		} else {

			try {
				writeRecord(this.raf, values);
				this.recordCount++;
			} catch (IOException e) {
				throw new DBFException("Error occured while writing record. " + e.getMessage());
			}
		}
	}

	/**
	 * Writes the set data to the OutputStream.
	 * @param out the OutputStream instance to which the data are sent
	 * @throws DBFException if an error has occurred
	 */
	public void write(OutputStream out) throws DBFException {

		try {

			if (this.raf == null) {

				DataOutputStream outStream = new DataOutputStream( out);

				getHeader().setNumberOfRecords(v_records.size());
				getHeader().write( outStream);

				/* Now write all the records */
				int t_recCount = v_records.size();
				for (int i = 0; i < t_recCount; i++) { /* iterate through records */
//					Object[] t_values = (Object[]) v_records.elementAt( i);
					Object[] t_values = (Object[]) v_records.get(i);
					writeRecord(outStream, t_values);
				}

				outStream.write( END_OF_DATA);
				outStream.flush();
			} else {
				/* everything is written already. just update the header for record count and the END_OF_DATA mark */
				updateFileBeforeClosing();
				this.raf.close();
			}

		} catch(IOException e) {
			throw new DBFException( e.getMessage());
		}
	}

	
	private void updateFileBeforeClosing() throws IOException {
		getHeader().setNumberOfRecords(this.recordCount);
//		long pointer = raf.getFilePointer();
		raf.seek(0);			// get back to the header
		getHeader().write(raf);
		raf.seek(raf.length()); // go to the end of the file
//		raf.seek(pointer); // go to the end of the file
		raf.writeByte( END_OF_DATA); // put a end of data
	}
	
	
	public void write() throws DBFException {
		write(null);
	}

	private void writeRecord(DataOutput dataOutput, Object[] objectArray) 	throws IOException {

		dataOutput.write( (byte)' ');
		for (int j = 0; j < getHeader().getNumberOfFields(); j++) { /* iterate throught fields */

			switch(getHeader().getField(j).getDataType()) {

				case 'C':
					if( objectArray[j] != null) {
						String str_value = objectArray[j].toString();	
						dataOutput.write( Utils.textPadding( str_value, characterSetName, getHeader().getField(j).getFieldLength()));
					} else {
						dataOutput.write( Utils.textPadding( "", this.characterSetName, getHeader().getField(j).getFieldLength()));
					}

					break;

				case 'D':
					if( objectArray[j] != null) {

						GregorianCalendar calendar = new GregorianCalendar();
						calendar.setTime( (Date)objectArray[j]);
						dataOutput.write( String.valueOf( calendar.get( Calendar.YEAR)).getBytes());
						dataOutput.write( Utils.textPadding( String.valueOf( calendar.get( Calendar.MONTH)+1), this.characterSetName, 2, Utils.ALIGN_RIGHT, (byte)'0'));
						dataOutput.write( Utils.textPadding( String.valueOf( calendar.get( Calendar.DAY_OF_MONTH)), this.characterSetName, 2, Utils.ALIGN_RIGHT, (byte)'0'));
					} else {
						dataOutput.write( "        ".getBytes());
					}
					break;

				case 'F':

					if( objectArray[j] != null) {
						dataOutput.write( Utils.doubleFormating( (Double)objectArray[j], this.characterSetName, getHeader().getField(j).getFieldLength(), getHeader().getField(j).getDecimalCount()));
					}
					else {

						dataOutput.write( Utils.textPadding( "?", this.characterSetName, getHeader().getField(j).getFieldLength(), Utils.ALIGN_RIGHT));
					}

					break;

				case 'N':

					if( objectArray[j] != null) {

						dataOutput.write(
							Utils.doubleFormating( (Double)objectArray[j], this.characterSetName, getHeader().getField(j).getFieldLength(), getHeader().getField(j).getDecimalCount()));
					}
					else {

						dataOutput.write( 
							Utils.textPadding( "?", this.characterSetName, getHeader().getField(j).getFieldLength(), Utils.ALIGN_RIGHT));
					}

					break;
				case 'L':

					if( objectArray[j] != null) {

						if( (Boolean)objectArray[j] == Boolean.TRUE) {

							dataOutput.write( (byte)'T');
						}
						else {

							dataOutput.write((byte)'F');
						}
					}
					else {

						dataOutput.write( (byte)'?');
					}

					break;

				case 'M':

					break;

				default:	
					throw new DBFException( "Unknown field type " + getHeader().getField(j).getDataType());
			}
		}	/* iterating through the fields */
	}
	
	@Override
	public void close() {
		try {
			updateFileBeforeClosing();
			raf.close();
		} catch (Exception e) {}
	}

	@Override
	public FormatField convertGExportFieldDetailsToFormatField(GExportFieldDetails details) {
		DBFField field = new DBFField();
		field.setName(details.getName());
		field.setDataType((byte) details.getType()); 
		field.setFieldLength(details.getLength()); 
		if (details.getDecimalNb() > 0) {
			field.setDecimalCount(details.getDecimalNb());
		}
		return field;
	}	
	
	
//	public static void main(String[] args) throws IOException {
//		String filename = "C:" + File.separator +
//				"Users" + File.separator +
//				"Don Enrique" + File.separator +
//				"Desktop" + File.separator +
//				"test2.dbf";
//		
//		
//		DBFWriter writer = new DBFWriter(new File(filename), true);
//		
//		List<FormatField> fields = new ArrayList<FormatField>();
//		DBFField field = new DBFField();
//		field.setName("Field1");
//		field.setDataType(DBFField.FIELD_TYPE_F);
//		field.setFieldLength(10);
//		fields.add(field);
//		
//		field = new DBFField();
//		field.setName("Field2");
//		field.setDataType(DBFField.FIELD_TYPE_F);
//		field.setFieldLength(10);
//		fields.add(field);
//		
//	//	writer.setFields(fields);
//		
//		Object[] record;
//		for (int i = 0; i < 10; i++) {
//			record = new Object[2];
//			record[0] = (double) i;
//			record[1] = i * 2d;
//			writer.addRecord(record);
//		}
//		
//		writer.close();
//	}

}
