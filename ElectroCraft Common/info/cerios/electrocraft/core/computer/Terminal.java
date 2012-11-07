package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.api.computer.ExposedToLua;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.CustomPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

@ExposedToLua
public class Terminal extends Writer {

	private Map<Integer, Map<Integer, Character>> terminal;
	private int columns, rows;
	private int currentColumn, currentRow;
	private int columnOffset = 0;
	private Object syncObject = new Object();
	private Computer computer;
	private volatile boolean editing = false;
	private List<int[]> queuedUpdates = new ArrayList<int[]>();
	private Map<EntityPlayer, List<int[]>> queuedPlayerUpdates = new HashMap<EntityPlayer, List<int[]>>();

	@ExposedToLua(value = false)
	public Terminal(int rows, int columns, Computer computer) {
		this.columns = columns;
		this.rows = rows;
		this.computer = computer;
		terminal = new TreeMap<Integer, Map<Integer, Character>>();
	}

	@ExposedToLua(value = false)
	public void updateTick() {
		if (!editing
				&& (queuedUpdates.size() > 0 || queuedPlayerUpdates.size() > 0)) {
			for (int[] rows : queuedUpdates) {
				sendUpdate(rows);
			}
			queuedUpdates.clear();
			for (EntityPlayer key : queuedPlayerUpdates.keySet()) {
				for (int[] rows : queuedPlayerUpdates.get(key)) {
					sendUpdate(key, rows);
				}
			}
			queuedPlayerUpdates.clear();
		}
	}

	@ExposedToLua(value = false)
	public void sendUpdate(EntityPlayer player, int... rows) {
		if (editing) {
			if (queuedPlayerUpdates.containsKey(player)) {
				List<int[]> queue = queuedPlayerUpdates.get(player);
				queue.add(rows);
				queuedPlayerUpdates.put(player, queue);
			} else {
				List<int[]> queue = new ArrayList<int[]>();
				queue.add(rows);
				queuedPlayerUpdates.put(player, queue);
			}
		} else if (ConfigHandler.getCurrentConfig()
				.get("general", "useMCServer", false).getBoolean(false)) {
			sendPacketUpdate(player, rows);
		} else {
			sendCustomServerUpdate(rows);
		}
	}

	@ExposedToLua(value = false)
	public void sendUpdate(int... rows) {
		if (editing) {
			queuedUpdates.add(rows);
		} else if (ConfigHandler.getCurrentConfig()
				.get("general", "useMCServer", false).getBoolean(false)) {
			sendPacketUpdate(null, rows);
		} else {
			sendCustomServerUpdate(rows);
		}
	}

