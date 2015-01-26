package me.querol.electrocraft.api.drone.tools;

import me.querol.electrocraft.core.entites.EntityDrone;

import java.util.List;

import me.querol.electrocraft.core.entites.EntityDrone;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IDroneTool {
    public boolean isRightTool(ItemStack item);

    public boolean appliesToBlock(IBlockAccess world, ItemStack item, BlockPos blockPos);

    public List<ItemStack> preformAction(ItemStack item, EntityDrone drone, World world, BlockPos pos);

    public void damageItem(EntityDrone drone, ItemStack item, IBlockState blockState);
}
