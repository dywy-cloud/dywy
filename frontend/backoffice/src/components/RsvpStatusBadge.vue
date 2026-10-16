<template>
  <span
    :class="badge.classes"
    class="inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs font-medium"
    data-test="rsvp-status-badge"
    :data-status="summary.status"
  >
    <span aria-hidden="true" data-test="rsvp-status-icon">{{ badge.icon }}</span>
    <span data-test="rsvp-status-label">{{ badge.label }}</span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { RsvpSummary } from '../services/invitationApi';

defineOptions({
  name: 'RsvpStatusBadge'
});

const props = defineProps<{
  summary: RsvpSummary;
}>();

const badge = computed(() => {
  const { status, attending, declined, total } = props.summary;

  switch (status) {
    case 'ACCEPTED_ALL':
      return {
        icon: '✓',
        label: `Accepted ${attending}/${total}`,
        classes: 'border-green-300 bg-green-50 text-green-800'
      };
    case 'PARTIALLY_ACCEPTED':
      return {
        icon: '◐',
        label: `Partially accepted ${attending}/${total}`,
        classes: 'border-amber-300 bg-amber-50 text-amber-800'
      };
    case 'DECLINED_ALL':
      return {
        icon: '✕',
        label: 'Declined',
        classes: 'border-red-300 bg-red-50 text-red-800'
      };
    case 'PARTIALLY_ANSWERED':
      return {
        icon: '…',
        label: `Replied ${attending + declined}/${total}`,
        classes: 'border-gray-300 bg-gray-100 text-gray-700'
      };
    default:
      return {
        icon: '…',
        label: 'Awaiting reply',
        classes: 'border-gray-300 bg-gray-100 text-gray-700'
      };
  }
});
</script>

