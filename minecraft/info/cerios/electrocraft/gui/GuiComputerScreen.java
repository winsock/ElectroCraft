package info.cerios.electrocraft.gui;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.jpc.emulator.PC;
import info.cerios.electrocraft.core.jpc.emulator.pci.peripheral.DefaultVGACard;
import info.cerios.electrocraft.core.jpc.emulator.pci.peripheral.VGACard;
import info.cerios.electrocraft.core.jpc.j2se.KeyMapping;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Tessellator;

public class GuiComputerScreen extends GuiScreen {
	
	public static final int CLOSE_BUTTON = 0;
	
	private PC computer;
	private DefaultVGACard vgaCard;
	private int displayTextureId;
	private info.cerios.electrocraft.core.jpc.emulator.peripheral.Keyboard keyboard;
	private boolean repeatEventsOldState = Keyboard.areRepeatEventsEnabled();
	
	public GuiComputerScreen(PC computer) {
		this.computer = computer;
		this.vgaCard = (DefaultVGACard)computer.getComponent(VGACard.class);
		this.keyboard = (info.cerios.electrocraft.core.jpc.emulator.peripheral.Keyboard) computer.getComponent(info.cerios.electrocraft.core.jpc.emulator.peripheral.Keyboard.class);
		Keyboard.enableRepeatEvents(true);
	}
	
	public void initGui() {
		controlList.add(new GuiButton(CLOSE_BUTTON, width - 5 - 4 - 17, 5 + 4, 17, 17, "X"));
		
		// Get the GPU ready for us
		updateScreen();
		
		displayTextureId = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, vgaCard.getDisplaySize().width, vgaCard.getDisplaySize().height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, vgaCard.getByteBuffer());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
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

        // Display the Video Card contents
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, vgaCard.getDisplaySize().width, vgaCard.getDisplaySize().height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, vgaCard.getByteBuffer());
		tess.startDrawingQuads();
		tess.addVertexWithUV(width - 30, 30, 0, 1, 0);
		tess.addVertexWithUV(30, 30, 0, 0, 0);
		tess.addVertexWithUV(30, height - 22, 0, 0, 1);
		tess.addVertexWithUV(width - 30, height - 22, 0, 1, 1);
		tess.draw();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		// Draw the screen title
		fontRenderer.drawString("JPC Laptop", 20, 10, 0x404040);
		
		super.drawScreen(par1, par2, par3);
	}
	
	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		mc.displayGuiScreen(null);
	}
	
	@Override
	public void updateScreen() {
		if (!mod_ElectroCraft.instance.getComputerHandler().isComputerRunning(computer))
			mc.displayGuiScreen(null);
		// Update Video Card
		vgaCard.prepareUpdate();
        vgaCard.updateDisplay();
	}

	@Override
    public void onGuiClosed() {
		Keyboard.enableRepeatEvents(repeatEventsOldState);
		GL11.glDeleteTextures(displayTextureId);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
        return false;
    }
	
	@Override
	public void handleInput() {
		keyboard.putMouseEvent(Mouse.getDX(), Mouse.getDY(), Mouse.getDWheel(), Mouse.getEventButton());

		while(Mouse.next()) {
			handleMouseInput();
		}
		
        while (Keyboard.next())
        {
        	if (Keyboard.getEventKeyState()) {
    			keyboard.keyPressed((byte)Keyboard.getEventKey());
    		} else {
    	        keyboard.keyReleased((byte)Keyboard.getEventKey());
    		}
        }
	}
}
