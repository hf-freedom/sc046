import React, { useState, useEffect } from 'react'
import { api } from '../api'

function DepartmentBudget() {
  const [departments, setDepartments] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadDepartments()
  }, [])

  const loadDepartments = async () => {
    try {
      setLoading(true)
      const response = await api.get('/employees/departments')
      setDepartments(response.data)
    } catch (error) {
      console.error('加载部门数据失败:', error)
    } finally {
      setLoading(false)
    }
  }
  const formatMoney = (amount) => {
    return new Intl.NumberFormat('zh-CN', {
      style: 'currency',
      currency: 'CNY'
    }).format(amount)
  }

  const getBudgetPercentage = (used, reserved, total) => {
    if (!total || total <= 0) return 0
    const occupied = (used || 0) + (reserved || 0)
    return Math.min(100, Math.round((occupied / total) * 100))
  }

  const getBudgetColor = (percentage) => {
    if (percentage >= 90) return '#ef4444'
    if (percentage >= 70) return '#f59e0b'
    return '#10b981'
  }

  if (loading) {
    return <div className="loading">加载中...</div>
  }

  return (
    <div>
      <h2 className="page-title">部门预算</h2>

      {departments.length === 0 ? (
        <div className="empty-state">
          <div className="icon">💰</div>
          <h3>暂无部门数据</h3>
        </div>
      ) : (
        <div className="grid grid-2">
          {departments.map(dept => {
            const annualBudget = dept.annualBudget || 0
            const usedBudget = dept.usedBudget || 0
            const reservedBudget = dept.reservedBudget || 0
            const availableBudget = annualBudget - usedBudget - reservedBudget
            const percentage = getBudgetPercentage(usedBudget, reservedBudget, annualBudget)

            return (
              <div key={dept.id} className="card">
                <div className="card-header">
                  <div>
                    <h3 className="card-title">{dept.name}</h3>
                    <p className="card-subtitle">部门编号：{dept.code}</p>
                  </div>
                </div>
                <div className="card-body">
                  <div className="stat-grid" style={{ marginBottom: '1rem' }}>
                    <div className="stat-card" style={{ padding: '0.75rem' }}>
                      <div className="label">年度预算</div>
                      <div className="value" style={{ fontSize: '1.25rem' }}>
                        {formatMoney(annualBudget)}
                      </div>
                    </div>
                    <div className="stat-card" style={{ padding: '0.75rem' }}>
                      <div className="label">已使用</div>
                      <div className="value" style={{ fontSize: '1.25rem', color: '#ef4444' }}>
                        {formatMoney(usedBudget)}
                      </div>
                    </div>
                    <div className="stat-card" style={{ padding: '0.75rem' }}>
                      <div className="label">已预留</div>
                      <div className="value" style={{ fontSize: '1.25rem', color: '#f59e0b' }}>
                        {formatMoney(reservedBudget)}
                      </div>
                    </div>
                    <div className="stat-card" style={{ padding: '0.75rem' }}>
                      <div className="label">可用余额</div>
                      <div className="value" style={{ fontSize: '1.25rem', color: '#10b981' }}>
                        {formatMoney(availableBudget)}
                      </div>
                    </div>
                  </div>

                  <div>
                    <div className="flex flex-between" style={{ marginBottom: '0.5rem' }}>
                      <span style={{ color: '#6b7280' }}>预算使用率</span>
                      <span style={{ fontWeight: '600', color: getBudgetColor(percentage) }}>
                        {percentage}%
                      </span>
                    </div>
                    <div className="progress-bar">
                      <div
                        className="progress-fill"
                        style={{
                          width: `${percentage}%`,
                          background: getBudgetColor(percentage)
                        }}
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      )}

      <div className="stat-grid" style={{ marginTop: '2rem' }}>
        <div className="stat-card">
          <div className="label">总年度预算</div>
          <div className="value" style={{ color: '#667eea' }}>
            {formatMoney(departments.reduce((sum, d) => sum + (d.annualBudget || 0), 0))}
          </div>
        </div>
        <div className="stat-card">
          <div className="label">总已使用</div>
          <div className="value" style={{ color: '#ef4444' }}>
            {formatMoney(departments.reduce((sum, d) => sum + (d.usedBudget || 0), 0))}
          </div>
        </div>
        <div className="stat-card">
          <div className="label">总已预留</div>
          <div className="value" style={{ color: '#f59e0b' }}>
            {formatMoney(departments.reduce((sum, d) => sum + (d.reservedBudget || 0), 0))}
          </div>
        </div>
        <div className="stat-card">
          <div className="label">总可用余额</div>
          <div className="value" style={{ color: '#10b981' }}>
            {formatMoney(departments.reduce((sum, d) => {
              const annual = d.annualBudget || 0
              const used = d.usedBudget || 0
              const reserved = d.reservedBudget || 0
              return sum + annual - used - reserved
            }, 0))}
          </div>
        </div>
      </div>
    </div>
  )
}

export default DepartmentBudget
