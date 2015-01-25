package info.cerios.electrocraft.blocks.render;

import info.cerios.electrocraft.blocks.render.models.SerialCableModel;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class SerialCableRenderer extends TileEntitySpecialRenderer {
    private SerialCableModel model;
    private ResourceLocation serialCableResource = new ResourceLocation("electrocraft", "blocks/serialCable");

    public SerialCableRenderer() {
        model = new SerialCableModel();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double posX, double posZ, double posY, float var7, int var8) {
        if (tileEntity instanceof  TileEntitySerialCable)
            this.renderAModelAt((TileEntitySerialCable) tileEntity, posX, posZ, posY, var7);
    }

    public void renderAModelAt(TileEntitySerialCable tileEntity, double d, double d1, double d2, float f) {
        // Texture file
        GL11.glPushMatrix();
        this.bindTexture(serialCableResource);
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);

        if (tileEntity.isConnectedInDirection(EnumFacing.DOWN)) {
            model.renderBottom();
        }

        if (tileEntity.isConnectedInDirection(EnumFacing.UP)) {
            model.renderTop();
        }

        if (tileEntity.isConnectedInDirection(EnumFacing.NORTH)) {
            model.renderBack();
        }

        if (tileEntity.isConnectedInDirection(EnumFacing.SOUTH)) {
            model.renderFront();
        }

        if (tileEntity.isConnectedInDirection(EnumFacing.WEST)) {
            model.renderLeft();
        }

        if (tileEntity.isConnectedInDirection(EnumFacing.EAST)) {
            model.renderRight();
        }

        model.renderMiddle();
        GL11.glPopMatrix();
    }
}
