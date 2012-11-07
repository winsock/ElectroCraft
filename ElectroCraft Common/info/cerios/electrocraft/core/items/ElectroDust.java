package info.cerios.electrocraft.core.items;

import net.minecraft.src.Item;

public class ElectroDust extends Item {

	protected ElectroDust(int id) {
		super(id);
	}

	@Override
	public String getTextureFile() {
		return "/info/cerios/electrocraft/gfx/items.png";
	}
}
