
export const decodeJWT = (token) => {
    try {
        const base64Url = token.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join(''))

        return JSON.parse(jsonPayload)
    } catch (error) {
        return null
    }
}

export const getRolesFromToken = () => {
    const token = localStorage.getItem('token')
    if (!token) return []

    const decoded = decodeJWT(token)
    return decoded?.roles || []
}

export const hasRequiredRole = (requiredRoles) => {
    const userRoles = getRolesFromToken();
    return requiredRoles.some(role => userRoles.includes(role))
}

export const formatRoleName = (role) => {
    const roleNames = {
        'KITCHEN': 'Keuken',
        'HALL': 'Zaal',
        'CASHIER': 'Kassa',
        'ADMIN': 'Admin'
    }
    return roleNames[role] || role
}


export const getAllRoles = () => {
    return ['ADMIN', 'KITCHEN', 'HALL', 'CASHIER'] // !!Should match backend roles!!
}