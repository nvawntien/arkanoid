UPDATE game_states
SET current_level = ?,
    score = ?,
    lives = ?,
    paddle_x = ?,
    ball_x = ?,
    ball_y = ?,
    bricks = ?::jsonb,
    powerups = ?::jsonb,
    enemies = ?::jsonb,   -- ✅ added line
    balls = ?::jsonb,
    effects = ?::jsonb,   -- ✅ added line
    in_progress = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE id = ?;
