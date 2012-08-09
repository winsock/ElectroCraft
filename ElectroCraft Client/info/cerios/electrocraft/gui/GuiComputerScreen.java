package info.cerios.electrocraft.gui;

import cpw.mods.fml.common.FMLCommonHandler;
import info.cerios.electrocraft.core.computer.*;
import info.cerios.electrocraft.core.network.ComputerInputPacket;
import info.cerios.electrocraft.core.network.ComputerProtocol;
import info.cerios.electrocraft.mod_ElectroCraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Tessellator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.ByteBuffer;

public class GuiComputerScreen extends GuiScreen implements IComputerCallback {

    public static final int CLOSE_BUTTON = 0;

    private XECInterface computer;
    private XECVGACard videoCard;
    private XECTerminal terminal;
    private XECKeyboard keyboard;
    private int displayTextureId;
    private boolean repeatEventsOldState = Keyboard.areRepeatEventsEnabled();
    private volatile boolean isVisible = true;
    private boolean isSmpComputerScreen = false;
    private int lastWidth, lastHeight;
    private int ticksSinceLastScreenRequest = 0;
    private ByteBuffer displayBuffer;

    public GuiComputerScreen(XECInterface object) {
        this.computer = object;
        this.videoCard = computer.getVideoCard();
        this.terminal = computer.getTerminal();
        this.keyboard = computer.getKeyboard();
        Keyboard.enableRepeatEvents(true);
    }

    public GuiComputerScreen() {
        isSmpComputerScreen = true;
        Keyboard.enableRepeatEvents(true);
        mod_ElectroCraft.instance.getClient().registerCallback(ComputerProtocol.DISPLAY, this);
    }

    public void initGui() {
        controlList.add(new GuiButton(CLOSE_BUTTON, width - 5 - 4 - 17, 5 + 4, 17, 17, "X"));

        if (!isSmpComputerScreen) {
            // OpenGL Stuff
            if (GL11.glIsTexture(displayTextureId))
                GL11.glDeleteTextures(displayTextureId);

            displayBuffer = ByteBuffer.allocateDirect(videoCard.getScreenWidth() * videoCard.getScreenHeight() * 3);
            displayBuffer.put(videoCard.getScreenData());
            displayBuffer.rewind();
            displayTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, videoCard.getScreenWidth(), videoCard.getScreenHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, displayBuffer);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        Tessellator tess = Tessellator.instance;

        float halfBoxWidth = width / 2.5f;
        float halfBoxHeight = height / 2.5f;
        float halfScreenWidth = halfBoxWidth - 20;
        float halfScreenHeight = halfBoxHeight - 20;

        // Draw the background
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/info/cerios/electrocraft/gfx/computerscreen.png"));
        tess.startDrawingQuads();
        tess.addVertexWithUV((width / 2) + halfBoxWidth, (height / 2) - halfBoxHeight, 0, 1, 0);
        tess.addVertexWithUV((width / 2) - halfBoxWidth, (height / 2) - halfBoxHeight, 0, 0, 0);
        tess.addVertexWithUV((width / 2) - halfBoxWidth, (height / 2) + halfBoxHeight, 0, 0, 1);
        tess.addVertexWithUV((width / 2) + halfBoxWidth, (height / 2) + halfBoxHeight, 0, 1, 1);
        tess.draw();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Display the Video Card contents
        //GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
        tess.startDrawingQuads();
        tess.setColorOpaque(0, 0, 0);
        tess.addVertexWithUV((width / 2) + halfBoxWidth - 15, (height / 2) - halfBoxHeight + 20, 0, 1, 0);
        tess.addVertexWithUV((width / 2) - halfBoxWidth + 15, (height / 2) - halfBoxHeight + 20, 0, 0, 0);
        tess.addVertexWithUV((width / 2) - halfBoxWidth + 15, (height / 2) + halfBoxHeight - 15, 0, 0, 1);
        tess.addVertexWithUV((width / 2) + halfBoxWidth - 15, (height / 2) + halfBoxHeight - 15, 0, 1, 1);
        tess.draw();
        //GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        float pixelsPerChar = (halfScreenWidth * 2) / terminal.getColoumns();
        float pixelsPerLineHeight = (halfScreenHeight * 2) / terminal.getRows();

        for (int i = 0; i < terminal.getRows(); i++) {
            float scaleFactorX = 1f;
            float scaleFactorY = 1f;
            if (fontRenderer.getStringWidth(terminal.getLine(i)) > (halfScreenWidth * 2)) {
                scaleFactorX = (float) ((halfScreenWidth * 2) / fontRenderer.getStringWidth(terminal.getLine(i)));
            }
            if ((fontRenderer.FONT_HEIGHT * terminal.getColoumns()) > (halfScreenHeight * 2)) {
                scaleFactorY = (float) ((halfScreenHeight * 2) / (fontRenderer.FONT_HEIGHT * terminal.getRows()));
            }
            GL11.glPushMatrix();
            GL11.glScalef(scaleFactorX, scaleFactorY, 1);
            this.fontRenderer.drawString(terminal.getLine(i), (int) ((1 / scaleFactorX) * ((width / 2) - halfScreenWidth)), (int) ((i * pixelsPerLineHeight) + ((1 / scaleFactorY) * ((height / 2) - halfScreenHeight))), 0xFFFFFF);
            GL11.glPopMatrix();
        }

        // Draw the screen title
        fontRenderer.drawString("xEC Computer System", 20, 10, 0x404040);

        super.drawScreen(par1, par2, par3);
    }

