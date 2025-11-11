UPDATE game_states
SET current_level = ?,
    score = ?,
    lives = ?,
    paddle_x = ?,
    ball_x = ?,
    ball_y = ?,
    bricks = ?::jsonb,
    powerups = ?::jsonb,
    balls = ?::jsonb,
    in_progress = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = ?;

