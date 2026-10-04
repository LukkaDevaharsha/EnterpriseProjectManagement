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
    RESOLVED: "OPEN",
    CLOSED: "OPEN",
  };

  return next[status];
}

function AdminIssues() {
  const [issues, setIssues] = useState([]);
  const [projects, setProjects] = useState([]);
  const [members, setMembers] = useState([]);

  const [filters, setFilters] = useState({
    search: "",
    status: "",
    priority: "",
  });

  const [form, setForm] = useState({
    projectId: "",
    title: "",
    description: "",
    priority: "MEDIUM",
    assignedTo: "",
    dueDate: "",
  });

  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [dueDateSort, setDueDateSort] = useState("soonest");
  const [pingingIssueId, setPingingIssueId] = useState(null);

  const navigate = useNavigate();

  const loadInitialData = useCallback(async () => {
    try {
      const [projectsResponse, issuesResponse] = await Promise.all([
        api.get("/projects"),
        api.get("/issues"),
      ]);

      setProjects(projectsResponse.data);
      setIssues(issuesResponse.data);
      setError("");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load issue data");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => void loadInitialData(), 0);
    return () => window.clearTimeout(timer);
  }, [loadInitialData]);

  const loadIssues = useCallback(async () => {
    try {
      const params = {};

      if (filters.search) {
        params.search = filters.search;
      }

      if (filters.status) {
        params.status = filters.status;
      }

      if (filters.priority) {
        params.priority = filters.priority;
      }

      const response = await api.get("/issues", {
        params,
      });

      setIssues(response.data);
      setError("");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load issues");
    }
  }, [filters]);

  useEffect(() => {
    const refreshIssues = () => void loadIssues();
    window.addEventListener("focus", refreshIssues);
    return () => window.removeEventListener("focus", refreshIssues);
  }, [loadIssues]);

  const loadMembers = async (projectId) => {
    if (!projectId) {
      setMembers([]);
      setShowCreateForm(false);
      return;
    }

    try {
      const response = await api.get(`/projects/${projectId}/members`);

      setMembers(response.data.filter((member) => member.role === "EMPLOYEE"));
    } catch (err) {
      setMembers([]);
      setError(err.response?.data?.message || "Failed to load project members");
    }
  };

  const handleFilterChange = (e) => {
    setFilters({
      ...filters,
      [e.target.name]: e.target.value,
    });
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));

    if (name === "projectId") {
      loadMembers(value);

      setForm((current) => ({
        ...current,
        projectId: value,
        assignedTo: "",
      }));
    }
  };

  const createIssue = async (e) => {
    e.preventDefault();

    setError("");
    setMessage("");

    try {
      await api.post("/issues", {
        projectId: Number(form.projectId),
        title: form.title,
        description: form.description,
        priority: form.priority,
        assignedTo: form.assignedTo ? Number(form.assignedTo) : null,
        dueDate: form.dueDate || null,
      });

      setMessage("Issue created successfully");

      setForm({
        projectId: "",
        title: "",
        description: "",
        priority: "MEDIUM",
        assignedTo: "",
        dueDate: "",
      });

      setMembers([]);

      await loadIssues();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create issue");
    }
  };

  const updateStatus = async (issueId, status) => {
    setError("");
    setMessage("");

    try {
      await api.patch(`/issues/${issueId}/status`, { status });

      setMessage("Issue status updated");
      await loadIssues();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to update issue status");
    }
  };

  const pingIssue = async (issueId) => {
    setError("");
    setMessage("");
    setPingingIssueId(issueId);
    try {
      const response = await api.post(`/issues/${issueId}/ping`);
      setMessage(response.data.message || "Issue reminder sent.");
    } catch (err) {
      setError(err.response?.data?.message || "Could not send the issue reminder.");
    } finally {
      setPingingIssueId(null);
    }
  };

  const sortedIssues = sortIssuesByDueDate(issues, dueDateSort);

  if (loading) {
    return (
      <div className="page">
        <p>Loading issues...</p>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header"><div><h1>Issues</h1><p>Track tasks, bugs and work across your projects.</p></div><button className="primary-button" onClick={() => setShowCreateForm(true)}>+ Create Issue</button></div>

      {error && <p className="error">{error}</p>}

      {message && <p className="success">{message}</p>}

      {showCreateForm && <div className="modal-overlay" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowCreateForm(false); }}><div className="project-form-container modal-card" role="dialog" aria-modal="true" aria-labelledby="create-issue-title">
        <div className="modal-heading"><div><h2 id="create-issue-title">Create issue</h2><p>Add work to a project and assign an owner.</p></div><button className="icon-button" aria-label="Close" onClick={() => setShowCreateForm(false)}>×</button></div>

        <form className="project-form" onSubmit={createIssue}>
          <label>Project<select
            name="projectId"
            value={form.projectId}
            onChange={handleFormChange}
            required
          >
            <option value="">Select Project</option>

            {projects.map((project) => (
              <option key={project.id} value={project.id}>
                {project.name}
              </option>
            ))}
          </select></label>

          <label>Issue title<input
            type="text"
            name="title"
            placeholder="Issue title"
            value={form.title}
            onChange={handleFormChange}
            required
          /></label>

          <label>Description<textarea
            name="description"
            placeholder="Issue description"
            value={form.description}
            onChange={handleFormChange}
            rows="4"
          /></label>

          <label>Priority<select
            name="priority"
            value={form.priority}
            onChange={handleFormChange}
          >
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="CRITICAL">CRITICAL</option>
          </select></label>

          <label>Assignee<select
            name="assignedTo"
            value={form.assignedTo}
            onChange={handleFormChange}
          >
            <option value="">Select Employee</option>

            {members.map((member) => (
              <option key={member.userId} value={member.userId}>
                {member.name} - {member.email}
              </option>
            ))}
          </select></label>

          <div className="form-row">
            <div>
              <label>Due Date</label>

              <input
                type="date"
                name="dueDate"
                value={form.dueDate}
                onChange={handleFormChange}
              />
            </div>
          </div>

          <div className="modal-actions"><button type="button" className="secondary-button" onClick={() => setShowCreateForm(false)}>Cancel</button><button type="submit" className="primary-button">Create Issue</button></div>
        </form>
      </div></div>}

      <div className="filter-bar">
        <input
          type="text"
          name="search"
          placeholder="Search issues..."
          value={filters.search}
          onChange={handleFilterChange}
        />

        <select
          name="status"
          value={filters.status}
          onChange={handleFilterChange}
        >
          <option value="">All Status</option>
          <option value="OPEN">OPEN</option>
          <option value="IN_PROGRESS">IN_PROGRESS</option>
          <option value="IN_REVIEW">IN_REVIEW</option>
          <option value="TESTING">TESTING</option>
          <option value="RESOLVED">RESOLVED</option>
          <option value="CLOSED">CLOSED</option>
        </select>

        <select
          name="priority"
          value={filters.priority}
          onChange={handleFilterChange}
        >
          <option value="">All Priority</option>
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
          <option value="CRITICAL">CRITICAL</option>
        </select>

        <button onClick={loadIssues}>Search</button>
        <select aria-label="Sort issues by due date" value={dueDateSort} onChange={(event) => setDueDateSort(event.target.value)}>
          <option value="soonest">Due date: soonest first</option>
          <option value="latest">Due date: latest first</option>
          <option value="none">Default order</option>
        </select>
      </div>

      <div className="table-container">
        <h2>All Issues</h2>

        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Project</th>
              <th>Title</th>
              <th>Priority</th>
              <th>Status</th>
              <th>Assigned To</th>
              <th>Due Date</th>
              <th>Next Status</th>
              <th>Details</th>
              <th>Notify</th>
            </tr>
          </thead>

          <tbody>
            {issues.length === 0 ? (
              <tr>
                <td colSpan="10">No issues found</td>
              </tr>
            ) : (
              sortedIssues.map((issue) => {
                const nextStatus = getNextStatus(issue.status);

                return (
                  <tr key={issue.id}>
                    <td>{issue.id}</td>

                    <td>{issue.projectName}</td>

                    <td>{issue.title}</td>

                    <td>{issue.priority}</td>

                    <td><span className={`status-badge issue-status-${issue.status.toLowerCase()}`}>{issue.status.replaceAll("_", " ")}</span></td>

                    <td>{issue.assignedToName || "Unassigned"}</td>

                    <td>{issue.dueDate || "-"}</td>

                    <td>
                      {nextStatus ? (
                        <button
                          onClick={() => updateStatus(issue.id, nextStatus)}
                        >
                          {nextStatus === "OPEN" ? "Reopen" : nextStatus.replaceAll("_", " ")}
                        </button>
                      ) : (
                        <span>Completed</span>
                      )}
                    </td>

                    <td>
                      <button
                        onClick={() => navigate(`/admin/issues/${issue.id}`)}
                      >
                        View
                      </button>
                    </td>

                    <td><button className="issue-ping-button" onClick={() => pingIssue(issue.id)} disabled={pingingIssueId === issue.id} aria-label={pingingIssueId === issue.id ? "Sending notification" : "Notify project manager and assignee"} title={pingingIssueId === issue.id ? "Sending notification" : "Notify project manager and assignee"}><svg aria-hidden="true" viewBox="0 0 20 20"><path d="M15.5 7.5a5.5 5.5 0 0 0-11 0c0 6-2 6-2 7.5h15c0-1.5-2-1.5-2-7.5ZM8 17h4" /></svg></button></td>
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

export default AdminIssues;
