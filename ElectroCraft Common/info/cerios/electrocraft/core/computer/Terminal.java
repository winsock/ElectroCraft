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
	private volatile boolean editing = false;

	@ExposedToLua(value = false)
	public Terminal(int rows, int columns) {
		this.columns = columns;
		this.rows = rows;
		terminal = new TreeMap<Integer, Map<Integer, Character>>();
	}
	
	@ExposedToLua(value = false)
	public String getLine(int row) {
		String output = "";
		int timeWaited = 0;
		while (editing) {
			if (timeWaited > 1000) {
				editing = false;
			}
			try {
				Thread.sleep(1);
				timeWaited += 1;
			} catch (InterruptedException e) {
			}
		}
		synchronized(syncObject) {
			if (getRow(row) != null) {
				for (int i = columnOffset; i < columns + columnOffset; i++) {
					if (getRow(row).get(i) != null)
						if (!Character.isIdentifierIgnorable(getRow(row).get(i)))
							output += getRow(row).get(i);
				}
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
		synchronized(syncObject) {
			terminal.clear();
			currentRow = 0;
			currentColumn = 0;
			columnOffset = 0;
		}
	}
	
	@ExposedToLua
	public void clearLine() {
		synchronized(syncObject) {
			if (getRow(currentRow) != null)
				getRow(currentRow).clear();
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

	@ExposedToLua
	public void print(String string) {
		synchronized(syncObject) {
			try {
				write(string);
			} catch (IOException e) {
			}
		}
	}
	
	@ExposedToLua
	public void printLine(String string) {
		synchronized(syncObject) {
			try {
				write(string + "\n");
			} catch (IOException e) {
			}
		}
	}
	
	@ExposedToLua
	public void setEditing(boolean state) {
		synchronized(syncObject) {
			editing = state;
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
				row = rows;
			if (column > columns) {
				columnOffset = column - columns;
				column = columns;
			} else {
				columnOffset = 0;
			}
			this.currentRow = row;
			Map<Integer, Map<Integer, Character>> newTerminal = new TreeMap<Integer, Map<Integer, Character>>();
			List<Integer> rows = new ArrayList<Integer>();
			rows.addAll(terminal.keySet());
			Collections.sort(rows);
			for (int j = 0; j < rows.size(); j++) {
				newTerminal.put(j, terminal.get(rows.get(j)));
			}
			if (rows.size() < row) {
				for (int i = rows.size(); i < row; i++) {
					newTerminal.put(i, new TreeMap<Integer, Character>());
				}
			}
			terminal = newTerminal;
			this.currentColumn = column;
		}
	}
	
	@ExposedToLua(value = false)
	public void deleteRow(int row) {
		if (terminal.size() < row)
			return;
		Map<Integer, Map<Integer, Character>> newTerminal = new TreeMap<Integer, Map<Integer, Character>>();
		List<Integer> rows = new ArrayList<Integer>();
		rows.addAll(terminal.keySet());
		Collections.sort(rows);
		for (int j = 0; j < rows.size(); j++) {
			if (j == row) {
				continue;
			} else if (j > row) {
				newTerminal.put(j - 1, terminal.get(rows.get(j)));
			} else {
				newTerminal.put(j, terminal.get(rows.get(j)));
			}
		}
		terminal = newTerminal;
	}
	
	@ExposedToLua
	public void insertRow(int row) {
		if (terminal.size() >= rows)
			return;
		Map<Integer, Map<Integer, Character>> newTerminal = new TreeMap<Integer, Map<Integer, Character>>();
		List<Integer> rows = new ArrayList<Integer>();
		rows.addAll(terminal.keySet());
		Collections.sort(rows);
		for (int j = 0; j < rows.size(); j++) {
			if (j == row) {
				newTerminal.put(j, new TreeMap<Integer, Character>());
			} else if (j > row) {
				newTerminal.put(j + 1, terminal.get(rows.get(j)));
			} else {
				newTerminal.put(j, terminal.get(rows.get(j)));
			}
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
				if (getRow(currentRow) != null) {
					Map<Integer, Character> row = terminal.get(currentRow);
					Map<Integer, Character> newRow = new TreeMap<Integer, Character>();
					int count = 0;
					for (int col : row.keySet()) {
						if (row.get(col) == '\0')
							continue;
						if (count == currentColumn - 1)
							continue;
						newRow.put(newRow.size(), row.get(col));
						count++;
					}
					terminal.put(currentRow, newRow);
				}
			} else if ((currentRow > 0) && canDeleteLine) {
				Map<Integer, Character> oldRow = terminal.get(currentRow);
				currentRow--;
				// Merge any remaining data onto the previous line
				if (oldRow.size() > 0) {
					for (int col : oldRow.keySet()) {
						getRow(currentRow).put(getRow(currentRow).size(), oldRow.get(col));
					}
				}
				currentColumn = getRow(currentRow).size();
				if (currentColumn > columns) {
					columnOffset = currentColumn - columns;
					currentColumn = columns;
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
					} else {
						terminal.put(currentRow, new TreeMap<Integer, Character>());
					}
					currentColumn = 0;
					columnOffset = 0;
				} else {
					setChar(currentRow, currentColumn, arg0[i]);
					if (++currentColumn > columns) {
						columnOffset++;
						currentColumn = columns;
					}
				}
			}
		}
	}
}
