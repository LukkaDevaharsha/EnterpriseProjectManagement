import { Navigate, Route, Routes } from "react-router-dom";

import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import ProtectedRoute from "./ProtectedRoute";

import AdminLayout from "../components/AdminLayout";
import AdminDashboard from "../pages/admin/AdminDashboard";
import AdminUsers from "../pages/admin/AdminUsers";
import AdminProjects from "../pages/admin/AdminProjects";
import AdminIssues from "../pages/admin/AdminIssues";
import AdminIssueDetails from "../pages/admin/AdminIssueDetails";
import ProjectTeam from "../pages/admin/ProjectTeam";

import ManagerLayout from "../components/ManagerLayout";
import ManagerDashboard from "../pages/manager/ManagerDashboard";
import ManagerProjects from "../pages/manager/ManagerProjects";
import ManagerIssues from "../pages/manager/ManagerIssues";
import ManagerIssueDetails from "../pages/manager/ManagerIssueDetails";

import EmployeeLayout from "../components/EmployeeLayout";
import EmployeeDashboard from "../pages/employee/EmployeeDashboard";
import EmployeeProjects from "../pages/employee/EmployeeProjects";
import EmployeeIssues from "../pages/employee/EmployeeIssues";
import EmployeeIssueDetails from "../pages/employee/EmployeeIssueDetails";
import ProfileRoute from "./ProfileRoute";
import ProjectWorkflow from "../pages/common/ProjectWorkflow";
import ProjectChat from "../pages/common/ProjectChat";
import ManagerSupport from "../pages/manager/ManagerSupport";

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/register" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <ProfileRoute />
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin"
        element={
          <ProtectedRoute roles={["ADMIN"]}>
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<AdminDashboard />} />
        <Route path="users" element={<AdminUsers />} />
        <Route path="projects" element={<AdminProjects />} />
        <Route path="projects/:projectId/team" element={<ProjectTeam />} />
        <Route path="projects/:projectId/workflow" element={<ProjectWorkflow />} />
        <Route path="issues" element={<AdminIssues />} />
        <Route path="issues/:issueId" element={<AdminIssueDetails />} />
      </Route>

      <Route
        path="/manager"
        element={
          <ProtectedRoute roles={["MANAGER"]}>
            <ManagerLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<ManagerDashboard />} />
        <Route path="projects" element={<ManagerProjects />} />
        <Route path="projects/:projectId/team" element={<ProjectTeam />} />
        <Route path="projects/:projectId/workflow" element={<ProjectWorkflow />} />
        <Route path="projects/:projectId/chat" element={<ProjectChat />} />
        <Route path="issues" element={<ManagerIssues />} />
        <Route path="issues/:issueId" element={<ManagerIssueDetails />} />
        <Route path="support" element={<ManagerSupport />} />
      </Route>

      <Route
        path="/employee"
        element={
          <ProtectedRoute roles={["EMPLOYEE"]}>
            <EmployeeLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<EmployeeDashboard />} />
        <Route path="projects" element={<EmployeeProjects />} />
        <Route path="projects/:projectId/workflow" element={<ProjectWorkflow />} />
        <Route path="projects/:projectId/chat" element={<ProjectChat />} />
        <Route path="issues" element={<EmployeeIssues />} />
        <Route path="issues/:issueId" element={<EmployeeIssueDetails />} />
      </Route>

      <Route path="*" element={<Navigate to="/register" replace />} />
    </Routes>
  );
}

export default AppRoutes;
