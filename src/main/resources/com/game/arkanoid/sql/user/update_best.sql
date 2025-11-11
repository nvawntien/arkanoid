UPDATE users
SET best_round = GREATEST(best_round, ?),
    best_score = GREATEST(best_score, ?),
    last_login = CURRENT_TIMESTAMP
WHERE id = ?;

