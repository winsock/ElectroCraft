package info.cerios.electrocraft.core.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public abstract class ElectroBlock extends BlockContainer {

    protected ElectroBlock(int id, int textureId, Material material) {
        super(id, textureId, material);
    }

    protected ElectroBlock(int id, Material material) {
        super(id, material);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
        super.onNeighborBlockChange(world, x, y, z, id);
    }

    @Override
    public String getTextureFile() {
        return "/info/cerios/electrocraft/gfx/blocks.png";
    }
}
