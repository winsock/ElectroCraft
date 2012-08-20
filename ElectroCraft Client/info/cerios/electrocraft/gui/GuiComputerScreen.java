package info.cerios.electrocraft.gui;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import info.cerios.electrocraft.ElectroCraftClient;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.*;
import info.cerios.electrocraft.core.network.ComputerInputPacket;
import info.cerios.electrocraft.core.network.ComputerProtocol;
import info.cerios.electrocraft.core.utils.ObjectPair;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Tessellator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class GuiComputerScreen extends GuiScreen implements IComputerCallback {

	public static final int CLOSE_BUTTON = 0;

	private int displayTextureId;
	private boolean repeatEventsOldState = Keyboard.areRepeatEventsEnabled();
	private volatile boolean isVisible = true;
	private int lastWidth, lastHeight, displayWidth, displayHeight;
	private ByteBuffer displayBuffer;
	private boolean terminalMode = true;
	private Map<Integer, String> terminalList = new HashMap<Integer, String>();
	private int rows = 24;
	private int columns = 80;
	private boolean shouldAskForScreenPacket = true;
	private Object syncObject = new Object();

	public GuiComputerScreen() {
		Keyboard.enableRepeatEvents(true);
		ElectroCraftClient.instance.getComputerClient().registerCallback(ComputerProtocol.DISPLAY, this);
		ElectroCraftClient.instance.getComputerClient().registerCallback(ComputerProtocol.TERMINAL, this);
		ElectroCraftClient.instance.getComputerClient().registerCallback(ComputerProtocol.TERMINAL_SIZE, this);
		ElectroCraftClient.instance.getComputerClient().registerCallback(ComputerProtocol.MODE, this);
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
				float pixelsPerLineHeight = ((halfScreenHeight * 2) / rows) + 3;

				float scaleFactorX = 1f;
				float scaleFactorY = 1f;
				if (fontRenderer.getStringWidth(line) > (halfScreenWidth * 2)) {
					scaleFactorX = (float) ((halfScreenWidth * 2) / fontRenderer.getStringWidth(line));
				}
				if ((fontRenderer.FONT_HEIGHT * columns) > (halfScreenHeight * 2)) {
					scaleFactorY = (float) ((halfScreenHeight * 2) / (fontRenderer.FONT_HEIGHT * rows));
				}
				GL11.glPushMatrix();
				GL11.glScalef(scaleFactorX, scaleFactorY, 1);
				this.fontRenderer.drawString(line, (int) ((1 / scaleFactorX) * ((width / 2) - halfScreenWidth)), (int) ((i * pixelsPerLineHeight) + ((1 / scaleFactorY) * ((height / 2) - halfScreenHeight))) + 2, 0xFFFFFF);
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
		fontRenderer.drawString("xEC Computer System", 20, 10, 0x404040);

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
						ElectroCraftClient.instance.getComputerClient().sendPacket(ComputerProtocol.DISPLAY);
						shouldAskForScreenPacket = false;
					} else {
						for (int i = 0; i < rows; i++) {
							ElectroCraftClient.instance.getComputerClient().sendTerminalPacket(i);
							shouldAskForScreenPacket = false;
						}
					}
				} catch (IOException e) {
					ElectroCraft.instance.getLogger().severe("Unable to send screen update packet!");
				}
			}
		}
	}

	@Override
	public void onGuiClosed() {
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
			boolean down;
			if (Keyboard.getEventKeyState()) {
				down = true;
			} else {
				down = false;
			}
			ComputerInputPacket inputPacket = new ComputerInputPacket();
			inputPacket.setEventKey(Keyboard.getEventCharacter());
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

	@Override
	public Object onTaskComplete(Object... objects) {
		synchronized (syncObject) {
			if (objects[0] instanceof ComputerProtocol) {
				ComputerProtocol type = (ComputerProtocol)objects[0];	
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
				} else if (type == ComputerProtocol.TERMINAL) {
					if (((Integer)objects[1]) == 0) {
						Object[] data = (Object[])objects[2];
						int rowNumber = (Integer)data[0];
						String rowData = (String)data[1];
						terminalList.put(rowNumber, rowData);
					} else if (((Integer)objects[1]) == 1) {
						Object[] data = (Object[])objects[2];
						int numberOfRowsChanged = (Integer) data[0];
						ObjectPair<Integer, String>[] changedRows = (ObjectPair[])data[1];

						for (int i = 0; i < numberOfRowsChanged; i++) {
							terminalList.put(changedRows[i].getValue1(), changedRows[i].getValue2());
						}
					}
				} else if (type == ComputerProtocol.TERMINAL_SIZE) {
					rows = (Integer)objects[1];
					columns = (Integer)objects[2];
				}

				shouldAskForScreenPacket = true;
			}
			return null;
		}
	}
}
