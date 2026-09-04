import {
  createRootRoute,
  createRoute,
  createRouter,
  Outlet,
  redirect,
} from "@tanstack/react-router";
import { getRole, getToken } from "./api/client";
import { LoginPage } from "./pages/LoginPage";
import { StudentDashboardPage } from "./pages/StudentDashboardPage";
import { UploadPage } from "./pages/UploadPage";
import { AdminDashboardPage } from "./pages/AdminDashboardPage";

function requireStudent() {
  if (!getToken() || getRole() !== "STUDENT") {
    throw redirect({ to: "/login" });
  }
}

function requireAdmin() {
  if (!getToken() || getRole() !== "ADMIN") {
    throw redirect({ to: "/login" });
  }
}

const rootRoute = createRootRoute({
  component: () => <Outlet />,
});

const loginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/login",
  component: LoginPage,
});

const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  beforeLoad: () => {
    const role = getRole();
    if (!getToken()) {
      throw redirect({ to: "/login" });
    }
    if (role === "ADMIN") {
      throw redirect({ to: "/admin" });
    }
    throw redirect({ to: "/dashboard" });
  },
});

const dashboardRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/dashboard",
  beforeLoad: requireStudent,
  component: StudentDashboardPage,
});

const uploadRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/upload",
  beforeLoad: requireStudent,
  component: UploadPage,
});

const adminRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/admin",
  beforeLoad: requireAdmin,
  component: AdminDashboardPage,
});

const routeTree = rootRoute.addChildren([
  indexRoute,
  loginRoute,
  dashboardRoute,
  uploadRoute,
  adminRoute,
]);

export const router = createRouter({ routeTree });

declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}
