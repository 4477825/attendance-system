export const getToken = () => {
  return localStorage.getItem('attendance_token')
}

export const setToken = (token) => {
  localStorage.setItem('attendance_token', token)
}

export const removeToken = () => {
  localStorage.removeItem('attendance_token')
}

export const setUser = (user) => {
  localStorage.setItem('attendance_user', JSON.stringify(user))
}

export const getUser = () => {
  const user = localStorage.getItem('attendance_user')
  return user ? JSON.parse(user) : null
}

export const removeUser = () => {
  localStorage.removeItem('attendance_user')
}

export const isLoggedIn = () => {
  return !!localStorage.getItem('attendance_token')
}

export const isAdmin = () => {
  const user = getUser()
  return user && user.role === 'ROLE_ADMIN'
}
