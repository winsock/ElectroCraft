package info.cerios.electrocraft.gui;

import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiNetworkAddressScreen extends GuiScreen {

    private GuiTextField controlLineTextField, dataLineTextField;
    private int xSize = 176, ySize = 100;
    private NetworkBlock device;
    private boolean isSmp = false;
    private NetworkAddressPacket packetInProgress;
    private ResourceLocation genericWindowResource = new ResourceLocation("electrocraft", "textures/gui/genericWindow.png");

    public GuiNetworkAddressScreen(NetworkAddressPacket packetInProgress) {
        isSmp = true;
        this.packetInProgress = packetInProgress;
    }

    @Override
    public void initGui() {
        controlLineTextField = new GuiTextField(0, fontRendererObj, (this.width / 2) - (xSize / 2) + 100, (this.height / 2) - (ySize / 2) + 30, 50, 15);
        if (!isSmp)
            controlLineTextField.setText(Integer.toString(device.getControlAddress()));
        else
            controlLineTextField.setText(Integer.toString(packetInProgress.getControlAddress()));
        dataLineTextField = new GuiTextField(1, fontRendererObj, (this.width / 2) - (xSize / 2) + 100, (this.height / 2) - (ySize / 2) + 60, 50, 15);
        if (!isSmp)
            dataLineTextField.setText(Integer.toString(device.getDataAddress()));
        else
            dataLineTextField.setText(Integer.toString(packetInProgress.getDataAddress()));
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        Tessellator tess = Tessellator.getInstance();

        // Draw the background
        mc.renderEngine.bindTexture(genericWindowResource);
        tess.getWorldRenderer().startDrawingQuads();
        tess.getWorldRenderer().addVertexWithUV((this.width / 2) - (xSize / 2), (this.height / 2) - (ySize / 2), 0, 0, 0);
        tess.getWorldRenderer().addVertexWithUV((this.width / 2) - (xSize / 2), (this.height / 2) + (ySize / 2), 0, 1, 0);
        tess.getWorldRenderer().addVertexWithUV((this.width / 2) + (xSize / 2), (this.height / 2) + (ySize / 2), 0, 1, 1);
        tess.getWorldRenderer().addVertexWithUV((this.width / 2) + (xSize / 2), (this.height / 2) - (ySize / 2), 0, 0, 1);
        tess.draw();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        drawCenteredString(fontRendererObj, "Networked Device Addresses", (this.width / 2), (this.height / 2) - (ySize / 2) + 5, 0xFFFFFF);

        controlLineTextField.drawTextBox();
        dataLineTextField.drawTextBox();

        fontRendererObj.drawString("Control Address: ", (this.width / 2) - (xSize / 2) + 5, (this.height / 2) - (ySize / 2) + 33, 0x404040);
        fontRendererObj.drawString("Data Address: ", (this.width / 2) - (xSize / 2) + 20, (this.height / 2) - (ySize / 2) + 63, 0x404040);
    }

    @Override
    public void onGuiClosed() {
        try {
            int controlAddress = Integer.parseInt(controlLineTextField.getText());
            int dataAddress = Integer.parseInt(dataLineTextField.getText());
            if (!isSmp) {
                device.setControlAddress(controlAddress);
            } else {
                packetInProgress.setControlAddress(controlAddress);
            }
            if (!isSmp) {
                device.setDataAddress(dataAddress);
            } else {
                packetInProgress.setDataAddress(dataAddress);
            }
            if (isSmp) {
                ElectroCraft.instance.getNetworkWrapper().sendToServer(packetInProgress);
            }
        } catch (NumberFormatException e) {
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException {
        super.mouseClicked(par1, par2, par3);
        controlLineTextField.mouseClicked(par1, par2, par3);
        dataLineTextField.mouseClicked(par1, par2, par3);
    }

    @Override
    protected void keyTyped(char key, int keyCode) throws IOException {
        super.keyTyped(key, keyCode);
        controlLineTextField.textboxKeyTyped(key, keyCode);
        dataLineTextField.textboxKeyTyped(key, keyCode);
    }
}
