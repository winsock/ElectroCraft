package info.cerios.electrocraft.items;

import net.minecraft.src.Item;
import net.minecraft.src.forge.ITextureProvider;

public class CopperIngot extends Item implements ITextureProvider {

	protected CopperIngot(int itemId) {
		super(itemId);
	}

	@Override
	public String getTextureFile(){
		return "/info/cerios/electrocraft/gfx/items.png";
	}
}
