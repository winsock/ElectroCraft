package me.querol.electrocraft.core.computer.luaapi;

import me.querol.electrocraft.api.computer.ExposedToLua;
import me.querol.electrocraft.api.computer.luaapi.LuaAPI;
import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.computer.Computer;
import me.querol.electrocraft.core.computer.ComputerSocketManager.Mode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import me.querol.com.naef.jnlua.LuaState;
import me.querol.com.naef.jnlua.NamedJavaFunction;
import me.querol.electrocraft.api.computer.ExposedToLua;
import me.querol.electrocraft.api.computer.luaapi.LuaAPI;
import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.computer.Computer;
import me.querol.electrocraft.core.computer.ComputerSocketManager;

@ExposedToLua
public class Network implements LuaAPI {

	private ByteArrayOutputStream qeuedData;
	private boolean isBound = false;
	private ComputerSocketManager.Mode mode;

	@ExposedToLua
	public Network() {
		qeuedData = new ByteArrayOutputStream();
	}

	@ExposedToLua(value = false)
	public void onReceive(byte[] data) throws IOException {
		qeuedData.write(data);
	}

	@ExposedToLua
	public boolean connect(int port, String hostname) {
		if (!isBound) {
			mode = ComputerSocketManager.Mode.SEND;
			InetSocketAddress address = new InetSocketAddress(hostname, port);
			ElectroCraft.instance.getComputerSocketManager().registerEndpoint(
					this, address, mode);
			return true;
		} else
			return false;
	}

	@ExposedToLua
	public boolean bind(int port) {
		if (!isBound) {
			mode = ComputerSocketManager.Mode.RECV;
			InetSocketAddress address = new InetSocketAddress("0.0.0.0", port);
			ElectroCraft.instance.getComputerSocketManager().registerEndpoint(
					this, address, mode);
			return true;
		} else
			return false;
	}

	@ExposedToLua
	public boolean isBound() {
		return isBound;
	}

	@ExposedToLua
	public boolean write(byte[] data) {
		if (isBound) {
			try {
				ElectroCraft.instance.getComputerSocketManager()
						.getConnectionThread(this, mode).sendData(data);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

	@ExposedToLua
	public byte[] read() {
		if (isBound)
			return qeuedData.toByteArray();
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
				string += (char) data[i];
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
		return new NamedJavaFunction[] { new NamedJavaFunction() {
			public NamedJavaFunction init(Computer computer) {
				return this;
			}

			@Override
			public int invoke(LuaState luaState) {
				luaState.pushJavaObject(new Network());
				return 1;
			}

			@Override
			public String getName() {
				return "createNewSocket";
			}
		}.init(computer) };
	}

	@Override
	public String getNamespace() {
		return "net";
	}

	@Override
	public void tick(Computer computer) {
	}
}
