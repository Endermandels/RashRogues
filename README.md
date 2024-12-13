# RashRogues

Made by:

    Max Greener
    Cade Tormanen
    Elijah Delavar

## Overview

Play as a tiny rogue on a quest to steal as much gold from the king as possible,
and perhaps even overthrow the king.  However, you aren't very good at stealth.
Be prepared for a bullet hell of arrows and bombs while swordsmen wait to swipe
at your neck!

## Controls

    WASD/Arrow Keys - MOVE
    Mouse Position  - AIM PROJECTILE
    SPACE           - DASH
    E               - USE HEALTH POTION
    Q               - THROW SMOKE BOMB
    ~               - TOGGLE HUD

## Cheat Codes

    debug                   - Show various debug information such as hitboxes
    spawn <enemy> <x> <y>   - Spawn an enemy at x and y position
                            - Valid enemy types are: 
                                - archer
                                - bomber
                                - swordsman
    addPots <num>           - Add num amount of health potions to inventory
    fps                     - Toggle fps debug info
    is <stat> <amount>      - Increase a stat by given amount
                            - Valid stats are:
                                - health
                                - damage
                                - attackSpeed
                                - moveSpeed
                                - dexterity
    tp <pid> <x> <y>        - Teleport player with pid to x and y
                            - pid resides in the range: [0,numplayers)

## Low Bar Goals

### Pathfinding: *Complete*
    Swordsmen will move straight towards the player’s position.
    Archers will move away from the player if the player gets too close.
    Grenadiers will try to keep a medium range from the player.

### Collision Detection: *Complete*
    Player will get hurt when colliding with projectiles, explosions, and enemy hitboxes.
    Enemies will get hurt if damaged by a player’s damaging projectile.

### UI: *Partially Complete*
    The player’s health bar will be visible, as well as their ability icons.
    The shop will open when a player interacts with the merchant.
    
    Note: The shop mechanic is not implemented yet.

### Enemies: *Complete*
    Swordsman: slow melee unit, deals heavy damage, large health pool
    Archer: fast ranged unit, rapidly shoots arrows, frail
    Grenadier: medium ranged unit, slowly lobs bombs with delayed explosion

### Player Movement/Interaction: *Complete*
    Use WASD/Arrow keys to move the player. 
    Press SPACE to attack enemies or interact with the merchant. 
        Above changed to: Press SPACE to dash.
    Attacks will fire off in the direction the player last moved. 
        Above changed to: Attacks will fire off towards the mouse.
    Press ESC to stop interacting with the merchant.  
    Press E to activate an ability. 
        Above changed to: Press Q to activate an ability.
    Press Q to use a consumable (potion, health pack, etc.).
        Above changed to: Press E to use a consumable.
    Press ~ to open the HUD. 

### Merchant: *Incomplete*
    Select an upgrade specific to the user from interacting with the merchant.  
    Upgrades include: smoke bombs, throwing knives, daggers, cloaks, gloves,
    lockpicking tools, and more.

### Difficulty Increase: *Complete*
    The longer the players stay in any given room,
    the more enemies spawn behind them to pressure the players.
    Each level of the castle also spawns more and tougher enemies.

### Custom Animations: *Complete*
    All assets in the game (at least the entities) will be custom made, 
    including animations. Particle Effects have also been added sparingly.

## Other Completed Goals

### SFX
    Each room has its own music.
    Most interactions have a sound effect.

### Networking
    The game can be multiplayer or single player.
    Players can only proceed if all of them reach the door at the end.

## License

This work is licensed under CC BY-NC-SA 4.0