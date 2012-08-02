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

import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.IComputerHandler;
import info.cerios.electrocraft.core.computer.IComputerRunnable;

/**
 *
 * @author Ian Preston
 */
public final class DefaultVGACard extends VGACard {

    private int[] rawImageData;
    private byte[] rawImageBytes;
    private int xmin,  xmax,  ymin,  ymax,  width,  height;
    private BufferedImage buffer;
    private ByteBuffer byteBuffer;
	private int displayTextureId;
	private IComputerHandler handler;

    public DefaultVGACard(IComputerHandler computerHandler) 
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
            
        this.width = width;
        this.height = height;

        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        buffer.setAccelerationPriority(1);
        DataBufferInt buf = (DataBufferInt) buffer.getRaster().getDataBuffer();
        rawImageData = buf.getData();
        byteBuffer = ByteBuffer.allocateDirect(width * height * 3);
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

    public final void prepareUpdate() 
    {
        xmin = width;
        xmax = 0;
        ymin = height;
        ymax = 0;
    }

	public ByteBuffer getByteBuffer() {
        for (int i = 0, j = 0; i < rawImageData.length; i++) {
            int val = rawImageData[i];
            byteBuffer.put(j++, (byte) (val >> 16));
            byteBuffer.put(j++, (byte) (val >> 8));
            byteBuffer.put(j++, (byte) (val));
        }
        return byteBuffer;
	}
	
	public byte[] getBytes() {
		byte[] data = new byte[rawImageData.length * 3];
		for (int i = 0, j = 0; i < rawImageData.length; i++) {
            int val = rawImageData[i];
            data[j++] = (byte) (val >> 16);
            data[j++] = (byte) (val >> 8);
            data[j++] = (byte) (val);
        }
		return data;
	}
}
