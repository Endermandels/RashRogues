# Info
Much info is found in AnimationHandler.java, but here are some extra notes:
1. AnimationTimer is handled by each Entity, passed to its own animations
2. The animations are a SHALLOW COPY from AnimationHandler!!!
2. Hurt and Die animations for enemies are handled in Enemy.java
2. These and the Player's counterparts are handled in the OnHurt function
3. All Entities have their Move animation logic handled in Entity
4. The above means that subclassed entities (players, specific enemies)
   only need to care about their Attack and other special animations
5. If there's no specified default animation, then a single frame is chosen
6. Check Entity's animation-related functions for more info

# Overview
This file maps each row of a sprite sheet to its intended animation use.

TODO: If you want aim/attack/stow to be different things at different times, 
that logic goes in Archer.java line 96, Archer.attack().

# Archer
1. idle
2. walk
3. aim - OPEN
4. attack
5. stow - CLOSE
6. die

# Bomber
1. idle
2. walk
3. attack
4. die

# King
1. sleep - CLOSE
2. wake - OPEN
3. idle
4. die

# Merchant
1. idle
2. reveal - OPEN
3. conceal - CLOSE

# Rogue
1. idle
2. walk
3. attack
4. hurt
5. die

# Swordsman
1. idle
2. walk
3. attack
4. die

# Dragon
1. walk - DEFAULT
2. attack
3. die
