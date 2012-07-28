package info.cerios.electrocraft.core.blocks;

import net.minecraft.src.Block;
import net.minecraft.src.Material;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForge;

public class CopperOre extends Block implements ITextureProvider {

	public CopperOre(int id) {
		super(id, 0, Material.rock);
		this.setHardness(3.0f);
		this.setResistance(5.0f);
		this.setStepSound(soundStoneFootstep);
		MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 0);
	}
	
	@Override
	public String getTextureFile(){
		return "/info/cerios/electrocraft/gfx/blocks.png";
	}
}
