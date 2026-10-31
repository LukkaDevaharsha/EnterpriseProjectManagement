export function RegisterFeedback({ error, success }) {
  return <>
    {error && <div className="form-error" role="alert">{error}</div>}
    {success && <div className="form-success" role="status">{success}</div>}
  </>;
}
