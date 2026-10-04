import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../services/api";
import AttachmentList from "../../components/common/AttachmentList";

function AdminIssueDetails() {
  const { issueId } = useParams();

  const [issue, setIssue] = useState(null);
  const [members, setMembers] = useState([]);
  const [comments, setComments] = useState([]);
  const [activities, setActivities] = useState([]);
  const [showReopenConfirm, setShowReopenConfirm] = useState(false);
  const [assigneeUnavailable, setAssigneeUnavailable] = useState(false);

  const [form, setForm] = useState({
    title: "",
    description: "",
    priority: "MEDIUM",
    assignedTo: "",
    dueDate: "",
  });

  const [comment, setComment] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    try {
      const [issueResponse, commentsResponse, activityResponse] = await Promise.all([
        api.get(`/issues/${issueId}`),
        api.get(`/issues/${issueId}/comments`),
        api.get(`/activities?issueId=${issueId}`),
      ]);

      const currentIssue = issueResponse.data;

      if (!currentIssue) {
        throw new Error("Issue not found");
      }

      setIssue(currentIssue);

      setForm({
        title: currentIssue.title || "",
        description: currentIssue.description || "",
        priority: currentIssue.priority || "MEDIUM",
        assignedTo: currentIssue.assignedTo
          ? String(currentIssue.assignedTo)
          : "",
        dueDate: currentIssue.dueDate || "",
      });

      const membersResponse = await api.get(
        `/projects/${currentIssue.projectId}/members`,
      );

      setMembers(
        membersResponse.data.filter((member) => member.role === "EMPLOYEE" && member.active),
      );

      setComments(commentsResponse.data);
      setActivities(activityResponse.data);
      setError("");
    } catch (err) {
      setError(
        err.response?.data?.message || err.message || "Failed to load issue",
      );
    } finally {
      setLoading(false);
    }
  }, [issueId]);

  useEffect(() => {
    const timer = window.setTimeout(() => void loadData(), 0);
    return () => window.clearTimeout(timer);
  }, [loadData]);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const updateIssue = async (e) => {
    e.preventDefault();

    setError("");
    setMessage("");

    try {
      await api.put(`/issues/${issueId}`, {
        title: form.title,
        description: form.description,
        priority: form.priority,
        assignedTo: form.assignedTo ? Number(form.assignedTo) : null,
        dueDate: form.dueDate || null,
      });

      setMessage("Issue updated successfully");

      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to update issue");
    }
  };

  const updateStatus = async (status) => {
    setError("");
    setMessage("");

    try {
      const response = await api.patch(`/issues/${issueId}/status`, { status });
      setIssue(response.data);
      setAssigneeUnavailable(response.data.previousAssigneeUnavailable || false);
      setMessage(status === "OPEN" && ["RESOLVED", "CLOSED"].includes(issue.status) ? "Issue reopened successfully" : "Status updated");
      await loadData();
      return true;
    } catch (err) {
      setError(err.response?.data?.message || "Failed to update status");
      return false;
    }
  };

  const reopenIssue = async () => {
    setShowReopenConfirm(false);
    await updateStatus("OPEN");
  };

  const addComment = async (e) => {
    e.preventDefault();

    if (!comment.trim()) {
      return;
    }

    setError("");
    setMessage("");

    try {
      await api.post(`/issues/${issueId}/comments`, {
        content: comment,
      });

      setComment("");
      setMessage("Comment added");

      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to add comment");
    }
  };

  const deleteComment = async (commentId) => {
    setError("");
    setMessage("");

    try {
      await api.delete(`/issues/${issueId}/comments/${commentId}`);

      setMessage("Comment deleted");

      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to delete comment");
    }
  };

  if (loading) {
    return (
      <div className="page">
        <p>Loading issue...</p>
      </div>
    );
  }

  if (!issue) {
    return (
      <div className="page">
        <p>Issue not found.</p>
      </div>
    );
  }

  return (
    <div className="page">
      <header className="issue-page-heading">
        <div><span className="issue-eyebrow">ISSUE #{issue.id}</span><h1>{issue.title}</h1><p>{issue.projectName}</p></div>
        <span className={`issue-status-pill issue-status-${issue.status.toLowerCase()}`}>{issue.status.replaceAll("_", " ")}</span>
      </header>

      {error && <p className="error">{error}</p>}

      {message && <p className="success">{message}</p>}

      <div className="issue-details issue-summary-card">
        <p>
          <strong>Project:</strong> {issue.projectName}
        </p>

        <p>
          <strong>Created By:</strong> {issue.createdByName}
        </p>

        <p>
          <strong>Current Status:</strong> {issue.status}
        </p>
      </div>

      {assigneeUnavailable && <div className="assignee-warning" role="status"><strong>Assignment needed</strong><span>Previous assignee is no longer available. Please assign the issue to a project member.</span></div>}

      <div className="project-form-container">
        <h2>Edit Issue</h2>

        <form className="project-form" onSubmit={updateIssue}>
          <input
            type="text"
            name="title"
            value={form.title}
            onChange={handleChange}
            placeholder="Issue title"
            required
          />

          <textarea
            name="description"
            value={form.description}
            onChange={handleChange}
            placeholder="Description"
            rows="5"
          />

          <select name="priority" value={form.priority} onChange={handleChange}>
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="CRITICAL">CRITICAL</option>
          </select>

          <select
            name="assignedTo"
            value={form.assignedTo}
            onChange={handleChange}
          >
            <option value="">Unassigned</option>

            {members.map((member) => (
              <option key={member.userId} value={member.userId}>
                {member.name} - {member.email}
              </option>
            ))}
          </select>

          <div className="form-row">
            <div>
              <label>Due Date</label>

              <input
                type="date"
                name="dueDate"
                value={form.dueDate}
                onChange={handleChange}
              />
            </div>
          </div>

          <button type="submit">Save Changes</button>
        </form>
      </div>

      <div className="project-form-container admin-workflow-card">
        <div className="workflow-copy"><span className="issue-eyebrow">ADMIN CONTROLS</span><h2>Workflow status</h2><p>Choose any valid status. Changes are recorded in the activity history.</p></div>
        <div className="workflow-actions"><label htmlFor="admin-issue-status">Current status</label>
        <select id="admin-issue-status" value={issue.status} onChange={(event) => {
          if (event.target.value === "OPEN" && (issue.status === "RESOLVED" || issue.status === "CLOSED")) setShowReopenConfirm(true);
          else void updateStatus(event.target.value);
        }}>
          {["OPEN", "IN_PROGRESS", "IN_REVIEW", "TESTING", "RESOLVED", "CLOSED"].map((status) => <option key={status} value={status}>{status}</option>)}
        </select>
        {(issue.status === "RESOLVED" || issue.status === "CLOSED") && <button className="reopen-button" type="button" onClick={() => setShowReopenConfirm(true)}><span aria-hidden="true">↶</span> Reopen Issue</button>}
        </div>
      </div>

      <section className="project-form-container activity-card">
        <div className="activity-heading"><div><span className="issue-eyebrow">AUDIT TRAIL</span><h2>Activity history</h2></div><span className="activity-count">{activities.length} {activities.length === 1 ? "event" : "events"}</span></div>
        {activities.length === 0 ? <p className="activity-empty">No activity has been recorded for this issue yet.</p> : <ol className="issue-timeline">{activities.map((activity) => <li className={activity.action === "ISSUE_REOPENED" ? "reopen-event" : ""} key={activity.id}><span className="timeline-marker" aria-hidden="true"/><div className="timeline-content"><div className="timeline-event-heading"><strong>{activity.action === "ISSUE_REOPENED" ? "Issue reopened" : activity.action.replaceAll("_", " ").toLowerCase()}</strong>{activity.oldStatus && activity.newStatus && <span className="timeline-transition">{activity.oldStatus.replaceAll("_", " ")} <span aria-hidden="true">→</span> {activity.newStatus.replaceAll("_", " ")}</span>}</div><p>{activity.description}</p><small>{activity.actorName} <span>·</span> {new Date(activity.createdAt).toLocaleString()}</small></div></li>)}</ol>}
      </section>

      {showReopenConfirm && <div className="modal-overlay" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowReopenConfirm(false); }}><div className="confirm-dialog reopen-confirm-dialog" role="alertdialog" aria-modal="true" aria-labelledby="reopen-dialog-title" aria-describedby="reopen-dialog-description"><span className="reopen-dialog-icon" aria-hidden="true">↶</span><h2 id="reopen-dialog-title">Reopen this issue?</h2><p id="reopen-dialog-description">The issue will return to <strong>OPEN</strong>. Its project, team, comments, attachments, and valid assignment will be kept.</p><div className="modal-actions"><button type="button" className="secondary-button" onClick={() => setShowReopenConfirm(false)}>Cancel</button><button type="button" className="reopen-button" onClick={reopenIssue}>Reopen Issue</button></div></div></div>}

      <AttachmentList issueId={issueId} />
      <div className="comments-section">
        <h2>Comments</h2>

        {comments.length === 0 ? (
          <p>No comments yet.</p>
        ) : (
          comments.map((item) => (
            <div className="comment-box" key={item.id}>
              <strong>{item.userName}</strong>

              <span>{item.createdAt}</span>

              <p>{item.content}</p>

              <button
                className="remove-button"
                onClick={() => deleteComment(item.id)}
              >
                Delete
              </button>
            </div>
          ))
        )}

        <form className="comment-form" onSubmit={addComment}>
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Write a comment..."
            rows="4"
          />

          <button type="submit">Add Comment</button>
        </form>
      </div>
    </div>
  );
}

export default AdminIssueDetails;
