// Date: 10/16/2012 7:26:36 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package info.cerios.electrocraft.entites;

import info.cerios.electrocraft.core.entites.EntityDrone;
import net.minecraft.src.Entity;
import net.minecraft.src.ModelBase;
import net.minecraft.src.ModelRenderer;

public class ModelDrone extends ModelBase
{
	//fields
	ModelRenderer DroneBody;
	ModelRenderer DroneBody1;
	ModelRenderer BodyConnector;

	public ModelDrone() {
		textureWidth = 64;
		textureHeight = 64;

		DroneBody = new ModelRenderer(this, 0, 3);
		DroneBody.addBox(-7F, -7F, -7F, 14, 14, 6);
		DroneBody.setRotationPoint(0F, 17F, 0F);
		DroneBody.setTextureSize(64, 64);
		DroneBody.mirror = true;
		setRotation(DroneBody, 0F, -1.570796F, 0F);
		DroneBody1 = new ModelRenderer(this, 0, 3);
		DroneBody1.addBox(-7F, -7F, 1F, 14, 14, 6);
		DroneBody1.setRotationPoint(0F, 17F, 0F);
		DroneBody1.setTextureSize(64, 64);
		DroneBody1.mirror = true;
		setRotation(DroneBody1, 0F, -1.570796F, 0F);
		BodyConnector = new ModelRenderer(this, 0, 0);
		BodyConnector.addBox(-7F, 6F, -1F, 14, 1, 2);
		BodyConnector.setRotationPoint(0F, 17F, 0F);
		BodyConnector.setTextureSize(64, 64);
		BodyConnector.mirror = true;
		setRotation(BodyConnector, 0F, -1.570796F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		setRotationAngles(entity.rotationYaw, ((EntityDrone)entity).renderYawOffset, f2, f3, f4, f5);
		DroneBody.render(f5);
		DroneBody1.render(f5);
		BodyConnector.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5) {
		DroneBody.rotateAngleY = (f - f1) * ((float)Math.PI / 180f);
		DroneBody.rotateAngleX = 0;
		DroneBody1.rotateAngleY = (f - f1) * ((float)Math.PI / 180f);
		DroneBody1.rotateAngleX = 0;
		BodyConnector.rotateAngleY = (f - f1) * ((float)Math.PI / 180f);
		BodyConnector.rotateAngleX = 0;
	}
}