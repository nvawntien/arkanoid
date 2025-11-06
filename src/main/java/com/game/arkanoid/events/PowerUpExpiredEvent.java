package com.game.arkanoid.events;

import com.game.arkanoid.models.PowerUpType;

public record PowerUpExpiredEvent(PowerUpType type) { }
