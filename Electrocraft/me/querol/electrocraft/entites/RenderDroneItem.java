package me.querol.electrocraft.entites;

import me.querol.electrocraft.core.entites.EntityDrone;
import me.querol.electrocraft.core.entites.EntityDrone;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

/**
 * Created by winsock on 1/25/15.
 */
public class RenderDroneItem extends LayerHeldItem {
    public RenderDroneItem(RendererLivingEntity p_i46115_1_) {
        super(p_i46115_1_);
    }

    @Override
    public void doRenderLayer(EntityLivingBase par1EntityLiving, float p_177141_2_, float p_177141_3_, float p_177141_4_, float p_177141_5_, float p_177141_6_, float p_177141_7_, float p_177141_8_) {
        GL11.glRotatef(par1EntityLiving.rotationYaw - par1EntityLiving.renderYawOffset, 0, 1, 0);
        if (((EntityDrone) par1EntityLiving).getInventoryInterface().tools[0] != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.115f, 0.60f, 0.17f);
            GL11.glScalef(0.25f, 0.25f, 0.25f);
            GL11.glRotatef(110, 1, 0, 0);
            GL11.glRotatef(60, 0, 1, 0);
            GL11.glRotatef(110, 1, 1, 0);
            GL11.glRotatef(-52, 1, 1, 1);
            FMLClientHandler.instance().getClient().getRenderItem().renderItemModelForEntity(((EntityDrone) par1EntityLiving).getInventoryInterface().tools[0], par1EntityLiving, ItemCameraTransforms.TransformType.NONE);
            GL11.glPopMatrix();
        }
        if (((EntityDrone) par1EntityLiving).getInventoryInterface().tools[2] != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef(-.385f, 0.60f, 0.17f);
            GL11.glScalef(0.25f, 0.25f, 0.25f);
            GL11.glRotatef(110, 1, 0, 0);
            GL11.glRotatef(60, 0, 1, 0);
            GL11.glRotatef(110, 1, 1, 0);
            GL11.glRotatef(-52, 1, 1, 1);
            FMLClientHandler.instance().getClient().getRenderItem().renderItemModelForEntity(((EntityDrone) par1EntityLiving).getInventoryInterface().tools[2], par1EntityLiving, ItemCameraTransforms.TransformType.NONE);
            GL11.glPopMatrix();
        }
        if (par1EntityLiving.getHeldItem() != null) {
            GL11.glRotatef(180, 1, 0, 0);
            GL11.glRotatef(-50f, 0, 1, 0);
            GL11.glRotatef(90, 0, 1, 0);
            GL11.glTranslatef(0, -1, 0);
            GL11.glRotatef(10 - ((EntityDrone) par1EntityLiving).getRotationTicks(), 1, 0, 1);
            GL11.glTranslatef(0.0625f, 0, 0);
            FMLClientHandler.instance().getClient().getRenderItem().renderItemModelForEntity(par1EntityLiving.getHeldItem(), par1EntityLiving, ItemCameraTransforms.TransformType.NONE);
        }
    }
}
