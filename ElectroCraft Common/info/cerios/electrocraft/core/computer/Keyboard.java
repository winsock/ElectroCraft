package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.network.ComputerInputPacket;
import info.cerios.electrocraft.core.network.ModifierPacket;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@ExposedToLua
public class Keyboard extends Reader {
	
	@ExposedToLua
	public static final int upScanCode = 1999999256, downScanCode = 1999999257, leftScanCode = 1999999258, rightScanCode = 1999999259;
	private List<Integer> keyBuffer = new ArrayList<Integer>();
	private Terminal terminal;
	private boolean isShiftDown = false, isCtrlDown = false;
	private boolean disableTerminalOutput = false;

	@ExposedToLua(value = false)
	public Keyboard(Terminal terminal) {
		this.terminal = terminal;
	}
	
	@ExposedToLua(value = false)
	public synchronized void onKeyPress(ComputerInputPacket inputPacket) {
		if (inputPacket.getDownArrowKey())
			keyBuffer.add(downScanCode);
		if (inputPacket.getLeftArrowKey())
			keyBuffer.add(leftScanCode);
		if (inputPacket.getRightArrowKey())
			keyBuffer.add(rightScanCode);
		if (inputPacket.getUpArrowKey())
			keyBuffer.add(upScanCode);
		if (inputPacket.getEventKey() > 0)
			onKeyPress(inputPacket.getEventKey());
	}
	
	@ExposedToLua(value = false)
	public void proccessModifierPacket(ModifierPacket packet) {
		isShiftDown = packet.isShiftDown();
		isCtrlDown = packet.isCtrlDown();
	}
	
	@ExposedToLua(value = false)
	public synchronized void onKeyPress(int key) {
		keyBuffer.add(key);
		if (!disableTerminalOutput) {
			try {
				terminal.write((char)key);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@ExposedToLua
	public void disableTerminalOutput() {
		disableTerminalOutput = true;
	}
	
	@ExposedToLua
	public void enableTerminalOutput() {
		disableTerminalOutput = false;
	}
	
	@ExposedToLua
	public boolean isTerminalOutputEnabled() {
		return !disableTerminalOutput;
	}
	
	@ExposedToLua
	public boolean isShiftDown() {
		return isShiftDown;
	}
	
	@ExposedToLua
	public boolean isCtrlDown() {
		return isCtrlDown;
	}
	
	@ExposedToLua
	public synchronized int popKey() {
		if (keyBuffer.size() > 0)
			return keyBuffer.remove(0);
		return '\0';
	}
	
	@ExposedToLua
	public int waitForKey() {
		int key = '\0';
		while ((key = popKey()) == '\0') {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) { }
		}
		
		return key;
	}

	@ExposedToLua(value = false)
	@Override
	public void close() throws IOException { }
	
	@ExposedToLua(value = false)
	@Override
	public int read(char[] arg0, int arg1, int arg2) throws IOException {
		if (keyBuffer.size() <= 0) {
			return -1;
		}
		int i = arg1;
		for (; i < arg2; i++) {
			int code = 0x0;
			if (!Character.isDefined(code = popKey()))
				continue;
			arg0[i] = (char) code;
			if (arg0[i] == '\0') {
				break;
			}
		}
		return i - arg1;
	}
}
