import { Navigate } from "react-router-dom";
import { useSelector } from "react-redux";

function ProtectedRoute({ children, roles }) {
  const { token, user } = useSelector((state) => state.auth);

  if (!token || !user) {
    return <Navigate to="/login" replace />;
  }

  if (roles && !roles.includes(user.role)) {
    if (user.role === "ADMIN") {
      return <Navigate to="/admin" replace />;
    }

    if (user.role === "MANAGER") {
      return <Navigate to="/manager" replace />;
    }

    return <Navigate to="/employee" replace />;
  }

  return children;
}

export default ProtectedRoute;
