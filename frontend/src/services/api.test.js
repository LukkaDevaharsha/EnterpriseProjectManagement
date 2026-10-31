import { afterEach, describe, expect, it, vi } from "vitest";

afterEach(() => {
  vi.unstubAllEnvs();
  vi.resetModules();
});

async function loadApiWithEnvironment({ apiBaseUrl, dev }) {
  vi.resetModules();
  vi.stubEnv("VITE_API_BASE_URL", apiBaseUrl);
  vi.stubEnv("DEV", dev);
  return (await import("./api")).default;
}

describe("API base URL", () => {
  it("uses VITE_API_BASE_URL and removes trailing slashes", async () => {
    const api = await loadApiWithEnvironment({
      apiBaseUrl: "https://enterpriseprojectmanagement.onrender.com/api///",
      dev: false,
    });
    expect(api.defaults.baseURL).toBe("https://enterpriseprojectmanagement.onrender.com/api");
  });

  it("uses localhost only in development when no variable is set", async () => {
    const api = await loadApiWithEnvironment({ apiBaseUrl: "", dev: true });
    expect(api.defaults.baseURL).toBe("http://localhost:8080/api");
  });

  it("uses the Render URL in production when no variable is set", async () => {
    const api = await loadApiWithEnvironment({ apiBaseUrl: "", dev: false });
    expect(api.defaults.baseURL).toBe("https://enterpriseprojectmanagement.onrender.com/api");
  });
});
