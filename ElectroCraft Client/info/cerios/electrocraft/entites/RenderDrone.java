package info.cerios.electrocraft.entites;

import info.cerios.electrocraft.core.entites.EntityDrone;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.ModelBase;
import net.minecraft.src.RenderLiving;

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
		if (((EntityDrone)par1EntityLiving).getInventory().tools[0] != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(0.115f, 0.60f, 0.17f);
			GL11.glScalef(0.25f, 0.25f, 0.25f);
			GL11.glRotatef(110, 1, 0, 0);
			GL11.glRotatef(60, 0, 1, 0);
			GL11.glRotatef(110, 1, 1, 0);
			GL11.glRotatef(-52, 1, 1, 1);
			this.renderManager.itemRenderer.renderItem(par1EntityLiving, ((EntityDrone)par1EntityLiving).getInventory().tools[0], 0);
			GL11.glPopMatrix();
		}
		if (((EntityDrone)par1EntityLiving).getInventory().tools[2] != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-.385f, 0.60f, 0.17f);
			GL11.glScalef(0.25f, 0.25f, 0.25f);
			GL11.glRotatef(110, 1, 0, 0);
			GL11.glRotatef(60, 0, 1, 0);
			GL11.glRotatef(110, 1, 1, 0);
			GL11.glRotatef(-52, 1, 1, 1);
			this.renderManager.itemRenderer.renderItem(par1EntityLiving, ((EntityDrone)par1EntityLiving).getInventory().tools[2], 0);
			GL11.glPopMatrix();
		}
    	if (par1EntityLiving.getHeldItem() != null) {
            // Item render code *Copied from RenderPlayer*
    		GL11.glRotatef(180, 1, 0, 0);
    		GL11.glRotatef(-50f, 0, 1, 0);
    		GL11.glRotatef(90, 0, 1, 0);
    		GL11.glTranslatef(0, -1, 0);
        	GL11.glRotatef(10 - ((EntityDrone)par1EntityLiving).getRotationTicks(), 1, 0, 1);
        	GL11.glTranslatef(0.0625f, 0, 0);

        	if (par1EntityLiving.getHeldItem().getItem().requiresMultipleRenderPasses()) {
        		for (int var25 = 0; var25 < par1EntityLiving.getHeldItem().getItem().getRenderPasses(par1EntityLiving.getHeldItem().getItemDamage()); ++var25) {
        			// func_82790_a used to be getColorFromDamage
        			int var24 = par1EntityLiving.getHeldItem().getItem().func_82790_a(par1EntityLiving.getHeldItem(), var25);
        			float var26 = (var24 >> 16 & 255) / 255.0F;
        			float var9 = (var24 >> 8 & 255) / 255.0F;
        			float var10 = (var24 & 255) / 255.0F;
        			GL11.glColor4f(var26, var9, var10, 1.0F);
        			this.renderManager.itemRenderer.renderItem(par1EntityLiving, par1EntityLiving.getHeldItem(), var25);
        		}
        	}
        	else {
        		this.renderManager.itemRenderer.renderItem(par1EntityLiving, par1EntityLiving.getHeldItem(), 0);
        	}
        }
    }
}
