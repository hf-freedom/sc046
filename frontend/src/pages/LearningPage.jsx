import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '../api'

function LearningPage({ currentEmployee }) {
  const { enrollmentId } = useParams()
  const [enrollment, setEnrollment] = useState(null)
  const [course, setCourse] = useState(null)
  const [chapters, setChapters] = useState([])
  const [progress, setProgress] = useState([])
  const [selectedChapter, setSelectedChapter] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [studyTime, setStudyTime] = useState(0)
  const [timerActive, setTimerActive] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    loadLearningData()
    return () => {
      if (timerActive) {
        saveStudyTime()
      }
    }
  }, [enrollmentId])

  useEffect(() => {
    let timer
    if (timerActive && selectedChapter) {
      timer = setInterval(() => {
        setStudyTime(t => t + 1)
      }, 60000)
    }
    return () => clearInterval(timer)
  }, [timerActive, selectedChapter])

  const loadLearningData = async () => {
    try {
      setLoading(true)
      const [enrollmentRes, progressRes] = await Promise.all([
        api.get(`/enrollments/${enrollmentId}`),
        api.get(`/learning/enrollment/${enrollmentId}`)
      ])
      setEnrollment(enrollmentRes.data)
      setProgress(progressRes.data)

      const courseRes = await api.get(`/courses/${enrollmentRes.data.courseId}`)
      setCourse(courseRes.data)

      const chaptersRes = await api.get(`/courses/${enrollmentRes.data.courseId}/chapters`)
      setChapters(chaptersRes.data)

      setError(null)
    } catch (err) {
      setError('加载学习数据失败')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const isChapterCompleted = (chapterId) => {
    return progress.some(p => p.chapterId === chapterId && p.isCompleted)
  }

  const getChapterProgress = (chapterId) => {
    return progress.find(p => p.chapterId === chapterId)
  }

  const canStartChapter = (chapter) => {
    if (isChapterCompleted(chapter.id)) return false
    if (!chapter.prerequisiteChapterIds || chapter.prerequisiteChapterIds.length === 0) return true
    return chapter.prerequisiteChapterIds.every(prereqId => isChapterCompleted(prereqId))
  }

  const startChapter = async (chapter) => {
    if (!canStartChapter(chapter)) {
      alert('请先完成前置章节')
      return
    }

    try {
      await api.post(`/learning/start?enrollmentId=${enrollmentId}&chapterId=${chapter.id}`)
      setSelectedChapter(chapter)
      setStudyTime(0)
      setTimerActive(true)
      loadLearningData()
    } catch (err) {
      alert(err.response?.data || '开始学习失败')
    }
  }

  const saveStudyTime = async () => {
    if (!selectedChapter || studyTime === 0) return

    try {
      await api.post(
        `/learning/progress?enrollmentId=${enrollmentId}&chapterId=${selectedChapter.id}&additionalMinutes=${studyTime}`
      )
      setStudyTime(0)
      loadLearningData()
      alert('学习进度已保存！')
    } catch (err) {
      console.error('保存学习进度失败', err)
      alert('保存失败：' + (err.response?.data || err.message))
    }
  }

  const addStudyTime = (minutes) => {
    setStudyTime(t => t + minutes)
  }

  const handleManualTimeChange = (e) => {
    const value = parseInt(e.target.value) || 0
    setStudyTime(value)
  }

  const completeChapter = async () => {
    if (!selectedChapter) return

    const currentProgress = getChapterProgress(selectedChapter.id)
    const currentMinutes = currentProgress?.studyDurationMinutes || 0
    const totalMinutes = currentMinutes + studyTime

    if (totalMinutes < selectedChapter.minStudyDurationMinutes) {
      alert(`学习时长不足！当前已学习 ${totalMinutes} 分钟，最低要求 ${selectedChapter.minStudyDurationMinutes} 分钟。请先保存学习进度。`)
      return
    }

    try {
      if (studyTime > 0) {
        await api.post(
          `/learning/progress?enrollmentId=${enrollmentId}&chapterId=${selectedChapter.id}&additionalMinutes=${studyTime}`
        )
      }
      await api.post(`/learning/complete?enrollmentId=${enrollmentId}&chapterId=${selectedChapter.id}`)
      setTimerActive(false)
      setSelectedChapter(null)
      setStudyTime(0)
      loadLearningData()
      alert('章节完成！')
    } catch (err) {
      alert(err.response?.data || '完成章节失败')
    }
  }

  const getCompletionPercentage = () => {
    if (chapters.length === 0) return 0
    const completed = chapters.filter(c => isChapterCompleted(c.id)).length
    return Math.round((completed / chapters.length) * 100)
  }

  if (loading) {
    return <div className="loading">加载中...</div>
  }

  return (
    <div>
      <button className="btn btn-secondary mb-4" onClick={() => navigate('/my-enrollments')}>
        ← 返回我的报名
      </button>

      {error && <div className="error">{error}</div>}

      {!enrollment ? (
        <div className="empty-state">
          <div className="icon">❓</div>
          <h3>报名记录不存在</h3>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: '1.5rem' }}>
          <div>
            <div className="card">
              <h3 className="card-title" style={{ marginBottom: '1rem' }}>
                {course?.name || '课程'}
              </h3>
              <div style={{ marginBottom: '1rem' }}>
                <div className="flex flex-between" style={{ marginBottom: '0.5rem' }}>
                  <span style={{ color: '#6b7280' }}>学习进度</span>
                  <span style={{ fontWeight: '600', color: '#667eea' }}>{getCompletionPercentage()}%</span>
                </div>
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: `${getCompletionPercentage()}%` }}></div>
                </div>
              </div>
              <p className="card-subtitle">共 {chapters.length} 个章节</p>
            </div>

            <div className="card" style={{ marginTop: '1rem' }}>
              <h4 style={{ marginBottom: '1rem' }}>章节列表</h4>
              <div className="chapter-list">
                {chapters.map((chapter, index) => {
                  const isCompleted = isChapterCompleted(chapter.id)
                  const canStart = canStartChapter(chapter)
                  const chapterProgress = getChapterProgress(chapter.id)
                  const isSelected = selectedChapter?.id === chapter.id

                  return (
                    <div
                      key={chapter.id}
                      className={`chapter-item ${isCompleted ? 'completed' : !canStart ? 'locked' : ''} ${isSelected ? 'selected' : ''}`}
                      style={{
                        cursor: canStart ? 'pointer' : 'not-allowed',
                        border: isSelected ? '2px solid #667eea' : 'none'
                      }}
                      onClick={() => canStart && startChapter(chapter)}
                    >
                      <div className={`chapter-number ${isCompleted ? 'completed' : !canStart ? 'locked' : ''}`}>
                        {isCompleted ? '✓' : index + 1}
                      </div>
                      <div className="chapter-info">
                        <div className="chapter-title">
                          {chapter.title}
                          {chapter.isMandatory && (
                            <span className="badge badge-danger" style={{ marginLeft: '0.5rem', fontSize: '0.65rem' }}>
                              必修
                            </span>
                          )}
                        </div>
                        <div className="chapter-meta">
                          {chapterProgress ? (
                            <>已学习 {chapterProgress.studyDurationMinutes} 分钟</>
                          ) : (
                            <>最低 {chapter.minStudyDurationMinutes} 分钟</>
                          )}
                          {!canStart && (
                            <span style={{ color: '#ef4444', marginLeft: '0.5rem' }}>
                              🔒 需完成前置章节
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          </div>

          <div>
            {selectedChapter ? (
              <div className="card">
                <div className="card-header">
                  <h3 className="card-title">{selectedChapter.title}</h3>
                </div>
                <div className="card-body" style={{ minHeight: '300px', padding: '2rem' }}>
                  <div style={{ marginBottom: '2rem', padding: '1rem', background: '#f8fafc', borderRadius: '0.5rem' }}>
                    <h4 style={{ marginBottom: '1rem', color: '#1f2937' }}>学习进度</h4>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                      <div style={{ textAlign: 'center', padding: '1rem', background: 'white', borderRadius: '0.5rem' }}>
                        <div style={{ fontSize: '0.85rem', color: '#6b7280', marginBottom: '0.5rem' }}>已学习时长</div>
                        <div style={{ fontSize: '1.5rem', fontWeight: '700', color: '#667eea' }}>
                          {(getChapterProgress(selectedChapter.id)?.studyDurationMinutes || 0) + studyTime} 分钟
                        </div>
                      </div>
                      <div style={{ textAlign: 'center', padding: '1rem', background: 'white', borderRadius: '0.5rem' }}>
                        <div style={{ fontSize: '0.85rem', color: '#6b7280', marginBottom: '0.5rem' }}>最低要求</div>
                        <div style={{ fontSize: '1.5rem', fontWeight: '700', color: '#ef4444' }}>
                          {selectedChapter.minStudyDurationMinutes} 分钟
                        </div>
                      </div>
                    </div>
                  </div>

                  <div style={{ marginBottom: '2rem', padding: '1rem', background: '#f8fafc', borderRadius: '0.5rem' }}>
                    <h4 style={{ marginBottom: '1rem', color: '#1f2937' }}>设置学习时长</h4>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                      <span style={{ color: '#6b7280' }}>待保存时长：</span>
                      <input
                        type="number"
                        value={studyTime}
                        onChange={handleManualTimeChange}
                        style={{
                          width: '100px',
                          padding: '0.5rem',
                          fontSize: '1rem',
                          border: '2px solid #e5e7eb',
                          borderRadius: '0.25rem'
                        }}
                        min="0"
                      />
                      <span style={{ color: '#6b7280' }}>分钟</span>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                      <button
                        onClick={() => addStudyTime(10)}
                        style={{
                          padding: '0.5rem 1rem',
                          background: '#e0e7ff',
                          border: 'none',
                          borderRadius: '0.25rem',
                          cursor: 'pointer',
                          color: '#4338ca'
                        }}
                      >
                        +10分钟
                      </button>
                      <button
                        onClick={() => addStudyTime(30)}
                        style={{
                          padding: '0.5rem 1rem',
                          background: '#e0e7ff',
                          border: 'none',
                          borderRadius: '0.25rem',
                          cursor: 'pointer',
                          color: '#4338ca'
                        }}
                      >
                        +30分钟
                      </button>
                      <button
                        onClick={() => addStudyTime(60)}
                        style={{
                          padding: '0.5rem 1rem',
                          background: '#e0e7ff',
                          border: 'none',
                          borderRadius: '0.25rem',
                          cursor: 'pointer',
                          color: '#4338ca'
                        }}
                      >
                        +60分钟
                      </button>
                      <button
                        onClick={() => addStudyTime(selectedChapter.minStudyDurationMinutes)}
                        style={{
                          padding: '0.5rem 1rem',
                          background: '#dcfce7',
                          border: 'none',
                          borderRadius: '0.25rem',
                          cursor: 'pointer',
                          color: '#166534'
                        }}
                      >
                        +最低要求({selectedChapter.minStudyDurationMinutes}分钟)
                      </button>
                    </div>
                  </div>

                  <h4 style={{ marginBottom: '1rem' }}>章节内容</h4>
                  <p style={{ lineHeight: '2', color: '#4b5563' }}>
                    {selectedChapter.content || '这是章节的学习内容。在实际应用中，这里会显示完整的章节内容，包括文字、图片、视频等多种形式的学习资料。'}
                  </p>
                </div>
                <div className="card-footer" style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                  <button
                    className="btn btn-info"
                    onClick={saveStudyTime}
                    disabled={studyTime === 0}
                  >
                    保存进度 ({studyTime}分钟)
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={() => {
                      setTimerActive(false)
                      setSelectedChapter(null)
                      setStudyTime(0)
                    }}
                  >
                    返回
                  </button>
                  <button
                    className="btn btn-success"
                    onClick={completeChapter}
                  >
                    完成章节
                  </button>
                </div>
              </div>
            ) : (
              <div className="card" style={{ minHeight: '400px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <div className="empty-state">
                  <div className="icon">📖</div>
                  <h3>选择章节开始学习</h3>
                  <p>请在左侧选择一个章节开始学习</p>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

export default LearningPage
