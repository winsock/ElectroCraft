package info.cerios.electrocraft.core.computer.luaapi;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.ComputerSocketManager;
import info.cerios.electrocraft.core.computer.ExposedToLua;
import info.cerios.electrocraft.core.computer.ComputerSocketManager.Mode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

@ExposedToLua
public class ComputerSocket implements LuaAPI {
	
	private ByteArrayOutputStream qeuedData;
	private boolean isBound = false;
	private Mode mode;
	
	@ExposedToLua
	public ComputerSocket() {
		qeuedData = new ByteArrayOutputStream();
	}
	
	@ExposedToLua(value = false)
	public void onReceive(byte[] data) throws IOException {
		qeuedData.write(data);
	}
	
	@ExposedToLua
	public boolean connect(int port, String hostname) {
		if (!isBound) {
			mode = Mode.SEND;
			InetSocketAddress address = new InetSocketAddress(hostname, port);
			ElectroCraft.instance.getComputerSocketManager().registerEndpoint(this, address, mode);
			return true;
		} else {
			return false;
		}
	}
	
	@ExposedToLua
	public boolean bind(int port) {
		if (!isBound) {
			mode = Mode.RECV;
			InetSocketAddress address = new InetSocketAddress("0.0.0.0", port);
			ElectroCraft.instance.getComputerSocketManager().registerEndpoint(this, address, mode);
			return true;
		} else {
			return false;
		}
	}
	
	@ExposedToLua
	public boolean isBound() {
		return isBound;
	}
	
	@ExposedToLua
	public boolean write(byte[] data) {
		if (isBound) {
			try {
				ElectroCraft.instance.getComputerSocketManager().getConnectionThread(this, mode).sendData(data);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}
	
	@ExposedToLua
	public byte[] read() {
		if (isBound) {
			return qeuedData.toByteArray();
		}
		return null;
	}
	
	@ExposedToLua
	public int readIntFromData(byte[] data, int beginOffset) {
		if (data.length < 4)
			return 0;
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return buffer.getInt(beginOffset);
	}
	
	@ExposedToLua
	public short readShortFromData(byte[] data, int beginOffset) {
		if (data.length < 2)
			return 0;
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return buffer.getShort(beginOffset);
	}
	
	@ExposedToLua
	public byte readByteFromData(byte[] data, int beginOffset) {
		if (data.length < beginOffset)
			return 0x0;
		return data[beginOffset];
	}
	
	@ExposedToLua
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
	
	@ExposedToLua
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
	
	@ExposedToLua(value = false)
	@Override
	public NamedJavaFunction[] getGlobalFunctions(Computer computer) {
		return new NamedJavaFunction[] {
				new NamedJavaFunction() {
					Computer computer;
					
					public NamedJavaFunction init(Computer computer) {
						this.computer = computer;
						return this;
					}
					
					@Override
					public int invoke(LuaState luaState) {
						luaState.pushJavaObject(new ComputerSocket());
						return 1;
					}

					@Override
					public String getName() {
						return "createNewSocket";
					}
				}.init(computer)
		};
	}
}
