package info.cerios.electrocraft.computer;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.IComputerRunnable;
import info.cerios.electrocraft.core.network.ComputerProtocol;
import info.cerios.electrocraft.core.utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;

import cpw.mods.fml.common.FMLCommonHandler;

public class ComputerClient implements Runnable {
	
	private Socket socket;
	private InputStream in;
	private DataInputStream dis;
	private OutputStream out;
	private DataOutputStream dos;
	ByteBuffer displayBuffer;
	private volatile boolean running = true;
	
	private Map<IComputerCallback, ComputerProtocol> callbackMap = new HashMap<IComputerCallback, ComputerProtocol>();
	
	public ComputerClient(int port, SocketAddress address) throws UnknownHostException, IOException {
		SocketAddress serverAddress = new InetSocketAddress(((InetSocketAddress)address).getAddress(), port);
		socket = new Socket();
		socket.connect(serverAddress);
		in = socket.getInputStream();
		out = socket.getOutputStream();
		dis = new DataInputStream(in);
		dos = new DataOutputStream(out);
	}

	public void registerCallback(ComputerProtocol message, IComputerCallback callback) {
		callbackMap.put(callback, message);
	}
	
	public void sendPacket(ComputerProtocol packet) throws IOException {
		dos.write(packet.ordinal());
		out.flush();
	}
	
	@Override
	public void run() {
		FMLCommonHandler.instance().getFMLLogger().info("ElectroCraft ComputerClient: Connected to server!");
		while (running && socket.isConnected()) {
			int type;
			try {
				type = dis.read();
				if (type == -1) {
					throw new IOException();
				}
				
				Object[] callbackData = {dis};
				
				if (type == ComputerProtocol.DISPLAY.ordinal()) {
					int width = dis.readInt();
					int height = dis.readInt();

					int transmissionType = dis.read();
					int length = dis.readInt();
					
					if (length <= 0)
						continue;
					
					if(displayBuffer == null || displayBuffer.capacity() < width * height * 3) {
						ByteBuffer newBuffer = ByteBuffer.allocateDirect(width * height * 3);
						if (displayBuffer != null) {
							displayBuffer.rewind();
							newBuffer.put(displayBuffer);
						}
						displayBuffer = newBuffer;
					}
					
					if (transmissionType == 0) {
						int compressedLength = dis.readInt();
						byte[] data = new byte[compressedLength];
						int bytesRead = dis.read(data, 0, compressedLength);
						if (bytesRead < compressedLength) {
							while (bytesRead < compressedLength) {
								bytesRead += dis.read(data, bytesRead, compressedLength - bytesRead);
							}
						}
						
						try {
							displayBuffer.clear();
							displayBuffer.put(Utils.extractBytes(data));
						} catch (DataFormatException e) {
							FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft ComputerClient: Unable to read display full update packet!");
							continue;
						}
						displayBuffer.rewind();
					} else {
						int currentLength = 0;
						while (currentLength < length) {
							int sectionLength = dis.readInt();
							byte[] data = new byte[sectionLength];
							int offset = dis.readInt();
							int bytesRead = in.read(data, 0, sectionLength);
							if (bytesRead < sectionLength) {
								while (bytesRead < sectionLength) {
									bytesRead += in.read(data, bytesRead, sectionLength - bytesRead);
								}
							}
							try {
								byte[] extractedData = Utils.extractBytes(data);
								for (int i = 0; i < extractedData.length; i++, offset++) {
									displayBuffer.put(offset, extractedData[i]);
								}
								currentLength += extractedData.length;
							} catch (DataFormatException e) {
								FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft ComputerClient: Unable to read display partial update packet!");
								continue;
							}
						}
						displayBuffer.rewind();
					}
					
					callbackData = new Object[] {displayBuffer, width, height};
				}
				
				Set<IComputerCallback> callbacks = Utils.getKeysByValue(callbackMap, ComputerProtocol.values()[type]);
				for (IComputerCallback callback : callbacks) {
					
					mod_ElectroCraft.instance.getComputerHandler().registerRunnableOnMainThread(new IComputerRunnable() {
						
						Object[] data;
						
						public IComputerRunnable init(Object[] data) {
							this.data = data;
							return this;
						}
						
						@Override
						public Object run() {
							return data;
						}}.init(callbackData), callback);
				}
				
				out.flush();
			} catch (IOException e) {
				FMLCommonHandler.instance().getFMLLogger().info("ElectroCraft ComputerClient: Disconnected from server!");
				return;
			}
		}
	}
}
