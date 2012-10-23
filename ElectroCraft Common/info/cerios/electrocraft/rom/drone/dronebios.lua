local tArgs = {...}

drone.forgeDir = {
	up = 1, down = 0, north = 2, south = 3, east = 5, west = 4, unknown = 6
}

drone.dir = {
	north = 0, west = 1, south = 2, east = 3, unknown = 4
}

drone.turnDir = {
	left = 0, right = 1, arround = 2
}

local nativeUseTool = drone.useTool
function drone.useTool(...)
	nativeUseTool(...)
	event, result = os.waitForEvent("tool")
	return result
end

function drone.turn(turn)
	if gyro == nil then
		return
	end
	gyro.face(gyro.getDir())
	if turn == drone.turnDir.left then
		gyro.rotate(gyro.getRotation() - 90)
	elseif turn == drone.turnDir.right then
		gyro.rotate(gyro.getRotation() + 90)
	elseif turn == drone.turnDir.arround then
		gyro.rotate(gyro.getRotation() - 180)
	end
	gyro.face(gyro.getDir())
end

if (#tArgs > 0) then
	if #tArgs > 1 and type(tArgs[1]) == "thread" then
		coroutine.resume(tArgs[1], unpack(tArgs, 2))
	elseif type(tArgs[1]) == "thread" then
		coroutine.resume(tArgs[1])
	else
		os.getTerminal():print("Error loading the main BIOS!")
		os.getTerminal():print("Check that you installed ElectroCraft correctly.")
	end
else
	os.getTerminal():print("Error loading the main BIOS!")
	os.getTerminal():print("Check that you installed ElectroCraft correctly.")
end