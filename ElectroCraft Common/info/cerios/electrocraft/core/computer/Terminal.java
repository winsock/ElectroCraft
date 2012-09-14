package info.cerios.electrocraft.core.computer;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@ExposedToLua
public class Terminal extends Writer {

	private Map<Integer, Map<Integer, Character>> terminal;
	private int columns, rows;
	private int currentColumn, currentRow;
	private int columnOffset = 0;
	private Object syncObject = new Object();

	@ExposedToLua(value = false)
	public Terminal(int rows, int columns) {
		this.columns = columns;
		this.rows = rows;
		terminal = new TreeMap<Integer, Map<Integer, Character>>();
	}
	
	@ExposedToLua
	public String getLine(int row) {
		String output = "";
		if (getRow(row) != null) {
			for (int i = columnOffset; i < columns + columnOffset; i++) {
				if (getRow(row).get(i) != null)
					if (!Character.isIdentifierIgnorable(getRow(row).get(i)))
						output += getRow(row).get(i);
			}
		}
		return output;
	}

	@ExposedToLua(value = false)
	public Map<Integer ,Character> getRow(int row) {
		if (terminal.size() == 0)
			return null;
		return terminal.get(row);
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
		synchronized(syncObject) {
			terminal.clear();
			currentRow = 0;
			currentColumn = 0;
			columnOffset = 0;
		}
	}

	@ExposedToLua(value = false)
	public void writeLine(String string) throws IOException {
		synchronized(syncObject) {
			write(string + "\n");
		}
	}

	// Used only for Lua
	@ExposedToLua
	public void print(String string) {
		synchronized(syncObject) {
			try {
				writeLine(string);
			} catch (IOException e) { }
		}
	}

	@ExposedToLua
	public void setChar(int row, int col, char chr) {
		synchronized(syncObject) {
			if (row <= rows && col <= columns) {
				if (getRow(row) != null)
					getRow(row).put(col + columnOffset, chr);
				else {
					Map<Integer, Character> list = new TreeMap<Integer, Character>();
					list.put(col + columnOffset, chr);
					terminal.put(row, list);
				}
			}
		}
	}

	@ExposedToLua
	public void setPosition(int row, int column) {
		synchronized(syncObject) {
			if (row > rows)
				row = rows - 1;
			if (column > columns)
				column = columns - 1;
			this.currentRow = row;
			this.currentColumn = column;
		}
	}

	@ExposedToLua
	public void deleteChar(boolean canDeleteLine) {
		synchronized(syncObject) {
			if (currentColumn > 0) {
				if (columnOffset > 0)
					columnOffset--;
				else
					currentColumn--;
				if (getRow(currentRow) != null)
					terminal.get(currentRow).put(currentColumn + columnOffset, '\0');
				
			} else if ((currentRow > 0) && canDeleteLine) {
				currentRow--;
				currentColumn = columns - 1;

				char chr = '\0';
				while (chr == '\0') {
					if (getRow(currentRow) != null)
						if (getRow(currentRow).size() > currentColumn + columnOffset)
							chr = terminal.get(currentRow).get(currentColumn + columnOffset);
					if (--currentColumn <= 0) {
						currentColumn = columns - 1;
						if (--currentRow < 0) {
							currentRow = 0;
							break;
						}
					}
				}
			}
		}
	}

	@ExposedToLua(value = false)
	@Override
	public void close() throws IOException { }

	@ExposedToLua(value = false)
	@Override
	public void flush() throws IOException { }

	@ExposedToLua(value = false)
	@Override
	public synchronized void write(char[] arg0, int arg1, int arg2) throws IOException {
		synchronized(syncObject) {
			for (int i = arg1; i < arg2; i++) {
				if (Character.isIdentifierIgnorable(arg0[i]))
					continue;
				if (arg0[i] == '\n' || arg0[i] == '\r') {
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
					}
					currentColumn = 0;
					columnOffset = 0;
				} else {
					setChar(currentRow, currentColumn, arg0[i]);
					if (++currentColumn >= columns) {
						columnOffset++;
						currentColumn = columns - 1;
					}
				}
			}
		}
	}
}
