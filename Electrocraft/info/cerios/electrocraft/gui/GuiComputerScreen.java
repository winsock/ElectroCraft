package info.cerios.electrocraft.gui;

import info.cerios.electrocraft.ElectroCraftClient;
import info.cerios.electrocraft.api.computer.IComputerCallback;
import info.cerios.electrocraft.api.utils.ObjectPair;
import info.cerios.electrocraft.api.utils.Utils;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.ComputerInputPacket;
import info.cerios.electrocraft.core.network.ComputerProtocol;
import info.cerios.electrocraft.core.network.CustomPacket;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiComputerScreen extends GuiScreen implements IComputerCallback {

    public static final int CLOSE_BUTTON = 0;

    private int displayTextureId;
    private boolean repeatEventsOldState = Keyboard.areRepeatEventsEnabled();
    private volatile boolean isVisible = true;
    private int lastWidth, lastHeight, displayWidth, displayHeight;
    private ByteBuffer displayBuffer;
    private boolean terminalMode = true;
    private Map<Integer, String> terminalList = new HashMap<Integer, String>();
    private int rows = 20;
    private int columns = 50;
    private int currentRow = 0;
    private int currentCol = 0;
    private boolean shouldAskForScreenPacket = true;
    private Object syncObject = new Object();
    private int ticksSinceLastBlink = 0;
    private int delayTicks = 0;
    private int ticksControlComboPressed = 0;

    public GuiComputerScreen() {
        Keyboard.enableRepeatEvents(true);
        if (ElectroCraftClient.instance.usingComputerClient()) {
            ElectroCraftClient.instance.getComputerClient().registerCallback(ComputerProtocol.DISPLAY, this);
            ElectroCraftClient.instance.getComputerClient().registerCallback(ComputerProtocol.TERMINAL, this);
            ElectroCraftClient.instance.getComputerClient().registerCallback(ComputerProtocol.MODE, this);
        }
    }

    public void initGui() {
        controlList.add(new GuiButton(CLOSE_BUTTON, width - 5 - 4 - 17, 5 + 4, 17, 17, "X"));
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        Tessellator tess = Tessellator.instance;

        float halfBoxWidth = width / 2.5f;
        float halfBoxHeight = height / 2.5f;
        float halfScreenWidth = halfBoxWidth - 20;
        float halfScreenHeight = halfBoxHeight - 20;

        // Draw the background
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/info/cerios/electrocraft/gfx/computerscreen.png"));
        tess.startDrawingQuads();
        tess.addVertexWithUV((width / 2) + halfBoxWidth, (height / 2) - halfBoxHeight, 0, 1, 0);
        tess.addVertexWithUV((width / 2) - halfBoxWidth, (height / 2) - halfBoxHeight, 0, 0, 0);
        tess.addVertexWithUV((width / 2) - halfBoxWidth, (height / 2) + halfBoxHeight, 0, 0, 1);
        tess.addVertexWithUV((width / 2) + halfBoxWidth, (height / 2) + halfBoxHeight, 0, 1, 1);
        tess.draw();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        if (terminalMode) {
            tess.startDrawingQuads();
            tess.setColorOpaque(0, 0, 0);
            tess.addVertexWithUV((width / 2) + halfBoxWidth - 15, (height / 2) - halfBoxHeight + 20, 0, 1, 0);
            tess.addVertexWithUV((width / 2) - halfBoxWidth + 15, (height / 2) - halfBoxHeight + 20, 0, 0, 0);
            tess.addVertexWithUV((width / 2) - halfBoxWidth + 15, (height / 2) + halfBoxHeight - 15, 0, 0, 1);
            tess.addVertexWithUV((width / 2) + halfBoxWidth - 15, (height / 2) + halfBoxHeight - 15, 0, 1, 1);
            tess.draw();

            for (int i = 0; i < terminalList.size(); i++) {
                int currentLine = terminalList.keySet().toArray(new Integer[terminalList.size()])[i];
                String line = terminalList.get(currentLine);
                float pixelsPerChar = (halfScreenWidth * 2) / columns;
                float pixelsPerLineHeight = ((halfScreenHeight * 2) / rows);

                float scaleFactorX = 1f;
                float scaleFactorY = 1f;
                if (fontRenderer.getStringWidth(line) > (halfScreenWidth * 2)) {
                    scaleFactorX = (float) ((halfScreenWidth * 2) / fontRenderer.getStringWidth(line));
                }
                if ((fontRenderer.FONT_HEIGHT * columns) > (halfScreenHeight * 2)) {
                    scaleFactorY = (float) ((halfScreenHeight * 2) / (pixelsPerLineHeight * rows));
                }
                GL11.glPushMatrix();
                GL11.glScalef(scaleFactorX, scaleFactorY, 1);
                this.fontRenderer.drawString(line, (int) ((1 / scaleFactorX) * ((width / 2) - halfScreenWidth)), (int) ((i * pixelsPerLineHeight) + ((1 / scaleFactorY) * ((height / 2) - halfScreenHeight))) + 4, 0xFFFFFF);
                if (i == currentRow && (ticksSinceLastBlink > 500 || delayTicks > 0)) {
                    this.fontRenderer.drawString("_", (int) ((1 / scaleFactorX) * ((width / 2) - halfScreenWidth)) + (currentCol > line.length() ? fontRenderer.getStringWidth(line) : fontRenderer.getStringWidth(line.substring(0, currentCol))), (int) ((i * pixelsPerLineHeight) + ((1 / scaleFactorY) * ((height / 2) - halfScreenHeight))) + 4, 0xFFFFFF);
                    if (delayTicks <= 0)
                        delayTicks = 500;
                } else if (ticksSinceLastBlink <= 500) {
                    ticksSinceLastBlink++;
                } else if (delayTicks > 0) {
                    delayTicks--;
                    if (delayTicks <= 0)
                        ticksSinceLastBlink = 0;
                }
                GL11.glPopMatrix();
            }
        } else {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
            tess.startDrawingQuads();
            tess.addVertexWithUV((width / 2) + halfBoxWidth - 15, (height / 2) - halfBoxHeight + 20, 0, 1, 0);
            tess.addVertexWithUV((width / 2) - halfBoxWidth + 15, (height / 2) - halfBoxHeight + 20, 0, 0, 0);
            tess.addVertexWithUV((width / 2) - halfBoxWidth + 15, (height / 2) + halfBoxHeight - 15, 0, 0, 1);
            tess.addVertexWithUV((width / 2) + halfBoxWidth - 15, (height / 2) + halfBoxHeight - 15, 0, 1, 1);
            tess.draw();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }

        // Draw the screen title
        fontRenderer.drawString("xEC Computer System", 53, 28, 0x404040);

        super.drawScreen(par1, par2, par3);
    }

    @Override
    protected void actionPerformed(GuiButton par1GuiButton) {
        mc.displayGuiScreen(null);
    }

    @Override
    public void updateScreen() {
        synchronized (syncObject) {
            if (shouldAskForScreenPacket) {
                try {
                    if (!terminalMode) {
                        if (displayBuffer != null) {
                            if (!GL11.glIsTexture(displayTextureId) || (displayWidth != lastWidth || displayHeight != lastHeight)) {
                                if (GL11.glIsTexture(displayTextureId))
                                    GL11.glDeleteTextures(displayTextureId);
                                displayTextureId = GL11.glGenTextures();
                                GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
                                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, displayWidth, displayHeight, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, displayBuffer);
                                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                                lastWidth = displayWidth;
                                lastHeight = displayHeight;
                            } else if (displayBuffer != null) {
                                GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
                                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, displayWidth, displayHeight, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, displayBuffer);
                                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                            }
                        }
                        // Ask for another screen packet
                        if (ElectroCraftClient.instance.usingComputerClient()) {
                            ElectroCraftClient.instance.getComputerClient().sendPacket(ComputerProtocol.DISPLAY);
                        } else {
                            CustomPacket packet = new CustomPacket();
                            packet.id = 1;
                            packet.data = new byte[] {};
                            PacketDispatcher.sendPacketToServer(packet.getMCPacket());
                        }
                        shouldAskForScreenPacket = false;
                        // } else {
                        // for (int i = 0; i < rows; i++) {
                        // if
                        // (ElectroCraftClient.instance.usingComputerClient()) {
                        // ElectroCraftClient.instance.getComputerClient().sendTerminalPacket(i);
                        // } else {
                        // CustomPacket packet = new CustomPacket();
                        // packet.id = 2;
                        // ByteArrayOutputStream out = new
                        // ByteArrayOutputStream();
                        // DataOutputStream dos = new DataOutputStream(out);
                        // dos.writeInt(i);
                        // packet.data = out.toByteArray();
                        // PacketDispatcher.sendPacketToServer(packet.getMCPacket());
                        // }
                        // shouldAskForScreenPacket = false;
                        // }
                    }
                } catch (IOException e) {
                    ElectroCraft.instance.getLogger().severe("Unable to send screen update packet!");
                }
            }
        }
    }

    @Override
    public void onGuiClosed() {
        GuiPacket packet = new GuiPacket();
        packet.setCloseWindow(true);
        packet.setGui(Gui.COMPUTER_SCREEN);
        try {
            PacketDispatcher.sendPacketToServer(packet.getMCPacket());
        } catch (IOException e) {
            // Oh well we tried
        }
        Keyboard.enableRepeatEvents(repeatEventsOldState);
        this.isVisible = false;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void handleInput() {
        while (Mouse.next()) {
            this.handleMouseInput();
        }
        while (Keyboard.next()) {
            if ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && Keyboard.isKeyDown(Keyboard.KEY_T)) {
                if (ticksControlComboPressed >= 50) {
                    CustomPacket customPacket = new CustomPacket();
                    customPacket.id = 3;
                    customPacket.data = new byte[] { 0 };
                    try {
                        FMLClientHandler.instance().getClient().getSendQueue().addToSendQueue(customPacket.getMCPacket());
                    } catch (IOException e) {
                        ElectroCraft.instance.getLogger().fine("Unable to send program terminate!");
                    }
                    ticksControlComboPressed = 0;
                } else {
                    ticksControlComboPressed++;
                }
            } else {
                ticksControlComboPressed = 0;
            }
            boolean down;
            if (Keyboard.getEventKeyState()) {
                down = true;
            } else {
                down = false;
            }
            if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                mc.displayGuiScreen(null);
                return;
            }
            synchronized (syncObject) {
                ComputerInputPacket inputPacket = new ComputerInputPacket();
                if (Keyboard.isKeyDown(Keyboard.KEY_UP))
                    inputPacket.setUpArrowKey();
                if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
                    inputPacket.setLeftArrowKey();
                if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
                    inputPacket.setDownArrowKey();
                if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
                    inputPacket.setRightArrowKey();
                inputPacket.setEventKey(Keyboard.getEventCharacter());
                inputPacket.setEventKeyName(Keyboard.getKeyName(Keyboard.getEventKey()));
                inputPacket.setMouseDeltas(Mouse.getDX(), Mouse.getDY(), Mouse.getDWheel());
                inputPacket.setEventMouseButton(Mouse.getEventButton());
                inputPacket.setWasKeyDown(down);
                try {
                    FMLClientHandler.instance().getClient().getSendQueue().addToSendQueue(inputPacket.getMCPacket());
                } catch (IOException e) {
                    ElectroCraft.instance.getLogger().fine("Unable to send computer input data!");
                }
            }
        }
    }

    public void handleCustomPacket(CustomPacket packet) {
        try {
            if (packet.id == 0) {
                terminalMode = packet.data[0] == 0 ? false : true;
            } else if (packet.id == 1) {
                ByteArrayInputStream in = new ByteArrayInputStream(packet.data);
                DataInputStream dis = new DataInputStream(in);
                int width = dis.readInt();
                int height = dis.readInt();

                int transmissionType = in.read();
                int length = dis.readInt();

                if (displayBuffer == null || displayBuffer.capacity() < width * height * 3) {
                    ByteBuffer newBuffer = ByteBuffer.allocateDirect(width * height * 3);
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
                        for (byte b : Utils.extractBytes(data)) {
                            int rgb = ElectroCraft.colorPalette[b & 0xFF];
                            byte red = (byte) ((rgb >> 16) & 0xFF);
                            byte green = (byte) ((rgb >> 8) & 0xFF);
                            byte blue = (byte) (rgb & 0xFF);
                            displayBuffer.put(red);
                            displayBuffer.put(green);
                            displayBuffer.put(blue);
                        }
                    } catch (DataFormatException e) {
                        ElectroCraft.instance.getLogger().severe("Unable to read display full update packet!");
                        return;
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
                                int rgb = ElectroCraft.colorPalette[extractedData[i] & 0xFF];
                                byte red = (byte) ((rgb >> 16) & 0xFF);
                                byte green = (byte) ((rgb >> 8) & 0xFF);
                                byte blue = (byte) (rgb & 0xFF);
                                displayBuffer.put(offset, red);
                                displayBuffer.put(offset + 1, green);
                                displayBuffer.put(offset + 2, blue);
                            }
                            currentLength += extractedData.length;
                        } catch (DataFormatException e) {
                            ElectroCraft.instance.getLogger().severe("Unable to read display partial update packet!");
                            return;
                        }
                    }
                    displayBuffer.rewind();
                }
                displayWidth = width;
                displayHeight = height;
                shouldAskForScreenPacket = true;
            } else if (packet.id == 2) {
                ByteArrayInputStream in = new ByteArrayInputStream(packet.data);
                DataInputStream dis = new DataInputStream(in);
                columns = dis.readInt();
                rows = dis.readInt();
                currentCol = dis.readInt();
                currentRow = dis.readInt();

                int transmissionType = in.read();

                if (transmissionType == 0) {
                    int row = dis.readInt();
                    if (dis.readBoolean()) {
                        String rowData = dis.readUTF();
                        terminalList.put(row, rowData);
                    } else {
                        terminalList.put(row, "");
                    }
                } else if (transmissionType == 1) {
                    int numberOfChangedRows = dis.readInt();
                    for (int i = 0; i < numberOfChangedRows; i++) {
                        int rowNumber = dis.readInt();
                        if (dis.readBoolean()) {
                            terminalList.put(rowNumber, dis.readUTF());
                        } else {
                            terminalList.put(rowNumber, "");
                        }
                    }
                }
                shouldAskForScreenPacket = true;
            }
        } catch (IOException e) {
            ElectroCraft.instance.getLogger().severe("Unable to read custom client computer packet!");
        }
    }

    @Override
    public Object onTaskComplete(Object... objects) {
        synchronized (syncObject) {
            if (objects[0] instanceof ComputerProtocol) {
                ComputerProtocol type = (ComputerProtocol) objects[0];
                if (type == ComputerProtocol.MODE) {
                    terminalMode = ((Integer) objects[1]) == 0 ? false : true;
                } else if (type == ComputerProtocol.DISPLAY) {
                    lastWidth = displayWidth;
                    lastHeight = displayHeight;
                    displayWidth = (Integer) objects[2];
                    displayHeight = (Integer) objects[3];

                    if (displayBuffer == null || displayBuffer.capacity() < (displayWidth * displayHeight * 3))
                        displayBuffer = ByteBuffer.allocateDirect(displayWidth * displayHeight * 3);

                    if (((ByteBuffer) objects[1]).capacity() * 3 != (displayWidth * displayHeight * 3)) {
                        ElectroCraft.instance.getLogger().severe("Error! Got corupted screen packet!");
                        return null;
                    }

                    ((ByteBuffer) objects[1]).rewind();
                    displayBuffer.rewind();
                    for (int i = 0; i < ((ByteBuffer) objects[1]).capacity(); i++) {
                        int rgb = ElectroCraft.colorPalette[((ByteBuffer) objects[1]).get() & 0xFF];
                        byte red = (byte) ((rgb >> 16) & 0xFF);
                        byte green = (byte) ((rgb >> 8) & 0xFF);
                        byte blue = (byte) (rgb & 0xFF);
                        displayBuffer.put(red);
                        displayBuffer.put(green);
                        displayBuffer.put(blue);
                    }
                    displayBuffer.rewind();
                    shouldAskForScreenPacket = true;
                } else if (type == ComputerProtocol.TERMINAL) {
                    rows = (Integer) objects[2];
                    columns = (Integer) objects[3];
                    currentCol = (Integer) objects[4];
                    currentRow = (Integer) objects[5];
                    if (((Integer) objects[1]) == 0) {
                        Object[] data = (Object[]) objects[6];
                        int rowNumber = (Integer) data[0];
                        String rowData = (String) data[1];
                        terminalList.put(rowNumber, rowData);
                    } else if (((Integer) objects[1]) == 1) {
                        Object[] data = (Object[]) objects[6];
                        int numberOfRowsChanged = (Integer) data[0];
                        ObjectPair<Integer, String>[] changedRows = (ObjectPair[]) data[1];

                        for (int i = 0; i < numberOfRowsChanged; i++) {
                            terminalList.put(changedRows[i].getValue1(), changedRows[i].getValue2());
                        }
                    }
                }
            }
            return null;
        }
    }
}
