import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../services/api";

function ProjectTeam() {
  const MAX_TEAM_MEMBERS = 4;
  const { projectId } = useParams();

  const [project, setProject] = useState(null);
  const [members, setMembers] = useState([]);
  const [employees, setEmployees] = useState([]);

  const [selectedEmployee, setSelectedEmployee] = useState("");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [showAddModal, setShowAddModal] = useState(false);
  const [memberToRemove, setMemberToRemove] = useState(null);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const [projectResponse, membersResponse, employeesResponse] =
        await Promise.all([
          api.get(`/projects/${projectId}`),
          api.get(`/projects/${projectId}/members`),
          api.get("/users/employees"),
        ]);

      setProject(projectResponse.data);
      setMembers(membersResponse.data);
      setEmployees(employeesResponse.data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Failed to load project team.",
      );
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    const timer = window.setTimeout(() => void loadData(), 0);
    return () => window.clearTimeout(timer);
  }, [loadData]);

  const addEmployee = async () => {
    if (!selectedEmployee) {
      setError("Please select an employee.");
      return;
    }

    try {
      setError("");
      setMessage("");

      await api.post(`/projects/${projectId}/members`, {
        userId: Number(selectedEmployee),
      });

      setMessage("Employee added to project.");
      setShowAddModal(false);

      setSelectedEmployee("");

      await loadData();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Failed to add employee.",
      );
    }
  };

  const removeEmployee = async (userId) => {
    try {
      setError("");
      setMessage("");

      await api.delete(`/projects/${projectId}/members/${userId}`);
      setMemberToRemove(null);

      setMessage("Employee removed from project.");

      await loadData();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Failed to remove employee.",
      );
    }
  };

  const availableEmployees = employees.filter(
    (employee) => !members.some((member) => member.userId === employee.id),
  );
  const teamIsFull = members.length >= MAX_TEAM_MEMBERS;

  if (loading) {
    return (
      <div className="page-container">
        <p>Loading project team...</p>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="page-container">
        <p>Project not found.</p>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Project Team</h1>

          <p>Manage employees assigned to {project.name}.</p>
        </div>

        <button className="primary-button" onClick={() => setShowAddModal(true)} disabled={teamIsFull}>+ Add team member</button>
      </div>

      {error && <div className="form-error">{error}</div>}

      {message && <div className="form-success">{message}</div>}

      <p className="team-capacity-note">{members.length} of {MAX_TEAM_MEMBERS} employee team members. Managers can add members until the team reaches four.</p>
      {teamIsFull && <p className="team-capacity-full" role="status">This project has reached its 4-member limit.</p>}

      {showAddModal && <div className="modal-overlay" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowAddModal(false); }}><div className="form-card modal-card" role="dialog" aria-modal="true" aria-labelledby="add-member-title">
        <div className="modal-heading"><div><h2 id="add-member-title">Add team member</h2><p>Select an employee to add to this project.</p></div><button className="icon-button" aria-label="Close" onClick={() => setShowAddModal(false)}>×</button></div>

        <div className="form-row">
          <div className="form-group">
            <label>Select Employee</label>

            <select
              value={selectedEmployee}
              onChange={(e) => setSelectedEmployee(e.target.value)}
            >
              <option value="">Select an employee</option>

              {availableEmployees.map((employee) => (
                <option key={employee.id} value={employee.id}>
                  {employee.name} - {employee.employeeId}
                </option>
              ))}
            </select>
          </div>

          <button className="primary-button" onClick={addEmployee} disabled={!selectedEmployee || teamIsFull}>Add Employee</button>
        </div>

        {availableEmployees.length === 0 && (
          <p className="empty-state">No available employees to add.</p>
        )}
      </div></div>}

      <div className="table-card">
        <h2>Project Members</h2>

        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Employee ID</th>
              <th>Email</th>
              <th>Department</th>
              <th>Role</th>
              <th>Action</th>
            </tr>
          </thead>

          <tbody>
            {members.map((member) => (
              <tr key={member.userId}>
                <td>{member.name}</td>

                <td>{member.employeeId}</td>

                <td>{member.email}</td>

                <td>{member.department}</td>

                <td>{member.role}</td>

                <td>
                  <button
                    className="danger-button"
                    onClick={() => setMemberToRemove(member)}
                  >
                    Remove
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {members.length === 0 && (
          <p className="empty-state">
            No employees are assigned to this project.
          </p>
        )}
      </div>
      {memberToRemove && <div className="modal-overlay"><div className="confirm-dialog" role="alertdialog" aria-modal="true"><h2>Remove team member?</h2><p>{memberToRemove.name} will lose access to this project.</p><div className="modal-actions"><button className="secondary-button" onClick={() => setMemberToRemove(null)}>Cancel</button><button className="danger-button" onClick={() => removeEmployee(memberToRemove.userId)}>Remove member</button></div></div></div>}
    </div>
  );
}

export default ProjectTeam;
