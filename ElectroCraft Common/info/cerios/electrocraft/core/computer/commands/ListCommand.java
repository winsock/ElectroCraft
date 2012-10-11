package info.cerios.electrocraft.core.computer.commands;

import java.io.File;

import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.luaapi.ComputerFile;

public class ListCommand implements IComputerCommand {

	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		ComputerFile currentDirectory = new ComputerFile(computer.getBaseDirectory().getAbsolutePath() + "/" + computer.getCurrentDirectory(), computer);
		if (!currentDirectory.exists()) {
			computer.getTerminal().print("ERROR: Error reading the current directory");
		}
		
		if (!currentDirectory.isDirectory()) {
			computer.getTerminal().print("ERROR: Error we somehow set the current directory as a file?");
		}
		
		int numberOfFiles = 0;
		double totalSize = 0;
		for (File f : currentDirectory.getJavaFile().listFiles()) {
			String sizeSuffix = "B";
			double size = f.length();
			totalSize += size;
			if (size > 1024) {
				size /= 1024;
				sizeSuffix = "K";
				if (size > 1024) {
					size /= 1024;
					sizeSuffix = "M";
				}
			}
			computer.getTerminal().print((f.isDirectory() ? "D:" : "F:") + f.getName() + " - " + String.valueOf(size) + sizeSuffix);
			numberOfFiles++;
		}
		String sizeSuffix = "B";
		if (totalSize > 1024) {
			totalSize /= 1024;
			sizeSuffix = "K";
			if (totalSize > 1024) {
				totalSize /= 1024;
				sizeSuffix = "M";
			}
		}
		computer.getTerminal().print("Number of files: " + numberOfFiles + " Total Size: " + String.valueOf(totalSize) + sizeSuffix);
	}
}
