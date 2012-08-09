package info.cerios.electrocraft.blocks.render;

import info.cerios.electrocraft.core.blocks.IBlockRenderer;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

public class RibbonCableRenderer implements IBlockRenderer {

    @Override
    public boolean render(Block block, int x, int y, int z) {
        RenderBlocks renderer = ModLoader.getMinecraftInstance().renderGlobal.globalRenderBlocks;
        boolean posX = false, negX = false, posZ = false, negZ = false; // Wire connection status
        boolean posXUp = false, negXUp = false, posZUp = false, negZUp = false, posXDown = false, negXDown = false, posZDown = false, negZDown = false; // Is the wire going up or down a block
        int connectionCount = 0;
        Tessellator tess = Tessellator.instance;

        TileEntity tileEntity = ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z);
        if (!(tileEntity instanceof NetworkBlock))
            return false;
        NetworkBlock netBlock = (NetworkBlock) tileEntity;

        // Check for connections
        posX = ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x + 1, y, z) instanceof NetworkBlock && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x + 1, y, z));
        negX = ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x - 1, y, z) instanceof NetworkBlock && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x - 1, y, z));
        posZ = ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z + 1) instanceof NetworkBlock && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z + 1));
        negZ = ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z - 1) instanceof NetworkBlock && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z - 1));

        // Check if we are connecting going up or down a block
        if (!posX) {
            posXDown = (ModLoader.getMinecraftInstance().theWorld.getBlockId(x + 1, y, z) == 0) && (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x + 1, y - 1, z) instanceof NetworkBlock) && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x + 1, y - 1, z));
            posXUp = (ModLoader.getMinecraftInstance().theWorld.getBlockId(x, y + 1, z) == 0) && (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x + 1, y + 1, z) instanceof NetworkBlock) && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x + 1, y + 1, z));
        }
        if (!negX) {
            negXDown = (ModLoader.getMinecraftInstance().theWorld.getBlockId(x - 1, y, z) == 0) && (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x - 1, y - 1, z) instanceof NetworkBlock) && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x - 1, y - 1, z));
            negXUp = (ModLoader.getMinecraftInstance().theWorld.getBlockId(x, y + 1, z) == 0) && (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x - 1, y + 1, z) instanceof NetworkBlock) && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x - 1, y + 1, z));
        }
        if (!posZ) {
            posZDown = (ModLoader.getMinecraftInstance().theWorld.getBlockId(x, y, z + 1) == 0) && (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y - 1, z + 1) instanceof NetworkBlock) && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y - 1, z + 1));
            posZUp = (ModLoader.getMinecraftInstance().theWorld.getBlockId(x, y + 1, z) == 0) && (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y + 1, z + 1) instanceof NetworkBlock) && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y + 1, z + 1));
        }
        if (!negZ) {
            negZDown = (ModLoader.getMinecraftInstance().theWorld.getBlockId(x, y, z - 1) == 0) && (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y - 1, z - 1) instanceof NetworkBlock) && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y - 1, z - 1));
            negZUp = (ModLoader.getMinecraftInstance().theWorld.getBlockId(x, y + 1, z) == 0) && (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y + 1, z - 1) instanceof NetworkBlock) && netBlock.canConnectNetwork((NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y + 1, z - 1));
        }

        if (posX)
            connectionCount++;
        if (negX)
            connectionCount++;
        if (posZ)
            connectionCount++;
        if (negZ)
            connectionCount++;

        if (posXUp)
            connectionCount++;
        if (negXUp)
            connectionCount++;
        if (posZUp)
            connectionCount++;
        if (negZUp)
            connectionCount++;
        if (posXDown)
            connectionCount++;
        if (negXDown)
            connectionCount++;
        if (posZDown)
            connectionCount++;
        if (negZDown)
            connectionCount++;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ModLoader.getMinecraftInstance().renderEngine.getTexture(block.getTextureFile()));

        if (posXUp || posZUp || negXUp || negZUp || posXDown || posZDown || negXDown || negZDown) {
            int texture = block.getBlockTextureFromSide(1);
            int texX = (texture & 15) << 4;
            int texY = texture & 240;
            double textureU = (double) ((float) texX / 256.0F);
            double textureV = (double) ((float) texY / 256.0F);

            if (posXUp) {
                tess.addVertexWithUV(x + 1, y + 1, z - 0.015625D, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y, z - 0.015625D, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y, z + 1 - 0.015625D, textureU, textureV);
                tess.addVertexWithUV(x + 1, y + 1, z + 1 - 0.015625D, textureU + 16D / 256D, textureV);
            } else if (negXUp) {
                tess.addVertexWithUV(x, y + 1, z - 0.015625D, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 1, z + 1 - 0.015625D, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y, z + 1 - 0.015625D, textureU, textureV);
                tess.addVertexWithUV(x, y, z - 0.015625D, textureU, textureV + 16D / 256D);
            } else if (posZUp) {
                tess.addVertexWithUV(x + 1, y, z + 1 + 0.015625D, textureU, textureV);
                tess.addVertexWithUV(x, y, z + 1 + 0.015625D, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 1, z + 1 + 0.015625D, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 1, z + 1 + 0.015625D, textureU + 16D / 256D, textureV);
            } else if (negZUp) {
                tess.addVertexWithUV(x + 1, y + 1, z + 0.015625D, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y + 1, z + 0.015625D, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x, y, z + 0.015625D, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y, z + 0.015625D, textureU, textureV);
            } else if (posXDown) {
                tess.addVertexWithUV(x + 1, y - 1 - 0.5, z - 0.015625D, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x, y - 1 - 0.5, z - 0.015625D, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y, z - 0.015625D, textureU, textureV);
                tess.addVertexWithUV(x + 1, y, z - 0.015625D, textureU, textureV + 16D / 256D);
            } else if (negXDown) {
                tess.addVertexWithUV(x, y - 1 - 0.5, z + 0.015625D, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x + 1, y - 1 - 0.5, z + 0.015625D, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y, z + 0.015625D, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x, y, z + 0.015625D, textureU, textureV);
            } else if (posZDown) {
                tess.addVertexWithUV(x - 1, y - 1, z - 0.015625D, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y - 1, z - 0.015625D, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x, y, z - 0.015625D, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x - 1, y, z - 0.015625D, textureU, textureV);
            } else if (negZDown) {
                tess.addVertexWithUV(x - 1, y - 1, z + 0.015625D, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y - 1, z + 0.015625D, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x, y, z + 0.015625D, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x - 1, y, z + 0.015625D, textureU, textureV);
            }
        }

        int texture = block.getBlockTextureFromSide(connectionCount);
        int texX = (texture & 15) << 4;
        int texY = texture & 240;
        double textureU = (double) ((float) texX / 256.0F);
        double textureV = (double) ((float) texY / 256.0F);

        if (connectionCount == 4) {
            // Draw 4 way intersection
            tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU, textureV + 16D / 256D);
            tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU + (16D / 256D), textureV + 16D / 256D);
            tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU + 16D / 256D, textureV);
            tess.addVertexWithUV(x, y + 0.015625D, z, textureU, textureV);
        } else if (connectionCount == 3) {
            // Draw 3 way intersection
            if (posX && (posZ && negZ)) {
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU, textureV);
            } else if (negX && (posZ && posX)) {
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU, textureV);
            } else if (negX && (negZ && posX)) {
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU, textureV);
            } else if (negZ && (negX && posZ)) {
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU, textureV);
            }
        } else if (connectionCount == 2 && !((posX && negX) || (posZ && negZ))) {
            // Draw right angle
            if (posX && posZ) {
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU, textureV);
            } else if (negX && negZ) {
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU, textureV);
            } else if (negX && posZ) {
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU, textureV);
            } else if (negZ && posX) {
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU, textureV);
            }
        } else {
            // Need to locally change this for straight connections with two wires on one side
            texture = block.getBlockTextureFromSide(0);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            textureU = (double) ((float) texX / 256.0F);
            textureV = (double) ((float) texY / 256.0F);

            // Draw straight piece
            if ((posX && negX) || (connectionCount == 1 && (posX || negX))) {
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU, textureV);
            } else {
                tess.addVertexWithUV(x, y + 0.015625D, z, textureU, textureV + 16D / 256D);
                tess.addVertexWithUV(x, y + 0.015625D, z + 1, textureU + (16D / 256D), textureV + 16D / 256D);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z + 1, textureU + 16D / 256D, textureV);
                tess.addVertexWithUV(x + 1, y + 0.015625D, z, textureU, textureV);
            }
        }

        return true;
    }
}
