SELECT name, best_score, best_round
FROM users
ORDER BY best_round DESC, best_score DESC
LIMIT ?;
