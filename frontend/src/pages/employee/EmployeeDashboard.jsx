import { useEffect, useState } from "react";
import api from "../../services/api";
import { useSelector } from "react-redux";

function EmployeeDashboard() {
  const [data, setData] = useState(null);
  const [projects, setProjects] = useState([]);
  const [issues, setIssues] = useState([]);
  const [error, setError] = useState("");
  const [contactProjectId, setContactProjectId] = useState("");
  const [contactSubject, setContactSubject] = useState("");
  const [contactDetails, setContactDetails] = useState("");
  const [contactError, setContactError] = useState("");
  const [contactSent, setContactSent] = useState(false);
  const [contactSending, setContactSending] = useState(false);
  const user = useSelector((state) => state.auth.user);
  const contactableProjects = projects.filter((project) => project.managerId && project.managerName);

  const sendManagerMessage = async (event) => {
    event.preventDefault();
    if (!contactProjectId || !contactSubject.trim() || !contactDetails.trim()) return;
    setContactSending(true);
    setContactError("");
    setContactSent(false);
    try {
      await api.post("/support/manager", {
        projectId: Number(contactProjectId),
        subject: contactSubject.trim(),
        details: contactDetails.trim(),
      });
      setContactSent(true);
      setContactSubject("");
      setContactDetails("");
    } catch (err) {
      setContactError(err.response?.data?.message || "Your message could not be emailed to the manager.");
    } finally {
      setContactSending(false);
    }
  };

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const [response, projectResponse, issueResponse] = await Promise.all([api.get("/dashboard"), api.get("/projects"), api.get("/issues")]);
        setData(response.data);
        setProjects(projectResponse.data);
        setIssues(issueResponse.data);
      } catch (err) {
        setError(err.response?.data?.message || "Failed to load dashboard");
      }
    };

    loadDashboard();
  }, []);

  if (error) {
    return (
      <div className="page">
        <p className="error">{error}</p>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="page">
        <p>Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div className="page command-dashboard">
      <header className="command-heading"><div><span className="command-eyebrow">PERSONAL / OVERVIEW</span><h1>My workspace</h1><p>{user?.name || "Team member"}<span> / </span>{new Intl.DateTimeFormat(undefined, { weekday: "long", month: "long", day: "numeric" }).format(new Date())}</p></div><div className="command-actions"><a href="/employee/issues" className="command-primary">Open my issues <span>→</span></a></div></header>

      <section className="metric-rail">{[["Projects", data.totalProjects], ["Issues", data.totalIssues], ["Open", data.openIssues], ["In progress", data.inProgressIssues], ["Resolved", data.resolvedIssues]].map(([name, value]) => <div className="metric-cell" key={name}><span>{name}</span><strong>{value ?? "—"}</strong><small>{name === "Open" ? "Needs a start" : name === "Resolved" ? "Completed work" : "Assigned to you"}</small></div>)}</section>
      <div className="command-grid"><section className="portfolio-panel"><header className="command-panel-heading"><div><span className="command-eyebrow">MY PORTFOLIO</span><h2>Projects I’m part of</h2></div><a href="/employee/projects">All projects →</a></header><div className="command-table-scroll"><table className="command-table"><thead><tr><th>Project</th><th>Status</th><th>Manager</th><th>End date</th></tr></thead><tbody>{projects.slice(0,8).map((project)=><tr key={project.id}><td><span className="command-project"><span>{(project.name||"P").trim()[0].toUpperCase()}</span><span><strong>{project.name}</strong><small>{project.description||"Project workspace"}</small></span></span></td><td><span className={`command-status status-${String(project.status||"planning").toLowerCase().replaceAll("_","-")}`}><i />{String(project.status||"Planning").toLowerCase().replaceAll("_"," ")}</span></td><td>{project.managerName||"—"}</td><td>{project.endDate||"—"}</td></tr>)}</tbody></table>{!projects.length&&<div className="command-empty"><strong>No projects assigned</strong><span>Your project work will appear here.</span></div>}</div></section>
        <aside className="command-rail"><section className="command-side-panel"><header className="command-panel-heading"><div><span className="command-eyebrow">MY WORK</span><h2>Issue queue</h2></div><a className="queue-count" href="/employee/issues">{issues.length}</a></header><ul className="command-queue">{issues.slice(0,8).map((issue)=><li key={issue.id}><a href={`/employee/issues/${issue.id}`}><i className={`priority-${String(issue.priority||"medium").toLowerCase()}`} /><span><strong>{issue.title}</strong><small>{issue.projectName} · {String(issue.status).toLowerCase().replaceAll("_"," ")}</small></span><small>{issue.dueDate||""}</small></a></li>)}</ul>{!issues.length&&<p className="command-quiet">No issues assigned right now.</p>}</section><section className="command-side-panel employee-contact-manager"><header className="command-panel-heading"><div><span className="command-eyebrow">NEED HELP?</span><h2>Email your manager</h2></div><span className="employee-contact-icon" aria-hidden="true">✉</span></header><p className="command-quiet">Choose a project to email its manager directly. Replies go to your account email.</p>{contactableProjects.length ? <form className="employee-contact-form" onSubmit={sendManagerMessage}><label htmlFor="manager-contact-project">Project and manager</label><select id="manager-contact-project" value={contactProjectId} onChange={(event) => setContactProjectId(event.target.value)} required><option value="">Choose a project</option>{contactableProjects.map((project) => <option key={project.id} value={project.id}>{project.name} — {project.managerName}</option>)}</select><label htmlFor="manager-contact-subject">Subject</label><input id="manager-contact-subject" value={contactSubject} onChange={(event) => setContactSubject(event.target.value)} maxLength={150} required placeholder="What do you need help with?"/><label htmlFor="manager-contact-details">Message</label><textarea id="manager-contact-details" value={contactDetails} onChange={(event) => setContactDetails(event.target.value)} maxLength={5000} required rows={4} placeholder="Write a message to your project manager…"/>{contactError && <p className="form-error" role="alert">{contactError}</p>}{contactSent && <p className="form-success" role="status">Email sent to the selected project manager.</p>}<button className="primary-button" disabled={contactSending || !contactProjectId || !contactSubject.trim() || !contactDetails.trim()}>{contactSending ? "Sending…" : "Email manager"}</button></form> : <p className="command-quiet">Your projects do not have an assigned manager yet.</p>}</section></aside></div>
    </div>
  );
}

export default EmployeeDashboard;
