import { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Segmented, Typography, Spin, Space } from 'antd'
import {
  MedicineBoxOutlined,
  TeamOutlined,
  CalendarOutlined,
  FunnelPlotOutlined,
} from '@ant-design/icons'
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  Legend, ResponsiveContainer,
} from 'recharts'
import dayjs from 'dayjs'
import {
  getOverview,
  getDoctorsOnboarded,
  getPatientsOnboarded,
  getAppointmentsAnalytics,
  getAppointmentsByDoctor,
} from '../api/analytics.api'

const { Title } = Typography

type Period = 'day' | 'month' | 'year'

function formatPeriod(period: string, type: Period) {
  if (!period) return ''
  const d = dayjs(period)
  if (type === 'day') return d.format('DD MMM')
  if (type === 'month') return d.format('MMM YYYY')
  return d.format('YYYY')
}

export default function Dashboard() {
  const [period, setPeriod] = useState<Period>('month')
  const [overview, setOverview] = useState<any>(null)
  const [doctorsData, setDoctorsData] = useState<any[]>([])
  const [patientsData, setPatientsData] = useState<any[]>([])
  const [appointmentsData, setAppointmentsData] = useState<any[]>([])
  const [byDoctorData, setByDoctorData] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getOverview().then((r) => setOverview(r.data)).catch(() => {})
  }, [])

  useEffect(() => {
    setLoading(true)
    const params = { period }
    Promise.all([
      getDoctorsOnboarded(params),
      getPatientsOnboarded(params),
      getAppointmentsAnalytics(params),
      getAppointmentsByDoctor(params),
    ])
      .then(([doc, pat, apt, byDoc]) => {
        setDoctorsData(doc.data.map((d: any) => ({ ...d, label: formatPeriod(d.period, period) })))
        setPatientsData(pat.data.map((d: any) => ({ ...d, label: formatPeriod(d.period, period) })))
        setAppointmentsData(apt.data.map((d: any) => ({ ...d, label: formatPeriod(d.period, period) })))
        // Aggregate by doctor for bar chart (sum counts per doctor)
        const doctorMap: Record<string, { name: string; count: number }> = {}
        byDoc.data.forEach((d: any) => {
          if (!doctorMap[d.doctorId]) doctorMap[d.doctorId] = { name: d.doctorName, count: 0 }
          doctorMap[d.doctorId].count += d.count
        })
        setByDoctorData(
          Object.values(doctorMap).sort((a, b) => b.count - a.count).slice(0, 10)
        )
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [period])

  // Merge doctor + patient onboarding into one chart dataset
  const onboardingData = doctorsData.map((d, i) => ({
    label: d.label,
    Doctors: d.count,
    Patients: patientsData[i]?.count ?? 0,
  }))

  const kpiCards = [
    { title: 'Total Doctors', value: overview?.totalDoctors, icon: <MedicineBoxOutlined />, color: '#1890ff' },
    { title: 'Total Patients', value: overview?.totalPatients, icon: <TeamOutlined />, color: '#52c41a' },
    { title: 'Total Appointments', value: overview?.totalAppointments, icon: <CalendarOutlined />, color: '#faad14' },
    { title: 'Total Leads', value: overview?.totalLeads, icon: <FunnelPlotOutlined />, color: '#722ed1' },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>Dashboard Overview</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        {kpiCards.map((card) => (
          <Col xs={24} sm={12} lg={6} key={card.title}>
            <Card>
              <Statistic
                title={card.title}
                value={card.value ?? '—'}
                prefix={<span style={{ color: card.color }}>{card.icon}</span>}
                valueStyle={{ color: card.color }}
              />
            </Card>
          </Col>
        ))}
      </Row>

      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
        <Space>
          <span>Period:</span>
          <Segmented
            options={[
              { label: 'Day', value: 'day' },
              { label: 'Month', value: 'month' },
              { label: 'Year', value: 'year' },
            ]}
            value={period}
            onChange={(v) => setPeriod(v as Period)}
          />
        </Space>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
      ) : (
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={12}>
            <Card title="Doctors & Patients Onboarded">
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={onboardingData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="Doctors" stroke="#1890ff" strokeWidth={2} dot={false} />
                  <Line type="monotone" dataKey="Patients" stroke="#52c41a" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </Card>
          </Col>

          <Col xs={24} lg={12}>
            <Card title="Appointments Over Time">
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={appointmentsData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="count" name="Appointments" fill="#faad14" />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </Col>

          <Col xs={24}>
            <Card title="Top 10 Doctors by Appointment Count">
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={byDoctorData} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis type="number" allowDecimals={false} />
                  <YAxis dataKey="name" type="category" width={160} tick={{ fontSize: 12 }} />
                  <Tooltip />
                  <Bar dataKey="count" name="Appointments" fill="#722ed1" />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </Col>
        </Row>
      )}
    </div>
  )
}
