package info.cerios.electrocraft.core.items;

import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.forge.ITextureProvider;

public class ElectroDust extends Item implements ITextureProvider {

	protected ElectroDust(int id) {
		super(id);
	}
	
	@Override
	public String getTextureFile(){
		return "/info/cerios/electrocraft/gfx/items.png";
	}
}
