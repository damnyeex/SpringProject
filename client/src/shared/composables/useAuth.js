import { ref } from "vue";

const tokenRef = ref(localStorage.getItem("token"));
const userRoleRef = ref(localStorage.getItem("userRole"));

export function useAuth() {
    const setAuth = (newToken, newRole) => {
        localStorage.setItem("token", newToken);
        localStorage.setItem("userRole", newRole);
        tokenRef.value = newToken;
        userRoleRef.value = newRole;
    };

    const clearAuth = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("userRole");
        tokenRef.value = null;
        userRoleRef.value = null;
    };

    return {
        token: tokenRef,
        userRole: userRoleRef,
        setAuth,
        clearAuth,
    };
}
