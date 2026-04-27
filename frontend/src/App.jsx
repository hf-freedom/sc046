import React, { useState, useEffect } from 'react'
import { Routes, Route, Link, useLocation } from 'react-router-dom'
import CourseList from './pages/CourseList'
import CourseDetail from './pages/CourseDetail'
import MyEnrollments from './pages/MyEnrollments'
import LearningPage from './pages/LearningPage'
import ExamPage from './pages/ExamPage'
import MyCertificates from './pages/MyCertificates'
import DepartmentBudget from './pages/DepartmentBudget'
import EmployeeList from './pages/EmployeeList'
import { api } from './api'
import './App.css'

function App() {
  const [currentEmployee, setCurrentEmployee] = useState(null)
  const [employees, setEmployees] = useState([])
  const [departments, setDepartments] = useState([])
  const location = useLocation()

  useEffect(() => {
    loadEmployees()
    loadDepartments()
  }, [])

  const loadEmployees = async () => {
    try {
      const response = await api.get('/employees')
      setEmployees(response.data)
      if (response.data.length > 0 && !currentEmployee) {
        setCurrentEmployee(response.data[0])
      }
    } catch (error) {
      console.error('加载员工列表失败:', error)
    }
  }

  const loadDepartments = async () => {
    try {
      const response = await api.get('/employees/departments')
      setDepartments(response.data)
    } catch (error) {
      console.error('加载部门列表失败:', error)
    }
  }

  const navItems = [
    { path: '/', label: '课程中心', icon: '📚' },
    { path: '/my-enrollments', label: '我的报名', icon: '📝' },
    { path: '/my-certificates', label: '我的证书', icon: '🎓' },
    { path: '/employees', label: '员工管理', icon: '👥' },
    { path: '/budget', label: '部门预算', icon: '💰' }
  ]

  const isActive = (path) => {
    if (path === '/') return location.pathname === '/'
    return location.pathname.startsWith(path)
  }

  return (
    <div className="app">
      <header className="header">
        <div className="header-content">
          <h1 className="title">企业培训管理系统</h1>
          <div className="employee-selector">
            <span className="label">当前员工：</span>
            <select
              value={currentEmployee?.id || ''}
              onChange={(event) => {
                const emp = employees.find(e => e.id === Number(event.target.value))
                setCurrentEmployee(emp)
              }}
              className="select"
            >
              {employees.map(emp => (
                <option key={emp.id} value={emp.id}>
                  {emp.name} ({emp.employeeNo})
                </option>
              ))}
            </select>
          </div>
        </div>
      </header>

      <nav className="nav">
        {navItems.map(item => (
          <Link
            key={item.path}
            to={item.path}
            className={`nav-item ${isActive(item.path) ? 'active' : ''}`}
          >
            <span className="icon">{item.icon}</span>
            <span>{item.label}</span>
          </Link>
        ))}
      </nav>

      <main className="main">
        <Routes>
          <Route path="/" element={<CourseList currentEmployee={currentEmployee} />} />
          <Route path="/course/:courseId" element={<CourseDetail currentEmployee={currentEmployee} />} />
          <Route path="/my-enrollments" element={<MyEnrollments currentEmployee={currentEmployee} />} />
          <Route path="/learn/:enrollmentId" element={<LearningPage currentEmployee={currentEmployee} />} />
          <Route path="/exam/:enrollmentId" element={<ExamPage currentEmployee={currentEmployee} />} />
          <Route path="/my-certificates" element={<MyCertificates currentEmployee={currentEmployee} />} />
          <Route path="/employees" element={<EmployeeList employees={employees} departments={departments} />} />
          <Route path="/budget" element={<DepartmentBudget />} />
        </Routes>
      </main>

      <footer className="footer">
        <p>企业培训管理系统 © 2026</p>
      </footer>
    </div>
  )
}

export default App
