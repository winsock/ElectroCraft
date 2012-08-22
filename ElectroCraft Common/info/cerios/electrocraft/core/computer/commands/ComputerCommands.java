package info.cerios.electrocraft.core.computer.commands;

public enum ComputerCommands {
	CLEAR("clear", "Clears the screen", 0, new ClearCommand()),
	HELP("help", "Prints the system help", 0, new HelpCommand()),
	EDIT("edit", "Creates or edits a file", 1, new EditCommand()),
	LUA("lua", "Runs a Lua program", 1, new LuaCommand()),
	LIST("ls", "Lists files and directories in the current folder", 0, new ListCommand()),
	MAKE_DIR("mkdir", "Makes a directory", 1, new MakeDirectoryCommand());
	
	private String command, helpText;
	private int argc;
	private IComputerCommand commandInstance;
	
	private ComputerCommands(String command, String helpText, int argc, IComputerCommand commandInstance) {
		this.command = command;
		this.helpText = helpText;
		this.argc = argc;
		this.commandInstance = commandInstance;
	}
	
	public IComputerCommand getCommand() {
		return commandInstance;
	}
	
	public String getCommandText() {
		return command;
	}
	
	public String getHelpText() {
		return helpText;
	}
	
	public int getNumberOfArgs() {
		return argc;
	}
}
