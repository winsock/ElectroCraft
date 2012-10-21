package info.cerios.electrocraft.entites;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D;
import info.cerios.electrocraft.core.entites.EntityDrone;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EnumAction;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ModelBase;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderLiving;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

public class RenderDrone extends RenderLiving {

	public RenderDrone(ModelBase par1ModelBase, float par2) {
		super(par1ModelBase, par2);
	}

	@Override
    protected void renderModel(EntityLiving par1EntityLiving, float par2, float par3, float par4, float par5, float par6, float par7) {
		this.loadTexture("/info/cerios/electrocraft/gfx/Drone.png");
        this.mainModel.render(par1EntityLiving, par2, par3, par4, par5, par6, par7);
    }
	
	@Override
    protected void renderEquippedItems(EntityLiving par1EntityLiving, float par2) {
		GL11.glRotatef(par1EntityLiving.rotationYaw - par1EntityLiving.renderYawOffset, 0, 1, 0);
    	if (par1EntityLiving.getHeldItem() != null) {
            // Item render code *Copied from RenderPlayer*
    		GL11.glRotatef(180, 1, 0, 0);
    		GL11.glRotatef(-50f, 0, 1, 0);
    		GL11.glRotatef(90, 0, 1, 0);
    		GL11.glTranslatef(0, -1, 0);
        	GL11.glRotatef(10 - ((EntityDrone)par1EntityLiving).getRotationTicks(), 1, 0, 1);
        	GL11.glTranslatef(0.0625f, 0, 0);

        	if (par1EntityLiving.getHeldItem().getItem().requiresMultipleRenderPasses())
        	{
        		for (int var25 = 0; var25 < par1EntityLiving.getHeldItem().getItem().getRenderPasses(par1EntityLiving.getHeldItem().getItemDamage()); ++var25)
        		{
        			int var24 = par1EntityLiving.getHeldItem().getItem().getColorFromDamage(par1EntityLiving.getHeldItem().getItemDamage(), var25);
        			float var26 = (float)(var24 >> 16 & 255) / 255.0F;
        			float var9 = (float)(var24 >> 8 & 255) / 255.0F;
        			float var10 = (float)(var24 & 255) / 255.0F;
        			GL11.glColor4f(var26, var9, var10, 1.0F);
        			this.renderManager.itemRenderer.renderItem(par1EntityLiving, par1EntityLiving.getHeldItem(), var25);
        		}
        	}
        	else
        	{
        		this.renderManager.itemRenderer.renderItem(par1EntityLiving, par1EntityLiving.getHeldItem(), 0);
        	}
        }
    }
}
