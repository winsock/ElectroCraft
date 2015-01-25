package info.cerios.electrocraft.entites;

import info.cerios.electrocraft.core.entites.EntityDrone;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.opengl.GL11;

public class RenderDrone extends RenderLiving {

    private ResourceLocation droneTexture = new ResourceLocation("electrocraft", "textures/models/drone");

    public RenderDrone(RenderManager renderManager, ModelBase par1ModelBase, float par2) {
        super(renderManager, par1ModelBase, par2);
        addLayer(new RenderDroneItem(this));
    }

    @Override
    protected void renderModel(EntityLivingBase par1EntityLiving, float par2, float par3, float par4, float par5, float par6, float par7) {
        this.mainModel.render(par1EntityLiving, par2, par3, par4, par5, par6, par7);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return droneTexture;
    }
}
