package info.cerios.electrocraft.core.computer;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Terminal extends Writer {
	
	private List<List<Character>> terminal = new ArrayList<List<Character>>();
	private int columns, rows;
	private int currentColumn, currentRow;
	private int columnOffset = 0;
	
	@ExposedToLua(value = false)
	public Terminal(int rows, int columns) {
		this.columns = columns;
		this.rows = rows;
	}
	
	public String getLine(int row) {
		String output = "";
		for (int i = columnOffset; i < columns + columnOffset; i++) {
			if (getRow(row) != null)
				if (getRow(row).size() > i)
					if (!Character.isIdentifierIgnorable(getRow(row).get(i)))
						output += getRow(row).get(i);
		}
		return output;
	}
	
	@ExposedToLua(value = false)
	public List<Character> getRow(int row) {
		if (terminal.size() <= row)
			return null;
		if (terminal.size() == 0)
			return null;
		return terminal.get(row);
	}
	
	public int getRows() {
		return rows;
	}
	
	public int getColumns() {
		return columns;
	}
	
	public int getCurrentRow() {
		return currentRow;
	}
	
	public int getCurrentColumn() {
		return currentColumn;
	}
	
	public void clear() {
		terminal.clear();
		currentRow = 0;
		currentColumn = 0;
		columnOffset = 0;
	}
	
	@ExposedToLua(value = false)
	public void writeLine(String string) throws IOException {
		write(string + "\n");
	}
	
	// Used only for Lua
	public void print(String string) {
		try {
			writeLine(string);
		} catch (IOException e) { }
	}
	
	public void setChar(int row, int col, char chr) {
		if (row <= rows && col <= columns) {
			if (getRow(row) != null)
				if (getRow(row).size() > col + columnOffset)
					getRow(row).set(col + columnOffset, chr);
				else
					getRow(row).add(col + columnOffset, chr);
			else {
				List<Character> list = new ArrayList<Character>();
				list.add(col + columnOffset, chr);
				terminal.add(row, list);
			}
		}
	}
	
	public void setPosition(int row, int column) {
		this.currentRow = row;
		this.currentColumn = column;
	}
	
	public void deleteChar(boolean canDeleteLine) {
		if (currentColumn > 0) {
			currentColumn--;
			if (getRow(currentRow) != null)
				if (getRow(currentRow).size() > currentColumn + columnOffset)
					terminal.get(currentRow).set(currentColumn + columnOffset, '\0');
			if (columnOffset > 0)
				columnOffset--;
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
					if (--currentRow <= 0) {
						currentRow = 0;
						break;
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
	public void write(char[] arg0, int arg1, int arg2) throws IOException {
		for (int i = arg1; i < arg2; i++) {
			if (Character.isIdentifierIgnorable(arg0[i]))
				continue;
			if (arg0[i] == '\n' || arg0[i] == '\r') {
				if (++currentRow >= rows) {
					terminal.remove(0);
					currentRow--;
				}
				currentColumn = 0;
				columnOffset = 0;
			} else {
				if (getRow(currentRow) != null)
					if (getRow(currentRow).size() > currentColumn + columnOffset)
						getRow(currentRow).set(currentColumn + columnOffset, arg0[i]);
					else
						getRow(currentRow).add(currentColumn + columnOffset, arg0[i]);
				else {
					List<Character> list = new ArrayList<Character>();
					list.add(currentColumn + columnOffset, arg0[i]);
					terminal.add(currentRow, list);
				}
				if (++currentColumn >= columns) {
					columnOffset++;
					currentColumn = columns - 1;
				}
			}
		}
	}
}
