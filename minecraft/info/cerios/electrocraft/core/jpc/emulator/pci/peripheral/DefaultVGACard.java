/*
    JPC: A x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.0

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007-2009 Isis Innovation Limited

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

    Details (including contact information) can be found at: 

    www-jpc.physics.ox.ac.uk
*/

package info.cerios.electrocraft.core.jpc.emulator.pci.peripheral;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import info.cerios.electrocraft.core.computer.ComputerHandler;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.IComputerRunnable;
import info.cerios.electrocraft.core.jpc.j2se.PCMonitor;

/**
 *
 * @author Ian Preston
 */
public final class DefaultVGACard extends VGACard implements IComputerCallback, IComputerRunnable {

    private int[] rawImageData;
    private byte[] rawImageBytes;
    private int xmin,  xmax,  ymin,  ymax,  width,  height;
    private BufferedImage buffer;
    private ByteBuffer byteBuffer;
    PCMonitor monitor;
	private int displayTextureId;
	private ComputerHandler handler;
	private boolean deleteTexture = false;
	private boolean updateOpenGL = false;
	private boolean recreateTexture = false;

    public DefaultVGACard(ComputerHandler computerHandler) 
    {
    	handler = computerHandler;
    }

    public int getXMin() {
        return xmin;
    }

    public int getXMax() {
        return xmax;
    }

    public int getYMin() {
        return ymin;
    }

    public int getYMax() {
        return ymax;
    }

    protected int rgbToPixel(int red, int green, int blue) {
        return ((0xFF & red) << 16) | ((0xFF & green) << 8) | (0xFF & blue);
    }

    public void resizeDisplay(int width, int height) 
    {
        if ((width == 0) || (height == 0))
            return;
        // Delete the old texture if needs to be resized
        if (displayTextureId >= 0 && (width != this.width || height != this.height))
            deleteTexture = true;
            
        this.width = width;
        this.height = height;

        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        buffer.setAccelerationPriority(1);
        DataBufferInt buf = (DataBufferInt) buffer.getRaster().getDataBuffer();
        rawImageData = buf.getData();
        byteBuffer = ByteBuffer.allocateDirect(width * height * 3);
        
        recreateTexture = true;
    }

    public void saveScreenshot()
    {
        File out = new File("Screenshot.png");
        try
        {
            ImageIO.write(buffer, "png", out);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setMonitor(PCMonitor mon) {
        this.monitor = mon;
    }

    public Dimension getDisplaySize()
    {
        return new Dimension(width, height);
    }

    public int[] getDisplayBuffer() 
    {
        return rawImageData;
    }

    protected void dirtyDisplayRegion(int x, int y, int w, int h) 
    {
        xmin = Math.min(x, xmin);
        xmax = Math.max(x + w, xmax);
        ymin = Math.min(y, ymin);
        ymax = Math.max(y + h, ymax);
    }

    public void paintPCMonitor(Graphics g, PCMonitor monitor)
    {
        g.drawImage(buffer, 0, 0, null);
        Dimension s = monitor.getSize();
        g.setColor(monitor.getBackground());
        g.fillRect(width, 0, s.width - width, height);
        g.fillRect(0, height, s.width, s.height - height);
    }

    public final void prepareUpdate() 
    {
        xmin = width;
        xmax = 0;
        ymin = height;
        ymax = 0;
    }

    public int updateOpenGL() {
    	updateOpenGL = true;
        handler.registerRunnableOnMainThread(this, this);
    	return displayTextureId;
    }
    
	private ByteBuffer getByteBuffer() {
        for (int i = 0, j = 0; i < rawImageData.length; i++) {
            int val = rawImageData[i];
            byteBuffer.put(j++, (byte) (val >> 16));
            byteBuffer.put(j++, (byte) (val >> 8));
            byteBuffer.put(j++, (byte) (val));
        }
        return byteBuffer;
	}

	@Override
	public Object run() {
		if (recreateTexture) {
			if (deleteTexture) {
				GL11.glDeleteTextures(displayTextureId);
			}
			// OpenGL Stuff
			if (!GL11.glIsTexture(displayTextureId))
				displayTextureId = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, getByteBuffer());
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
		if (updateOpenGL) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayTextureId);
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, getByteBuffer());
	    	GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	    }
		return null;
	}

	@Override
	public void onTaskComplete(Object object) {
		recreateTexture = false;
    	updateOpenGL = false;
    	deleteTexture = false;
	}
}
