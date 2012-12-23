package info.cerios.electrocraft.api.drone.tools;

import info.cerios.electrocraft.core.entites.EntityDrone;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IDroneTool {
    public boolean isRightTool(ItemStack item);

    public boolean appliesToBlock(ItemStack item, Block block, int metadata);

    public List<ItemStack> preformAction(ItemStack item, EntityDrone drone, World world, int x, int y, int z);

    public void damageItem(EntityDrone drone, ItemStack item, Block block, int metadata);
}
