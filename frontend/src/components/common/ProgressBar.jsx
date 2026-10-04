export default function ProgressBar({ value = 0 }) {
  const percent = Math.max(0, Math.min(100, Number(value) || 0));
  return (
    <div className="project-progress">
      <div className="project-progress-label">
        <span>Project Progress</span>
        <strong>{percent}%</strong>
      </div>
      <div className="project-progress-track">
        <div
          className="project-progress-fill"
          style={{ width: `${percent}%` }}
        />
      </div>
    </div>
  );
}
