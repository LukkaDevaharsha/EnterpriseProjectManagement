import { useEffect, useState } from "react";
import api from "../../services/api";
import { useSelector } from "react-redux";

function AdminDashboard() {
  const [data, setData] = useState(null);
  const [system, setSystem] = useState(null);
  const [projects, setProjects] = useState([]);
  const [issues, setIssues] = useState([]);
  const [error, setError] = useState("");
  const user = useSelector((state) => state.auth.user);

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const response = await api.get("/dashboard");
        setData(response.data);
        const [statistics, projectList, issueList] = await Promise.all([
          api.get("/admin/statistics"), api.get("/projects"), api.get("/issues"),
        ]);
        setSystem(statistics.data);
        setProjects(projectList.data);
        setIssues(issueList.data);
      } catch (err) {
        setError(err.response?.data?.message || "Failed to load dashboard");
      }
    };

    loadDashboard();
  }, []);

  if (error) {
    return <p>{error}</p>;
  }

  if (!data) {
    return <p>Loading dashboard...</p>;
  }

  return (
    <div className="dashboard">
      <span className="command-eyebrow">ORGANIZATION / OVERVIEW</span>
      <h1>Dashboard</h1>
      <p className="dashboard-subtitle">Organization overview · {user?.name || "Administrator"}</p>

      <section className="metric-rail" aria-label="Organization metrics">
        {[["People", data.totalUsers, "Organization"], ["Projects", data.totalProjects, "Organization"], ["Issues", data.totalIssues, "Organization"], ["Resolved", data.resolvedIssues, "Completed"], ["Inactive users", system?.inactiveUsers, "Accounts"]].map(([name, value, detail]) => <div className="metric-cell" key={name}><span>{name}</span><strong>{value ?? "—"}</strong><small>{detail}</small></div>)}
      </section>
      <div className="command-grid">
        <section className="portfolio-panel"><header className="command-panel-heading"><div><span className="command-eyebrow">PORTFOLIO</span><h2>Projects across the organization</h2></div><a href="/admin/projects">All projects →</a></header><div className="command-table-scroll"><table className="command-table"><thead><tr><th>Project</th><th>Status</th><th>Progress</th><th>End date</th></tr></thead><tbody>{projects.slice(0,7).map((project) => { const progress=Math.min(100,Math.max(0,Number(project.progressPercent)||0)); return <tr key={project.id}><td><a className="command-project" href="/admin/projects"><span>{(project.name||"P").trim()[0].toUpperCase()}</span><span><strong>{project.name}</strong><small>{project.managerName||"Project workspace"}</small></span></a></td><td><span className={`command-status status-${String(project.status||"planning").toLowerCase().replaceAll("_","-")}`}><i />{String(project.status||"Planning").toLowerCase().replaceAll("_"," ")}</span></td><td><div className="command-progress"><span><i style={{width:`${progress}%`}} /></span><small>{progress}%</small></div></td><td>{project.endDate||"—"}</td></tr>; })}</tbody></table>{!projects.length&&<div className="command-empty"><strong>No projects available</strong><span>Projects will appear here as they are created.</span></div>}</div></section>
        <aside className="command-rail"><section className="command-side-panel"><span className="command-eyebrow">WORKLOAD</span><h2>Issue status</h2><div className="command-legend">{[["Open",system?.issuesByStatus?.open],["In progress",system?.issuesByStatus?.in_progress],["Needs review",system?.issuesByStatus?.in_review],["Overdue",system?.overdueIssues]].map(([name,value])=><div key={name}><span>{name}</span><strong>{value??0}</strong></div>)}</div></section>
        <section className="command-side-panel"><header className="command-panel-heading"><div><span className="command-eyebrow">WORK QUEUE</span><h2>Needs attention</h2></div><a className="queue-count" href="/admin/issues">{issues.filter((issue)=>issue.status==="OPEN"||issue.status==="IN_REVIEW"||issue.priority==="CRITICAL").length}</a></header><ul className="command-queue">{issues.filter((issue)=>issue.status==="OPEN"||issue.status==="IN_REVIEW"||issue.priority==="CRITICAL").slice(0,5).map((issue)=><li key={issue.id}><a href={`/admin/issues/${issue.id}`}><i className={`priority-${String(issue.priority||"medium").toLowerCase()}`} /><span><strong>{issue.title}</strong><small>{issue.projectName} · {String(issue.status).toLowerCase().replaceAll("_"," ")}</small></span></a></li>)}</ul></section></aside>
      </div>

      {system && (
        <section className="table-container system-activity-card">
          <h2>Recent System Activity</h2>
          {system.recentActivity.length ? (
            system.recentActivity.slice(0, 8).map((item) => (
              <article className="system-activity-item" key={item.id}><span className="system-activity-marker" aria-hidden="true" /><p>
                {item.description}{" "}
                <small>
                  {item.actorName} · {item.createdAt}
                </small>
              </p></article>
            ))
          ) : (
            <p>No activity recorded yet.</p>
          )}
        </section>
      )}
    </div>
  );
}

export default AdminDashboard;
