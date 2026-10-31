import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { configureStore } from "@reduxjs/toolkit";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import api from "../../services/api";
import AdminUsers from "./AdminUsers";

vi.mock("../../services/api", () => ({ default: { get: vi.fn(), patch: vi.fn(), delete: vi.fn() } }));

const users = [
  { id: 1, name: "Workspace Admin", email: "admin@example.com", employeeId: "ADMIN-001", department: "Admin", role: "ADMIN", active: true },
  { id: 2, name: "Employee One", email: "employee@example.com", employeeId: "EMP-001", department: "Engineering", role: "EMPLOYEE", active: true },
];

function renderAdminUsers() {
  api.get.mockResolvedValue({ data: users });
  const store = configureStore({
    reducer: { auth: (state = { user: { id: 1, role: "ADMIN" } }) => state },
  });
  return render(<Provider store={store}><MemoryRouter><AdminUsers /></MemoryRouter></Provider>);
}

describe("Admin user management protections", () => {
  it("hides self deactivate/delete controls and leaves other user actions available", async () => {
    const user = userEvent.setup();
    renderAdminUsers();

    const ownRow = await screen.findByRole("row", { name: /Workspace Admin/ });
    expect(within(ownRow).getByText("Your Admin account is protected")).toBeInTheDocument();
    expect(within(ownRow).queryByRole("button", { name: "Deactivate" })).not.toBeInTheDocument();
    expect(within(ownRow).queryByRole("button", { name: "Delete" })).not.toBeInTheDocument();

    const employeeRow = screen.getByRole("row", { name: /Employee One/ });
    await user.click(within(employeeRow).getByRole("button", { name: "Deactivate" }));
    await waitFor(() => expect(api.patch).toHaveBeenCalledWith("/admin/users/2/status", { active: false }));
  });
});
