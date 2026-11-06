import { ref } from "vue";
import { getAllTenants, getOneTenant } from "@/features/admin/api";

const tenants = ref(null);
const tenant = ref(null);
let loadingPromise = null;

export function useTenants() {
    const loadTenants = async (force = false) => {
        if (!force && tenants.value !== null) return tenants.value;
        if (loadingPromise) return loadingPromise;

        loadingPromise = (async () => {
            try {
                const response = await getAllTenants();
                if (response.success) {
                    tenants.value = response.data;
                    return tenants.value;
                }
                throw new Error(response.error || "Failed to load tenants");
            } catch (err) {
                console.error("useTenants error:", err);
                throw err;
            } finally {
                loadingPromise = null;
            }
        })();
        return loadingPromise;
    };

    const invalidate = () => {
        tenants.value = null;
        tenant.value = null;
    };

    const getTenantName = (tenantId) => {
        if (!tenantId || !tenants.value) return "—";
        const found = tenants.value.find((t) => t.id === tenantId);
        return found ? found.name : "—";
    };

    return {
        tenants,
        tenant,
        loadTenants,
        invalidate,
        getTenantName,
    };
}
