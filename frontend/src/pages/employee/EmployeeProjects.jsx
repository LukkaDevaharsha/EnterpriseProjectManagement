import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../services/api";

function EmployeeProjects() {
  const [projects, setProjects] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [unreadChats, setUnreadChats] = useState({});
  const [teamProject, setTeamProject] = useState(null);
  const [teamMembers, setTeamMembers] = useState([]);
  const [teamLoading, setTeamLoading] = useState(false);
  const [teamError, setTeamError] = useState("");
  const navigate = useNavigate();

  const openTeam = async (project) => {
    setTeamProject(project);
    setTeamMembers([]);
    setTeamError("");
    setTeamLoading(true);
    try {
      const response = await api.get(`/projects/${project.id}/members`);
      setTeamMembers(response.data);
    } catch (err) {
      setTeamError(err.response?.data?.message || "Could not load this project team.");
    } finally {
      setTeamLoading(false);
    }
  };

  useEffect(() => {
    const loadProjects = async () => {
      try {
        const response = await api.get("/projects");

        setProjects(response.data);
        const unreadEntries = await Promise.all(response.data.map(async (project) => {
          try {
            const unread = await api.get(`/projects/${project.id}/chat/unread`);
            return [project.id, unread.data.count || 0];
          } catch {
            return [project.id, 0];
          }
        }));
        setUnreadChats(Object.fromEntries(unreadEntries));
      } catch (err) {
        setError(err.response?.data?.message || "Failed to load projects");
      } finally {
        setLoading(false);
      }
    };

    loadProjects();
  }, []);

  useEffect(() => {
    if (!projects.length) return undefined;
    const refreshUnread = async () => {
      const unreadEntries = await Promise.all(projects.map(async (project) => {
        try {
          const response = await api.get(`/projects/${project.id}/chat/unread`);
          return [project.id, response.data.count || 0];
        } catch {
          return [project.id, 0];
        }
      }));
      setUnreadChats(Object.fromEntries(unreadEntries));
    };
    const timer = window.setInterval(() => void refreshUnread(), 10000);
    return () => window.clearInterval(timer);
  }, [projects]);

  if (loading) {
    return (
      <div className="page">
        <p>Loading projects...</p>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>My Projects</h1>

      {error && <p className="error">{error}</p>}

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Status</th>
              <th>Manager</th>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Work plan</th>
              <th>Team</th>
              <th>Project chat</th>
            </tr>
          </thead>

          <tbody>
            {projects.length === 0 ? (
              <tr>
                <td colSpan="9">No projects assigned to you.</td>
              </tr>
            ) : (
              projects.map((project) => (
                <tr key={project.id}>
                  <td>{project.id}</td>

                  <td>{project.name}</td>

                  <td><span className={`status-badge project-status project-status-${String(project.status || "planning").toLowerCase().replaceAll("_", "-")}`}>{project.status}</span></td>

                  <td>{project.managerName || "Not assigned"}</td>

                  <td>{project.startDate || "-"}</td>

                  <td>{project.endDate || "-"}</td>
                  <td><button className="workflow-link-button" onClick={() => navigate(`/employee/projects/${project.id}/workflow`)}>View workflow</button></td>
                  <td><button className="workflow-link-button" onClick={() => void openTeam(project)}>View team</button></td>
                  <td><button className={`project-chat-open-button ${unreadChats[project.id] > 0 ? "has-unread" : ""}`} onClick={() => navigate(`/employee/projects/${project.id}/chat`)} aria-label={`Open ${project.name} team chat${unreadChats[project.id] > 0 ? `, ${unreadChats[project.id]} unread messages` : ""}`}><span className="project-chat-icon" aria-hidden="true">▤</span>Open team chat{unreadChats[project.id] > 0 && <span className="chat-unread-badge">{unreadChats[project.id] > 99 ? "99+" : unreadChats[project.id]}</span>}</button></td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {teamProject && <div className="modal-overlay" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setTeamProject(null); }}><section className="form-card modal-card employee-team-modal" role="dialog" aria-modal="true" aria-labelledby="employee-team-title"><div className="modal-heading"><div><h2 id="employee-team-title">{teamProject.name} team</h2><p>Project manager and assigned team members</p></div><button className="icon-button" aria-label="Close team details" onClick={() => setTeamProject(null)}>×</button></div>{teamProject.managerName && <article className="employee-team-person"><span className="employee-team-avatar">{teamProject.managerName.split(/\s+/).map((word) => word[0]).slice(0, 2).join("").toUpperCase()}</span><div><strong>{teamProject.managerName}</strong><small>Project manager</small></div></article>}{teamLoading ? <p>Loading project team…</p> : teamError ? <p className="form-error">{teamError}</p> : teamMembers.length ? <div className="employee-team-list">{teamMembers.map((member) => <article className="employee-team-person" key={member.userId}><span className="employee-team-avatar">{member.name.split(/\s+/).map((word) => word[0]).slice(0, 2).join("").toUpperCase()}</span><div><strong>{member.name}</strong><small>{[member.employeeId, member.department].filter(Boolean).join(" · ") || "Team member"}</small></div></article>)}</div> : !teamLoading && <p className="empty-state">No team members have been assigned yet.</p>}</section></div>}
    </div>
  );
}

export default EmployeeProjects;
