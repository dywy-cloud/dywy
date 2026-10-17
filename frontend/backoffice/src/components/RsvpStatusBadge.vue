<template>
  <span
    :class="badge.classes"
    class="group relative inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs font-medium"
    data-test="rsvp-status-badge"
    :data-status="summary.status"
  >
    <span aria-hidden="true" data-test="rsvp-status-icon">{{ badge.icon }}</span>
    <span v-if="!compact" aria-hidden="true" data-test="rsvp-status-counter">{{ badge.counter }}</span>
    <span class="sr-only" data-test="rsvp-status-label">{{ badge.label }}</span>
    <span
      aria-hidden="true"
      class="pointer-events-none absolute right-0 top-full z-10 mt-1 hidden whitespace-nowrap rounded-md bg-text px-2 py-1 text-xs font-normal text-white shadow group-hover:block"
      data-test="rsvp-status-tooltip"
    >
      {{ badge.label }}
    </span>
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
  compact?: boolean;
}>();

const badge = computed(() => {
  const { status, attending, declined, total } = props.summary;

  switch (status) {
    case 'ACCEPTED_ALL':
      return {
        icon: '✓',
        counter: `${attending}/${total}`,
        label: `Accepted ${attending}/${total}`,
        classes: 'border-transparent bg-badge-gradient text-white'
      };
    case 'PARTIALLY_ACCEPTED':
      return {
        icon: '◐',
        counter: `${attending}/${total}`,
        label: `Partially accepted ${attending}/${total}`,
        classes: 'border-primary bg-primary/15 text-text'
      };
    case 'DECLINED_ALL':
      return {
        icon: '✕',
        counter: `${declined}/${total}`,
        label: 'Declined',
        classes: 'border-accent bg-accent/15 text-accent-dark'
      };
    case 'PARTIALLY_ANSWERED':
      return {
        icon: '…',
        counter: `${attending + declined}/${total}`,
        label: `Replied ${attending + declined}/${total}`,
        classes: 'border-secondary bg-background text-text/70'
      };
    default:
      return {
        icon: '…',
        counter: `0/${total}`,
        label: 'Awaiting reply',
        classes: 'border-secondary bg-background text-text/70'
      };
  }
});
</script>
