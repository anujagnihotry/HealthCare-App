import { useEffect, useState } from 'react'
import {
  Table, Select, Space, Tag, Button, DatePicker, message, Modal, Typography, Row, Col,
} from 'antd'
import dayjs from 'dayjs'
import { getAppointments, updateAppointmentStatus } from '../../api/appointments.api'

const { Title } = Typography
const { RangePicker } = DatePicker
const { Option } = Select

const STATUS_COLORS: Record<string, string> = {
  booked: 'blue',
  completed: 'green',
  cancelled: 'red',
  no_show: 'orange',
}

const STATUSES = ['booked', 'completed', 'cancelled', 'no_show']

export default function AppointmentsList() {
  const [appointments, setAppointments] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [statusFilter, setStatusFilter] = useState<string>()
  const [dateRange, setDateRange] = useState<[string, string] | null>(null)
  const [statusModal, setStatusModal] = useState<{ open: boolean; id: string; current: string }>({
    open: false, id: '', current: '',
  })
  const [newStatus, setNewStatus] = useState('')

  const fetchAppointments = async (p = page) => {
    setLoading(true)
    try {
      const params: any = { page: p, limit: 20 }
      if (statusFilter) params.status = statusFilter
      if (dateRange) { params.dateFrom = dateRange[0]; params.dateTo = dateRange[1] }
      const res = await getAppointments(params)
      setAppointments(res.data.data)
      setTotal(res.data.total)
    } catch {
      message.error('Failed to load appointments')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchAppointments() }, [statusFilter, dateRange])

  const openStatusModal = (id: string, current: string) => {
    setNewStatus(current)
    setStatusModal({ open: true, id, current })
  }

  const handleStatusUpdate = async () => {
    try {
      await updateAppointmentStatus(statusModal.id, newStatus)
      message.success('Status updated')
      setStatusModal({ open: false, id: '', current: '' })
      fetchAppointments()
    } catch {
      message.error('Failed to update status')
    }
  }

  const columns = [
    {
      title: 'Date',
      dataIndex: 'date',
      render: (d: string) => dayjs(d).format('DD MMM YYYY'),
    },
    {
      title: 'Time',
      dataIndex: 'timeSlot',
      render: (t: string) => t?.slice(0, 5) || '—',
    },
    {
      title: 'Patient',
      dataIndex: ['patient', 'name'],
      render: (name: string, r: any) => name || r.familyMember?.name || '—',
    },
    {
      title: 'Doctor',
      dataIndex: ['doctor', 'name'],
    },
    {
      title: 'Specialization',
      dataIndex: ['doctor', 'specialization'],
    },
    {
      title: 'Location',
      dataIndex: ['location', 'name'],
      render: (v: string) => v || '—',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      render: (s: string) => <Tag color={STATUS_COLORS[s] || 'default'}>{s?.replace('_', ' ').toUpperCase()}</Tag>,
    },
    {
      title: 'Actions',
      render: (_: any, r: any) => (
        <Button size="small" onClick={() => openStatusModal(r.id, r.status)}>Change Status</Button>
      ),
    },
  ]

  return (
    <div>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col><Title level={4} style={{ margin: 0 }}>Appointments ({total})</Title></Col>
        <Col>
          <Space>
            <RangePicker
              onChange={(dates) => {
                if (dates) {
                  setDateRange([
                    dates[0]!.format('YYYY-MM-DD'),
                    dates[1]!.format('YYYY-MM-DD'),
                  ])
                } else {
                  setDateRange(null)
                }
              }}
            />
            <Select
              placeholder="Filter by status"
              allowClear
              style={{ width: 180 }}
              onChange={(v) => setStatusFilter(v)}
            >
              {STATUSES.map((s) => (
                <Option key={s} value={s}>{s.replace('_', ' ').toUpperCase()}</Option>
              ))}
            </Select>
          </Space>
        </Col>
      </Row>

      <Table
        dataSource={appointments}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{ total, pageSize: 20, current: page, onChange: (p) => { setPage(p); fetchAppointments(p) } }}
      />

      <Modal
        title="Change Appointment Status"
        open={statusModal.open}
        onOk={handleStatusUpdate}
        onCancel={() => setStatusModal({ open: false, id: '', current: '' })}
      >
        <Select value={newStatus} onChange={setNewStatus} style={{ width: '100%' }}>
          {STATUSES.map((s) => (
            <Option key={s} value={s}>{s.replace('_', ' ').toUpperCase()}</Option>
          ))}
        </Select>
      </Modal>
    </div>
  )
}
