package info.cerios.electrocraft.blocks.render;

import org.lwjgl.opengl.GL11;

import info.cerios.electrocraft.blocks.render.models.SerialCableModel;
import info.cerios.electrocraft.core.blocks.BlockSerialCable;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class SerialCableRenderer extends TileEntitySpecialRenderer {
    private SerialCableModel model;

    public SerialCableRenderer()
    {
        model = new SerialCableModel();
    }
    
    public void renderAModelAt(TileEntitySerialCable tileEntity, double d, double d1, double d2, float f)
    {
        //Texture file
        GL11.glPushMatrix();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileEntity.getBlockType().blockIndexInTexture);
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);

        if (tileEntity.isConnectedInDirection(ForgeDirection.DOWN))
        {
            model.renderBottom();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.UP))
        {
            model.renderTop();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.NORTH))
        {
        	model.renderBack();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.SOUTH))
        {
        	model.renderFront();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.WEST))
        {
        	model.renderLeft();
        }

        if (tileEntity.isConnectedInDirection(ForgeDirection.EAST))
        {
        	model.renderRight();
        }

        model.renderMiddle();
        GL11.glPopMatrix();
    }
    
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8) {
        this.renderAModelAt((TileEntitySerialCable)tileEntity, var2, var4, var6, var8);
	}
}
