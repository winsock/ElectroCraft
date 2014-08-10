package info.cerios.electrocraft.blocks.render;

import info.cerios.electrocraft.blocks.render.models.SerialCableModel;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class SerialCableRenderer extends TileEntitySpecialRenderer {
    private SerialCableModel model;
    private ResourceLocation serialCableResource = new ResourceLocation("electrocraft", "blocks/serialCable");

    public SerialCableRenderer() {
        model = new SerialCableModel();
    }

    public void renderAModelAt(TileEntitySerialCable tileEntity, double d, double d1, double d2, float f) {
        // Texture file
        GL11.glPushMatrix();
        this.bindTexture(serialCableResource);
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);

        if (tileEntity.isConnectedInDirection(ForgeDirection.DOWN)) {
            model.renderBottom();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.UP)) {
            model.renderTop();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.NORTH)) {
            model.renderBack();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.SOUTH)) {
            model.renderFront();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.WEST)) {
            model.renderLeft();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.EAST)) {
            model.renderRight();
        }

        model.renderMiddle();
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8) {
        this.renderAModelAt((TileEntitySerialCable) tileEntity, var2, var4, var6, var8);
    }
}
