package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.api.computer.ExposedToLua;

@ExposedToLua
public class VideoCard {
	
	private int width;
	private int height;
	private byte[] data;
	
	@ExposedToLua(value = false)
	public VideoCard(int width, int height) {
		this.width = width;
		this.height = height;
		this.data = new byte[width * height];
	}
	
	@ExposedToLua
	public void clear() {
		this.data = new byte[width * height];
	}
	
	@ExposedToLua
	public int getWidth() {
		return width;
	}
	
	@ExposedToLua
	public int getHeight() {
		return height;
	}
	
	@ExposedToLua(value = false)
	public byte[] getData() {
		return data;
	}
	
	@ExposedToLua(value = false)
	public void setData(byte[] data) {
		this.data = data;
	}
	
	@ExposedToLua
	public void setPixel(int x, int y, byte color) {
		if (x < width && y < height)
			data[(y * width) + x] = color;
	}
	
	@ExposedToLua
	public void drawLine(int x, int y, int x2, int y2, byte color) {
		if (x > width || y > height)
			return;
		if (x2 > width || y2 > height)
			return;
		
		int w = x2 - x ;
	    int h = y2 - y ;
	    int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
	    if (w < 0) dx1 = -1 ; else if (w > 0) dx1 = 1 ;
	    if (h < 0) dy1 = -1 ; else if (h > 0) dy1 = 1 ;
	    if (w < 0) dx2 = -1 ; else if (w > 0) dx2 = 1 ;
	    int longest = Math.abs(w) ;
	    int shortest = Math.abs(h) ;
	    if (!(longest>shortest)) {
	        longest = Math.abs(h) ;
	        shortest = Math.abs(w) ;
	        if (h < 0) dy2 = -1 ; else if (h > 0) dy2 = 1 ;
	        dx2 = 0 ;            
	    }
	    int numerator = longest >> 1 ;
	    for (int i = 0; i <= longest; i++) {
	    	setPixel(x, y, color) ;
	        numerator += shortest ;
	        if (!(numerator < longest)) {
	            numerator -= longest ;
	            x += dx1 ;
	            y += dy1 ;
	        } else {
	            x += dx2 ;
	            y += dy2 ;
	        }
	    }
	}
}
