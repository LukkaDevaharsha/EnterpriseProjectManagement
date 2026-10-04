import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../services/api";
import AttachmentList from "../../components/common/AttachmentList";

function ManagerIssueDetails() {
  const { issueId } = useParams();

  const [issue, setIssue] = useState(null);
  const [members, setMembers] = useState([]);
  const [comments, setComments] = useState([]);

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
      const [issuesResponse, commentsResponse] = await Promise.all([
        api.get("/issues"),
        api.get(`/issues/${issueId}/comments`),
      ]);

      const currentIssue = issuesResponse.data.find(
        (item) => item.id === Number(issueId),
      );

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
        membersResponse.data.filter((member) => member.role === "EMPLOYEE"),
      );

      setComments(commentsResponse.data);
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
      await api.patch(`/issues/${issueId}/status`, { status });

      setMessage("Status updated");

      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to update status");
    }
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
      <h1>Issue Details</h1>

      {error && <p className="error">{error}</p>}

      {message && <p className="success">{message}</p>}

      <div className="issue-details">
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

      <div className="project-form-container">
        <h2>Change Status</h2>

        <div className="team-add-row">
          {issue.status === "RESOLVED" && <button onClick={() => updateStatus("OPEN")}>Reopen Resolved Issue</button>}
          <button onClick={() => updateStatus("IN_PROGRESS")}>
            In Progress
          </button>

          <button onClick={() => updateStatus("IN_REVIEW")}>In Review</button>

          <button onClick={() => updateStatus("TESTING")}>Testing</button>

          <button onClick={() => updateStatus("RESOLVED")}>Resolved</button>

          <button onClick={() => updateStatus("CLOSED")}>Closed</button>
        </div>
      </div>

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

export default ManagerIssueDetails;
