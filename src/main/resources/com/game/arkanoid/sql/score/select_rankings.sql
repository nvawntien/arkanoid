SELECT name_alias AS name, best_score, best_round
FROM (
  SELECT LOWER(name) AS key,
         MAX(best_score) AS best_score,
         MAX(best_round) AS best_round,
         MIN(name) AS name_alias
  FROM users
  GROUP BY LOWER(name)
) t
ORDER BY best_round DESC, best_score DESC
LIMIT ?;

