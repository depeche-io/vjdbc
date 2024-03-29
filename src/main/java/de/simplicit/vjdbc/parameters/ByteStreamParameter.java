// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OraclePreparedStatement;

public class ByteStreamParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 8868161011164192986L;

    public static final int TYPE_ASCII = 1;
    public static final int TYPE_UNICODE = 2;
    public static final int TYPE_BINARY = 3;

    private int _type;
    private byte[] _value;
    private int _length;
    
    public ByteStreamParameter() {
    }

    public ByteStreamParameter(int type, InputStream x, int length) throws SQLException {
        _type = type;
        _length = length;

        BufferedInputStream s = new BufferedInputStream(x);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(length >= 0 ? length : 1024);
            byte buf[] = new byte[1024];
            int br;
            while((br = s.read(buf)) >= 0) {
                if(br > 0) {
                    bos.write(buf, 0, br);
                }
            }
            _value = bos.toByteArray();
            // Adjust length to the amount of read bytes if the user provided
            // -1 as the length parameter
            if(_length < 0) {
                _length = _value.length;
            }
        } catch(IOException e) {
            throw new SQLException("InputStream conversion to byte-array failed");
        } finally {
            try {
                s.close();
            } catch(IOException e) {
            }
        }
    }
    
    public byte[] getValue() {
        return _value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _type = in.readInt();
        _value = (byte[])in.readObject();
        _length = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(_type);
        out.writeObject(_value);
        out.writeInt(_length);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        ByteArrayInputStream bais = new ByteArrayInputStream(_value);

        switch(_type) {
            case TYPE_ASCII:
                pstmt.setAsciiStream(index, bais, _length);
                break;

            case TYPE_UNICODE:
                pstmt.setUnicodeStream(index, bais, _length);
                break;

            case TYPE_BINARY:
                pstmt.setBinaryStream(index, bais, _length);
                break;
        }
    }

    public String toString() {
        return "ByteStream: " + _length + " bytes";
    }

	public void setParameterAtName(PreparedStatement pstmt, String name)
			throws SQLException {
		if(pstmt instanceof OraclePreparedStatement){
			  ByteArrayInputStream bais = new ByteArrayInputStream(_value);
			 switch(_type) {
	            case TYPE_ASCII:
	            	((OraclePreparedStatement)pstmt).setAsciiStreamAtName(name, bais, _length);
	                break;

	            case TYPE_UNICODE:
	            	((OraclePreparedStatement)pstmt).setUnicodeStreamAtName(name, bais, _length);
	                break;

	            case TYPE_BINARY:
	            	((OraclePreparedStatement)pstmt).setBinaryStreamAtName(name, bais, _length);
	                break;
	        }
		}
		else {
			throw new SQLException("Unsupported operation");
		}
		
	}
}
