import { useEffect, useState } from 'react'
import {
  Table, Button, Input, Select, Space, Tag, Popconfirm, message, Modal, Form, Typography, Row, Col,
} from 'antd'
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { getLeads, createLead, updateLead, deleteLead } from '../../api/leads.api'

const { Title } = Typography
const { Option } = Select
const { TextArea } = Input

const SOURCES = ['website', 'referral', 'social_media', 'walk_in', 'phone_call', 'other']
const STATUSES = ['new', 'contacted', 'qualified', 'converted', 'lost']

const STATUS_COLORS: Record<string, string> = {
  new: 'blue',
  contacted: 'orange',
  qualified: 'geekblue',
  converted: 'green',
  lost: 'red',
}

export default function LeadsList() {
  const [leads, setLeads] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>()
  const [page, setPage] = useState(1)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingLead, setEditingLead] = useState<any>(null)
  const [form] = Form.useForm()

  const fetchLeads = async (p = page) => {
    setLoading(true)
    try {
      const params: any = { page: p, limit: 20 }
      if (search) params.search = search
      if (statusFilter) params.status = statusFilter
      const res = await getLeads(params)
      setLeads(res.data.data)
      setTotal(res.data.total)
    } catch { message.error('Failed to load leads') }
    finally { setLoading(false) }
  }

  useEffect(() => { fetchLeads() }, [statusFilter])

  const openCreate = () => {
    setEditingLead(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEdit = (lead: any) => {
    setEditingLead(lead)
    form.setFieldsValue({ status: lead.status, notes: lead.notes, assignedDoctorId: lead.assignedDoctorId })
    setModalOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    try {
      if (editingLead) {
        await updateLead(editingLead.id, values)
        message.success('Lead updated')
      } else {
        await createLead(values)
        message.success('Lead created')
      }
      setModalOpen(false)
      fetchLeads()
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Operation failed')
    }
  }

  const handleDelete = async (id: string) => {
    try {
      await deleteLead(id)
      message.success('Lead deleted')
      fetchLeads()
    } catch { message.error('Failed to delete lead') }
  }

  const columns = [
    { title: 'Name', dataIndex: 'name' },
    { title: 'Phone', dataIndex: 'phone', render: (v: string) => v || '—' },
    { title: 'Email', dataIndex: 'email', render: (v: string) => v || '—' },
    {
      title: 'Source',
      dataIndex: 'source',
      render: (s: string) => <Tag>{s?.replace('_', ' ')}</Tag>,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      render: (s: string) => <Tag color={STATUS_COLORS[s] || 'default'}>{s?.toUpperCase()}</Tag>,
    },
    { title: 'Notes', dataIndex: 'notes', ellipsis: true, render: (v: string) => v || '—' },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      render: (d: string) => dayjs(d).format('DD MMM YYYY'),
    },
    {
      title: 'Actions',
      render: (_: any, r: any) => (
        <Space>
          <Button icon={<EditOutlined />} size="small" onClick={() => openEdit(r)} />
          <Popconfirm title="Delete this lead?" onConfirm={() => handleDelete(r.id)}>
            <Button icon={<DeleteOutlined />} size="small" danger />
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col><Title level={4} style={{ margin: 0 }}>Leads ({total})</Title></Col>
        <Col>
          <Space>
            <Input
              placeholder="Search name, phone, email"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onPressEnter={() => fetchLeads(1)}
              prefix={<SearchOutlined />}
              style={{ width: 240 }}
              allowClear
            />
            <Select
              placeholder="Filter by status"
              allowClear
              style={{ width: 160 }}
              onChange={(v) => { setStatusFilter(v); setPage(1) }}
            >
              {STATUSES.map((s) => <Option key={s} value={s}>{s.toUpperCase()}</Option>)}
            </Select>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Add Lead</Button>
          </Space>
        </Col>
      </Row>

      <Table
        dataSource={leads}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{ total, pageSize: 20, current: page, onChange: (p) => { setPage(p); fetchLeads(p) } }}
      />

      <Modal
        title={editingLead ? 'Edit Lead' : 'Add Lead'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText={editingLead ? 'Update' : 'Create'}
      >
        <Form form={form} layout="vertical">
          {!editingLead && (
            <>
              <Form.Item name="name" label="Name" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="phone" label="Phone">
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="email" label="Email" rules={[{ type: 'email' }]}>
                    <Input />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item name="source" label="Source" rules={[{ required: true }]}>
                <Select>
                  {SOURCES.map((s) => <Option key={s} value={s}>{s.replace('_', ' ')}</Option>)}
                </Select>
              </Form.Item>
            </>
          )}
          {editingLead && (
            <Form.Item name="status" label="Status">
              <Select>
                {STATUSES.map((s) => <Option key={s} value={s}>{s.toUpperCase()}</Option>)}
              </Select>
            </Form.Item>
          )}
          <Form.Item name="notes" label="Notes">
            <TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
