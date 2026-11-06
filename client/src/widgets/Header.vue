<template>
    <header class="app-header">
        <nav>
            <router-link
                v-if="userTenantId && userTenantName"
                :to="{ name: 'tenant-detail', params: { id: userTenantId } }"
                class="nav-link"
            >
                <Icon name="Building2" :size="18" />
                {{ userTenantName }}
            </router-link>

            <span v-else-if="tenantsLoading" class="loading">
                <Icon name="Loader2" :size="18" class="spinner" />
            </span>

            <router-link v-if="isUser" to="/users" class="nav-link"
                ><Icon name="Users" :size="18" /><span
                    >Пользователи</span
                ></router-link
            >
            <router-link v-if="isAdmin" to="/tenants" class="nav-link"
                ><Icon name="Briefcase" :size="18" />
                <span>Отделы</span></router-link
            >
            <router-link v-if="isAdmin" to="/admin" class="nav-link">
                <Icon name="Shield" :size="18" />
                <span>Админ-панель</span></router-link
            >
            <div class="nav-action">
                <BaseButton variant="heading" @click="logout">
                    <Icon name="LogOut" :size="24" />
                    <span>Выйти</span>
                </BaseButton>
                <BaseButton variant="heading" @click="openMyProfile">
                    <Icon name="UserCircle" :size="24" />
                    <span>Мой профиль</span>
                </BaseButton>
            </div>
        </nav>
        <Modal v-model="showProfileModal" title="Мой профиль">
            <Profile :userId="currentUserId" />
        </Modal>
    </header>
</template>

<script setup>
import { useRoute, useRouter } from "vue-router";
import { computed, ref, onMounted, watch } from "vue";
import BaseButton from "@/shared/ui/Button/BaseButton.vue";
import Modal from "@/shared/ui/Modal/Modal.vue";
import Icon from "@/shared/ui/Icon/Icon.vue";
import Profile from "@/features/profile/Profile.vue";
import { getCurrentUserId, getCurrentUserTenantId } from "@/shared/utils/token";
import { getOneTenant } from "@/features/admin/api";
import { useAuth } from "@/shared/composables/useAuth";

const router = useRouter();
const route = useRoute();

const { token, userRole, clearAuth } = useAuth();

const showProfileModal = ref(false);
const currentUserId = ref(null);
const userTenantId = ref(null);
const userTenantName = ref("");
const tenantsLoading = ref(false);

const isAdmin = computed(() => userRole.value === "ADMIN");
const isUser = computed(() => userRole.value === "USER");

const logout = () => {
    clearAuth();
    router.push("/login");
    currentUserId.value = null;
    userTenantId.value = null;
    userTenantName.value = "";
};

const openMyProfile = () => {
    if (currentUserId.value) {
        showProfileModal.value = true;
    }
};

const resetTenantData = () => {
    userTenantId.value = null;
    userTenantName.value = "";
    tenantsLoading.value = false;
};

const loadUserTenant = async (force = false) => {
    if (!token.value) {
        resetTenantData();
        return;
    }

    const tenantId = getCurrentUserTenantId();
    if (!tenantId) {
        resetTenantData();
        return;
    }

    if (!force && userTenantId.value !== null) return userTenantId.value;

    tenantsLoading.value = true;
    userTenantId.value = tenantId;

    try {
        const response = await getOneTenant(tenantId);
        if (response.success && response.data) {
            userTenantName.value = response.data.name;
        }
    } catch (err) {
        console.error("Error loading tenant:", err);
        if (err.response?.status === 401) resetTenantData();
    } finally {
        tenantsLoading.value = false;
    }
};

const loadHeaderData = async () => {
    currentUserId.value = getCurrentUserId();
    await loadUserTenant(true);
};

watch(
    () => [route.path, token.value],
    ([newPath, newToken]) => {
        if (newPath === "/login" || !newToken) {
            resetTenantData();
            currentUserId.value = null;
            return;
        }
        loadHeaderData();
    },
    { immediate: true },
);
</script>

<style scoped>
.app-header {
    background-color: #42b983;
    color: white;
    padding: 0.75rem 1rem;
}
nav {
    display: flex;
    gap: 1.5rem;
    align-items: center;
    max-width: 1200px;
    margin: 0 auto;
}
.nav-link {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    color: white;
    text-decoration: none;
    font-weight: 500;
    padding: 0.5rem 0;
    border-bottom: 2px solid transparent;
    transition: border-color 0.2s;
}
.nav-link:hover {
    border-bottom-color: rgba(255, 255, 255, 0.5);
}
.router-link-active {
    border-bottom-color: white;
}
.nav-action {
    display: flex;
    gap: 1.5rem;
    margin-left: auto;
}
.tenant-link {
    background-color: rgba(255, 255, 255, 0.2);
    padding: 0.5rem 1rem;
    border-radius: 20px;
    border-bottom: none;
}
.tenant-link:hover {
    background-color: rgba(255, 255, 255, 0.3);
    border-bottom: none;
}
.tenant-loading {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.9rem;
    padding: 0.5rem 1rem;
}

.spinner {
    animation: spin 1s linear infinite;
}
@keyframes spin {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}
</style>
