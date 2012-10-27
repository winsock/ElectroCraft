package info.cerios.electrocraft.api.drone.upgrade;

import info.cerios.electrocraft.core.drone.Drone;
import net.minecraft.src.ItemStack;

public interface ICard {
	/**
	 * Gets the short name of the card
	 * Also used for the function table creation
	 * @return The short name
	 */
	public String getName(ItemStack stack);
	
	/**
	 * Gets called every tick, use it if the upgrade has some passive function
	 * or needs to update itself for any reason
	 */
	public void passiveFunctionTick(ItemStack stack);
}
