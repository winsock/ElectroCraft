package info.cerios.electrocraft.event;

import java.util.HashMap;
import java.util.Map;

import info.cerios.electrocraft.blocks.ElectroBlocks;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.ItemStack;
import net.minecraft.src.forge.oredict.ShapedOreRecipe;

public class EventHandler {

	public static final EventHandler instance = new EventHandler();
	
	// Make the constructor private of the singleton class
	private EventHandler() {}
	
	// Method that triggers the DC age content
	public void triggerDCAge() {
		
	}
	
	// Method that triggers the AC age content
	public void triggerACAge() {
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ElectroBlocks.WIRE.getBlock(), 8), "RRR", "RCR", "RRR", 'R', "rubber", 'C', "ingotCopper"));
	}
	
	// Method that triggers the analog age content
	public void triggerAnalogAge() {
		
	}
	
	// Method that triggers the digital age content
	public void triggerDigitalAge() {
		
	}
	
	// Method that triggers the refined redstone age content
	public void triggerRefinedRedstoneAge() {
		
	}
	
	// Class that contains all of the DC Age Requirement Info and status
	public static class DCAgeRequirements {
		public enum Requirements {
			staticGenerator,
			staticEfficency,
			rareEarthMagnet,
			magicalDevice,
			magicalFurance,
			useFurnace
		}
		
		private static Map<Requirements, Boolean> requirements = new HashMap<Requirements, Boolean>();

		private static void setRequirementStatus(Requirements requirement, boolean status) {
			requirements.put(requirement, status);
			if (completedEverything()) {
				EventHandler.instance.triggerDCAge();
			}
		}
		
		public static boolean completedEverything() {
			for (Requirements requirement : Requirements.values()) {
				if (!(requirements.containsKey(requirement) && requirements.get(requirement))) {
					return false;
				}
			}
			return true;
		}
	}
}
