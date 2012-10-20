package info.cerios.electrocraft.core.drone.tools;

import java.util.List;

import info.cerios.electrocraft.core.entites.EntityDrone;
import net.minecraft.src.Block;
import net.minecraft.src.ItemPickaxe;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeHooks;

public class AbstractTool implements IDroneTool {
	
	@Override
	public boolean breakBlock(ItemStack item, Block block, int metadata) {
		return ForgeHooks.isToolEffective(item, block, metadata);
	}

	@Override
	public void damageItem(EntityDrone drone, ItemStack item, Block block, int metadata) {
		if (item.getItem().isItemTool(item)) {
			item.damageItem(1, drone);
		}
	}
	
	@Override
	public List<ItemStack> preformAction(EntityDrone drone, World world, int x, int y, int z) {
		List<ItemStack> stacks = drone.getBlockDropped(world, x, y, z);
		world.setBlockWithNotify(x, y, z, 0);
		return stacks;
	}
}