    @Override
    protected void actionPerformed(GuiButton par1GuiButton) {
        mc.displayGuiScreen(null);
    }

    @Override
    public void updateScreen() {
        if (!isSmpComputerScreen) {
            displayBuffer.clear();
            displayBuffer.put(videoCard.getScreenData());
            displayBuffer.rewind();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, videoCard.getScreenWidth(), videoCard.getScreenHeight(), GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, displayBuffer);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        } else {
            if (ticksSinceLastScreenRequest >= 1) {
                try {
                    mod_ElectroCraft.instance.getClient().sendPacket(ComputerProtocol.DISPLAY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ticksSinceLastScreenRequest = 0;
            } else {
                ticksSinceLastScreenRequest++;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(repeatEventsOldState);
        this.isVisible = false;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void handleInput() {

        while (Mouse.next()) {
            handleMouseInput();
        }

        if (!isSmpComputerScreen) {
            while (Keyboard.next()) {
                if (Keyboard.getEventKeyState()) {
                    keyboard.onKeyPress((byte) Keyboard.getEventCharacter());
                }
            }
        } else if (isSmpComputerScreen) {
            while (Keyboard.next()) {
                int key = Keyboard.getEventKey();
                boolean down;
                if (Keyboard.getEventKeyState()) {
                    down = true;
                } else {
                    down = false;
                }

                ComputerInputPacket inputPacket = new ComputerInputPacket();
                inputPacket.setEventKey(key);
                inputPacket.setMouseDeltas(Mouse.getDX(), Mouse.getDY(), Mouse.getDWheel());
                inputPacket.setEventMouseButton(Mouse.getEventButton());
                inputPacket.setWasKeyDown(down);
                try {
                    ModLoader.sendPacket(inputPacket.getMCPacket());
                } catch (IOException e) {
                    FMLCommonHandler.instance().getFMLLogger().fine("ElectroCraft: Unable to send computer input data!");
                }

            }
        }
    }

    @Override
    public void onTaskComplete(Object... objects) {
        if (objects[0] instanceof Object[]) {
            int width = (Integer) ((Object[]) objects[0])[1];
            int height = (Integer) ((Object[]) objects[0])[2];
            if (!GL11.glIsTexture(displayTextureId) || (width != lastWidth || height != lastHeight)) {
                if (GL11.glIsTexture(displayTextureId))
                    GL11.glDeleteTextures(displayTextureId);
                displayTextureId = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) ((Object[]) objects[0])[0]);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            } else {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) ((Object[]) objects[0])[0]);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            }

            lastWidth = width;
            lastHeight = height;
        }
    }
}
