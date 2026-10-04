import { useSelector } from "react-redux";
import WorkspaceLayout from "../components/WorkspaceLayout";
import Profile from "../pages/profile/Profile";

export default function ProfileRoute() {
  const role = useSelector((state) => state.auth.user?.role || "EMPLOYEE");
  return <WorkspaceLayout role={role}><Profile /></WorkspaceLayout>;
}
