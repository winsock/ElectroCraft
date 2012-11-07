package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.api.utils.ObjectPair;
import info.cerios.electrocraft.api.utils.Utils;
import info.cerios.electrocraft.core.computer.luaapi.Network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ComputerSocketManager {

	private Map<Network, ObjectPair<InetSocketAddress, Mode>> registeredEndpoints = new HashMap<Network, ObjectPair<InetSocketAddress, Mode>>();
	private Map<ObjectPair<InetSocketAddress, Mode>, Socket> boundSockets = new HashMap<ObjectPair<InetSocketAddress, Mode>, Socket>();
	private Map<ObjectPair<Thread, ConnectionThread>, ObjectPair<Network, Mode>> threads = new HashMap<ObjectPair<Thread, ConnectionThread>, ObjectPair<Network, Mode>>();
	private static boolean running = false;

	public enum Mode {
		RECV, SEND
	}

	public void registerEndpoint(Network computerSocket,
			InetSocketAddress address, Mode mode) {
		ObjectPair<InetSocketAddress, Mode> pair = new ObjectPair<InetSocketAddress, Mode>(
				address, mode);
		registeredEndpoints.put(computerSocket, pair);

		if (!boundSockets.containsKey(pair)) {
			Socket socket = new Socket();
			boundSockets.put(pair, socket);
			ConnectionThread cThread = new ConnectionThread(socket,
					computerSocket, address, mode);
			Thread thread = new Thread(cThread);
			threads.put(new ObjectPair<Thread, ConnectionThread>(thread,
					cThread), new ObjectPair<Network, Mode>(computerSocket,
					mode));
			thread.start();
		} else {
			ObjectPair<Thread, ConnectionThread> thread = Utils.getKeyByValue(
					threads,
					new ObjectPair<Network, Mode>(computerSocket, mode));
			if (thread != null) {
				thread.getValue2().addSocket(computerSocket);
			}
		}
	}

	public ConnectionThread getConnectionThread(Network adapter, Mode mode) {
		ObjectPair<Thread, ConnectionThread> thread = Utils.getKeyByValue(
				threads, new ObjectPair<Network, Mode>(adapter, mode));
		return thread.getValue2();
	}

	public static class ConnectionThread implements Runnable {
		private Socket socket;
		private InetSocketAddress address;
		private Mode mode;

		private InputStream in;
		private OutputStream out;

		private Set<Network> hookedSockets = new HashSet<Network>();

		public ConnectionThread(Socket socket, Network adapter,
				InetSocketAddress address, Mode mode) {
			this.hookedSockets.add(adapter);
			this.mode = mode;
			this.socket = socket;
			this.address = address;
		}

		public void addSocket(Network adpater) {
			this.hookedSockets.add(adpater);
		}

		public void sendData(byte[] data) throws IOException {
			out.write(data);
		}

		@Override
		public void run() {
			try {
				if (mode == Mode.RECV) {
					socket.bind(address);
				} else if (mode == Mode.SEND) {
					socket.connect(address);
				}

				in = socket.getInputStream();
				out = socket.getOutputStream();
				while (running) {
					byte[] buffer = new byte[256];
					if (in.read(buffer) < 0)
						return;

					for (Network socket : hookedSockets) {
						socket.onReceive(buffer);
					}
				}
			} catch (Exception e) {
			}
		}
	}
}
