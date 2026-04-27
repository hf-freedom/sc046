import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '../api'

function ExamPage({ currentEmployee }) {
  const { enrollmentId } = useParams()
  const [exam, setExam] = useState(null)
  const [currentAttempt, setCurrentAttempt] = useState(null)
  const [remainingAttempts, setRemainingAttempts] = useState(0)
  const [answers, setAnswers] = useState({})
  const [timeLeft, setTimeLeft] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [examStarted, setExamStarted] = useState(false)
  const [examResult, setExamResult] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    loadExamData()
    return () => {
      if (examStarted && timeLeft > 0) {
        submitExam()
      }
    }
  }, [enrollmentId])

  useEffect(() => {
    let timer
    if (examStarted && timeLeft > 0) {
      timer = setInterval(() => {
        setTimeLeft(t => {
          if (t <= 1) {
            clearInterval(timer)
            submitExam()
            return 0
          }
          return t - 1
        })
      }, 1000)
    }
    return () => clearInterval(timer)
  }, [examStarted, timeLeft > 0])

  const loadExamData = async () => {
    try {
      setLoading(true)
      const enrollmentRes = await api.get(`/enrollments/${enrollmentId}`)
      const enrollment = enrollmentRes.data

      if (!['COMPLETED', 'IN_PROGRESS'].includes(enrollment.status)) {
        setError('您还没有完成课程学习，无法参加考试')
        return
      }

      const canTakeExamRes = await api.get(`/learning/can-take-exam/${enrollmentId}`)
      if (!canTakeExamRes.data) {
        setError('您还没有完成所有必修章节，无法参加考试')
        return
      }

      const examRes = await api.get(`/courses/${enrollment.courseId}/exam`)
      setExam(examRes.data)

      const attemptsRes = await api.get(`/exams/attempts/enrollment/${enrollmentId}`)
      if (attemptsRes.data.length > 0) {
        const latestAttempt = attemptsRes.data[0]
        if (latestAttempt.status === 'PASSED') {
          setExamResult({
            passed: true,
            score: latestAttempt.score,
            message: `恭喜您已通过考试！得分：${latestAttempt.score} 分`
          })
          setLoading(false)
          return
        }
      }

      const remainingRes = await api.get(`/exams/remaining-attempts/${enrollmentId}`)
      setRemainingAttempts(remainingRes.data)

      if (remainingRes.data <= 0) {
        setError('您的考试次数已用尽')
        return
      }

      setError(null)
    } catch (err) {
      setError('加载考试数据失败')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const startExam = async () => {
    try {
      const response = await api.post(`/exams/start?enrollmentId=${enrollmentId}`)
      setCurrentAttempt(response.data)
      setExamStarted(true)
      setTimeLeft(exam.durationMinutes * 60)
      setAnswers({})
    } catch (err) {
      alert(err.response?.data || '开始考试失败')
    }
  }

  const submitExam = async () => {
    if (!currentAttempt) return

    try {
      const answersList = exam.questions.map((_, index) => answers[index] ?? -1)
      const response = await api.post(`/exams/submit/${currentAttempt.id}`, answersList)
      const result = response.data

      setExamResult({
        passed: result.status === 'PASSED',
        score: result.score,
        message: result.status === 'PASSED'
          ? `恭喜！您通过了考试，得分：${result.score} 分`
          : `很遗憾，您未通过考试，得分：${result.score} 分（及格线：${exam.passingScore} 分）`
      })
      setExamStarted(false)
    } catch (err) {
      console.error('提交考试失败', err)
    }
  }

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }

  const getTimerClass = () => {
    if (timeLeft < 60) return 'danger'
    if (timeLeft < 300) return 'warning'
    return ''
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

      {examResult && (
        <div className="card">
          <div className="text-center" style={{ padding: '2rem' }}>
            <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>
              {examResult.passed ? '🎉' : '😞'}
            </div>
            <h2 style={{ marginBottom: '1rem', color: examResult.passed ? '#10b981' : '#ef4444' }}>
              {examResult.passed ? '考试通过！' : '考试未通过'}
            </h2>
            <p style={{ fontSize: '1.25rem', marginBottom: '1.5rem' }}>
              {examResult.message}
            </p>
            {examResult.passed ? (
              <button
                className="btn btn-primary"
                onClick={() => navigate('/my-certificates')}
              >
                查看证书
              </button>
            ) : remainingAttempts > 1 ? (
              <div>
                <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
                  您还有 {remainingAttempts - 1} 次补考机会
                </p>
                <button
                  className="btn btn-primary"
                  onClick={() => {
                    setExamResult(null)
                    loadExamData()
                  }}
                >
                  重新考试
                </button>
              </div>
            ) : (
              <p style={{ color: '#ef4444' }}>您的考试次数已用尽</p>
            )}
          </div>
        </div>
      )}

      {!examStarted && !examResult && exam && (
        <div className="exam-container">
          <div className="card">
            <div className="card-header">
              <h2 className="card-title" style={{ fontSize: '1.5rem' }}>{exam.name}</h2>
            </div>
            <div className="card-body">
              <div className="stat-grid">
                <div className="stat-card">
                  <div className="label">题目数量</div>
                  <div className="value">{exam.totalQuestions} 题</div>
                </div>
                <div className="stat-card">
                  <div className="label">考试时长</div>
                  <div className="value">{exam.durationMinutes} 分钟</div>
                </div>
                <div className="stat-card">
                  <div className="label">及格分数</div>
                  <div className="value" style={{ color: '#10b981' }}>{exam.passingScore} 分</div>
                </div>
                <div className="stat-card">
                  <div className="label">剩余考试次数</div>
                  <div className="value" style={{ color: '#ef4444' }}>{remainingAttempts} 次</div>
                </div>
              </div>
            </div>
            <div className="card-footer flex-center">
              <button
                className="btn btn-primary"
                onClick={startExam}
                style={{ padding: '1rem 3rem', fontSize: '1.1rem' }}
              >
                开始考试
              </button>
            </div>
          </div>
        </div>
      )}

      {examStarted && exam && (
        <>
          <div className={`timer ${getTimerClass()}`}>
            ⏱ {formatTime(timeLeft)}
          </div>

          <div className="exam-container">
            {exam.questions.map((question, index) => (
              <div key={question.id} className="card question-card">
                <div className="question-number">第 {index + 1} 题（{question.points} 分）</div>
                <div className="question-text">{question.questionText}</div>
                <div className="options-list">
                  {question.options.map((option, optIndex) => (
                    <div
                      key={optIndex}
                      className={`option-item ${answers[index] === optIndex ? 'selected' : ''}`}
                      onClick={() => setAnswers({ ...answers, [index]: optIndex })}
                    >
                      <div className={`option-radio ${answers[index] === optIndex ? 'selected' : ''}`}></div>
                      <span className="option-text">
                        {String.fromCharCode(65 + optIndex)}. {option}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            ))}

            <div className="card">
              <div className="flex flex-between items-center">
                <div>
                  <span style={{ color: '#6b7280' }}>
                    已作答：{Object.keys(answers).filter(k => answers[k] >= 0).length} / {exam.questions.length}
                  </span>
                </div>
                <button
                  className="btn btn-success"
                  onClick={() => {
                    if (confirm('确定要提交考试吗？')) {
                      submitExam()
                    }
                  }}
                  style={{ padding: '1rem 2rem', fontSize: '1rem' }}
                >
                  提交考试
                </button>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default ExamPage
