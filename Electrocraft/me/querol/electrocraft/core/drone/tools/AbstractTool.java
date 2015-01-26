package me.querol.electrocraft.core.drone.tools;

import me.querol.electrocraft.api.drone.tools.IDroneTool;
import me.querol.electrocraft.core.entites.EntityDrone;

import java.util.List;

import me.querol.electrocraft.core.entites.EntityDrone;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class AbstractTool implements IDroneTool {

    @Override
    public boolean appliesToBlock(IBlockAccess world, ItemStack item, BlockPos blockPos) {
        return ForgeHooks.isToolEffective(world, blockPos, item);
    }

    @Override
    public void damageItem(EntityDrone drone, ItemStack item, IBlockState blockState) {
        if (item.getItem().isItemTool(item)) {
            item.damageItem(1, drone);
        }
    }

    @Override
    public List<ItemStack> preformAction(ItemStack item, EntityDrone drone, World world, BlockPos pos) {
        List<ItemStack> stacks = drone.getBlockDropped(world, pos);
        world.setBlockToAir(pos);
        return stacks;
    }

    @Override
    public boolean isRightTool(ItemStack item) {
        return true;
    }
}
