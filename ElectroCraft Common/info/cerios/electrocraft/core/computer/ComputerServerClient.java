package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.network.ComputerProtocol;
import info.cerios.electrocraft.core.utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.src.World;
import cpw.mods.fml.common.FMLCommonHandler;

public class ComputerServerClient implements Runnable {

	private Socket socket;
	private InputStream in;
	private DataInputStream dis;
	private OutputStream out;
	private DataOutputStream dos;
	private byte[] lastVGAData;
	private XECVGACard videoCard;
	
	TileEntityComputer computer;
	ComputerServer server;
	
	public ComputerServerClient(ComputerServer server, Socket connection) {
		socket = connection;
		this.server = server;
		videoCard = ElectroCraft.instance.getComputer().getVideoCard();
		try {
			out = connection.getOutputStream();
			in = connection.getInputStream();
			dis = new DataInputStream(in);
			dos = new DataOutputStream(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setComputer(TileEntityComputer pc) {
		this.computer = pc;
		lastVGAData = null;
	}
	
	public TileEntityComputer getComputer() {
		return computer;
	}
	
	@Override
	public void run() {
		while (server.getRunning() && socket.isConnected()) {
			try {
				int type = dis.readInt();
				
				switch(ComputerProtocol.values()[type]) {
				case DISPLAY:
					
					out.write(ComputerProtocol.DISPLAY.ordinal());
					dos.writeInt(videoCard.getScreenWidth());
					dos.writeInt(videoCard.getScreenHeight());
					
					byte[] vgadata = videoCard.getScreenData();
					lastVGAData = vgadata;
					out.write(0);
					dos.writeInt(vgadata.length);
					byte[] compressedData = Utils.compressBytes(vgadata);
					dos.writeInt(compressedData.length);
					out.write(compressedData);
//					if (lastVGAData == null) {
//						
//					} else {						
//						ChangedBytes current = null;
//						List<ChangedBytes> changedBytes = new ArrayList<ChangedBytes>();
//
//						int lastOffset = 0;
//						int totalLength = 0;
//						while (current == null ? true : current.length > 0) {
//							if (current == null ) {
//								current = getNextBlock(0, vgadata, lastVGAData);
//							} else {
//								current = getNextBlock(lastOffset + current.length, vgadata, lastVGAData);
//							}
//							lastOffset = current.offset;
//							totalLength += current.length;
//							changedBytes.add(current);
//						}
//						
//						out.write(1);
//						dos.writeInt(totalLength);
//						
//						for (ChangedBytes changedByte : changedBytes) {
//							if (changedByte.length > 0) {
//								byte[] compressedData = Utils.compressBytes(changedByte.b);
//								dos.writeInt(compressedData.length);
//								dos.writeInt(changedByte.offset);
//								out.write(compressedData);
//							}
//						}
//					}
					lastVGAData = vgadata;
					break;
				case DOWNLOAD_IMAGE:
					break;
				case UPLOAD_FILE:
					int size = dis.readInt();
					byte[] data = new byte[size];
					dis.read(data, 0, size);
					// TODO Do something with this data!
					break;
				case TERMINATE:
					throw new IOException();
				default:
					FMLCommonHandler.instance().getFMLLogger().fine("ElectroCraft ComputerServer: Got Unknown Packet!");
					break;
				}
				out.flush();
			} catch (IOException e) {
				FMLCommonHandler.instance().getFMLLogger().fine("ElectroCraft ComputerServer: Client Disconnected!");
				return;
			}
		}
	}
	
	/**
	 * Compares array1 to array two and gets the ChangedBytes
	 * @param beginOffset
	 * @param array1
	 * @param array2
	 * @return
	 */
	public ChangedBytes getNextBlock(int beginOffset, byte[] array1, byte[] array2) {
		if (beginOffset >= array1.length || beginOffset >= array2.length) {
			ChangedBytes changedBytes = new ChangedBytes();
			changedBytes.length = 0;
			return changedBytes;
		} else if (array1.length <= beginOffset) {
			ChangedBytes changedBytes = new ChangedBytes();
			changedBytes.b = Arrays.copyOfRange(array2, beginOffset, array2.length);
			changedBytes.offset = beginOffset;
			changedBytes.length = array2.length - beginOffset;
			return changedBytes;
		} else if (array2.length <= beginOffset) {
			ChangedBytes changedBytes = new ChangedBytes();
			changedBytes.length = 0;
			return changedBytes;
		} else if (array2 == null || array2.length <= 0) {
			ChangedBytes changedBytes = new ChangedBytes();
			changedBytes.b = array1;
			changedBytes.offset = beginOffset;
			changedBytes.length = array1.length;
			return changedBytes;
		} else {
			ChangedBytes changedBytes = new ChangedBytes();
			int offset = beginOffset;
			while (array1[offset] == array2[offset]) {
				offset++;
				if (offset >= array1.length || offset >= array2.length) {
					changedBytes.length = 0;
					return changedBytes;
				}
			}
			changedBytes.offset = offset;
			ByteBuffer buffer = ByteBuffer.allocate(array1.length - beginOffset);
			while (array1[offset] != array2[offset]) {
				buffer.put(array1[offset]);
				offset++;
			}
			changedBytes.length = offset - changedBytes.offset;
			changedBytes.b = new byte[changedBytes.length];
			buffer.rewind();
			buffer.get(changedBytes.b, 0, changedBytes.length);
			return changedBytes;
		}
	}
	
	private class ChangedBytes {
		public byte[] b;
		public int offset;
		public int length;
	}
}
