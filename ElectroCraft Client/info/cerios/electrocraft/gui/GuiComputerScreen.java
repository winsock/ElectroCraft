package info.cerios.electrocraft.gui;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.zip.DataFormatException;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.AbstractElectroCraftMod;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.XECInterface;
import info.cerios.electrocraft.core.computer.XECVGACard;
import info.cerios.electrocraft.core.network.ComputerInputPacket;
import info.cerios.electrocraft.core.network.ComputerProtocol;
import info.cerios.electrocraft.core.utils.Utils;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Tessellator;

public class GuiComputerScreen extends GuiScreen implements IComputerCallback {

	public static final int CLOSE_BUTTON = 0;

	private XECInterface computer;
	private XECVGACard videoCard;
	private int displayTextureId;
	private boolean repeatEventsOldState = Keyboard.areRepeatEventsEnabled();
	private volatile boolean isVisible = true;
	private boolean isSmpComputerScreen = false;
	private int lastWidth, lastHeight;
	private int ticksSinceLastScreenRequest = 0;
	private ByteBuffer displayBuffer;
	
	public GuiComputerScreen(XECInterface object) {
		this.computer = object;
		this.videoCard = computer.getVideoCard();
		Keyboard.enableRepeatEvents(true);
	}

	public GuiComputerScreen() {
		isSmpComputerScreen = true;
		Keyboard.enableRepeatEvents(true);
	}

	public void initGui() {
		controlList.add(new GuiButton(CLOSE_BUTTON, width - 5 - 4 - 17, 5 + 4, 17, 17, "X"));
		
		if (!isSmpComputerScreen) {
			// OpenGL Stuff
			if (GL11.glIsTexture(displayTextureId))
				GL11.glDeleteTextures(displayTextureId);

			displayBuffer = ByteBuffer.allocateDirect(videoCard.getScreenWidth() * videoCard.getScreenHeight() * 3);
			displayBuffer.put(videoCard.getScreenData());
			displayBuffer.rewind();
			displayTextureId = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, videoCard.getScreenWidth(), videoCard.getScreenHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, displayBuffer);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		Tessellator tess = Tessellator.instance;

		// Draw the background
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/info/cerios/electrocraft/gfx/computerscreen.png"));
		tess.startDrawingQuads();
		tess.addVertexWithUV(width - 5, 5, 0, 1, 0);
		tess.addVertexWithUV(5, 5, 0, 0, 0);
		tess.addVertexWithUV(5, height - 5, 0, 0, 1);
		tess.addVertexWithUV(width - 5, height - 5, 0, 1, 1);
		tess.draw();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		if (GL11.glIsTexture(displayTextureId)) {
			// Display the Video Card contents
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
			tess.startDrawingQuads();
			tess.addVertexWithUV(width - 30, 30, 0, 1, 0);
			tess.addVertexWithUV(30, 30, 0, 0, 0);
			tess.addVertexWithUV(30, height - 22, 0, 0, 1);
			tess.addVertexWithUV(width - 30, height - 22, 0, 1, 1);
			tess.draw();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}

		// Draw the screen title
		fontRenderer.drawString("JPC Terminal", 20, 10, 0x404040);

		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		mc.displayGuiScreen(null);
	}

	@Override
	public void updateScreen() {
		if (!isSmpComputerScreen){
			displayBuffer.clear();
			displayBuffer.put(videoCard.getScreenData());
			displayBuffer.rewind();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, videoCard.getScreenWidth(), videoCard.getScreenHeight(), GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, displayBuffer);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		} else {
			if (ticksSinceLastScreenRequest >= 1) {
				ticksSinceLastScreenRequest = 0;
			} else {
				ticksSinceLastScreenRequest++;
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

		while(Mouse.next()) {
			handleMouseInput();
		}

		if ((!isSmpComputerScreen)) {
			while (Keyboard.next()) {

			}
		} else if (isSmpComputerScreen) {
			while (Keyboard.next()) {
				int key = Keyboard.getEventKey();
				boolean down;
				if (Keyboard.getEventKeyState()) {
					down = true;
				} else {
					down = false;
				}

				ComputerInputPacket inputPacket = new ComputerInputPacket();
				inputPacket.setEventKey(key);
				inputPacket.setMouseDeltas(Mouse.getDX(), Mouse.getDY(), Mouse.getDWheel());
				inputPacket.setEventMouseButton(Mouse.getEventButton());
				inputPacket.setWasKeyDown(down);
				try {
					ModLoader.sendPacket(inputPacket.getMCPacket());
				} catch (IOException e) {
					FMLCommonHandler.instance().getFMLLogger().fine("ElectroCraft: Unable to send computer input data!");
				}

			}
		}
	}

	@Override
	public void onTaskComplete(Object... objects) {
		if (objects[0] instanceof Object[]) {
			int width = (Integer) ((Object[])objects[0])[1];
			int height = (Integer) ((Object[])objects[0])[2];
			if (!GL11.glIsTexture(displayTextureId) || (width != lastWidth || height != lastHeight)) {
				if (GL11.glIsTexture(displayTextureId))
					GL11.glDeleteTextures(displayTextureId);
				displayTextureId = GL11.glGenTextures();
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) ((Object[])objects[0])[0]);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			} else {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
				GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) ((Object[])objects[0])[0]);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			}
			
			lastWidth = width;
			lastHeight = height;
		}
	}
}
