import React, { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'

function MyEnrollments({ currentEmployee }) {
  const [enrollments, setEnrollments] = useState([])
  const [courses, setCourses] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [withdrawing, setWithdrawing] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    if (currentEmployee) {
      loadEnrollments()
    } else {
      setLoading(false)
    }
  }, [currentEmployee])

  const loadEnrollments = async () => {
    try {
      setLoading(true)
      const response = await api.get(`/enrollments/employee/${currentEmployee.id}`)
      setEnrollments(response.data)
      
      const courseIds = [...new Set(response.data.map(e => e.courseId))]
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
      setError('加载报名记录失败')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleWithdraw = async (enrollmentId) => {
    if (!confirm('确定要退课吗？')) return

    try {
      setWithdrawing(enrollmentId)
      await api.post(`/enrollments/${enrollmentId}/withdraw`)
      loadEnrollments()
    } catch (err) {
      alert(err.response?.data || '退课失败')
    } finally {
      setWithdrawing(null)
    }
  }

  const getStatusBadge = (status) => {
    const statusMap = {
      'ENROLLED': { text: '已报名', class: 'badge-info' },
      'IN_PROGRESS': { text: '学习中', class: 'badge-warning' },
      'COMPLETED': { text: '已完成', class: 'badge-success' },
      'CERTIFIED': { text: '已获证', class: 'badge-success' },
      'WITHDRAWN': { text: '已退课', class: 'badge-default' },
      'FAILED': { text: '未通过', class: 'badge-danger' }
    }
    const info = statusMap[status] || { text: status, class: 'badge-default' }
    return <span className={`badge ${info.class}`}>{info.text}</span>
  }

  const canWithdraw = (enrollment) => {
    return ['ENROLLED', 'IN_PROGRESS'].includes(enrollment.status)
  }

  const canLearn = (enrollment) => {
    return ['ENROLLED', 'IN_PROGRESS'].includes(enrollment.status)
  }

  const canTakeExam = async (enrollment) => {
    try {
      const response = await api.get(`/learning/can-take-exam/${enrollment.id}`)
      return response.data
    } catch (e) {
      return false
    }
  }

  if (loading) {
    return <div className="loading">加载中...</div>
  }

  if (!currentEmployee) {
    return (
      <div className="empty-state">
        <div className="icon">👤</div>
        <h3>请选择员工</h3>
        <p>请在页面顶部选择一个员工查看其报名记录</p>
      </div>
    )
  }

  return (
    <div>
      <h2 className="page-title">我的报名</h2>

      {error && <div className="error">{error}</div>}

      {enrollments.length === 0 ? (
        <div className="empty-state">
          <div className="icon">📝</div>
          <h3>暂无报名记录</h3>
          <p>您还没有报名任何课程</p>
          <Link to="/" className="btn btn-primary" style={{ marginTop: '1rem' }}>
            浏览课程
          </Link>
        </div>
      ) : (
        <div className="grid grid-2">
          {enrollments.map(enrollment => {
            const course = courses[enrollment.courseId]
            return (
              <div key={enrollment.id} className="card">
                <div className="card-header">
                  <div>
                    <h3 className="card-title">{course?.name || `课程 ${enrollment.courseId}`}</h3>
                    <p className="card-subtitle">
                      报名时间：{new Date(enrollment.enrollmentDate).toLocaleString()}
                    </p>
                  </div>
                  {getStatusBadge(enrollment.status)}
                </div>
                <div className="card-body">
                  <div className="flex gap-4">
                    <div>
                      <span style={{ color: '#6b7280', fontSize: '0.85rem' }}>预留费用</span>
                      <p style={{ fontWeight: '600' }}>¥{enrollment.reservedFee?.toFixed(2) || '0.00'}</p>
                    </div>
                    {enrollment.actualFee !== null && enrollment.actualFee !== undefined && (
                      <div>
                        <span style={{ color: '#6b7280', fontSize: '0.85rem' }}>实际费用</span>
                        <p style={{ fontWeight: '600', color: '#ef4444' }}>¥{enrollment.actualFee.toFixed(2)}</p>
                      </div>
                    )}
                    {enrollment.refundAmount !== null && enrollment.refundAmount !== undefined && enrollment.refundAmount > 0 && (
                      <div>
                        <span style={{ color: '#6b7280', fontSize: '0.85rem' }}>退款金额</span>
                        <p style={{ fontWeight: '600', color: '#10b981' }}>¥{enrollment.refundAmount.toFixed(2)}</p>
                      </div>
                    )}
                  </div>
                  {enrollment.startDate && (
                    <p className="mt-2" style={{ color: '#6b7280', fontSize: '0.85rem' }}>
                      开始学习时间：{new Date(enrollment.startDate).toLocaleString()}
                    </p>
                  )}
                  {enrollment.completionDate && (
                    <p className="mt-1" style={{ color: '#6b7280', fontSize: '0.85rem' }}>
                      完成时间：{new Date(enrollment.completionDate).toLocaleString()}
                    </p>
                  )}
                </div>
                <div className="card-footer">
                  {canLearn(enrollment) && (
                    <Link
                      to={`/learn/${enrollment.id}`}
                      className="btn btn-primary"
                    >
                      继续学习
                    </Link>
                  )}
                  {enrollment.status === 'COMPLETED' && (
                    <Link
                      to={`/exam/${enrollment.id}`}
                      className="btn btn-success"
                    >
                      参加考试
                    </Link>
                  )}
                  {enrollment.status === 'CERTIFIED' && (
                    <Link
                      to="/my-certificates"
                      className="btn btn-outline"
                    >
                      查看证书
                    </Link>
                  )}
                  {canWithdraw(enrollment) && (
                    <button
                      className="btn btn-danger"
                      onClick={() => handleWithdraw(enrollment.id)}
                      disabled={withdrawing === enrollment.id}
                    >
                      {withdrawing === enrollment.id ? '退课中...' : '退课'}
                    </button>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

export default MyEnrollments
