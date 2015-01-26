package me.querol.electrocraft.core.blocks.tileentities;

import net.minecraft.util.EnumFacing;

public interface IDirectionalBlock {
    public void setDirection(EnumFacing direction);

    public EnumFacing getDirection();
}
