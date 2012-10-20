local tArgs = {...}

drone.dir = {
	up = 1, down = 0, north = 2, south = 3, east = 5, west = 4, unknown = 6
}

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