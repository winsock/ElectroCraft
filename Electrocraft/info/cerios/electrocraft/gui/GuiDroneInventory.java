package info.cerios.electrocraft.gui;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.container.ContainerDrone;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;

import net.minecraft.client.gui.inventory.GuiContainer;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiDroneInventory extends GuiContainer {

    private ResourceLocation droneInventoryScreen = new ResourceLocation("electrocraft", "textures/gui/droneinv.png");
    public GuiDroneInventory(ContainerDrone container) {
        super(container);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GuiPacket packet = new GuiPacket();
        packet.setCloseWindow(true);
        packet.setGui(Gui.COMPUTER_SCREEN);
        ElectroCraft.instance.getNetworkWrapper().sendToServer(packet);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(droneInventoryScreen);
        int startX = (width - xSize) / 2;
        int startY = (height - ySize) / 2;
        drawTexturedModalRect(startX, startY, 0, 0, xSize, ySize);
    }
}
