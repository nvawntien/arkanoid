SELECT 
  current_level, score, lives,
  paddle_x, paddle_width,
  ball_x, ball_y, ball_dx, ball_dy,
  ball_moving, ball_downward, ball_stuck, ball_stuck_offset_x,
  time_scale, laser_cooldown,
  bricks::text   AS bricks,
  powerups::text AS powerups,
  enemies::text  AS enemies,
  balls::text    AS balls,
  effects::text  AS effects
FROM game_states
WHERE user_id = ? AND in_progress = TRUE
ORDER BY updated_at DESC
LIMIT 1;
