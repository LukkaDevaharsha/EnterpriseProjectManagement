import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../services/api";
import ProgressBar from "../../components/common/ProgressBar";

function ManagerProjects() {
  const [projects, setProjects] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [unreadChats, setUnreadChats] = useState({});

  const navigate = useNavigate();

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
              <th>Start Date</th>
              <th>End Date</th>
              <th>Team</th>
              <th>Project chat</th>
              <th>Progress</th>
            </tr>
          </thead>

          <tbody>
            {projects.length === 0 ? (
              <tr>
                <td colSpan="8">No projects assigned to you.</td>
              </tr>
            ) : (
              projects.map((project) => (
                <tr key={project.id}>
                  <td>{project.id}</td>

                  <td>{project.name}</td>

                  <td><span className={`status-badge project-status project-status-${String(project.status || "planning").toLowerCase().replaceAll("_", "-")}`}>{project.status}</span></td>

                  <td>{project.startDate || "-"}</td>

                  <td>{project.endDate || "-"}</td>

                  <td>
                    <button
                      onClick={() =>
                        navigate(`/manager/projects/${project.id}/team`)
                      }
                    >
                      View Team
                    </button>
                    <button className="workflow-link-button" onClick={() => navigate(`/manager/projects/${project.id}/workflow`)}>Workflow</button>
                  </td>
                  <td><button className={`project-chat-open-button ${unreadChats[project.id] > 0 ? "has-unread" : ""}`} onClick={() => navigate(`/manager/projects/${project.id}/chat`)} aria-label={`Open ${project.name} team chat${unreadChats[project.id] > 0 ? `, ${unreadChats[project.id]} unread messages` : ""}`}><span className="project-chat-icon" aria-hidden="true">▤</span>Open team chat{unreadChats[project.id] > 0 && <span className="chat-unread-badge">{unreadChats[project.id] > 99 ? "99+" : unreadChats[project.id]}</span>}</button></td>
                  <td>
                    <ProgressBar value={project.progressPercent} />
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default ManagerProjects;
