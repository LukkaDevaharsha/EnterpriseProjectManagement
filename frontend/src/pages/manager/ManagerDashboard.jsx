import { useEffect, useState } from "react";
import api from "../../services/api";
import { useSelector } from "react-redux";

function ManagerDashboard() {
  const [data, setData] = useState(null);
  const [projects, setProjects] = useState([]);
  const [issues, setIssues] = useState([]);
  const [error, setError] = useState("");
  const user = useSelector((state) => state.auth.user);

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
      <header className="command-heading"><div><span className="command-eyebrow">PROJECTS / OVERVIEW</span><h1>Project overview</h1><p>{user?.name || "Manager"}<span> / </span>{new Intl.DateTimeFormat(undefined, { weekday: "long", month: "long", day: "numeric" }).format(new Date())}</p></div><div className="command-actions"><a href="/manager/issues" className="command-primary">Open issue tracker <span>→</span></a></div></header>

      <section className="metric-rail">{[["Projects", data.totalProjects], ["Issues", data.totalIssues], ["Open", data.openIssues], ["In progress", data.inProgressIssues], ["Resolved", data.resolvedIssues]].map(([name, value]) => <div className="metric-cell" key={name}><span>{name}</span><strong>{value ?? "—"}</strong><small>{name === "Open" ? "Awaiting action" : name === "Resolved" ? "Completed" : "In your scope"}</small></div>)}</section>
      <div className="command-grid"><section className="portfolio-panel"><header className="command-panel-heading"><div><span className="command-eyebrow">PORTFOLIO</span><h2>Projects in your scope</h2></div><a href="/manager/projects">All projects →</a></header><div className="command-table-scroll"><table className="command-table"><thead><tr><th>Project</th><th>Status</th><th>Progress</th><th>End date</th><th>Team</th></tr></thead><tbody>{projects.slice(0,8).map((project)=>{const progress=Math.min(100,Math.max(0,Number(project.progressPercent)||0));return <tr key={project.id}><td><a className="command-project" href={`/manager/projects/${project.id}/team`}><span>{(project.name||"P").trim()[0].toUpperCase()}</span><span><strong>{project.name}</strong><small>{project.managerName||"Managed project"}</small></span></a></td><td><span className={`command-status status-${String(project.status||"planning").toLowerCase().replaceAll("_","-")}`}><i />{String(project.status||"Planning").toLowerCase().replaceAll("_"," ")}</span></td><td><div className="command-progress"><span><i style={{width:`${progress}%`}} /></span><small>{progress}%</small></div></td><td>{project.endDate||"—"}</td><td><a className="subtle-link" href={`/manager/projects/${project.id}/team`}>View team →</a></td></tr>;})}</tbody></table>{!projects.length&&<div className="command-empty"><strong>No projects assigned</strong><span>Projects in your scope will appear here.</span></div>}</div></section>
        <aside className="command-rail"><section className="command-side-panel"><header className="command-panel-heading"><div><span className="command-eyebrow">WORK QUEUE</span><h2>Needs attention</h2></div><a className="queue-count" href="/manager/issues">{issues.filter((issue)=>issue.status==="OPEN"||issue.status==="IN_REVIEW"||issue.priority==="CRITICAL").length}</a></header><ul className="command-queue">{issues.filter((issue)=>issue.status==="OPEN"||issue.status==="IN_REVIEW"||issue.priority==="CRITICAL").slice(0,7).map((issue)=><li key={issue.id}><a href={`/manager/issues/${issue.id}`}><i className={`priority-${String(issue.priority||"medium").toLowerCase()}`} /><span><strong>{issue.title}</strong><small>{issue.projectName} · {String(issue.status).toLowerCase().replaceAll("_"," ")}</small></span></a></li>)}</ul></section></aside></div>
    </div>
  );
}

export default ManagerDashboard;
