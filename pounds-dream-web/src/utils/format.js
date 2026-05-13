export function formatDate(date) {
  if (!date) return ''
  if (typeof date === 'string') return date
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function formatDateTime(date) {
  if (!date) return ''
  if (typeof date === 'string') return date
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

export function formatNumber(num, decimals = 1) {
  if (num === null || num === undefined) return '--'
  return Number(num).toFixed(decimals)
}

export function getMealTypeName(type) {
  const names = { 1: '早餐', 2: '午餐', 3: '晚餐', 4: '加餐' }
  return names[type] || '未知'
}

export function getBMICategory(bmi) {
  if (!bmi) return '--'
  const value = Number(bmi)
  if (value < 18.5) return '偏瘦'
  if (value < 24) return '正常'
  if (value < 28) return '偏胖'
  return '肥胖'
}

export function getBMIColor(bmi) {
  if (!bmi) return '#9b8aad'
  const value = Number(bmi)
  if (value < 18.5) return '#ffb347'
  if (value < 24) return '#9b7fe6'
  if (value < 28) return '#ffb347'
  return '#ff6b8a'
}
