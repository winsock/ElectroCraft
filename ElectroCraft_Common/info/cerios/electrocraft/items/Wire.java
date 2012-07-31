package info.cerios.electrocraft.items;

import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.forge.ITextureProvider;

public class Wire extends ItemBlock implements ITextureProvider {

	public static final String[] damageNames = {"Tin Wire", "Copper Wire", "Gold Wire", "Redstone Wire"};
	
	protected Wire(int itemId) {
		super(itemId);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setIconIndex(0);
	}
	
	@Override
	public int getMetadata(int metadata) {
		return metadata;
	}
	
	@Override
	public String getItemNameIS(ItemStack par1ItemStack) {
		return damageNames[par1ItemStack.getItemDamage()];
	}
	
	@Override
	public String getTextureFile(){
		return "/info/cerios/electrocraft/gfx/items.png";
	}
}
