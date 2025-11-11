INSERT INTO users (name, password)
VALUES (?, ?)
RETURNING id, name, password, best_score, best_round, last_login;

