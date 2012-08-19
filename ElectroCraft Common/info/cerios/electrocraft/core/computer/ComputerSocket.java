package info.cerios.electrocraft.core.computer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComputerSocket {
	
	private ByteArrayOutputStream qeuedData;
	private boolean isBound = false;
	
	public ComputerSocket() {
		qeuedData = new ByteArrayOutputStream();
	}
	
	@ExposedToLua(value = false)
	public void onReceive(byte[] data) throws IOException {
		qeuedData.write(data);
	}
	
	public boolean connect(int port, String hostname) {
		if (!isBound) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean bind(int port) {
		if (!isBound) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isBound() {
		return isBound;
	}
	
	public void write(byte[] data) {
		if (isBound)
			
	}
	
	public byte[] read() {
		if (isBound) {
			return qeuedData.toByteArray();
		}
		return null;
	}
	
	public int readIntFromData(byte[] data, int beginOffset) {
		if (data.length < 4)
			return 0;
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return buffer.getInt(beginOffset);
	}
	
	public short readShortFromData(byte[] data, int beginOffset) {
		if (data.length < 2)
			return 0;
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return buffer.getShort(beginOffset);
	}
	
	public byte readByteFromData(byte[] data, int beginOffset) {
		if (data.length < beginOffset)
			return 0x0;
		return data[beginOffset];
	}
	
	public String readASCIIStringFromData(byte[] data, int beginOffset) {
		String string = "";
		for (int i = beginOffset; i < data.length; i++) {
			if (data[i] == 0x0) {
				break;
			} else {
				string += (char)data[i];
			}
		}
		return string;
	}
	
	public String readUTF16StringFromData(byte[] data, int beginOffset) {
		String string = "";
		ByteBuffer buffer = ByteBuffer.wrap(data);
		while (buffer.hasRemaining()) {
			char chr = buffer.getChar();
			if (chr != '\0') {
				string += chr;
			} else {
				break;
			}
		}
		return string;
	}
}
