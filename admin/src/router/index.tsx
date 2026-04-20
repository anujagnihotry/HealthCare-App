import { createBrowserRouter, Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth.store'
import AdminLayout from '../layouts/AdminLayout'
import Login from '../pages/Login'
import Dashboard from '../pages/Dashboard'
import DoctorsList from '../pages/doctors/DoctorsList'
import DoctorDetail from '../pages/doctors/DoctorDetail'
import PatientsList from '../pages/patients/PatientsList'
import PatientDetail from '../pages/patients/PatientDetail'
import AppointmentsList from '../pages/appointments/AppointmentsList'
import ReportsPage from '../pages/reports/ReportsPage'
import LeadsList from '../pages/leads/LeadsList'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore((s) => s.token)
  if (!token) return <Navigate to="/login" replace />
  return <>{children}</>
}

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <AdminLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <Dashboard /> },
      { path: 'doctors', element: <DoctorsList /> },
      { path: 'doctors/:id', element: <DoctorDetail /> },
      { path: 'patients', element: <PatientsList /> },
      { path: 'patients/:id', element: <PatientDetail /> },
      { path: 'appointments', element: <AppointmentsList /> },
      { path: 'reports', element: <ReportsPage /> },
      { path: 'leads', element: <LeadsList /> },
    ],
  },
])
