import { useEffect, useState } from "react";
import api from "../../services/api";

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ name: "", department: "" });
  const [password, setPassword] = useState({
    currentPassword: "",
    newPassword: "",
    confirm: "",
  });
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  useEffect(() => {
    api
      .get("/users/me")
      .then(({ data }) => {
        setProfile(data);
        setForm({ name: data.name, department: data.department });
      })
      .catch((e) =>
        setError(e.response?.data?.message || "Could not load profile"),
      );
  }, []);
  const save = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      const { data } = await api.put("/users/me", form);
      setProfile(data);
      setMessage("Profile updated");
    } catch (e) {
      setError(e.response?.data?.message || "Could not update profile");
    }
  };
  const changePassword = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    if (password.newPassword !== password.confirm) {
      setError("New passwords do not match");
      return;
    }
    try {
      await api.patch("/users/me/password", {
        currentPassword: password.currentPassword,
        newPassword: password.newPassword,
      });
      setPassword({ currentPassword: "", newPassword: "", confirm: "" });
      setMessage("Password updated");
    } catch (e) {
      setError(e.response?.data?.message || "Could not change password");
    }
  };
  if (!profile)
    return <div className="page">{error || "Loading profile..."}</div>;
  return (
    <div className="page">
      <h1>My Profile</h1>
      {error && <p className="error">{error}</p>}
      {message && <p className="success">{message}</p>}
      <section className="project-form-container">
        <h2>Account details</h2>
        <p>
          Email: {profile.email} · Employee ID: {profile.employeeId} · Role:{" "}
          {profile.role} · Status: {profile.active ? "Active" : "Inactive"}
        </p>
        <p>Account created: {profile.createdAt}</p>
        <form className="project-form" onSubmit={save}>
          <label>
            Name
            <input
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </label>
          <label>
            Department
            <input
              required
              value={form.department}
              onChange={(e) => setForm({ ...form, department: e.target.value })}
            />
          </label>
          <button>Save profile</button>
        </form>
      </section>
      <section className="project-form-container">
        <h2>Change password</h2>
        <form className="project-form" onSubmit={changePassword}>
          <input
            type="password"
            required
            placeholder="Current password"
            value={password.currentPassword}
            onChange={(e) =>
              setPassword({ ...password, currentPassword: e.target.value })
            }
          />
          <input
            type="password"
            required
            minLength="8"
            placeholder="New password"
            value={password.newPassword}
            onChange={(e) =>
              setPassword({ ...password, newPassword: e.target.value })
            }
          />
          <input
            type="password"
            required
            minLength="8"
            placeholder="Confirm new password"
            value={password.confirm}
            onChange={(e) =>
              setPassword({ ...password, confirm: e.target.value })
            }
          />
          <button>Change password</button>
        </form>
      </section>
    </div>
  );
}
