import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api from "../../services/api";

function Register() {
  const location = useLocation();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    email: location.state?.verifyEmail || "",
    password: "",
    employeeId: "",
    department: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [verificationPending, setVerificationPending] = useState(Boolean(location.state?.verifyEmail));
  const [code, setCode] = useState("");
  const [customDepartment, setCustomDepartment] = useState("");

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    const department = form.department === "Other"
      ? customDepartment.trim()
      : form.department;

    if (
      !form.name ||
      !form.email ||
      !form.password ||
      !form.employeeId ||
      !department
    ) {
      setError("Please fill in all fields.");
      return;
    }

    try {
      setLoading(true);

      const response = await api.post("/auth/register", {
        name: form.name,
        email: form.email,
        password: form.password,
        employeeId: form.employeeId,
        department,
      });

      setVerificationPending(true);
      setSuccess(response.data.message || "We sent a verification code to your email.");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Registration failed.",
      );
    } finally {
      setLoading(false);
    }
  };

  const verifyEmail = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    try {
      setLoading(true);
      const response = await api.post("/auth/verify-email", { email: form.email, code });
      setSuccess(response.data.message);
      setTimeout(() => navigate("/login", { state: { verifiedEmail: form.email } }), 900);
    } catch (err) {
      setError(err.response?.data?.message || "Could not verify this code.");
    } finally {
      setLoading(false);
    }
  };

  const resendCode = async () => {
    setError("");
    setSuccess("");
    try {
      const response = await api.post("/auth/resend-verification", { email: form.email });
      setSuccess(response.data.message);
    } catch (err) {
      setError(err.response?.data?.message || "Could not resend the code.");
    }
  };

  return (
    <div className="auth-page register-page">
      <div className="auth-card register-auth-card">
        <aside className="auth-showcase" aria-label="Workspace introduction">
          <div className="showcase-decoration" aria-hidden="true"><i /><i /><i /><span /></div>
          <span className="showcase-label">PROJECTS, IN SYNC</span>
          <h2>Good work starts with a clear plan.</h2>
          <p>Bring your team, tasks, and ideas together in one workspace.</p>
          <div className="showcase-tags"><span>Plan</span><b /><span>Build</span><b /><span>Deliver</span></div>
          <div className="showcase-mark" aria-hidden="true"><span /><span /><span /></div>
        </aside>
        <div className="auth-content">
        <div className="auth-brand"><span className="auth-brand-mark">EP</span><span>Enterprise Project Management</span></div>
        <div className="auth-header">
          <span className="auth-kicker">EMPLOYEE ACCESS</span>
          <h1>{verificationPending ? "Verify your email" : "Create your account"}</h1>
          <p>{verificationPending ? `Enter the 6-digit code sent to ${form.email}.` : "Set up your account to join your project team."}</p>
        </div>

        {verificationPending ? <form onSubmit={verifyEmail} className="auth-form auth-verification-form">
          <div className="verification-note"><span aria-hidden="true">✉</span><p>Check your inbox for a one-time code. It expires in 15 minutes.</p></div>
          <div className="form-group"><label htmlFor="verification-code">Verification code</label><input id="verification-code" type="text" inputMode="numeric" autoComplete="one-time-code" maxLength={6} pattern="[0-9]{6}" value={code} onChange={(e) => setCode(e.target.value.replace(/\D/g, ""))} placeholder="000000" required /></div>
          {error && <div className="form-error">{error}</div>}
          {success && <div className="form-success">{success}</div>}
          <button type="submit" className="primary-button" disabled={loading}>{loading ? "Verifying..." : "Verify email"}</button>
          <button type="button" className="text-button" onClick={resendCode}>Resend code</button>
        </form> : <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="register-name">Full name</label>
            <input
              id="register-name"
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              placeholder="Your full name"
              autoComplete="name"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="register-email">Work email</label>
            <input
              id="register-email"
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder="you@example.com"
              autoComplete="email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="register-password">Password</label>
            <input
              id="register-password"
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Create a strong password"
              autoComplete="new-password"
              minLength={10}
              maxLength={72}
              required
            />
            <ul className="password-guidance"><li>10 to 72 characters</li><li>Uppercase and lowercase letters</li><li>At least one number and one symbol</li></ul>
          </div>

          <div className="form-group">
            <label htmlFor="register-employee-id">Employee ID</label>
            <input
              id="register-employee-id"
              type="text"
              name="employeeId"
              value={form.employeeId}
              onChange={handleChange}
              placeholder="Enter employee ID"
              autoComplete="off"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="register-department">Department</label>
            <select
              id="register-department"
              name="department"
              value={form.department}
              onChange={handleChange}
              required
            >
              <option value="">Select your department</option>
              <option value="Fresher">Fresher</option>
              <option value="Developer">Developer</option>
              <option value="Analysis">Analysis</option>
              <option value="QA Testing">QA Testing</option>
              <option value="Design">Design</option>
              <option value="Product">Product</option>
              <option value="Data & Analytics">Data &amp; Analytics</option>
              <option value="DevOps">DevOps</option>
              <option value="Human Resources">Human Resources</option>
              <option value="Finance">Finance</option>
              <option value="Other">Other</option>
            </select>
          </div>

          {form.department === "Other" && (
            <div className="form-group">
              <label htmlFor="register-custom-department">Department name</label>
              <input
                id="register-custom-department"
                type="text"
                value={customDepartment}
                onChange={(e) => setCustomDepartment(e.target.value)}
                placeholder="Enter your department"
                maxLength={100}
                required
              />
            </div>
          )}

          {error && <div className="form-error">{error}</div>}

          {success && <div className="form-success">{success}</div>}

          <button type="submit" className="primary-button" disabled={loading}>
            {loading ? "Creating account..." : "Create account"}
          </button>
        </form>}

        <div className="auth-footer">
          {verificationPending ? <><span>Entered the wrong email?</span><button className="text-button" type="button" onClick={() => { setVerificationPending(false); setCode(""); setSuccess(""); }}>Edit email</button></> : <><span>Already have an account?</span><Link to="/login">Sign in</Link></>}
        </div>
        </div>
      </div>
    </div>
  );
}

export default Register;
