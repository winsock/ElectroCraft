package info.cerios.electrocraft.entites;

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
		GL11.glPushMatrix();
		this.loadTexture("/info/cerios/electrocraft/gfx/Drone.png");
        GL11.glScalef(1.0f, 1.0F, 1.0F);
        this.mainModel.render(par1EntityLiving, par2, par3, par4, par5, par6, par7);
        GL11.glPopMatrix();
    }
}
