<template>
    <div class="app">
        <Header v-if="isAuthenticated" />
        <main class="app-main">
            <router-view />
        </main>
    </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useAuth } from "@/shared/composables/useAuth";
import Header from "../widgets/Header.vue";

const route = useRoute();
const { token } = useAuth();

const isAuthenticated = computed(
    () => !!token.value && route.path !== "/login",
);
</script>
