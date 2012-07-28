package info.cerios.electrocraft.blocks.render;

import net.minecraft.src.Block;
import net.minecraft.src.RenderBlocks;

public interface IBlockRenderer {
	public boolean render(RenderBlocks renderer, Block block, int x, int y, int z);
}
