package info.cerios.electrocraft.core.blocks;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.src.Block;
import net.minecraft.src.Material;
import net.minecraftforge.common.MinecraftForge;

public class CopperOre extends Block {

    public CopperOre(int id) {
        super(id, 3, Material.rock);
        this.setHardness(3.0f);
        this.setResistance(5.0f);
        this.setStepSound(soundStoneFootstep);
        MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 0);
    }

    @Override
    public String getTextureFile() {
        return "/info/cerios/electrocraft/gfx/blocks.png";
    }
    
    @Override
    public void addCreativeItems(ArrayList itemList) { 
    	itemList.add(this);
    }
}
