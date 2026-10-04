import { useCallback, useEffect, useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "../store/authSlice";
import api from "../services/api";

const navigation = {
  ADMIN: [["/admin", "Dashboard", "◫"], ["/admin/users", "Users", "♙"], ["/admin/projects", "Projects", "▦"], ["/admin/issues", "Issues", "◎"], ["/profile", "My Profile", "○"]],
  MANAGER: [["/manager", "Dashboard", "◫"], ["/manager/projects", "Projects", "▦"], ["/manager/issues", "Issues", "◎"], ["/profile", "My Profile", "○"]],
  EMPLOYEE: [["/employee", "Dashboard", "◫"], ["/employee/projects", "My Projects", "▦"], ["/employee/issues", "My Issues", "◎"], ["/profile", "My Profile", "○"]],
};

export default function WorkspaceLayout({ role, children }) {
  const user = useSelector((state) => state.auth.user);
  const workspaceLabel = role === "ADMIN"
    ? "Admin Workspace"
    : role === "MANAGER"
      ? "Manager Workspace"
      : "Employee Workspace";
  const [menuOpen, setMenuOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [notificationInbox, setNotificationInbox] = useState({ notifications: [], unreadCount: 0 });
  const [mobileOpen, setMobileOpen] = useState(false);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const path = location.pathname;
  const active = (navigation[role] || []).find(([to]) => to !== "/profile" && (path === to || path.startsWith(`${to}/`)));
  const title = path === "/profile" ? "My Profile" : path.includes("/issues/") ? "Issue details" : path.includes("/team") ? "Project team" : active?.[1] || "Workspace";
  const initials = (user?.name || "User").split(/\s+/).slice(0, 2).map((part) => part[0]).join("").toUpperCase();
  const signOut = () => { dispatch(logout()); navigate("/login"); };
  const loadNotifications = useCallback(async () => {
    try {
      const response = await api.get("/notifications");
      setNotificationInbox(response.data);
    } catch { /* Keep the workspace usable if notifications are temporarily unavailable. */ }
  }, []);

  useEffect(() => {
    void loadNotifications();
    const timer = window.setInterval(() => void loadNotifications(), 30000);
    return () => window.clearInterval(timer);
  }, [loadNotifications]);

  const openNotification = async (notification) => {
    if (!notification.read) {
      try {
        await api.patch(`/notifications/${notification.id}/read`);
        setNotificationInbox((current) => ({
          ...current,
          unreadCount: Math.max(0, current.unreadCount - 1),
          notifications: current.notifications.map((item) => item.id === notification.id ? { ...item, read: true } : item),
        }));
      } catch { /* The issue link remains usable if marking the notification fails. */ }
    }
    setNotificationsOpen(false);
    if (notification.issueId) navigate(`/${role.toLowerCase()}/issues/${notification.issueId}`);
  };

  const markAllNotificationsRead = async () => {
    try {
      await api.patch("/notifications/read-all");
      setNotificationInbox((current) => ({ ...current, unreadCount: 0, notifications: current.notifications.map((item) => ({ ...item, read: true })) }));
    } catch { /* Leave inbox state unchanged if the request fails. */ }
  };

  return <div className="app-layout">
    {mobileOpen && <button aria-label="Close navigation" className="sidebar-scrim" onClick={() => setMobileOpen(false)} />}
    <aside className={`sidebar ${mobileOpen ? "sidebar-open" : ""}`}>
      <div className="brand"><span className="brand-mark">P</span><span>{workspaceLabel}</span></div>
      <div className="workspace-switch"><span className="workspace-avatar">{(user?.name || "W")[0]}</span><span><strong>Workspace</strong><small>{role?.toLowerCase()}</small></span><span className="workspace-chevron">⌄</span></div>
      <div className="nav-caption">WORKSPACE</div>
      <nav aria-label="Main navigation">{(navigation[role] || []).map(([to, label, icon]) => <NavLink key={to} to={to} end={to === `/${role?.toLowerCase()}`} onClick={() => setMobileOpen(false)}><span className="nav-icon" aria-hidden="true">{icon}</span><span>{label}</span></NavLink>)}{role === "MANAGER" && <NavLink to="/manager/support" onClick={() => setMobileOpen(false)}><span className="nav-icon" aria-hidden="true">✉</span><span>Contact admin</span></NavLink>}</nav>
      <div className="sidebar-bottom"><div className="user-info"><span className="avatar">{initials}</span><span className="user-copy"><strong>{user?.name || "Workspace member"}</strong><small>{role?.toLowerCase()}</small></span></div></div>
    </aside>
    <section className="workspace-main">
      <header className="topbar">
        <button className="mobile-menu-button" aria-label="Open navigation" onClick={() => setMobileOpen(true)}>☰</button>
        <div className="topbar-title">{title}</div>
        <div className="topbar-right">
          <div className="notification-wrap">
            <button className="notification-bell" aria-label={'Notifications, ' + notificationInbox.unreadCount + ' unread'} aria-expanded={notificationsOpen} onClick={() => { setNotificationsOpen((open) => !open); setMenuOpen(false); void loadNotifications(); }}>
              <svg aria-hidden="true" viewBox="0 0 20 20"><path d="M15.5 7.5a5.5 5.5 0 0 0-11 0c0 6-2 6-2 7.5h15c0-1.5-2-1.5-2-7.5ZM8 17h4" /></svg>
              {notificationInbox.unreadCount > 0 && <span>{notificationInbox.unreadCount > 99 ? "99+" : notificationInbox.unreadCount}</span>}
            </button>
            {notificationsOpen && <section className="notification-panel" aria-label="Notifications">
              <div className="notification-panel-heading"><div><strong>Notifications</strong><small>{notificationInbox.unreadCount ? notificationInbox.unreadCount + " unread" : "All caught up"}</small></div>{notificationInbox.unreadCount > 0 && <button onClick={markAllNotificationsRead}>Mark all read</button>}</div>
              {notificationInbox.notifications.length ? <ul>{notificationInbox.notifications.slice(0, 8).map((notification) => <li key={notification.id}><button className={notification.read ? "notification-item is-read" : "notification-item is-unread"} onClick={() => void openNotification(notification)}><span className="notification-item-dot" /><span className="notification-item-copy"><strong>{notification.title}</strong><span>{notification.message}</span><small>{notification.actorName} · {new Date(notification.createdAt).toLocaleString()}</small></span></button></li>)}</ul> : <p className="notification-empty">No notifications yet.</p>}
            </section>}
          </div>
          <button className="topbar-account" aria-label="Open account menu" aria-expanded={menuOpen} onClick={() => { setMenuOpen((open) => !open); setNotificationsOpen(false); }}><span className="topbar-account-copy"><strong>{user?.name || "Workspace member"}</strong><small>{role?.toLowerCase()}</small></span><span className="avatar topbar-avatar">{initials}</span><span className="account-chevron" aria-hidden="true">⌄</span></button>
          {menuOpen && <div className="user-menu user-menu-top"><button onClick={() => { navigate("/profile"); setMenuOpen(false); }}>My Profile</button><button className="menu-logout" onClick={signOut}><svg aria-hidden="true" viewBox="0 0 20 20"><path d="M8 3H4.5A1.5 1.5 0 0 0 3 4.5v11A1.5 1.5 0 0 0 4.5 17H8m4-3 4-4-4-4m4 4H7" /></svg>Log out</button></div>}
        </div>
      </header>
      <main className="main-content">{children || <Outlet />}</main>
      <footer className="workspace-footer"><div><strong>ProjectHub</strong><span>Plan together. Deliver better.</span></div><nav aria-label="Social media"><a href="https://www.linkedin.com/" target="_blank" rel="noreferrer" aria-label="LinkedIn">in</a><a href="https://www.instagram.com/" target="_blank" rel="noreferrer" aria-label="Instagram">◎</a><a href="https://www.facebook.com/" target="_blank" rel="noreferrer" aria-label="Facebook">f</a><a href="https://x.com/" target="_blank" rel="noreferrer" aria-label="X">𝕏</a></nav><small>© {new Date().getFullYear()} ProjectHub</small></footer>
    </section>
  </div>;
}
