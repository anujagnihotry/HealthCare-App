import { useEffect, useState } from 'react'
import {
  Table, Button, Input, Space, Tag, Popconfirm, message, Modal, Form,
  Select, InputNumber, Typography, Row, Col,
} from 'antd'
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getDoctors, createDoctor, updateDoctor, deleteDoctor } from '../../api/doctors.api'

const { Title } = Typography
const { Option } = Select

const MEDICAL_SYSTEMS = ['allopathy', 'homeopathy', 'ayush']

export default function DoctorsList() {
  const [doctors, setDoctors] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingDoctor, setEditingDoctor] = useState<any>(null)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const fetchDoctors = async (p = page, s = search) => {
    setLoading(true)
    try {
      const res = await getDoctors({ search: s, page: p, limit: 20 })
      setDoctors(res.data.data)
      setTotal(res.data.total)
    } catch {
      message.error('Failed to load doctors')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchDoctors() }, [])

  const handleSearch = () => { setPage(1); fetchDoctors(1, search) }

  const openCreate = () => {
    setEditingDoctor(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEdit = (doctor: any) => {
    setEditingDoctor(doctor)
    form.setFieldsValue({
      name: doctor.name,
      specialization: doctor.specialization,
      bio: doctor.bio,
      contactPhone: doctor.contactPhone,
      contactEmail: doctor.contactEmail,
      yearsOfExperience: doctor.yearsOfExperience,
      degree: doctor.degree,
      medicalSystem: doctor.medicalSystem,
    })
    setModalOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    try {
      if (editingDoctor) {
        await updateDoctor(editingDoctor.id, values)
        message.success('Doctor updated')
      } else {
        await createDoctor(values)
        message.success('Doctor created')
      }
      setModalOpen(false)
      fetchDoctors()
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Operation failed')
    }
  }

  const handleDelete = async (id: string) => {
    try {
      await deleteDoctor(id)
      message.success('Doctor deactivated')
      fetchDoctors()
    } catch {
      message.error('Failed to deactivate doctor')
    }
  }

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      render: (name: string, r: any) => (
        <a onClick={() => navigate(`/doctors/${r.id}`)}>{name}</a>
      ),
    },
    { title: 'Specialization', dataIndex: 'specialization' },
    { title: 'Medical System', dataIndex: 'medicalSystem', render: (v: string) => v ? <Tag color="blue">{v}</Tag> : '—' },
    { title: 'Contact', dataIndex: 'contactPhone', render: (v: string) => v || '—' },
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
          <Button icon={<EyeOutlined />} size="small" onClick={() => navigate(`/doctors/${r.id}`)} />
          <Button icon={<EditOutlined />} size="small" onClick={() => openEdit(r)} />
          <Popconfirm title="Deactivate this doctor?" onConfirm={() => handleDelete(r.id)} okText="Yes">
            <Button icon={<DeleteOutlined />} size="small" danger />
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col><Title level={4} style={{ margin: 0 }}>Doctors ({total})</Title></Col>
        <Col>
          <Space>
            <Input
              placeholder="Search by name, phone, email, specialization"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onPressEnter={handleSearch}
              prefix={<SearchOutlined />}
              style={{ width: 320 }}
              allowClear
              onClear={() => { setSearch(''); fetchDoctors(1, '') }}
            />
            <Button onClick={handleSearch}>Search</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Add Doctor</Button>
          </Space>
        </Col>
      </Row>

      <Table
        dataSource={doctors}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{ total, pageSize: 20, current: page, onChange: (p) => { setPage(p); fetchDoctors(p) } }}
      />

      <Modal
        title={editingDoctor ? 'Edit Doctor' : 'Add Doctor'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        width={640}
        okText={editingDoctor ? 'Update' : 'Create'}
      >
        <Form form={form} layout="vertical">
          {!editingDoctor && (
            <>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="email" label="Email" rules={[{ type: 'email' }]}>
                    <Input placeholder="doctor@example.com" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="phone" label="Phone">
                    <Input placeholder="+919876543210" />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item name="password" label="Password" rules={[{ required: true, min: 6 }]}>
                <Input.Password placeholder="Min 6 characters" />
              </Form.Item>
            </>
          )}
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="name" label="Full Name" rules={[{ required: true }]}>
                <Input placeholder="Dr. Name" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="specialization" label="Specialization" rules={[{ required: true }]}>
                <Input placeholder="Cardiology" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="medicalSystem" label="Medical System">
                <Select placeholder="Select system" allowClear>
                  {MEDICAL_SYSTEMS.map((s) => <Option key={s} value={s}>{s}</Option>)}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="yearsOfExperience" label="Years of Experience">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="degree" label="Degree">
            <Input placeholder="MBBS, MD" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="contactPhone" label="Contact Phone">
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="contactEmail" label="Contact Email" rules={[{ type: 'email' }]}>
                <Input />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="bio" label="Bio">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
