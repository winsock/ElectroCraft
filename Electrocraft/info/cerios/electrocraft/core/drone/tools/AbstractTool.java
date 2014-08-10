package info.cerios.electrocraft.core.drone.tools;

import info.cerios.electrocraft.api.drone.tools.IDroneTool;
import info.cerios.electrocraft.core.entites.EntityDrone;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class AbstractTool implements IDroneTool {

    @Override
    public boolean appliesToBlock(ItemStack item, Block block, int metadata) {
        return ForgeHooks.isToolEffective(item, block, metadata);
    }

    @Override
    public void damageItem(EntityDrone drone, ItemStack item, Block block, int metadata) {
        if (item.getItem().isItemTool(item)) {
            item.damageItem(1, drone);
        }
    }

    @Override
    public List<ItemStack> preformAction(ItemStack item, EntityDrone drone, World world, int x, int y, int z) {
        List<ItemStack> stacks = drone.getBlockDropped(world, x, y, z);
        world.setBlockToAir(x, y, z);
        return stacks;
    }

    @Override
    public boolean isRightTool(ItemStack item) {
        return true;
    }
}
