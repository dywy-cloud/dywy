<template>
  <article class="rounded-xl border border-secondary/30 bg-white p-4 shadow-sm">
    <h3 class="text-xs font-semibold uppercase tracking-wide text-primary">{{ title }}</h3>
    <p :data-test="totalTest" class="mb-2 text-4xl font-bold text-text">{{ total }}</p>
    <ul class="text-sm text-text">
      <li
        v-for="row in rows"
        :key="row.key"
        :data-test="`${rowTestPrefix}-${row.key}`"
        class="flex justify-between border-t border-background py-1"
      >
        <span>{{ row.label }}</span>
        <strong>{{ row.count }}</strong>
      </li>
    </ul>
  </article>
</template>

<script setup lang="ts">
defineOptions({
  name: 'DashboardStatCard'
});

defineProps<{
  title: string;
  total: number;
  totalTest: string;
  rowTestPrefix: string;
  rows: { key: string; label: string; count: number }[];
}>();
</script>
