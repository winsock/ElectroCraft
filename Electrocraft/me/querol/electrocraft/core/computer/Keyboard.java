package me.querol.electrocraft.core.computer;

import me.querol.electrocraft.api.computer.ExposedToLua;
import me.querol.electrocraft.core.network.ComputerInputPacket;
import me.querol.electrocraft.core.network.ModifierPacket;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@ExposedToLua
public class Keyboard extends InputStream {

    @ExposedToLua
    public static final int upScanCode = 1999999256, downScanCode = 1999999257,
            leftScanCode = 1999999258;
    @ExposedToLua
    public static final int rightScanCode = 1999999259,
            backspaceScanCode = 1999999260, ctrlScanCode = 1999999261;

    private List<Integer> keyBuffer = new ArrayList<Integer>();
    private boolean isShiftDown = false, isCtrlDown = false;

    @ExposedToLua(value = false)
    public Keyboard(Terminal terminal) {
    }

    @ExposedToLua(value = false)
    public synchronized void onKeyPress(ComputerInputPacket inputPacket) {
        if (inputPacket.getDownArrowKey()) {
            keyBuffer.add(downScanCode);
        }
        if (inputPacket.getLeftArrowKey()) {
            keyBuffer.add(leftScanCode);
        }
        if (inputPacket.getRightArrowKey()) {
            keyBuffer.add(rightScanCode);
        }
        if (inputPacket.getUpArrowKey()) {
            keyBuffer.add(upScanCode);
        }
        if (inputPacket.getEventKeyName().equalsIgnoreCase("RETURN")) {
            onKeyPress('\n');
        } else if (inputPacket.getEventKeyName().equalsIgnoreCase("BACK")) {
            onKeyPress(backspaceScanCode);
        } else if (inputPacket.getEventKeyName().equalsIgnoreCase("LCONTROL") || inputPacket.getEventKeyName().equalsIgnoreCase("RCONTROL")) {
            onKeyPress(ctrlScanCode);
        } else if (inputPacket.getEventKey() > 0) {
            onKeyPress(inputPacket.getEventKey());
        }
    }

    @ExposedToLua(value = false)
    public void proccessModifierPacket(ModifierPacket packet) {
        isShiftDown = packet.isShiftDown();
        isCtrlDown = packet.isCtrlDown();
    }

    @ExposedToLua(value = false)
    public synchronized void onKeyPress(int key) {
        keyBuffer.add(key);
    }

    @ExposedToLua
    public boolean isShiftDown() {
        return isShiftDown;
    }

    @ExposedToLua
    public boolean isCtrlDown() {
        return isCtrlDown;
    }

    public int peak() {
        if (keyBuffer.size() > 0)
            return keyBuffer.get(0);
        return '\0';
    }

    @ExposedToLua(value = false)
    public synchronized int popKey() {
        if (keyBuffer.size() > 0)
            return keyBuffer.remove(0);
        return '\0';
    }

    @ExposedToLua
    public String popChar() {
        if (peak() > Character.MAX_VALUE)
            return "";
        return String.valueOf((char) popKey());
    }

    @ExposedToLua
    public int getKeysInBuffer() {
        return keyBuffer.size();
    }

    @ExposedToLua(value = false)
    public int waitForKey() {
        int key = '\0';
        while ((key = popKey()) == '\0') {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }

        return key;
    }

    @ExposedToLua(value = false)
    @Override
    public void close() throws IOException {
    }

    @Override
    public int read() throws IOException {
        return waitForKey();
    }
}
