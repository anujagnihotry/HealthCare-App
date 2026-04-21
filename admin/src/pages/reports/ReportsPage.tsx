import { useEffect, useState } from 'react'
import { Tabs, Table, Select, DatePicker, Tag, Button, Typography, Row, Col, message } from 'antd'
import { DownloadOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { getUploads, getConsultations, getMedicalRecords } from '../../api/reports.api'

const { Title } = Typography
const { RangePicker } = DatePicker
const { Option } = Select

function UploadsTab() {
  const [data, setData] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [type, setType] = useState<string>()
  const [dateRange, setDateRange] = useState<[string, string] | null>(null)

  const fetch = async () => {
    setLoading(true)
    try {
      const params: any = {}
      if (type) params.type = type
      if (dateRange) { params.dateFrom = dateRange[0]; params.dateTo = dateRange[1] }
      const res = await getUploads(params)
      setData(res.data.data)
      setTotal(res.data.total)
    } catch { message.error('Failed to load uploads') }
    finally { setLoading(false) }
  }

  useEffect(() => { fetch() }, [type, dateRange])

  const columns = [
    { title: 'File Name', dataIndex: 'fileName' },
    { title: 'Type', dataIndex: 'type', render: (t: string) => <Tag color={t === 'prescription' ? 'purple' : 'cyan'}>{t}</Tag> },
    { title: 'Patient', dataIndex: ['familyMember', 'name'], render: (v: string) => v || '—' },
    { title: 'Date', dataIndex: 'date', render: (d: string) => d ? dayjs(d).format('DD MMM YYYY') : '—' },
    {
      title: 'Download',
      dataIndex: 'fileUrl',
      render: (url: string) => (
        <Button
          size="small"
          icon={<DownloadOutlined />}
          href={url}
          target="_blank"
          rel="noopener noreferrer"
        >
          View
        </Button>
      ),
    },
  ]

  return (
    <>
      <Row style={{ marginBottom: 16 }} gutter={8}>
        <Col>
          <Select placeholder="Type" allowClear style={{ width: 160 }} onChange={setType}>
            <Option value="report">Report</Option>
            <Option value="prescription">Prescription</Option>
          </Select>
        </Col>
        <Col>
          <RangePicker
            onChange={(dates) => setDateRange(dates ? [dates[0]!.format('YYYY-MM-DD'), dates[1]!.format('YYYY-MM-DD')] : null)}
          />
        </Col>
      </Row>
      <Table dataSource={data} columns={columns} rowKey="id" loading={loading} pagination={{ total, pageSize: 20 }} />
    </>
  )
}

function ConsultationsTab() {
  const [data, setData] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [dateRange, setDateRange] = useState<[string, string] | null>(null)

  const fetch = async () => {
    setLoading(true)
    try {
      const params: any = {}
      if (dateRange) { params.dateFrom = dateRange[0]; params.dateTo = dateRange[1] }
      const res = await getConsultations(params)
      setData(res.data.data)
      setTotal(res.data.total)
    } catch { message.error('Failed to load consultations') }
    finally { setLoading(false) }
  }

  useEffect(() => { fetch() }, [dateRange])

  const columns = [
    { title: 'Doctor', dataIndex: ['doctor', 'name'] },
    { title: 'Patient', dataIndex: ['patient', 'name'] },
    { title: 'Family Member', dataIndex: ['familyMember', 'name'], render: (v: string) => v || '—' },
    { title: 'Symptoms', dataIndex: 'symptoms', ellipsis: true },
    { title: 'Diagnosis', dataIndex: 'diagnosis', ellipsis: true },
    { title: 'Medications', dataIndex: 'medications', ellipsis: true },
    { title: 'Date', dataIndex: 'createdAt', render: (d: string) => dayjs(d).format('DD MMM YYYY') },
  ]

  return (
    <>
      <Row style={{ marginBottom: 16 }}>
        <Col>
          <RangePicker
            onChange={(dates) => setDateRange(dates ? [dates[0]!.format('YYYY-MM-DD'), dates[1]!.format('YYYY-MM-DD')] : null)}
          />
        </Col>
      </Row>
      <Table dataSource={data} columns={columns} rowKey="id" loading={loading} pagination={{ total, pageSize: 20 }} />
    </>
  )
}

function MedicalRecordsTab() {
  const [data, setData] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    setLoading(true)
    getMedicalRecords()
      .then((r) => { setData(r.data.data); setTotal(r.data.total) })
      .catch(() => message.error('Failed to load records'))
      .finally(() => setLoading(false))
  }, [])

  const columns = [
    { title: 'Doctor', dataIndex: ['doctor', 'name'] },
    { title: 'Patient', dataIndex: ['familyMember', 'name'] },
    { title: 'Diagnosis', dataIndex: 'diagnosis', ellipsis: true, render: (v: string) => v || '—' },
    { title: 'Notes', dataIndex: 'notes', ellipsis: true, render: (v: string) => v || '—' },
    { title: 'Date', dataIndex: 'createdAt', render: (d: string) => dayjs(d).format('DD MMM YYYY') },
  ]

  return (
    <Table dataSource={data} columns={columns} rowKey="id" loading={loading} pagination={{ total, pageSize: 20 }} />
  )
}

export default function ReportsPage() {
  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>Reports & Documents</Title>
      <Tabs
        items={[
          { key: 'uploads', label: 'Uploads (Reports & Prescriptions)', children: <UploadsTab /> },
          { key: 'consultations', label: 'Consultations', children: <ConsultationsTab /> },
          { key: 'medical-records', label: 'Medical Records', children: <MedicalRecordsTab /> },
        ]}
      />
    </div>
  )
}
