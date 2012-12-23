package info.cerios.electrocraft.computer;

import info.cerios.electrocraft.api.computer.IComputerCallback;
import info.cerios.electrocraft.api.utils.ObjectPair;
import info.cerios.electrocraft.api.utils.Utils;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.ComputerProtocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class ComputerClient implements Runnable {

    private Socket socket;
    private InputStream in;
    private DataInputStream dis;
    private OutputStream out;
    private DataOutputStream dos;
    private ByteBuffer displayBuffer;
    private volatile boolean running = true;
    private Object syncObject = new Object();

    private Map<ComputerProtocol, List<IComputerCallback>> callbackMap = new HashMap<ComputerProtocol, List<IComputerCallback>>();

    public ComputerClient(int port, SocketAddress address) throws UnknownHostException, IOException {
        SocketAddress serverAddress = new InetSocketAddress(((InetSocketAddress) address).getAddress(), port);
        socket = new Socket();
        socket.connect(serverAddress);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        dis = new DataInputStream(in);
        dos = new DataOutputStream(out);
    }

    public void registerCallback(ComputerProtocol message, IComputerCallback callback) {
        List<IComputerCallback> computerList;
        if (callbackMap.containsKey(message)) {
            computerList = callbackMap.get(message);
        } else {
            computerList = new ArrayList<IComputerCallback>();
        }

        computerList.add(callback);
        callbackMap.put(message, computerList);
    }

    public void sendPacket(ComputerProtocol packet) throws IOException {
        synchronized (syncObject) {
            out.write(packet.ordinal());
            out.flush();
        }
    }

    public void sendTerminalPacket(int row) throws IOException {
        synchronized (syncObject) {
            out.write(ComputerProtocol.TERMINAL.ordinal());
            dos.writeInt(row);
            out.flush();
        }
    }

    @Override
    public void run() {
        ElectroCraft.instance.getLogger().info("ComputerClient: Connected to server!");
        while (running && socket.isConnected()) {
            int type;
            try {
                type = dis.read();
                if (type == -1) {
                    throw new IOException();
                }

                Object[] callbackData = { ComputerProtocol.values()[type], dis };

                if (type == ComputerProtocol.DISPLAY.ordinal()) {
                    int width = dis.readInt();
                    int height = dis.readInt();

                    int transmissionType = in.read();
                    int length = dis.readInt();

                    if (displayBuffer == null || displayBuffer.capacity() < width * height) {
                        ByteBuffer newBuffer = ByteBuffer.allocateDirect(width * height);
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
                            ElectroCraft.instance.getLogger().severe("ComputerClient: Unable to read display full update packet!");
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
                                ElectroCraft.instance.getLogger().severe("ComputerClient: Unable to read display partial update packet!");
                                continue;
                            }
                        }
                        displayBuffer.rewind();
                    }

                    callbackData = new Object[] { ComputerProtocol.values()[type], displayBuffer, width, height };
                } else if (type == ComputerProtocol.TERMINAL.ordinal()) {
                    int cols = dis.readInt();
                    int rows = dis.readInt();
                    int currentCol = dis.readInt();
                    int currentRow = dis.readInt();

                    int transmissionType = in.read();
                    Object returnData = null;

                    if (transmissionType == 0) {
                        int row = dis.readInt();
                        if (dis.readBoolean()) {
                            String rowData = dis.readUTF();
                            returnData = new Object[] { row, rowData };
                        } else {
                            returnData = new Object[] { row, "" };
                        }
                    } else if (transmissionType == 1) {
                        int numberOfChangedRows = dis.readInt();

                        ObjectPair<Integer, String>[] changedRows = new ObjectPair[numberOfChangedRows];
                        for (int i = 0; i < numberOfChangedRows; i++) {
                            int rowNumber = dis.readInt();
                            if (dis.readBoolean()) {
                                changedRows[i] = new ObjectPair<Integer, String>(rowNumber, dis.readUTF());
                            } else {
                                returnData = new ObjectPair<Integer, String>(rowNumber, "");
                            }
                        }

                        returnData = new Object[] { numberOfChangedRows, changedRows };
                    }

                    callbackData = new Object[] { ComputerProtocol.values()[type], transmissionType, rows, cols, currentCol, currentRow, returnData };
                } else if (type == ComputerProtocol.MODE.ordinal()) {
                    callbackData = new Object[] { ComputerProtocol.values()[type], in.read() };
                }

                List<IComputerCallback> callbacks = callbackMap.get(ComputerProtocol.values()[type]);
                if (callbacks != null) {
                    for (IComputerCallback callback : callbacks) {
                        callback.onTaskComplete(callbackData);
                    }
                }

                synchronized (syncObject) {
                    out.flush();
                }
            } catch (IOException e) {
                ElectroCraft.instance.getLogger().info("ComputerClient: Disconnected from server!");
                return;
            }
        }
    }
}
