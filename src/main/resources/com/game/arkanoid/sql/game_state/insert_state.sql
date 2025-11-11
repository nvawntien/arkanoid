INSERT INTO game_states (
  user_id, current_level, score, lives, paddle_x, ball_x, ball_y, bricks, powerups, balls, in_progress, updated_at
) VALUES (
  ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, TRUE, CURRENT_TIMESTAMP
);
