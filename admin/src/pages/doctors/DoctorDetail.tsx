import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card, Descriptions, Tag, Button, Statistic, Row, Col, Typography, Space, Spin, message,
} from 'antd'
import { ArrowLeftOutlined, MedicineBoxOutlined } from '@ant-design/icons'
import { getDoctor } from '../../api/doctors.api'

const { Title } = Typography

export default function DoctorDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [doctor, setDoctor] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    getDoctor(id)
      .then((r) => setDoctor(r.data))
      .catch(() => message.error('Failed to load doctor'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <div style={{ textAlign: 'center', padding: 60 }}><Spin /></div>
  if (!doctor) return <div>Doctor not found</div>

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/doctors')}>Back</Button>
        <Title level={4} style={{ margin: 0 }}>
          <MedicineBoxOutlined /> {doctor.name}
        </Title>
      </Space>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <Card>
            <Statistic
              title="Total Appointments"
              value={doctor.appointmentCount ?? 0}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card>
            <Statistic
              title="Years of Experience"
              value={doctor.yearsOfExperience ?? '—'}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card>
            <Statistic
              title="Locations"
              value={doctor.locations?.length ?? 0}
            />
          </Card>
        </Col>

        <Col xs={24}>
          <Card title="Doctor Profile">
            <Descriptions column={2} bordered>
              <Descriptions.Item label="Specialization">{doctor.specialization}</Descriptions.Item>
              <Descriptions.Item label="Medical System">
                {doctor.medicalSystem ? <Tag color="blue">{doctor.medicalSystem}</Tag> : '—'}
              </Descriptions.Item>
              <Descriptions.Item label="Degree">{doctor.degree || '—'}</Descriptions.Item>
              <Descriptions.Item label="Contact Phone">{doctor.contactPhone || '—'}</Descriptions.Item>
              <Descriptions.Item label="Contact Email">{doctor.contactEmail || '—'}</Descriptions.Item>
              <Descriptions.Item label="Account Email">{doctor.user?.email || '—'}</Descriptions.Item>
              <Descriptions.Item label="Account Phone">{doctor.user?.phone || '—'}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={doctor.user?.isActive !== false ? 'green' : 'red'}>
                  {doctor.user?.isActive !== false ? 'Active' : 'Inactive'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Bio" span={2}>{doctor.bio || '—'}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>

        {doctor.locations?.length > 0 && (
          <Col xs={24}>
            <Card title="Clinic Locations">
              {doctor.locations.map((loc: any) => (
                <Card.Grid key={loc.id} style={{ width: '33%', minWidth: 250 }}>
                  <strong>{loc.name}</strong>
                  <p style={{ margin: '4px 0 0', color: '#888', fontSize: 13 }}>{loc.address}</p>
                  <Tag color={loc.isActive ? 'green' : 'default'} style={{ marginTop: 6 }}>
                    {loc.isActive ? 'Active' : 'Inactive'}
                  </Tag>
                </Card.Grid>
              ))}
            </Card>
          </Col>
        )}
      </Row>
    </div>
  )
}
