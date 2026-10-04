import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api from "../../services/api";
import { loginSuccess } from "../../store/authSlice";

function Login() {
  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [verificationNeeded, setVerificationNeeded] = useState(false);
  const location = useLocation();

  const dispatch = useDispatch();
  const navigate = useNavigate();

  useEffect(() => {
    if (location.state?.verifiedEmail) setForm((current) => ({ ...current, email: location.state.verifiedEmail }));
  }, [location.state]);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setVerificationNeeded(false);

    try {
      const response = await api.post("/auth/login", form);

      const data = response.data;

      dispatch(
        loginSuccess({
          token: data.token,
          user: {
            id: data.id,
            name: data.name,
            email: data.email,
            role: data.role,
          },
        }),
      );

      if (data.role === "ADMIN") {
        navigate("/admin");
      } else if (data.role === "MANAGER") {
        navigate("/manager");
      } else {
        navigate("/employee");
      }
    } catch (error) {
      const message = error.response?.data?.message || "Login failed";
      setError(message);
      setVerificationNeeded(message.toLowerCase().includes("verify your email"));
    }
  };

  const resendVerification = async () => {
    try {
      await api.post("/auth/resend-verification", { email: form.email });
      navigate("/register", { state: { verifyEmail: form.email } });
    } catch (error) {
      setError(error.response?.data?.message || "Could not resend a verification code.");
    }
  };

  return (
    <div className="auth-page login-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Welcome back</h1>
          <p>Sign in to continue to your workspace</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="login-email">Email</label>
            <input
              id="login-email"
              type="email"
              name="email"
              placeholder="Enter your email"
              value={form.email}
              onChange={handleChange}
              autoComplete="email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="login-password">Password</label>
            <input
              id="login-password"
              type="password"
              name="password"
              placeholder="Enter your password"
              value={form.password}
              onChange={handleChange}
              autoComplete="current-password"
              required
            />
          </div>

          {error && <p className="form-error">{error}</p>}
          {verificationNeeded && <button type="button" className="text-button login-verify-link" onClick={resendVerification}>Send a new verification code</button>}

          <button type="submit" className="primary-button">
            Login
          </button>
        </form>

        <div className="auth-footer">
          <span>New here?</span>
          <Link to="/register">Create an account</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
