<template>
  <section>
    <header class="mb-6">
      <h2 class="page-title text-center">Dashboard</h2>
    </header>

    <p v-if="isLoading" class="py-8 text-center text-sm" aria-live="polite">Loading dashboard...</p>

    <div v-else-if="errorMessage" class="space-y-3 py-8 text-center">
      <p class="text-sm text-red-700" role="alert">{{ errorMessage }}</p>
      <button
        class="rounded-md bg-accent-gradient px-4 py-2 text-white shadow transition hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        type="button"
        @click="loadStatistics"
      >
        Try again
      </button>
    </div>

    <template v-else-if="statistics">
      <div class="grid gap-4 md:grid-cols-2">
        <DashboardStatCard
          :rows="guestRows"
          :total="statistics.totalGuests"
          data-test="dashboard-guests-card"
          row-test-prefix="dashboard-attendance"
          title="Guests"
          total-test="dashboard-total-guests"
        />
        <DashboardStatCard
          :rows="invitationRows"
          :total="statistics.totalInvitations"
          data-test="dashboard-invitations-card"
          row-test-prefix="dashboard-invitation-status"
          title="Invitations"
          total-test="dashboard-total-invitations"
        />
      </div>

      <article class="mt-4 rounded-xl border border-secondary/30 bg-white p-4 shadow-sm" data-test="dashboard-rsvp-progress">
        <h3 class="mb-2 text-xs font-semibold uppercase tracking-wide text-primary">Guest replies</h3>
        <p v-if="statistics.totalGuests === 0" class="text-sm text-text/80" data-test="dashboard-no-guests">
          No guest yet. Add guests to follow their replies.
        </p>
        <template v-else>
          <div
            :aria-label="progressLabel"
            class="flex h-5 overflow-hidden rounded-full border border-secondary/40 bg-background"
            role="img"
          >
            <div
              v-for="segment in progressSegments"
              :key="segment.key"
              :class="segment.barClass"
              :style="{ width: `${segment.percentage}%` }"
              :data-test="`dashboard-progress-${segment.key}`"
            ></div>
          </div>
          <ul class="mt-2 flex flex-wrap gap-4 text-sm text-text">
            <li v-for="segment in progressSegments" :key="segment.key" :data-test="`dashboard-legend-${segment.key}`">
              <span :class="segment.barClass" aria-hidden="true" class="mr-1.5 inline-block h-2.5 w-2.5 rounded-full"></span>
              {{ segment.label }} {{ segment.count }} ({{ Math.round(segment.percentage) }}%)
            </li>
          </ul>
        </template>
      </article>

      <div class="mt-4 flex flex-wrap gap-3">
        <RouterLink
          :to="{ name: BACKOFFICE_ROUTE_NAMES.invitationList }"
          class="rounded-md bg-accent-gradient px-4 py-2 text-sm text-white shadow transition hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          data-test="dashboard-invitations-link"
        >
          View invitations
        </RouterLink>
        <RouterLink
          :to="{ name: BACKOFFICE_ROUTE_NAMES.guestList }"
          class="rounded-md border border-secondary/40 bg-white px-4 py-2 text-sm text-text transition hover:bg-background/60 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          data-test="dashboard-guests-link"
        >
          View guests
        </RouterLink>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getDashboardStatistics } from '../services/dashboardApi';
import type { DashboardStatisticsResponse, RsvpAttendance } from '../services/dashboardApi';
import type { RsvpStatus } from '../services/invitationApi';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import DashboardStatCard from '../components/DashboardStatCard.vue';

defineOptions({
  name: 'DashboardView'
});

const attendanceRows: { key: RsvpAttendance; label: string; barClass: string }[] = [
  { key: 'ATTENDING', label: 'Attending', barClass: 'bg-text' },
  { key: 'DECLINED', label: 'Declined', barClass: 'bg-accent' },
  { key: 'PENDING', label: 'Pending', barClass: 'bg-secondary' }
];

const invitationStatusRows: { key: RsvpStatus; label: string }[] = [
  { key: 'ACCEPTED_ALL', label: 'Accepted' },
  { key: 'PARTIALLY_ACCEPTED', label: 'Partially accepted' },
  { key: 'DECLINED_ALL', label: 'Declined' },
  { key: 'PARTIALLY_ANSWERED', label: 'Partially answered' },
  { key: 'PENDING', label: 'Awaiting reply' }
];

const statistics = ref<DashboardStatisticsResponse | null>(null);
const isLoading = ref(true);
const errorMessage = ref('');

const guestRows = computed(() => attendanceRows.map(({ key, label }) => ({
  key,
  label,
  count: statistics.value?.guestsByAttendance[key] ?? 0
})));

const invitationRows = computed(() => invitationStatusRows.map(({ key, label }) => ({
  key,
  label,
  count: statistics.value?.invitationsByRsvpStatus[key] ?? 0
})));

const progressSegments = computed(() => {
  const current = statistics.value;

  if (!current || current.totalGuests === 0) {
    return [];
  }

  return attendanceRows.map(({ key, label, barClass }) => ({
    key,
    label,
    barClass,
    count: current.guestsByAttendance[key],
    percentage: (current.guestsByAttendance[key] / current.totalGuests) * 100
  }));
});

const progressLabel = computed(() => {
  const current = statistics.value;
  const counts = progressSegments.value.map(({ count, label }) => `${count} ${label.toLowerCase()}`).join(', ');

  return `${counts} out of ${current?.totalGuests ?? 0} guests`;
});

const loadStatistics = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  try {
    statistics.value = await getDashboardStatistics();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Unable to retrieve dashboard statistics at the moment.';
  } finally {
    isLoading.value = false;
  }
};

onMounted(loadStatistics);
</script>

