package com.game.arkanoid.events;

import com.game.arkanoid.models.Brick;

public record BrickDestroyedEvent(Brick brick) { }
