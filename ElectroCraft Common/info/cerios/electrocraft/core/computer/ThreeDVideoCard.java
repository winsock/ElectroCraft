package info.cerios.electrocraft.core.computer;

public class ThreeDVideoCard extends VideoCard {
	
	private Computer computer;
	
	@ExposedToLua(value = false)
	public ThreeDVideoCard(Computer computer, int width, int height) {
		super(width, height);
	}
	
	public void Begin(String mode) {
		OpenGLCommands.Constants glMode = OpenGLCommands.Constants.TRIANGLES;
		if (mode.equalsIgnoreCase("TRIANGLES"))
			glMode = OpenGLCommands.Constants.TRIANGLES;
		else if (mode.equalsIgnoreCase("LINES"))
			glMode = OpenGLCommands.Constants.LINES;
		else if (mode.equalsIgnoreCase("RECTANGLES"))
			glMode = OpenGLCommands.Constants.RECTANGLES;
		
		computer.getClient().sendOpenGLPacket(OpenGLCommands.BEGIN, glMode.ordinal());
	}
}
