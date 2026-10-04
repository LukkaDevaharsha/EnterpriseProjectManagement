import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../services/api";
import AttachmentList from "../../components/common/AttachmentList";

function EmployeeIssueDetails() {
  const { issueId } = useParams();

  const [issue, setIssue] = useState(null);
  const [comments, setComments] = useState([]);
  const [content, setContent] = useState("");

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

      setIssue(currentIssue);
      setComments(commentsResponse.data);
      setError("");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load issue");
    } finally {
      setLoading(false);
    }
  }, [issueId]);

  useEffect(() => {
    const timer = window.setTimeout(() => void loadData(), 0);
    return () => window.clearTimeout(timer);
  }, [loadData]);

  const addComment = async (e) => {
    e.preventDefault();

    if (!content.trim()) {
      return;
    }

    try {
      await api.post(`/issues/${issueId}/comments`, {
        content: content,
      });

      setContent("");
      setMessage("Comment added");

      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to add comment");
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
      <h1>{issue.title}</h1>

      {error && <p className="error">{error}</p>}

      {message && <p className="success">{message}</p>}

      <div className="issue-details">
        <p>
          <strong>Project:</strong> {issue.projectName}
        </p>

        <p>
          <strong>Description:</strong> {issue.description || "No description"}
        </p>

        <p>
          <strong>Priority:</strong> {issue.priority}
        </p>

        <p>
          <strong>Status:</strong> {issue.status}
        </p>

        <p>
          <strong>Due Date:</strong> {issue.dueDate || "-"}
        </p>

        <p>
          <strong>Created By:</strong> {issue.createdByName}
        </p>
      </div>

      <div className="comments-section">
        <AttachmentList issueId={issueId} />

        <h2>Comments</h2>

        {comments.length === 0 ? (
          <p>No comments yet.</p>
        ) : (
          comments.map((comment) => (
            <div className="comment-box" key={comment.id}>
              <strong>{comment.userName}</strong>

              <span>{comment.createdAt}</span>

              <p>{comment.content}</p>
            </div>
          ))
        )}

        <form className="comment-form" onSubmit={addComment}>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Write a comment..."
            rows="4"
          />

          <button type="submit">Add Comment</button>
        </form>
      </div>
    </div>
  );
}

export default EmployeeIssueDetails;
