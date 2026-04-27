import React, { useState } from 'react'

function EmployeeList({ employees, departments }) {
  const [selectedDept, setSelectedDept] = useState('all')

  const getDepartmentName = (deptId) => {
    const dept = departments.find(d => d.id === deptId)
    return dept ? dept.name : '未知部门'
  }

  const getStatusBadge = (status) => {
    const statusMap = {
      'ACTIVE': { text: '在职', class: 'badge-success' },
      'INACTIVE': { text: '离职', class: 'badge-danger' },
      'ON_LEAVE': { text: '休假', class: 'badge-warning' },
      'TERMINATED': { text: '已终止', class: 'badge-default' }
    }
    const info = statusMap[status] || { text: status, class: 'badge-default' }
    return <span className={`badge ${info.class}`}>{info.text}</span>
  }

  const filteredEmployees = selectedDept === 'all'
    ? employees
    : employees.filter(e => e.departmentId === Number(selectedDept))

  return (
    <div>
      <h2 className="page-title">员工管理</h2>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="flex items-center gap-3">
          <label className="form-label" style={{ marginBottom: 0 }}>部门筛选：</label>
          <select
            className="form-select"
            style={{ width: 'auto', minWidth: '200px' }}
            value={selectedDept}
            onChange={(e) => setSelectedDept(e.target.value)}
          >
            <option value="all">全部部门</option>
            {departments.map(dept => (
              <option key={dept.id} value={dept.id}>{dept.name}</option>
            ))}
          </select>
        </div>
      </div>

      {employees.length === 0 ? (
        <div className="empty-state">
          <div className="icon">👥</div>
          <h3>暂无员工数据</h3>
        </div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>工号</th>
              <th>姓名</th>
              <th>部门</th>
              <th>职位</th>
              <th>邮箱</th>
              <th>电话</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            {filteredEmployees.map(emp => (
              <tr key={emp.id}>
                <td><strong>{emp.employeeNo}</strong></td>
                <td>{emp.name}</td>
                <td>{getDepartmentName(emp.departmentId)}</td>
                <td>{emp.position}</td>
                <td>{emp.email}</td>
                <td>{emp.phone}</td>
                <td>{getStatusBadge(emp.status)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <div className="stat-grid" style={{ marginTop: '2rem' }}>
        <div className="stat-card">
          <div className="label">总员工数</div>
          <div className="value">{employees.length}</div>
        </div>
        <div className="stat-card">
          <div className="label">在职员工</div>
          <div className="value" style={{ color: '#10b981' }}>
            {employees.filter(e => e.status === 'ACTIVE').length}
          </div>
        </div>
        <div className="stat-card">
          <div className="label">部门数量</div>
          <div className="value" style={{ color: '#667eea' }}>{departments.length}</div>
        </div>
      </div>
    </div>
  )
}

export default EmployeeList
