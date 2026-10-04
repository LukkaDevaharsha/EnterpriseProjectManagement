export function sortIssuesByDueDate(issues, direction = "soonest") {
  if (direction === "none") return issues;
  const multiplier = direction === "latest" ? -1 : 1;
  return [...issues].sort((first, second) => {
    if (!first.dueDate && !second.dueDate) return 0;
    if (!first.dueDate) return 1;
    if (!second.dueDate) return -1;
    return first.dueDate.localeCompare(second.dueDate) * multiplier;
  });
}
