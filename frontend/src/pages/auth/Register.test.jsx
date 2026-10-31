import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import api from "../../services/api";
import Register from "./Register";
import { RegisterFeedback } from "./RegisterFeedback";
import { registrationErrorMessage } from "./registrationErrors";

vi.mock("../../services/api", () => ({ default: { post: vi.fn() } }));

function renderRegister() {
  return render(
    <MemoryRouter initialEntries={["/register"]}>
      <Routes>
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<h1>Sign in</h1>} />
      </Routes>
    </MemoryRouter>,
  );
}

async function fillRegistrationForm(user, password = "StrongTestPassword123!") {
  await user.type(screen.getByLabelText("Full name"), "  Test Employee  ");
  await user.type(screen.getByLabelText("Work email"), "  test@example.com  ");
  await user.type(screen.getByLabelText("Password"), password);
  await user.type(screen.getByLabelText("Employee ID"), "  EMP-TEST-001  ");
  await user.selectOptions(screen.getByLabelText("Department"), "Developer");
}

describe("registration and email verification flow", () => {
  beforeEach(() => api.post.mockReset());

  it("renders a real submit form and browser validation blocks missing required fields", async () => {
    const user = userEvent.setup();
    renderRegister();

    const submit = screen.getByRole("button", { name: "Create account" });
    expect(submit).toHaveAttribute("type", "submit");
    expect(screen.getByLabelText("Work email").checkValidity()).toBe(false);
    await user.click(submit);
    expect(api.post).not.toHaveBeenCalled();
    expect(screen.getByRole("alert")).toBeInTheDocument();
  });

  it("blocks weak passwords before calling the API", async () => {
    const user = userEvent.setup();
    renderRegister();
    await fillRegistrationForm(user, "weakpass");

    expect(screen.getByLabelText("Password").checkValidity()).toBe(false);
    await user.click(screen.getByRole("button", { name: "Create account" }));
    expect(api.post).not.toHaveBeenCalled();
    expect(screen.getByRole("alert")).toBeInTheDocument();
  });

  it("uses browser email validation before calling the API", async () => {
    const user = userEvent.setup();
    renderRegister();
    await fillRegistrationForm(user);
    const email = screen.getByLabelText("Work email");
    await user.clear(email);
    await user.type(email, "invalid-email");

    expect(email.checkValidity()).toBe(false);
    await user.click(screen.getByRole("button", { name: "Create account" }));
    expect(api.post).not.toHaveBeenCalled();
  });

  it("posts normalized registration details, shows loading, then enters verification", async () => {
    const user = userEvent.setup();
    let resolveRequest;
    api.post.mockReturnValue(new Promise((resolve) => { resolveRequest = resolve; }));
    renderRegister();

    await fillRegistrationForm(user);
    await user.click(screen.getByRole("button", { name: "Create account" }));

    expect(screen.getByRole("button", { name: "Creating account..." })).toBeDisabled();
    expect(api.post).toHaveBeenCalledWith("/auth/register", {
      name: "Test Employee",
      email: "test@example.com",
      password: "StrongTestPassword123!",
      employeeId: "EMP-TEST-001",
      department: "Developer",
    });

    resolveRequest({ data: { message: "Account created. Verification code sent." } });
    expect(await screen.findByText("Account created. Verification code sent.")).toBeInTheDocument();
    expect(screen.getByLabelText("Verification code")).toBeInTheDocument();
    expect(sessionStorage.getItem("pendingVerificationEmail")).toBe("test@example.com");
  });

  it("renders backend validation and duplicate-registration errors", () => {
    const message = registrationErrorMessage({
      response: { status: 409, data: { message: "Email already registered" } },
    });
    render(<RegisterFeedback error={message} />);
    expect(screen.getByRole("alert")).toHaveTextContent("Email already registered");
  });

  it("renders a useful network error and success as distinct statuses", () => {
    const message = registrationErrorMessage(new Error("Network Error"));
    const { rerender } = render(<RegisterFeedback error={message} />);
    expect(screen.getByRole("alert")).toHaveTextContent("Could not reach the registration service");
    rerender(<RegisterFeedback success="Account created." />);
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent("Account created.");
  });

  it("restores pending verification after refresh and allows editing the email", async () => {
    const user = userEvent.setup();
    sessionStorage.setItem("pendingVerificationEmail", "test@example.com");
    const rendered = renderRegister();
    expect(screen.getByText(/Enter the 6-digit code sent to test@example.com/)).toBeInTheDocument();

    rendered.unmount();
    renderRegister();
    await user.click(screen.getByRole("button", { name: "Edit email" }));

    expect(screen.getByLabelText("Work email")).toHaveValue("test@example.com");
    expect(sessionStorage.getItem("pendingVerificationEmail")).toBeNull();
  });

  it("verifies the email, clears pending state, and navigates to login", async () => {
    const user = userEvent.setup();
    sessionStorage.setItem("pendingVerificationEmail", "test@example.com");
    api.post.mockResolvedValue({ data: { message: "Email verified. You can now sign in." } });
    renderRegister();

    await user.type(screen.getByLabelText("Verification code"), "123456");
    await user.click(screen.getByRole("button", { name: "Verify email" }));

    expect(api.post).toHaveBeenCalledWith("/auth/verify-email", { email: "test@example.com", code: "123456" });
    expect(await screen.findByText("Email verified. You can now sign in.")).toBeInTheDocument();
    expect(sessionStorage.getItem("pendingVerificationEmail")).toBeNull();
    expect(await screen.findByRole("heading", { name: "Sign in" })).toBeInTheDocument();
  });
});
