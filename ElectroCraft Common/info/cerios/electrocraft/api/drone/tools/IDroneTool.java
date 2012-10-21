package info.cerios.electrocraft.api.drone.tools;

import java.util.List;

import info.cerios.electrocraft.core.entites.EntityDrone;
import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public interface IDroneTool {
	public boolean isRightTool(ItemStack item);
	public boolean appliesToBlock(ItemStack item, Block block, int metadata);
	public List<ItemStack> preformAction(EntityDrone drone, World world, int x, int y, int z);
	public void damageItem(EntityDrone drone, ItemStack item, Block block, int metadata);
}
