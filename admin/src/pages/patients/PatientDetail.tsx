import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Tag, Button, Space, Spin, message, Table, Typography } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { getPatient } from '../../api/patients.api'

const { Title } = Typography

export default function PatientDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [patient, setPatient] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    getPatient(id)
      .then((r) => setPatient(r.data))
      .catch(() => message.error('Failed to load patient'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <div style={{ textAlign: 'center', padding: 60 }}><Spin /></div>
  if (!patient) return <div>Patient not found</div>

  const familyColumns = [
    { title: 'Name', dataIndex: 'name' },
    { title: 'Age', dataIndex: 'age' },
    { title: 'Gender', dataIndex: 'gender', render: (v: string) => <Tag>{v}</Tag> },
    { title: 'Blood Group', dataIndex: 'bloodGroup', render: (v: string) => v || '—' },
    { title: 'Self', dataIndex: 'isSelf', render: (v: boolean) => v ? <Tag color="blue">Self</Tag> : '—' },
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/patients')}>Back</Button>
        <Title level={4} style={{ margin: 0 }}>{patient.name}</Title>
      </Space>

      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        <Card title="Patient Profile">
          <Descriptions column={2} bordered>
            <Descriptions.Item label="Name">{patient.name}</Descriptions.Item>
            <Descriptions.Item label="Phone">{patient.phone || '—'}</Descriptions.Item>
            <Descriptions.Item label="Email">{patient.email || '—'}</Descriptions.Item>
            <Descriptions.Item label="Account Email">{patient.user?.email || '—'}</Descriptions.Item>
            <Descriptions.Item label="Account Phone">{patient.user?.phone || '—'}</Descriptions.Item>
            <Descriptions.Item label="Status">
              <Tag color={patient.user?.isActive !== false ? 'green' : 'red'}>
                {patient.user?.isActive !== false ? 'Active' : 'Inactive'}
              </Tag>
            </Descriptions.Item>
          </Descriptions>
        </Card>

        <Card title={`Family Members (${patient.familyMembers?.length || 0})`}>
          <Table
            dataSource={patient.familyMembers || []}
            columns={familyColumns}
            rowKey="id"
            pagination={false}
            size="small"
          />
        </Card>
      </Space>
    </div>
  )
}
