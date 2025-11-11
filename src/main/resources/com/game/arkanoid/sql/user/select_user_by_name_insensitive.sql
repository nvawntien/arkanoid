SELECT id, name, password, best_score, best_round, last_login
FROM users
WHERE LOWER(name) = LOWER(?)
ORDER BY best_round DESC, best_score DESC
LIMIT 1;

