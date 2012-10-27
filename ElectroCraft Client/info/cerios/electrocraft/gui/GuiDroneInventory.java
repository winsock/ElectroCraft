package info.cerios.electrocraft.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;

import info.cerios.electrocraft.core.container.ContainerDrone;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;

public class GuiDroneInventory extends GuiContainer {

	public GuiDroneInventory(ContainerDrone container) {
		super(container);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		GuiPacket packet = new GuiPacket();
		packet.setCloseWindow(true);
		packet.setGui(Gui.COMPUTER_SCREEN);
		try {
			PacketDispatcher.sendPacketToServer(packet.getMCPacket());
		} catch (IOException e) {
			// Oh well we tried
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int texId = mc.renderEngine.getTexture("/info/cerios/electrocraft/gfx/droneinv.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(texId);
        int startX = (width - xSize) / 2;
        int startY = (height - ySize) / 2;
        drawTexturedModalRect(startX, startY, 0, 0, xSize, ySize);
	}
}