	@ExposedToLua(value = false)
	public void sendPacketUpdate(EntityPlayer player, int... rows) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(getColumns());
			dos.writeInt(getRows());
			dos.writeInt(getCurrentColumn());
			dos.writeInt(getCurrentRow());
			if (rows.length == 1) {
				out.write(0); // Terminal packet type 0
				dos.writeInt(rows[0]);
				String rowData = getLine(rows[0]);
				if (!rowData.isEmpty()) {
					dos.writeBoolean(true);
					dos.writeUTF(rowData);
				} else {
					dos.writeBoolean(false);
				}
			} else {
				out.write(1); // Terminal packet type 1
				dos.writeInt(rows.length);
				for (int row : rows) {
					dos.writeInt(row);
					String rowData = getLine(row);
					if (!rowData.isEmpty()) {
						dos.writeBoolean(true);
						dos.writeUTF(rowData);
					} else {
						dos.writeBoolean(false);
					}
				}
			}
			CustomPacket returnPacket = new CustomPacket();
			returnPacket.id = 2;
			returnPacket.data = out.toByteArray();
			if (player != null) {
				PacketDispatcher.sendPacketToPlayer(returnPacket.getMCPacket(),
						(Player) player);
			} else {
				for (EntityPlayer e : computer.getClients()) {
					PacketDispatcher.sendPacketToPlayer(
							returnPacket.getMCPacket(), (Player) e);
				}
			}
		} catch (IOException e) {
			ElectroCraft.instance.getLogger().fine(
					"Error sending screen update packet");
		}
	}

	@ExposedToLua(value = false)
	public void sendCustomServerUpdate(int... rows) {
		for (EntityPlayer e : computer.getClients()) {
			ElectroCraft.instance.getServer().getClient((EntityPlayerMP) e)
					.sendScreenUpdate(rows);
		}
	}

	@ExposedToLua(value = false)
	public void sendCustomServerUpdate(EntityPlayer player, int... rows) {
		ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player)
				.sendScreenUpdate(rows);
	}

	@ExposedToLua(value = false)
	public String getLine(int row) {
		String output = "";
		synchronized (syncObject) {
			if (getRow(row) != null) {
				for (int i = columnOffset; i < columns + columnOffset; i++) {
					if (getRow(row).get(i) != null) {
						if (!Character
								.isIdentifierIgnorable(getRow(row).get(i))) {
							if (getRow(row).get(i) == '\t') {
								getRow(row).put(i, ' ');
								output += " ";
							} else {
								output += getRow(row).get(i);
							}
						}
					}
				}
			}
		}
		return output;
	}

	@ExposedToLua(value = false)
	public Map<Integer, Character> getRow(int row) {
		if (terminal.size() == 0)
			return null;
		return terminal.get(row);
	}

	@ExposedToLua(value = false)
	public void setTerminal(Map<Integer, Map<Integer, Character>> text) {
		terminal = text;
	}

	@ExposedToLua
	public int getRows() {
		return rows;
	}

	@ExposedToLua
	public int getColumns() {
		return columns;
	}

	@ExposedToLua
	public int getCurrentRow() {
		return currentRow;
	}

	@ExposedToLua
	public int getCurrentColumn() {
		return currentColumn;
	}

	@ExposedToLua
	public void clear() {
		synchronized (syncObject) {
			terminal.clear();
			currentRow = 0;
			currentColumn = 0;
			columnOffset = 0;
		}
		int[] updateRows = new int[rows];
		for (int i = 0; i < rows; i++) {
			updateRows[i] = i;
		}
		sendUpdate(updateRows);
	}

	@ExposedToLua
	public void clearLine() {
		synchronized (syncObject) {
			if (getRow(currentRow) != null) {
				getRow(currentRow).clear();
				sendUpdate(currentRow);
			}
			currentColumn = 0;
			columnOffset = 0;
		}
	}

	@ExposedToLua(value = false)
	public void writeLine(String string) throws IOException {
		synchronized (syncObject) {
			write(string + "\n");
		}
	}

	@ExposedToLua
	public void print(String string) {
		synchronized (syncObject) {
			try {
				write(string);
			} catch (IOException e) {
			}
		}
	}

	@ExposedToLua
	public void printLine(String string) {
		synchronized (syncObject) {
			try {
				write(string + "\n");
			} catch (IOException e) {
			}
		}
	}

	@ExposedToLua
	public void setEditing(boolean state) {
		synchronized (syncObject) {
			editing = state;
		}
	}

	@ExposedToLua
	public void setChar(int row, int col, char chr) {
		synchronized (syncObject) {
			if (row <= rows && col <= columns) {
				if (getRow(row) != null) {
					getRow(row).put(col + columnOffset, chr);
				} else {
					Map<Integer, Character> list = new TreeMap<Integer, Character>();
					list.put(col + columnOffset, chr);
					terminal.put(row, list);
				}
			}
		}
	}

	@ExposedToLua
	public void setPosition(int row, int column) {
		synchronized (syncObject) {
			if (row > rows || row < 0) {
				row = rows;
			}
			if (column > columns) {
				columnOffset = column - columns;
				column = columns;
			} else if (column < 0) {
				column = columns;
				columnOffset = 0;
			} else {
				columnOffset = 0;
			}
			int oldRow = this.currentRow;
			this.currentRow = row;
			Map<Integer, Map<Integer, Character>> newTerminal = new TreeMap<Integer, Map<Integer, Character>>();
			List<Integer> rows = new ArrayList<Integer>();
			rows.addAll(terminal.keySet());
			Collections.sort(rows);
			for (int j = 0; j < rows.size(); j++) {
				newTerminal.put(j, terminal.get(rows.get(j)));
			}
			for (int i = rows.size(); i < row; i++) {
				newTerminal.put(i, new TreeMap<Integer, Character>());
			}
			terminal = newTerminal;
			this.currentColumn = column;
			sendUpdate(currentRow, oldRow);
		}
	}

	@ExposedToLua
	public void deleteRow(int row) {
		if (terminal.size() < row)
			return;
		Map<Integer, Map<Integer, Character>> newTerminal = new TreeMap<Integer, Map<Integer, Character>>();
		List<Integer> rows = new ArrayList<Integer>();
		rows.addAll(terminal.keySet());
		Collections.sort(rows);
		for (int j = 0; j < rows.size(); j++) {
			if (j > row) {
				newTerminal.put(j - 1, terminal.get(rows.get(j)));
			} else if (j == row || rows.size() <= j) {
				continue;
			} else if (rows.size() > j) {
				newTerminal.put(j, terminal.get(rows.get(j)));
			}
		}
		terminal = newTerminal;
		int[] updateRows = new int[rows.size()];
		for (int i = row; i < rows.size(); i++) {
			updateRows[i] = i;
		}
		sendUpdate(updateRows);
	}

	@ExposedToLua
	public void insertRow(int row) {
		Map<Integer, Map<Integer, Character>> newTerminal = new TreeMap<Integer, Map<Integer, Character>>();
		List<Integer> rows = new ArrayList<Integer>();
		rows.addAll(terminal.keySet());
		Collections.sort(rows);
		for (int j = 0; j < rows.size() + 1; j++) {
			if (j == row) {
				newTerminal.put(j, new TreeMap<Integer, Character>());
			} else if (j > row) {
				newTerminal.put(j, terminal.get(rows.get(j - 1)));
			} else {
				newTerminal.put(j, terminal.get(rows.get(j)));
			}
		}
		terminal = newTerminal;
		int[] updateRows = new int[this.rows];
		for (int i = row + (row > 0 ? -1 : 0); i < this.rows; i++) {
			updateRows[i] = i;
		}
		sendUpdate(updateRows);
	}

	@ExposedToLua
	public boolean isCharVisible(String string) {
		return !Character.isIdentifierIgnorable(string.charAt(0));
	}

	@ExposedToLua
	public void deleteChar(boolean canDeleteLine) {
		synchronized (syncObject) {
			if (currentColumn > 0) {
				if (columnOffset > 0) {
					columnOffset--;
				} else {
					currentColumn--;
				}
				if (getRow(currentRow) != null) {
					Map<Integer, Character> row = terminal.get(currentRow);
					Map<Integer, Character> newRow = new TreeMap<Integer, Character>();
					int count = 0;
					for (int col : row.keySet()) {
						if (row.get(col) == '\0') {
							continue;
						}
						if (count == currentColumn - 1) {
							continue;
						}
						newRow.put(newRow.size(), row.get(col));
						count++;
					}
					terminal.put(currentRow, newRow);
				}
				sendUpdate(currentRow);
			} else if ((currentRow > 0) && canDeleteLine) {
				Map<Integer, Character> oldRow = terminal.get(currentRow);
				currentRow--;
				// Merge any remaining data onto the previous line
				if (oldRow.size() > 0) {
					for (int col : oldRow.keySet()) {
						getRow(currentRow).put(getRow(currentRow).size(),
								oldRow.get(col));
					}
				}
				currentColumn = getRow(currentRow).size();
				if (currentColumn > columns) {
					columnOffset = currentColumn - columns;
					currentColumn = columns;
				}
				sendUpdate(currentRow, currentRow + 1);
			}
		}
	}

	@ExposedToLua(value = false)
	@Override
	public void close() throws IOException {
	}

	@ExposedToLua(value = false)
	@Override
	public void flush() throws IOException {
	}

	@ExposedToLua(value = false)
	@Override
	public synchronized void write(char[] arg0, int arg1, int arg2)
			throws IOException {
		synchronized (syncObject) {
			for (int i = arg1; i < arg2; i++) {
				if (Character.isIdentifierIgnorable(arg0[i])) {
					continue;
				}
				if (arg0[i] == '\n') {
					if (++currentRow >= rows) {
						Map<Integer, Map<Integer, Character>> newTerminal = new TreeMap<Integer, Map<Integer, Character>>();
						List<Integer> rows = new ArrayList<Integer>();
						rows.addAll(terminal.keySet());
						Collections.sort(rows);
						for (int j = 1; j < rows.size(); j++) {
							newTerminal.put(j - 1, terminal.get(rows.get(j)));
						}
						terminal = newTerminal;
						currentRow = this.rows - 1;
						currentColumn = 0;
						columnOffset = 0;
						int[] updateRows = new int[this.rows];
						for (int j = 0; j < this.rows; j++) {
							updateRows[j] = j;
						}
						sendUpdate(updateRows);
					} else {
						Map<Integer, Map<Integer, Character>> newTerminal = new TreeMap<Integer, Map<Integer, Character>>();
						List<Integer> rows = new ArrayList<Integer>();
						rows.addAll(terminal.keySet());
						Collections.sort(rows);
						for (int j = 0; j < rows.size() + 1; j++) {
							if (j > currentRow) {
								newTerminal.put(j,
										terminal.get(rows.get(j - 1)));
							} else if (j == currentRow || rows.size() <= j) {
								newTerminal.put(j,
										new TreeMap<Integer, Character>());
							} else if (rows.size() > j) {
								newTerminal.put(j, terminal.get(rows.get(j)));
							}
						}
						currentColumn = 0;
						columnOffset = 0;
						terminal = newTerminal;
						int[] updateRows = new int[newTerminal.size()
								- currentRow];
						for (int j = 0; j < newTerminal.size() - currentRow; j++) {
							updateRows[j] = currentRow + j;
						}
						sendUpdate(updateRows);
					}
				} else {
					setChar(currentRow, currentColumn, arg0[i]);
					if (++currentColumn > columns) {
						columnOffset++;
						currentColumn = columns;
					}
					sendUpdate(currentRow);
				}
			}
		}
	}

	/**
	 * Adapter for a Writer to behave like an OutputStream.
	 * 
	 * Bytes are converted to chars using the platform default encoding. If this
	 * encoding is not a single-byte encoding, some data may be lost.
	 */
	public class WriterOutputStream extends OutputStream {

		private final Writer writer;

		public WriterOutputStream(Writer writer) {
			this.writer = writer;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b }, 0, 1);
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			writer.write(new String(b, off, len));
		}

		@Override
		public void flush() throws IOException {
			writer.flush();
		}

		@Override
		public void close() throws IOException {
			writer.close();
		}
	}
}
