import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useSelector } from "react-redux";
import api from "../../services/api";

const statusLabels = {
  OPEN: "Open", IN_PROGRESS: "In progress", IN_REVIEW: "In review",
  TESTING: "Testing", RESOLVED: "Resolved", CLOSED: "Closed",
};

function ProjectWorkflow() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const user = useSelector((state) => state.auth.user);
  const canManage = user?.role === "ADMIN" || user?.role === "MANAGER";
  const [workflow, setWorkflow] = useState(null);
  const [members, setMembers] = useState([]);
  const [form, setForm] = useState({ title: "", description: "", priority: "MEDIUM", assignedTo: "", dueDate: "" });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadWorkflow = useCallback(async () => {
    try {
      setError("");
      const requests = [api.get(`/projects/${projectId}/workflow`)];
      if (canManage) requests.push(api.get(`/projects/${projectId}/members`));
      const [workflowResponse, membersResponse] = await Promise.all(requests);
      setWorkflow(workflowResponse.data);
      if (membersResponse) setMembers(membersResponse.data.filter((member) => member.active !== false && member.role === "EMPLOYEE"));
    } catch (err) {
      setError(err.response?.data?.message || "Could not load this project workflow.");
    } finally { setLoading(false); }
  }, [projectId, canManage]);

  useEffect(() => { void loadWorkflow(); }, [loadWorkflow]);

  const addTask = async (event) => {
    event.preventDefault();
    setSaving(true); setError(""); setMessage("");
    try {
      await api.post("/issues", {
        projectId: Number(projectId), title: form.title, description: form.description,
        priority: form.priority, assignedTo: form.assignedTo ? Number(form.assignedTo) : null,
        dueDate: form.dueDate || null,
      });
      setForm({ title: "", description: "", priority: "MEDIUM", assignedTo: "", dueDate: "" });
      setMessage("Project step added.");
      await loadWorkflow();
    } catch (err) { setError(err.response?.data?.message || "Could not add this project step."); }
    finally { setSaving(false); }
  };

  const updateTask = async (task, changes) => {
    setError(""); setMessage("");
    try {
      await api.put(`/issues/${task.id}`, {
        title: task.title, description: task.description || "", priority: task.priority,
        assignedTo: task.assignedTo || null, dueDate: task.dueDate || null,
        ...changes,
      });
      setMessage(`Step ${task.stepNumber} updated.`);
      await loadWorkflow();
    } catch (err) { setError(err.response?.data?.message || "Could not update the task assignment."); }
  };

  if (loading) return <div className="page"><p>Loading project workflow...</p></div>;
  if (!workflow) return <div className="page"><p className="error">{error || "Project not found."}</p></div>;
  const base = user?.role === "ADMIN" ? "/admin" : user?.role === "MANAGER" ? "/manager" : "/employee";

  return <div className="page project-workflow-page">
    <div className="page-header">
      <div><p className="workflow-eyebrow">PROJECT WORKFLOW</p><h1>{workflow.projectName}</h1><p>{workflow.description || "Follow the project steps and see what the team is working on."}</p></div>
      <button className="workflow-back-button" onClick={() => navigate(location.key === "default" ? `${base}/projects` : -1)}><span aria-hidden="true">←</span> Previous page</button>
    </div>
    {error && <p className="error">{error}</p>}{message && <p className="success">{message}</p>}
    <section className="workflow-summary">
      <div><span className={`status-badge project-status project-status-${String(workflow.status).toLowerCase().replaceAll("_", "-")}`}>{workflow.status.replaceAll("_", " ")}</span><span className="workflow-step-count">{workflow.tasks.length} {workflow.tasks.length === 1 ? "step" : "steps"}</span></div>
      <div className="workflow-progress-label"><strong>{workflow.progressPercent}%</strong><span>complete</span></div>
      <div className="workflow-progress-track"><div style={{ width: `${workflow.progressPercent}%` }} /></div>
    </section>

    <section className="workflow-section">
      <div className="workflow-section-heading"><div><h2>Project steps</h2><p>Work is listed in the order it was added. Completed steps contribute to project progress.</p></div></div>
      {workflow.tasks.length === 0 ? <div className="workflow-empty"><h3>No steps have been added yet</h3><p>A manager can add the work breakdown here.</p></div> : <ol className="workflow-task-list">
        {workflow.tasks.map((task) => {
          const canOpen = user?.role !== "EMPLOYEE" || task.assignedTo === user?.id;
          return <li className="workflow-task" key={task.id}>
            <span className={`workflow-step-number ${["RESOLVED", "CLOSED"].includes(task.status) ? "is-complete" : ""}`}>{task.stepNumber}</span>
            <div className="workflow-task-content">
              <div className="workflow-task-title-row"><h3>{task.title}</h3><span className={`status-badge status-${task.status.toLowerCase().replaceAll("_", "-")}`}>{statusLabels[task.status] || task.status}</span></div>
              {task.description && <p>{task.description}</p>}
              <div className="workflow-task-meta"><span className={`priority-badge priority-${task.priority.toLowerCase()}`}>{task.priority}</span>{canManage ? <label className="workflow-assignee-control">Assignee<select aria-label={`Assignee for ${task.title}`} value={task.assignedTo || ""} onChange={(event) => void updateTask(task, { assignedTo: event.target.value ? Number(event.target.value) : null })}><option value="">Unassigned</option>{members.map((member) => <option key={member.userId} value={member.userId}>{member.name}</option>)}</select></label> : <span>Assigned to: <strong>{task.assignedToName || "Unassigned"}</strong></span>}{canManage ? <label className="workflow-due-date-control">Due date<input type="date" aria-label={`Due date for ${task.title}`} value={task.dueDate || ""} onChange={(event) => void updateTask(task, { dueDate: event.target.value || null })} /></label> : task.dueDate ? <span>Due {task.dueDate}</span> : <span>No due date</span>}</div>
            </div>
            {canOpen ? <button className="secondary-button workflow-open-task" onClick={() => navigate(`${base}/issues/${task.id}`)}>Open task</button> : <span className="workflow-assignment-note">Assigned to a teammate</span>}
          </li>;
        })}
      </ol>}
    </section>

    {canManage && <section className="workflow-section workflow-add-section"><div className="workflow-add-heading"><span className="workflow-add-icon" aria-hidden="true">+</span><div><h2>Add a project step</h2><p>New steps appear at the end of the workflow.</p></div></div>
      <form className="workflow-add-form" onSubmit={addTask}>
        <div className="workflow-add-fields"><label className="workflow-title-field">Step title<input required maxLength="200" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="e.g. Implement account validation" /></label>
        <label className="workflow-description-field">Description<textarea rows="2" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="Describe the expected work" /></label></div>
        <div className="workflow-form-options"><label>Priority<select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>CRITICAL</option></select></label>
          <label>Assign to<select value={form.assignedTo} onChange={(e) => setForm({ ...form, assignedTo: e.target.value })}><option value="">Unassigned</option>{members.map((member) => <option key={member.userId} value={member.userId}>{member.name}</option>)}</select></label>
          <label>Due date<input type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} /></label></div>
        <div className="workflow-form-footer"><span>Step {workflow.tasks.length + 1} in sequence</span><button className="primary-button" type="submit" disabled={saving}><span aria-hidden="true">+</span>{saving ? "Adding..." : "Add step"}</button></div>
      </form>
    </section>}
  </div>;
}

export default ProjectWorkflow;
