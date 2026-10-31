export function registrationErrorMessage(error) {
  return error.response?.data?.message || (error.response
    ? `Registration failed (HTTP ${error.response.status}). Please check the details and try again.`
    : "Could not reach the registration service. Check your internet connection and Vercel's VITE_API_BASE_URL setting.");
}
