import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useSelector } from "react-redux";
import api from "../../services/api";

export default function ProjectChat() {
  const { projectId } = useParams();
  const user = useSelector((state) => state.auth.user);
  const role = user?.role?.toLowerCase() || "employee";
  const [project, setProject] = useState(null);
  const [teamMembers, setTeamMembers] = useState([]);
  const [messages, setMessages] = useState([]);
  const [content, setContent] = useState("");
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);
  const bottomRef = useRef(null);
  const load = useCallback(async () => {
    try {
      const [chat, projectResponse, membersResponse] = await Promise.all([api.get(`/projects/${projectId}/chat`), api.get(`/projects/${projectId}`), api.get(`/projects/${projectId}/members`)]);
      setMessages(chat.data); setProject(projectResponse.data); setTeamMembers(membersResponse.data); setError("");
    } catch (err) { setError(err.response?.data?.message || "Could not load this project chat."); }
  }, [projectId]);
  useEffect(() => { void load(); const timer = window.setInterval(() => void load(), 5000); return () => window.clearInterval(timer); }, [load]);
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages]);
  const send = async (event) => {
    event.preventDefault(); if (!content.trim() || sending) return;
    setSending(true); setError("");
    try { const response = await api.post(`/projects/${projectId}/chat`, { content: content.trim() }); setMessages((current) => [...current.filter((item) => item.id !== response.data.id), response.data]); setContent(""); }
    catch (err) { setError(err.response?.data?.message || "Message could not be sent."); }
    finally { setSending(false); }
  };
  const rosterNames = [project?.managerName, ...teamMembers.map((member) => member.name)].filter(Boolean);
  return <div className="page project-chat-page"><div className="page-header"><div className="project-chat-heading"><Link className="chat-back-link" to={`/${role}/projects`}>← Projects</Link><h1>{project?.name || "Project team chat"}</h1><p className="project-chat-roster" title={rosterNames.join(", ")}>{rosterNames.length ? rosterNames.join(" · ") : "Project manager and team members"}</p></div><span className="chat-live-indicator"><i /> Team chat</span></div>
    {error && <p className="form-error" role="alert">{error}</p>}
    <section className="project-chat-card" aria-label="Project messages"><div className="project-chat-messages" aria-live="polite">{messages.length ? messages.map((message) => <article key={message.id} className={`chat-message ${message.senderId === user?.id ? "chat-message-own" : ""}`}><span className="chat-avatar">{message.senderName.split(/\s+/).map((part) => part[0]).slice(0, 2).join("").toUpperCase()}</span><div className="chat-message-body"><div className="chat-message-meta"><strong>{message.senderName}{message.senderId === user?.id ? " (you)" : ""}</strong><time>{new Date(message.createdAt).toLocaleString()}</time></div><p>{message.content}</p></div></article>) : <div className="chat-empty"><span>✳</span><h2>Start the conversation</h2><p>Share an update or ask your project team a question.</p></div>}<div ref={bottomRef} /></div>
      <form className="project-chat-form" onSubmit={send}><label className="sr-only" htmlFor="project-chat-input">Write a message</label><textarea id="project-chat-input" value={content} onChange={(event) => setContent(event.target.value)} maxLength={2000} placeholder="Write a message to your team…" rows={2} /><button className="primary-button" disabled={!content.trim() || sending}>{sending ? "Sending…" : "Send message"}</button></form></section>
  </div>;
}
