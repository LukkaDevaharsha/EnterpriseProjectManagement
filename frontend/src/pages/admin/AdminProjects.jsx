import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../services/api";
import ProgressBar from "../../components/common/ProgressBar";

function AdminProjects() {
  const navigate = useNavigate();

  const [projects, setProjects] = useState([]);
  const [managers, setManagers] = useState([]);

  const [form, setForm] = useState({
    name: "",
    description: "",
    startDate: "",
    endDate: "",
    managerId: "",
  });

  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [search, setSearch] = useState("");
  const [initialTasks, setInitialTasks] = useState([{ title: "", description: "", priority: "MEDIUM", dueDate: "" }]);

  const loadData = useCallback(async () => {
    try {
      const [projectsResponse, usersResponse] = await Promise.all([
        api.get("/projects"),
        api.get("/admin/users"),
      ]);

      setProjects(projectsResponse.data);

      setManagers(usersResponse.data.filter((user) => user.role === "MANAGER"));

      setError("");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load project data");
    } finally {
      setLoading(false);
    }
  }, []);

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

  const createProject = async (e) => {
    e.preventDefault();

    setError("");
    setMessage("");

    try {
      const response = await api.post("/projects", {
        name: form.name,
        description: form.description,
        startDate: form.startDate || null,
        endDate: form.endDate || null,
        managerId: form.managerId ? Number(form.managerId) : null,
        tasks: initialTasks,
      });

      setProjects((current) => [...current, response.data]);

      setForm({
        name: "",
        description: "",
        startDate: "",
        endDate: "",
        managerId: "",
      });
      setInitialTasks([{ title: "", description: "", priority: "MEDIUM", dueDate: "" }]);
      setShowCreateForm(false);

      setMessage("Project created successfully");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create project");
    }
  };

  if (loading) {
    return (
      <div className="page">
        <p>Loading projects...</p>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header"><div><h1>Projects</h1><p>Manage projects, teams and progress.</p></div><button className="primary-button" onClick={() => setShowCreateForm(true)}>+ New Project</button></div>

      {error && <p className="error">{error}</p>}

      {message && <p className="success">{message}</p>}

      {showCreateForm && <div className="modal-overlay" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowCreateForm(false); }}><div className="project-form-container modal-card" role="dialog" aria-modal="true" aria-labelledby="create-project-title">
        <div className="modal-heading"><div><h2 id="create-project-title">Create project</h2><p>Set up a new project for your team.</p></div><button className="icon-button" aria-label="Close" onClick={() => setShowCreateForm(false)}>×</button></div>

        <form className="project-form" onSubmit={createProject}>
          <label>Project name<input
            type="text"
            name="name"
            value={form.name}
            onChange={handleChange}
            placeholder="e.g. Enterprise Issue Tracker" required
          /></label>

          <label>Description<textarea
            name="description"
            value={form.description}
            onChange={handleChange}
            rows="4" placeholder="What is this project about?"
          /></label>

          <div className="form-row">
            <div>
              <label>Start Date</label>

              <input
                type="date"
                name="startDate"
                value={form.startDate}
                onChange={handleChange}
              />
            </div>

            <div>
              <label>End Date</label>

              <input
                type="date"
                name="endDate"
                value={form.endDate}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="initial-steps-editor">
            <div className="workflow-section-heading"><div><h3>Project steps</h3><p>Add the work breakdown employees will follow. Steps stay in this order.</p></div><button type="button" className="secondary-button" onClick={() => setInitialTasks((tasks) => [...tasks, { title: "", description: "", priority: "MEDIUM", dueDate: "" }])} disabled={initialTasks.length >= 50}>+ Add step</button></div>
            {initialTasks.map((task, index) => <div className="initial-step-row" key={index}><span className="workflow-step-number">{index + 1}</span><label>Step title<input required maxLength="200" value={task.title} placeholder="e.g. Plan requirements" onChange={(e) => setInitialTasks((tasks) => tasks.map((item, i) => i === index ? { ...item, title: e.target.value } : item))} /></label><label>Priority<select value={task.priority} onChange={(e) => setInitialTasks((tasks) => tasks.map((item, i) => i === index ? { ...item, priority: e.target.value } : item))}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>CRITICAL</option></select></label><label>Due date<input type="date" value={task.dueDate || ""} onChange={(e) => setInitialTasks((tasks) => tasks.map((item, i) => i === index ? { ...item, dueDate: e.target.value } : item))} /></label><button type="button" className="icon-button" aria-label={`Remove step ${index + 1}`} disabled={initialTasks.length === 1} onClick={() => setInitialTasks((tasks) => tasks.filter((_, i) => i !== index))}>×</button><label className="initial-step-description">Step details<textarea rows="2" maxLength="2000" value={task.description} placeholder="What should be completed in this step?" onChange={(e) => setInitialTasks((tasks) => tasks.map((item, i) => i === index ? { ...item, description: e.target.value } : item))} /></label></div>)}
          </div>

          <label>Project manager<select
            name="managerId"
            value={form.managerId}
            onChange={handleChange}
          >
            <option value="">Select Manager</option>

            {managers.map((manager) => (
              <option key={manager.id} value={manager.id}>
                {manager.name} - {manager.email}
              </option>
            ))}
          </select></label>

          <div className="modal-actions"><button type="button" className="secondary-button" onClick={() => setShowCreateForm(false)}>Cancel</button><button type="submit" className="primary-button">Create Project</button></div>
        </form>
      </div></div>}

      <div className="filter-bar"><input aria-label="Search projects" placeholder="Search projects..." value={search} onChange={(e) => setSearch(e.target.value)} /></div>

      <div className="table-container">
        <h2>All Projects</h2>

        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Status</th>
              <th>Manager</th>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Team</th>
              <th>Workflow</th>
              <th>Progress</th>
            </tr>
          </thead>

          <tbody>
            {projects.filter((project) => `${project.name} ${project.managerName || ""}`.toLowerCase().includes(search.toLowerCase())).length === 0 ? (
              <tr>
                <td colSpan="9">No projects found</td>
              </tr>
            ) : (
              projects.filter((project) => `${project.name} ${project.managerName || ""}`.toLowerCase().includes(search.toLowerCase())).map((project) => (
                <tr key={project.id}>
                  <td>{project.id}</td>

                  <td>{project.name}</td>

                  <td><span className={`status-badge project-status project-status-${String(project.status || "planning").toLowerCase().replaceAll("_", "-")}`}>{project.status}</span></td>

                  <td>{project.managerName || "Not assigned"}</td>

                  <td>{project.startDate || "-"}</td>

                  <td>{project.endDate || "-"}</td>

                  <td>
                    <button
                      onClick={() =>
                        navigate(`/admin/projects/${project.id}/team`)
                      }
                    >
                      Manage Team
                    </button>
                  </td>
                  <td><button className="workflow-configure-button" onClick={() => navigate(`/admin/projects/${project.id}/workflow`)}>Configure workflow</button></td>
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

export default AdminProjects;
