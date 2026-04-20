import { useState } from 'react'
import { Layout, Menu, Avatar, Dropdown, Typography, theme } from 'antd'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import {
  DashboardOutlined,
  UserOutlined,
  TeamOutlined,
  CalendarOutlined,
  FileTextOutlined,
  FunnelPlotOutlined,
  LogoutOutlined,
  MedicineBoxOutlined,
} from '@ant-design/icons'
import { useAuthStore } from '../store/auth.store'

const { Sider, Header, Content } = Layout
const { Text } = Typography

const menuItems = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/doctors', icon: <MedicineBoxOutlined />, label: 'Doctors' },
  { key: '/patients', icon: <TeamOutlined />, label: 'Patients' },
  { key: '/appointments', icon: <CalendarOutlined />, label: 'Appointments' },
  { key: '/reports', icon: <FileTextOutlined />, label: 'Reports' },
  { key: '/leads', icon: <FunnelPlotOutlined />, label: 'Leads' },
]

export default function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuthStore()
  const { token } = theme.useToken()

  const handleMenuClick = ({ key }: { key: string }) => navigate(key)

  const userMenu = {
    items: [
      {
        key: 'logout',
        icon: <LogoutOutlined />,
        label: 'Logout',
        onClick: () => { logout(); navigate('/login') },
      },
    ],
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        theme="dark"
        style={{ position: 'fixed', height: '100vh', left: 0, top: 0, zIndex: 100 }}
      >
        <div style={{ padding: '16px', textAlign: 'center', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
          {!collapsed && (
            <Text strong style={{ color: '#fff', fontSize: 16 }}>
              HealthCare Admin
            </Text>
          )}
          {collapsed && <MedicineBoxOutlined style={{ color: '#1890ff', fontSize: 20 }} />}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
          style={{ marginTop: 8 }}
        />
      </Sider>

      <Layout style={{ marginLeft: collapsed ? 80 : 200, transition: 'margin-left 0.2s' }}>
        <Header
          style={{
            background: token.colorBgContainer,
            padding: '0 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
            position: 'sticky',
            top: 0,
            zIndex: 99,
          }}
        >
          <Dropdown menu={userMenu} placement="bottomRight">
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
              <Avatar icon={<UserOutlined />} style={{ background: '#1890ff' }} />
              <Text>{user?.email || 'Admin'}</Text>
            </div>
          </Dropdown>
        </Header>

        <Content style={{ padding: 24, minHeight: 'calc(100vh - 64px)', background: '#f5f5f5' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
