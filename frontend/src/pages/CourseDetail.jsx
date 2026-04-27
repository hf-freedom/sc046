import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '../api'

function CourseDetail({ currentEmployee }) {
  const { courseId } = useParams()
  const [course, setCourse] = useState(null)
  const [chapters, setChapters] = useState([])
  const [exam, setExam] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [enrolling, setEnrolling] = useState(false)
  const [enrollResult, setEnrollResult] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    loadCourseDetail()
  }, [courseId])

  const loadCourseDetail = async () => {
    try {
      setLoading(true)
      const [courseRes, chaptersRes, examRes] = await Promise.all([
        api.get(`/courses/${courseId}`),
        api.get(`/courses/${courseId}/chapters`),
        api.get(`/courses/${courseId}/exam`).catch(() => ({ data: null }))
      ])
      setCourse(courseRes.data)
      setChapters(chaptersRes.data)
      setExam(examRes.data)
    } catch (err) {
      setError('加载课程详情失败')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleEnroll = async () => {
    if (!currentEmployee) {
      setError('请先选择员工')
      return
    }

    try {
      setEnrolling(true)
      const response = await api.post(
        `/enrollments/enroll?employeeId=${currentEmployee.id}&courseId=${courseId}`
      )
      setEnrollResult({ success: true, message: '报名成功！即将跳转到我的报名页面...' })
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

  if (loading) {
    return <div className="loading">加载中...</div>
  }

  if (!course) {
    return (
      <div className="empty-state">
        <div className="icon">❓</div>
        <h3>课程不存在</h3>
      </div>
    )
  }

  return (
    <div>
      <button className="btn btn-secondary mb-4" onClick={() => navigate(-1)}>
        ← 返回
      </button>

      {enrollResult && (
        <div className={enrollResult.success ? 'success' : 'error'}>
          {enrollResult.message}
        </div>
      )}

      {error && <div className="error">{error}</div>}

      <div className="card">
        <div className="card-header">
          <div>
            <h2 className="card-title" style={{ fontSize: '1.5rem' }}>{course.name}</h2>
            <p className="card-subtitle">课程编号：{course.code}</p>
          </div>
          {course.currentParticipants < course.maxParticipants ? (
            <span className="badge badge-success">可报名</span>
          ) : (
            <span className="badge badge-danger">名额已满</span>
          )}
        </div>
        <div className="card-body">
          <p style={{ marginBottom: '1.5rem', lineHeight: '1.8' }}>{course.description}</p>
          
          <div className="stat-grid">
            <div className="stat-card">
              <div className="label">课程费用</div>
              <div className="value" style={{ color: '#667eea' }}>¥{course.courseFee.toFixed(2)}</div>
            </div>
            <div className="stat-card">
              <div className="label">剩余名额</div>
              <div className="value" style={{ color: '#10b981' }}>
                {course.maxParticipants - course.currentParticipants} / {course.maxParticipants}
              </div>
            </div>
            <div className="stat-card">
              <div className="label">章节数量</div>
              <div className="value" style={{ color: '#4b5563' }}>{course.totalChapters} 章</div>
            </div>
            <div className="stat-card">
              <div className="label">考试配置</div>
              <div className="value" style={{ color: '#f59e0b' }}>
                {exam ? '已配置' : '未配置'}
              </div>
            </div>
          </div>
        </div>
        <div className="card-footer">
          <button
            className="btn btn-primary"
            onClick={handleEnroll}
            disabled={enrolling || course.currentParticipants >= course.maxParticipants}
            style={{ padding: '1rem 2rem', fontSize: '1rem' }}
          >
            {enrolling ? '报名中...' : '立即报名'}
          </button>
        </div>
      </div>

      <div className="card" style={{ marginTop: '1.5rem' }}>
        <h3 className="card-title" style={{ marginBottom: '1rem' }}>课程章节</h3>
        <div className="chapter-list">
          {chapters.map((chapter, index) => (
            <div key={chapter.id} className="chapter-item">
              <div className="chapter-number">{index + 1}</div>
              <div className="chapter-info">
                <div className="chapter-title">
                  {chapter.title}
                  {chapter.isMandatory && <span className="badge badge-danger" style={{ marginLeft: '0.5rem' }}>必修</span>}
                </div>
                <div className="chapter-meta">
                  最低学习时长：{chapter.minStudyDurationMinutes} 分钟
                  {chapter.prerequisiteChapterIds?.length > 0 && (
                    <span style={{ marginLeft: '1rem' }}>
                      前置章节：第 {chapter.prerequisiteChapterIds.join('、')} 章
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {exam && (
        <div className="card" style={{ marginTop: '1.5rem' }}>
          <h3 className="card-title" style={{ marginBottom: '1rem' }}>考试配置</h3>
          <div className="stat-grid">
            <div className="stat-card">
              <div className="label">考试名称</div>
              <div className="value" style={{ fontSize: '1.1rem' }}>{exam.name}</div>
            </div>
            <div className="stat-card">
              <div className="label">题目数量</div>
              <div className="value">{exam.totalQuestions} 题</div>
            </div>
            <div className="stat-card">
              <div className="label">及格分数</div>
              <div className="value" style={{ color: '#10b981' }}>{exam.passingScore} 分</div>
            </div>
            <div className="stat-card">
              <div className="label">考试时长</div>
              <div className="value">{exam.durationMinutes} 分钟</div>
            </div>
            <div className="stat-card">
              <div className="label">补考次数</div>
              <div className="value" style={{ color: '#ef4444' }}>最多 {exam.maxRetakeAttempts} 次</div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default CourseDetail
