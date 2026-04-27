import React, { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'

function CourseList({ currentEmployee }) {
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [enrolling, setEnrolling] = useState(false)
  const [enrollResult, setEnrollResult] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    loadCourses()
  }, [])

  const loadCourses = async () => {
    try {
      setLoading(true)
      const response = await api.get('/courses/published')
      setCourses(response.data)
      setError(null)
    } catch (err) {
      setError('加载课程列表失败')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleEnroll = async (courseId) => {
    if (!currentEmployee) {
      setError('请先选择员工')
      return
    }

    try {
      setEnrolling(true)
      const response = await api.post(
        `/enrollments/enroll?employeeId=${currentEmployee.id}&courseId=${courseId}`
      )
      setEnrollResult({ success: true, message: '报名成功！' })
      setTimeout(() => {
        navigate('/my-enrollments')
      }, 1500)
    } catch (err) {
      setEnrollResult({
        success: false,
        message: err.response?.data || '报名失败'
      })
    } finally {
      setEnrolling(false)
    }
  }

  const hasAvailableSlots = (course) => {
    return course.currentParticipants < course.maxParticipants
  }

  const getStatusBadge = (course) => {
    if (!hasAvailableSlots(course)) {
      return <span className="badge badge-danger">名额已满</span>
    }
    return <span className="badge badge-success">可报名</span>
  }

  if (loading) {
    return <div className="loading">加载中...</div>
  }

  return (
    <div>
      <h2 className="page-title">课程中心</h2>

      {enrollResult && (
        <div className={enrollResult.success ? 'success' : 'error'}>
          {enrollResult.message}
        </div>
      )}

      {error && <div className="error">{error}</div>}

      {courses.length === 0 ? (
        <div className="empty-state">
          <div className="icon">📚</div>
          <h3>暂无课程</h3>
          <p>目前没有可用的培训课程</p>
        </div>
      ) : (
        <div className="grid grid-2">
          {courses.map(course => (
            <div key={course.id} className="card">
              <div className="card-header">
                <div>
                  <h3 className="card-title">{course.name}</h3>
                  <p className="card-subtitle">{course.code}</p>
                </div>
                {getStatusBadge(course)}
              </div>
              <div className="card-body">
                <p className="mb-2">{course.description}</p>
                <div className="flex gap-4" style={{ marginTop: '1rem' }}>
                  <div>
                    <span style={{ color: '#6b7280', fontSize: '0.85rem' }}>课程费用</span>
                    <p style={{ fontWeight: '600', color: '#667eea' }}>¥{course.courseFee.toFixed(2)}</p>
                  </div>
                  <div>
                    <span style={{ color: '#6b7280', fontSize: '0.85rem' }}>剩余名额</span>
                    <p style={{ fontWeight: '600', color: '#10b981' }}>
                      {course.maxParticipants - course.currentParticipants} / {course.maxParticipants}
                    </p>
                  </div>
                  <div>
                    <span style={{ color: '#6b7280', fontSize: '0.85rem' }}>章节数</span>
                    <p style={{ fontWeight: '600', color: '#4b5563' }}>{course.totalChapters} 章</p>
                  </div>
                </div>
              </div>
              <div className="card-footer">
                <Link
                  to={`/course/${course.id}`}
                  className="btn btn-secondary"
                >
                  查看详情
                </Link>
                <button
                  className="btn btn-primary"
                  onClick={() => handleEnroll(course.id)}
                  disabled={enrolling || !hasAvailableSlots(course)}
                >
                  {enrolling ? '报名中...' : '立即报名'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default CourseList
