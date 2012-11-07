package info.cerios.electrocraft.core.items;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class ItemHandler {

	private Map<ElectroItems, Set<Item>> items = new HashMap<ElectroItems, Set<Item>>();

	public Collection<Item> getItems(ElectroItems item) {
		// Check if the item has been registered
		if (!items.containsKey(item) && item.getItem() != null) {
			// Create the set and add the item to it
			Set<Item> items = new HashSet<Item>();
			items.add(item.getItem());
			this.items.put(item, items);

			// Loop through all of the names, each name increments the damage
			// value by 1. The damage is kept in i
			for (int i = 0; i < item.getHumanNames().length; i++) {
				// Register the item
				LanguageRegistry.addName(new ItemStack(item.getItem(), 1, i),
						item.getHumanNames()[i]);

				if (item.isOreDicItem()) {
					// If it is a ore dictionary item add it to the ore
					// dictionary
					OreDictionary.registerOre(item.getItemName(),
							new ItemStack(item.getItem(), 1, i));
				}
			}
		}
		return items.get(item);
	}

	public void registerOreDicItem(String itemName, Item item) {
		for (ElectroItems electroItem : ElectroItems.values()) {
			if (electroItem.getItemName().equalsIgnoreCase(itemName)) {
				if (items.containsKey(electroItem)) {
					Set<Item> currentItems = items.get(electroItem);
					if (!currentItems.contains(item)) {
						currentItems.add(item);
					}
					items.put(electroItem, currentItems);
				} else {
					Set<Item> items = new HashSet<Item>();
					items.add(item);
					this.items.put(electroItem, items);
				}
			}
		}
	}

	public void registerItems() {
		for (ElectroItems item : ElectroItems.values()) {
			// Check if the item's item is null
			if (item.getItem() == null) {
				continue;
			}

			// Create the set and add the item to it
			Set<Item> items;
			if (this.items.containsKey(item)) {
				items = this.items.get(item);
			} else {
				items = new HashSet<Item>();
			}
			items.add(item.getItem());
			this.items.put(item, items);

			// Loop through all of the names, each name increments the damage
			// value by 1. The damage is kept in i
			for (int i = 0; i < item.getHumanNames().length; i++) {
				// Register the item
				LanguageRegistry.addName(new ItemStack(item.getItem(), 1, i),
						item.getHumanNames()[i]);

				if (item.isOreDicItem()) {
					// If it is a ore dictionary item add it to the ore
					// dictionary
					OreDictionary.registerOre(item.getItemName(),
							new ItemStack(item.getItem(), 1, i));
				}
			}
		}
	}
}
