import { useEffect, useState } from 'react'
import {
  Table, Button, Input, Space, Tag, Popconfirm, message, Modal, Form, Typography, Row, Col,
} from 'antd'
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getPatients, createPatient, updatePatient, deletePatient } from '../../api/patients.api'

const { Title } = Typography

export default function PatientsList() {
  const [patients, setPatients] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingPatient, setEditingPatient] = useState<any>(null)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const fetchPatients = async (p = page, s = search) => {
    setLoading(true)
    try {
      const res = await getPatients({ search: s, page: p, limit: 20 })
      setPatients(res.data.data)
      setTotal(res.data.total)
    } catch {
      message.error('Failed to load patients')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchPatients() }, [])

  const handleSearch = () => { setPage(1); fetchPatients(1, search) }

  const openCreate = () => {
    setEditingPatient(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEdit = (patient: any) => {
    setEditingPatient(patient)
    form.setFieldsValue({ name: patient.name, phone: patient.phone, email: patient.email })
    setModalOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    try {
      if (editingPatient) {
        await updatePatient(editingPatient.id, values)
        message.success('Patient updated')
      } else {
        await createPatient(values)
        message.success('Patient created')
      }
      setModalOpen(false)
      fetchPatients()
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Operation failed')
    }
  }

  const handleDelete = async (id: string) => {
    try {
      await deletePatient(id)
      message.success('Patient deactivated')
      fetchPatients()
    } catch {
      message.error('Failed to deactivate patient')
    }
  }

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      render: (name: string, r: any) => <a onClick={() => navigate(`/patients/${r.id}`)}>{name}</a>,
    },
    { title: 'Phone', dataIndex: 'phone', render: (v: string) => v || '—' },
    { title: 'Email', dataIndex: 'email', render: (v: string) => v || '—' },
    {
      title: 'Family Members',
      dataIndex: 'familyMembers',
      render: (fm: any[]) => fm?.length ?? '—',
    },
    {
      title: 'Status',
      dataIndex: ['user', 'isActive'],
      render: (active: boolean) => (
        <Tag color={active !== false ? 'green' : 'red'}>{active !== false ? 'Active' : 'Inactive'}</Tag>
      ),
    },
    {
      title: 'Actions',
      render: (_: any, r: any) => (
        <Space>
          <Button icon={<EyeOutlined />} size="small" onClick={() => navigate(`/patients/${r.id}`)} />
          <Button icon={<EditOutlined />} size="small" onClick={() => openEdit(r)} />
          <Popconfirm title="Deactivate this patient?" onConfirm={() => handleDelete(r.id)} okText="Yes">
            <Button icon={<DeleteOutlined />} size="small" danger />
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col><Title level={4} style={{ margin: 0 }}>Patients ({total})</Title></Col>
        <Col>
          <Space>
            <Input
              placeholder="Search by name, phone, email"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onPressEnter={handleSearch}
              prefix={<SearchOutlined />}
              style={{ width: 280 }}
              allowClear
              onClear={() => { setSearch(''); fetchPatients(1, '') }}
            />
            <Button onClick={handleSearch}>Search</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Add Patient</Button>
          </Space>
        </Col>
      </Row>

      <Table
        dataSource={patients}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{ total, pageSize: 20, current: page, onChange: (p) => { setPage(p); fetchPatients(p) } }}
      />

      <Modal
        title={editingPatient ? 'Edit Patient' : 'Add Patient'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText={editingPatient ? 'Update' : 'Create'}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Full Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          {!editingPatient && (
            <>
              <Form.Item name="phone" label="Phone" rules={[{ required: true }]}>
                <Input placeholder="+919876543210" />
              </Form.Item>
              <Form.Item name="email" label="Email" rules={[{ type: 'email' }]}>
                <Input />
              </Form.Item>
              <Form.Item name="password" label="Password" rules={[{ required: true, min: 6 }]}>
                <Input.Password />
              </Form.Item>
            </>
          )}
          {editingPatient && (
            <>
              <Form.Item name="phone" label="Phone">
                <Input />
              </Form.Item>
              <Form.Item name="email" label="Email" rules={[{ type: 'email' }]}>
                <Input />
              </Form.Item>
            </>
          )}
        </Form>
      </Modal>
    </div>
  )
}
