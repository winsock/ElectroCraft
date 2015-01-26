package me.querol.electrocraft.blocks.render.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class SerialCableModel extends ModelBase {
    // fields
    ModelRenderer Middle;
    ModelRenderer Right;
    ModelRenderer Left;
    ModelRenderer Back;
    ModelRenderer Front;
    ModelRenderer Top;
    ModelRenderer Bottom;

    public SerialCableModel() {
        Middle = new ModelRenderer(this, 0, 0);
        Middle.addBox(-1F, -1F, -1F, 2, 2, 2);
        Middle.setRotationPoint(-1F, 15F, -1F);
        Middle.setTextureSize(16, 16);
        Middle.mirror = true;
        setRotation(Middle, 0F, 0F, 0F);
        Right = new ModelRenderer(this, 21, 0);
        Right.addBox(-2F, 0F, 0F, 8, 2, 2);
        Right.setRotationPoint(2F, 14F, -2F);
        Right.setTextureSize(16, 16);
        Right.mirror = true;
        setRotation(Right, 0F, 0F, 0F);
        Left = new ModelRenderer(this, 21, 0);
        Left.addBox(-2F, 0F, 0F, 8, 2, 2);
        Left.setRotationPoint(-8F, 14F, -2F);
        Left.setTextureSize(16, 16);
        Left.mirror = true;
        setRotation(Left, 0F, 0F, 0F);
        Back = new ModelRenderer(this, 0, 11);
        Back.addBox(0F, 0F, -2F, 2, 2, 6);
        Back.setRotationPoint(-2F, 14F, 2F);
        Back.setTextureSize(16, 16);
        Back.mirror = true;
        setRotation(Back, 0F, 0F, 0F);
        Front = new ModelRenderer(this, 0, 11);
        Front.addBox(0F, 0F, -2F, 2, 2, 8);
        Front.setRotationPoint(-2F, 14F, -8F);
        Front.setTextureSize(16, 16);
        Front.mirror = true;
        setRotation(Front, 0F, 0F, 0F);
        Top = new ModelRenderer(this, 21, 11);
        Top.addBox(0F, -2F, 0F, 2, 8, 2);
        Top.setRotationPoint(-2F, 8F, -2F);
        Top.setTextureSize(16, 16);
        Top.mirror = true;
        setRotation(Top, 0F, 0F, 0F);
        Bottom = new ModelRenderer(this, 21, 11);
        Bottom.addBox(0F, -2F, 0F, 2, 8, 2);
        Bottom.setRotationPoint(-2F, 18F, -2F);
        Bottom.setTextureSize(16, 16);
        Bottom.mirror = true;
        setRotation(Bottom, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.renderMiddle();
        this.renderBottom();
        this.renderTop();
        this.renderLeft();
        this.renderRight();
        this.renderBack();
        this.renderFront();
    }

    public void renderMiddle() {
        Middle.render(0.0625F);
    }

    public void renderBottom() {
        Bottom.render(0.0625F);
    }

    public void renderTop() {
        Top.render(0.0625F);
    }

    public void renderLeft() {
        Left.render(0.0625F);
    }

    public void renderRight() {
        Right.render(0.0625F);
    }

    public void renderBack() {
        Back.render(0.0625F);
    }

    public void renderFront() {
        Front.render(0.0625F);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}