import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { ProtectedRoute } from './components/ProtectedRoute';
import { ClientsPage } from './pages/ClientsPage';
import { DashboardPage } from './pages/DashboardPage';
import { LoginPage } from './pages/LoginPage';
import { ProjectDetailPage } from './pages/ProjectDetailPage';
import { ProjectsPage } from './pages/ProjectsPage';
import { CodeTab } from './pages/project/CodeTab';
import { MapTab } from './pages/project/MapTab';
import { ModernizeTab } from './pages/project/ModernizeTab';
import { ReportTab } from './pages/project/ReportTab';
import { VerifyTab } from './pages/project/VerifyTab';

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route index element={<DashboardPage />} />
          <Route path="clients" element={<ClientsPage />} />
          <Route path="projects" element={<ProjectsPage />} />
          <Route path="projects/:projectId" element={<ProjectDetailPage />}>
            <Route index element={<Navigate to="code" replace />} />
            <Route path="code" element={<CodeTab />} />
            <Route path="map" element={<MapTab />} />
            <Route path="modernize" element={<ModernizeTab />} />
            <Route path="verify" element={<VerifyTab />} />
            <Route path="report" element={<ReportTab />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
