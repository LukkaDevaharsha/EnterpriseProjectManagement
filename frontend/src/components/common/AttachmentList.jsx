import { useCallback, useEffect, useState } from "react";
import api from "../../services/api";
export default function AttachmentList({ issueId }) {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");
  const [file, setFile] = useState(null);
  const load = useCallback(() =>
    api
      .get(`/issues/${issueId}/attachments`)
      .then((r) => setItems(r.data))
      .catch((e) =>
        setError(e.response?.data?.message || "Could not load attachments"),
      ), [issueId]);
  useEffect(() => {
    const timer = window.setTimeout(() => void load(), 0);
    return () => window.clearTimeout(timer);
  }, [load]);
  const upload = async (e) => {
    e.preventDefault();
    if (!file) return;
    const body = new FormData();
    body.append("file", file);
    try {
      await api.post(`/issues/${issueId}/attachments`, body);
      setFile(null);
      e.target.reset();
      setError("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Upload failed");
    }
  };
  const download = async (item) => {
    try {
      const r = await api.get(`/issues/${issueId}/attachments/${item.id}`, {
        responseType: "blob",
      });
      const url = URL.createObjectURL(r.data);
      const a = document.createElement("a");
      a.href = url;
      a.download = item.originalName;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setError("Download failed");
    }
  };
  const remove = async (item) => {
    if (!window.confirm(`Delete ${item.originalName}?`)) return;
    try {
      await api.delete(`/issues/${issueId}/attachments/${item.id}`);
      load();
    } catch (e) {
      setError(e.response?.data?.message || "Delete failed");
    }
  };
  return (
    <section className="comments-section">
      <h2>Attachments</h2>
      {error && <p className="error">{error}</p>}
      {items.length === 0 ? (
        <p>No attachments yet.</p>
      ) : (
        items.map((i) => (
          <p key={i.id}>
            {i.originalName} ({Math.ceil(i.fileSize / 1024)} KB){" "}
            <button type="button" onClick={() => download(i)}>
              Download
            </button>{" "}
            <button type="button" onClick={() => remove(i)}>
              Delete
            </button>
          </p>
        ))
      )}
      <form onSubmit={upload}>
        <input
          type="file"
          onChange={(e) => setFile(e.target.files?.[0] || null)}
          required
        />
        <button>Upload attachment</button>
      </form>
    </section>
  );
}
