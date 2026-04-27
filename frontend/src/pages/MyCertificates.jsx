import React, { useState, useEffect } from 'react'
import { api } from '../api'

function MyCertificates({ currentEmployee }) {
  const [certificates, setCertificates] = useState([])
  const [courses, setCourses] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (currentEmployee) {
      loadCertificates()
    } else {
      setLoading(false)
    }
  }, [currentEmployee])

  const loadCertificates = async () => {
    try {
      setLoading(true)
      const response = await api.get(`/certificates/employee/${currentEmployee.id}`)
      setCertificates(response.data)

      const courseIds = [...new Set(response.data.map(c => c.courseId))]
      const courseMap = {}
      for (const courseId of courseIds) {
        try {
          const courseRes = await api.get(`/courses/${courseId}`)
          courseMap[courseId] = courseRes.data
        } catch (e) {
          console.error(`加载课程 ${courseId} 失败`, e)
        }
      }
      setCourses(courseMap)
      setError(null)
    } catch (err) {
      setError('加载证书列表失败')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const getStatusBadge = (status) => {
    const statusMap = {
      'VALID': { text: '有效', class: 'badge-success' },
      'EXPIRING_SOON': { text: '即将过期', class: 'badge-warning' },
      'EXPIRED': { text: '已过期', class: 'badge-danger' },
      'REVOKED': { text: '已吊销', class: 'badge-default' }
    }
    const info = statusMap[status] || { text: status, class: 'badge-default' }
    return <span className={`badge ${info.class}`}>{info.text}</span>
  }

  if (loading) {
    return <div className="loading">加载中...</div>
  }

  if (!currentEmployee) {
    return (
      <div className="empty-state">
        <div className="icon">👤</div>
        <h3>请选择员工</h3>
        <p>请在页面顶部选择一个员工查看其证书</p>
      </div>
    )
  }

  return (
    <div>
      <h2 className="page-title">我的证书</h2>

      {error && <div className="error">{error}</div>}

      {certificates.length === 0 ? (
        <div className="empty-state">
          <div className="icon">🎓</div>
          <h3>暂无证书</h3>
          <p>您还没有获得任何培训证书</p>
        </div>
      ) : (
        <div className="grid grid-2">
          {certificates.map(cert => {
            const course = courses[cert.courseId]
            const isExpiring = cert.status === 'EXPIRING_SOON'
            const isExpired = cert.status === 'EXPIRED' || cert.status === 'REVOKED'

            return (
              <div
                key={cert.id}
                className="card"
                style={{
                  borderLeft: isExpired ? '4px solid #ef4444' : isExpiring ? '4px solid #f59e0b' : '4px solid #10b981',
                  opacity: isExpired ? 0.8 : 1
                }}
              >
                <div className="card-header">
                  <div>
                    <h3 className="card-title">{course?.name || `课程 ${cert.courseId}`}</h3>
                    <p className="card-subtitle">证书编号：{cert.certificateNo}</p>
                  </div>
                  {getStatusBadge(cert.status)}
                </div>
                <div className="card-body">
                  <div className="stat-grid" style={{ marginBottom: 0 }}>
                    <div style={{ padding: '0.5rem' }}>
                      <span style={{ color: '#6b7280', fontSize: '0.8rem' }}>颁发日期</span>
                      <p style={{ fontWeight: '600' }}>
                        {cert.issueDate ? new Date(cert.issueDate).toLocaleDateString() : '-'}
                      </p>
                    </div>
                    <div style={{ padding: '0.5rem' }}>
                      <span style={{ color: '#6b7280', fontSize: '0.8rem' }}>有效期至</span>
                      <p style={{
                        fontWeight: '600',
                        color: isExpired ? '#ef4444' : isExpiring ? '#f59e0b' : '#10b981'
                      }}>
                        {cert.expiryDate ? new Date(cert.expiryDate).toLocaleDateString() : '永久有效'}
                      </p>
                    </div>
                  </div>
                </div>
                {isExpiring && (
                  <div className="warning" style={{ margin: '1rem 0', fontSize: '0.85rem' }}>
                    ⚠️ 证书即将过期，请及时安排复训
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

export default MyCertificates
