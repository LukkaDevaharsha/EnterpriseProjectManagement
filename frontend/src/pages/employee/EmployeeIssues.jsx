import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../services/api";
import { sortIssuesByDueDate } from "../../utils/issueSorting";

function getNextStatus(status) {
  const next = {
    OPEN: "IN_PROGRESS",
    IN_PROGRESS: "IN_REVIEW",
    IN_REVIEW: "TESTING",
    TESTING: "RESOLVED",
    RESOLVED: "CLOSED",
    CLOSED: null,
  };

  return next[status];
}

function EmployeeIssues() {
  const [issues, setIssues] = useState([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [dueDateSort, setDueDateSort] = useState("soonest");
  const [searchTerm, setSearchTerm] = useState("");

  const navigate = useNavigate();

  const loadIssues = useCallback(async () => {
    try {
      const response = await api.get("/issues");

      setIssues(response.data);
      setError("");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load issues");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => void loadIssues(), 0);
    return () => window.clearTimeout(timer);
  }, [loadIssues]);

  const updateStatus = async (issueId, status) => {
    setError("");
    setMessage("");

    try {
      await api.patch(`/issues/${issueId}/status`, { status });

      setMessage("Issue status updated");

      await loadIssues();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to update status");
    }
  };

  const sortedIssues = sortIssuesByDueDate(issues, dueDateSort);
  const visibleIssues = sortedIssues.filter((issue) =>
    [issue.title, issue.projectName, issue.status, issue.priority, issue.dueDate, issue.id]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(searchTerm.trim().toLowerCase())),
  );

  if (loading) {
    return (
      <div className="page">
        <p>Loading issues...</p>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>My Issues</h1>

      {error && <p className="error">{error}</p>}

      {message && <p className="success">{message}</p>}

      <div className="issue-sort-toolbar"><label className="issue-search-label" htmlFor="employee-issue-search">Search work</label><input id="employee-issue-search" type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder="Issue, project, status..." /><label htmlFor="employee-issue-date-sort">Sort by due date</label><select id="employee-issue-date-sort" value={dueDateSort} onChange={(event) => setDueDateSort(event.target.value)}><option value="soonest">Soonest first</option><option value="latest">Latest first</option><option value="none">Default order</option></select></div>

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Project</th>
              <th>Title</th>
              <th>Priority</th>
              <th>Status</th>
              <th>Due Date</th>
              <th>Next Status</th>
              <th>Details</th>
            </tr>
          </thead>

          <tbody>
            {visibleIssues.length === 0 ? (
              <tr>
                <td colSpan="8">{issues.length === 0 ? "No issues assigned to you." : "No work matches your search."}</td>
              </tr>
            ) : (
              visibleIssues.map((issue) => {
                const nextStatus = getNextStatus(issue.status);

                return (
                  <tr key={issue.id}>
                    <td>{issue.id}</td>

                    <td>{issue.projectName}</td>

                    <td>{issue.title}</td>

                    <td>{issue.priority}</td>

                    <td>{issue.status}</td>

                    <td>{issue.dueDate || "-"}</td>

                    <td>
                      {nextStatus ? (
                        <button
                          onClick={() => updateStatus(issue.id, nextStatus)}
                        >
                          {nextStatus}
                        </button>
                      ) : (
                        <span>Completed</span>
                      )}
                    </td>

                    <td>
                      <button
                        onClick={() => navigate(`/employee/issues/${issue.id}`)}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default EmployeeIssues;
