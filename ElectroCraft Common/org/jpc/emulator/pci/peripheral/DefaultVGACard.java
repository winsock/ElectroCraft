/*
    JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.4

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007-2010 The University of Oxford

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

    jpc.sourceforge.net
    or the developer website
    sourceforge.net/projects/jpc/

    Conceived and Developed by:
    Rhys Newman, Ian Preston, Chris Dennis

    End of licence header
 */

package org.jpc.emulator.pci.peripheral;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Ian Preston
 */
public final class DefaultVGACard extends VGACard {

	private int[] rawImageData;
	private int xmin,  xmax,  ymin,  ymax,  width,  height;
	private BufferedImage buffer;

	public DefaultVGACard() 
	{
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
