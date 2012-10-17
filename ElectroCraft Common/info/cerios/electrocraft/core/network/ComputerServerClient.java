package info.cerios.electrocraft.core.network;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.IComputer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.utils.Utils;
import info.cerios.electrocraft.core.utils.Utils.ChangedBytes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
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
	private IComputer computer;
	private ComputerServer server;
	private Object syncObject = new Object();
	
	public ComputerServerClient(ComputerServer server, Socket connection) {
		socket = connection;
		this.server = server;
		try {
			out = connection.getOutputStream();
			in = connection.getInputStream();
			dis = new DataInputStream(in);
			dos = new DataOutputStream(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setComputer(IComputer pc) {
		this.computer = pc;
		lastVGAData = null;
	}
	
	public IComputer getComputer() {
		return computer;
	}
	
	public void changeModes(boolean terminal) {
		synchronized (syncObject) {
			try {
				out.write(ComputerProtocol.MODE.ordinal());
				out.write(terminal ? 1 : 0);
			} catch (IOException e) {
				try {
					socket.close();
					ElectroCraft.instance.getLogger().fine("ComputerServer: Client Disconnected!");
				} catch (IOException e1) { }
			}
		}
	}
	
	@Override
	public void run() {
		while (server.getRunning() && socket.isConnected()) {
			try {
				int type = in.read();
				synchronized (syncObject) {
					switch(ComputerProtocol.values()[type]) {
					case DISPLAY:
						out.write(ComputerProtocol.DISPLAY.ordinal());
						dos.writeInt(computer.getComputer().getVideoCard().getWidth());
						dos.writeInt(computer.getComputer().getVideoCard().getHeight());

						byte[] vgadata = computer.getComputer().getVideoCard().getData();
						
						if (lastVGAData == null) {
							lastVGAData = vgadata;
							out.write(0);
							dos.writeInt(vgadata.length);
							byte[] compressedData = Utils.compressBytes(vgadata);
							dos.writeInt(compressedData.length);
							out.write(compressedData);
						} else {						
							ChangedBytes current = null;
							List<ChangedBytes> changedBytes = new ArrayList<ChangedBytes>();

							int lastOffset = 0;
							int totalLength = 0;
							while (current == null ? true : current.length > 0) {
								if (current == null) {
									current = Utils.getNextBlock(0, vgadata, lastVGAData);
								} else {
									current = Utils.getNextBlock(lastOffset + current.length, vgadata, lastVGAData);
								}
								lastOffset = current.offset;
								totalLength += current.length;
								changedBytes.add(current);
							}

							out.write(1);
							dos.writeInt(totalLength);

							for (ChangedBytes changedByte : changedBytes) {
								if (changedByte.length > 0) {
									byte[] compressedData = Utils.compressBytes(changedByte.b);
									dos.writeInt(compressedData.length);
									dos.writeInt(changedByte.offset);
									out.write(compressedData);
								}
							}
						}
						lastVGAData = vgadata;
						break;
					case TERMINAL:
						int row = dis.readInt();
						out.write(ComputerProtocol.TERMINAL.ordinal());
						dos.writeInt(computer.getComputer().getTerminal().getColumns());
						dos.writeInt(computer.getComputer().getTerminal().getRows());
						dos.writeInt(computer.getComputer().getTerminal().getCurrentColumn());
						dos.writeInt(computer.getComputer().getTerminal().getCurrentRow());
						out.write(0); // Terminal packet type 0
						dos.writeInt(row); // Resend the row number
						String rowData = computer.getComputer().getTerminal().getLine(row);
						if (!rowData.isEmpty()) {
							dos.writeBoolean(true);
							dos.writeUTF(rowData);
						} else {
							dos.writeBoolean(false);
						}
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
						ElectroCraft.instance.getLogger().fine("ComputerServer: Got Unknown Packet!");
						break;
					}
					out.flush();
				}
			} catch (IOException e) {
				ElectroCraft.instance.getLogger().fine("ComputerServer: Client Disconnected!");
				return;
			}
		}
	}
}
