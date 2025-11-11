UPDATE game_states
SET in_progress = FALSE
WHERE user_id = ?;

