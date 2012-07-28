package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.items.ElectroItems;

import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForge;

public class MagnetiteOre extends Block implements ITextureProvider {

	public MagnetiteOre(int blockId) {
		super(blockId, 5, Material.rock);
		this.setHardness(1.0f);
		this.setResistance(2.0f);
		this.setStepSound(soundStoneFootstep);
		MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 0);
	}
	
	@Override
	public float blockStrength(EntityPlayer player, int meta) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack == null)
        	return 0f;
		if (stack.itemID == Item.pickaxeGold.shiftedIndex || stack.itemID == Item.pickaxeSteel.shiftedIndex) {
			return 0f;
		}
		return super.blockStrength(player, meta);
	}
	
	@Override
	public int quantityDropped(Random rand) {
		return rand.nextInt(5) + 2;
	}
	
	@Override
	public int idDropped(int par1, Random par2Random, int par3) {
		return ElectroItems.MAGNETITE_DUST.getItem().shiftedIndex;
	}
	
	@Override
	public String getTextureFile(){
		return "/info/cerios/electrocraft/gfx/blocks.png";
	}
}
