import { useCallback, useEffect, useState } from "react";
import { useSelector } from "react-redux";
import api from "../../services/api";

function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);
  const [search, setSearch] = useState("");
  const currentUser = useSelector((state) => state.auth.user);

  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    employeeId: "",
    department: "",
    role: "EMPLOYEE",
  });

  const loadUsers = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/admin/users");

      setUsers(response.data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Failed to load users.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => void loadUsers(), 0);
    return () => window.clearTimeout(timer);
  }, [loadUsers]);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const createUser = async (e) => {
    e.preventDefault();

    try {
      setError("");
      setMessage("");

      await api.post("/admin/users", form);

      setMessage("User created successfully.");

      setForm({
        name: "",
        email: "",
        password: "",
        employeeId: "",
        department: "",
        role: "EMPLOYEE",
      });

      setShowCreateForm(false);

      await loadUsers();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Failed to create user.",
      );
    }
  };

  const changeRole = async (id, role) => {
    if (String(id) === String(currentUser?.id)) {
      setError("Your administrator role is protected and cannot be changed here.");
      return;
    }

    if (role === "ADMIN") {
      setError("The workspace has one administrator. Other users cannot be promoted to Admin.");
      return;
    }

    try {
      setError("");
      setMessage("");

      await api.patch(`/admin/users/${id}/role`, {
        role,
      });

      setMessage("Role updated successfully.");

      await loadUsers();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Failed to update role.",
      );
    }
  };

  const toggleStatus = async (user) => {
    try {
      setError("");
      setMessage("");

      await api.patch(`/admin/users/${user.id}/status`, {
        active: !user.active,
      });

      setMessage(
        user.active
          ? "User deactivated successfully."
          : "User activated successfully.",
      );

      await loadUsers();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Failed to update user status.",
      );
    }
  };

  const deleteUser = async (id) => {
    try {
      setError("");
      setMessage("");

      await api.delete(`/admin/users/${id}`);
      setUserToDelete(null);

      setMessage("User deleted successfully.");

      await loadUsers();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Unable to delete this user.",
      );
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <h1>Users</h1>
        <p>Loading users...</p>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Users</h1>
          <p>Manage employees, managers and administrators.</p>
        </div>

        <button
          className="primary-button"
          onClick={() => setShowCreateForm(true)}
        >
          + Create User
        </button>
      </div>

      {error && <div className="form-error">{error}</div>}

      {message && <div className="form-success">{message}</div>}

      {showCreateForm && (
        <div className="modal-overlay" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowCreateForm(false); }}>
        <div className="form-card modal-card" role="dialog" aria-modal="true" aria-labelledby="create-user-title">
          <div className="modal-heading"><div><h2 id="create-user-title">Create new user</h2><p>Add a new member to your organization.</p></div><button className="icon-button" aria-label="Close" onClick={() => setShowCreateForm(false)}>×</button></div>

          <form onSubmit={createUser}>
            <div className="form-grid">
              <div className="form-group">
                <label>Full name</label>
                <input
                  type="text"
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  placeholder="e.g. Rahul Kumar"
                  required
                />
              </div>

              <div className="form-group">
                <label>Email address</label>
                <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="name@company.com"
                  required
                />
              </div>

              <div className="form-group">
                <label>Password</label>
                <input
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="Create a password"
                  required
                />
              </div>

              <div className="form-group">
                <label>Employee ID</label>
                <input
                  type="text"
                  name="employeeId"
                  value={form.employeeId}
                  onChange={handleChange}
                  placeholder="e.g. EMP-1024"
                  required
                />
              </div>

              <div className="form-group">
                <label>Department</label>
                <input
                  type="text"
                  name="department"
                  value={form.department}
                  onChange={handleChange}
                  placeholder="e.g. Engineering"
                  required
                />
              </div>

              <div className="form-group">
                <label>Role</label>

                <select name="role" value={form.role} onChange={handleChange}>
                  <option value="EMPLOYEE">Employee</option>

                  <option value="MANAGER">Manager</option>
                </select>
                <small className="field-hint">The administrator role is reserved for the workspace owner.</small>
              </div>
            </div>

            <button type="submit" className="primary-button">
              Create User
            </button>
          </form>
        </div>
        </div>
      )}

      <div className="filter-bar"><input aria-label="Search users" placeholder="Search users by name or email..." value={search} onChange={(e) => setSearch(e.target.value)} /></div>

      <div className="table-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Employee ID</th>
              <th>Department</th>
              <th>Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {users.filter((user) => `${user.name} ${user.email} ${user.employeeId} ${user.department}`.toLowerCase().includes(search.toLowerCase())).map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>

                <td>{user.name}</td>

                <td>{user.email}</td>

                <td>{user.employeeId}</td>

                <td>{user.department}</td>

                <td>{user.role === "ADMIN" ? <span className="status-badge admin-role">Workspace Admin</span> : <select aria-label={`Role for ${user.name}`} value={user.role} disabled={String(user.id) === String(currentUser?.id)} onChange={(e) => changeRole(user.id, e.target.value)}><option value="EMPLOYEE">Employee</option><option value="MANAGER">Manager</option></select>}</td>

                <td>
                  {user.active ? (
                    <span className="status-badge active">Active</span>
                  ) : (
                    <span className="status-badge inactive">Inactive</span>
                  )}
                </td>

                <td>
                  <div className="table-actions">
                    {String(user.id) === String(currentUser?.id) && user.role === "ADMIN" ? (
                      <span className="field-hint" title="Administrators cannot disable or delete their own account.">Your Admin account is protected</span>
                    ) : <>
                      <button
                        className="secondary-button"
                        onClick={() => toggleStatus(user)}
                      >
                        {user.active ? "Deactivate" : "Activate"}
                      </button>

                      <button
                        className="danger-button"
                        onClick={() => setUserToDelete(user)}
                      >
                        Delete
                      </button>
                    </>}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {users.length === 0 && <p className="empty-state">No users found.</p>}
      </div>
      {userToDelete && <div className="modal-overlay"><div className="confirm-dialog" role="alertdialog" aria-modal="true"><h2>Delete user?</h2><p>This will permanently delete {userToDelete.name} and cannot be undone.</p><div className="modal-actions"><button className="secondary-button" onClick={() => setUserToDelete(null)}>Cancel</button><button className="danger-button" onClick={() => deleteUser(userToDelete.id)}>Delete User</button></div></div></div>}
    </div>
  );
}

export default AdminUsers;
