package info.cerios.electrocraft.gui;

import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.jpc.emulator.motherboard.IOPortHandler;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.Tessellator;

public class GuiNetworkAddressScreen extends GuiScreen {
	
	private GuiTextField controlLineTextField, dataLineTextField;
	private int xSize = 176, ySize = 100;
	private NetworkBlock device;

	public GuiNetworkAddressScreen(NetworkBlock device) {
		this.device = device;
	}
	
	@Override
	public void initGui() {
		controlLineTextField = new GuiTextField(fontRenderer, (this.width / 2) - (xSize / 2) + 100, (this.height / 2) - (ySize / 2) + 30, 50, 15);
		controlLineTextField.setText(Integer.toString(device.ioPortsRequested()[0]));
		dataLineTextField = new GuiTextField(fontRenderer, (this.width / 2) - (xSize / 2) + 100, (this.height / 2) - (ySize / 2) + 60, 50, 15);
		dataLineTextField.setText(Integer.toString(device.ioPortsRequested()[1]));
	}
	
	@Override
    public void drawScreen(int par1, int par2, float par3) {
		Tessellator tess = Tessellator.instance;

        // Draw the background
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/info/cerios/electrocraft/gfx/genericWindow.png"));
        tess.startDrawingQuads();
        tess.addVertexWithUV((this.width / 2) - (xSize / 2), (this.height / 2) - (ySize / 2), 0, 0, 0);
        tess.addVertexWithUV((this.width / 2) - (xSize / 2), (this.height / 2) + (ySize / 2), 0, 1, 0);
        tess.addVertexWithUV((this.width / 2) + (xSize / 2), (this.height / 2) + (ySize / 2), 0, 1, 1);
        tess.addVertexWithUV((this.width / 2) + (xSize / 2), (this.height / 2) - (ySize / 2), 0, 0, 1);
        tess.draw();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        
        drawCenteredString(fontRenderer, "Networked Device Addresses", (this.width / 2), (this.height / 2) - (ySize / 2) + 5, 0xFFFFFF);
        
        controlLineTextField.drawTextBox();
        dataLineTextField.drawTextBox();
        
        fontRenderer.drawString("Control Address: ", (this.width / 2) - (xSize / 2) + 5, (this.height / 2) - (ySize / 2) + 33, 0x404040);
        fontRenderer.drawString("Data Address: ", (this.width / 2) - (xSize / 2) + 20, (this.height / 2) - (ySize / 2) + 63, 0x404040);
	}
	
	@Override
	public void onGuiClosed() {
		try {
			int controlAddress = Integer.parseInt(controlLineTextField.getText());
			int dataAddress = Integer.parseInt(dataLineTextField.getText());
			if (controlAddress > 223 && controlAddress < IOPortHandler.MAX_IOPORTS) {
				device.setControlAddress(controlAddress);
			}
			if (dataAddress > 223 && dataAddress < IOPortHandler.MAX_IOPORTS) {
				device.setDataAddress(dataAddress);
			}
		} catch (NumberFormatException e) {}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
        return false;
    }
	
	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		controlLineTextField.mouseClicked(par1, par2, par3);
		dataLineTextField.mouseClicked(par1, par2, par3);
	}
	
	@Override
	protected void keyTyped(char key, int keyCode) {
		super.keyTyped(key, keyCode);
		controlLineTextField.textboxKeyTyped(key, keyCode);
		dataLineTextField.textboxKeyTyped(key, keyCode);
	}
}