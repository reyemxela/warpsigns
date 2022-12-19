# Warp Signs
A simple mod that lets you pair sets of signs together to allow teleporting between them.
This mod works both client-side or server-side, and if running on a server, clients don't need the mod installed.

## Basic Usage
Simply right-click any two signs with the "pairing item" (a diamond by default, configurable) to pair them together.
The item will be used up when right-clicking the second sign to finish pairing.

Breaking either sign of a pair will un-pair them and give back the diamond.

On first run, a config file is created at `<minecraft_dir>/config/warpSigns.json`. This can be edited to change the pairing item, whether non-admins can pair/edit signs, etc.

## Interactions
- Right-click with item to pair signs
- Sneak-right-click for global pairing (another player can finish pairing by doing the same)
- Sneak-break a sign to break it and start re-pairing, useful for moving it
- Sneak-right-click to edit sign text