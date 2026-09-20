<template>
  <component
    :is="tag"
    :type="to ? undefined : type"
    :to="to"
    :disabled="!to && disabled"
    class="flex h-10 w-44 items-center justify-center truncate rounded border border-transparent px-2 text-xs font-medium text-white shadow transition duration-300 disabled:cursor-not-allowed disabled:opacity-50"
    :class="variantClasses"
    v-bind="$attrs"
  >
    <slot />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, type RouteLocationRaw } from 'vue-router';

const props = withDefaults(defineProps<{
  type?: 'button' | 'submit' | 'reset';
  variant?: 'solid' | 'gradient' | 'danger';
  to?: RouteLocationRaw;
  disabled?: boolean;
}>(), {
  type: 'button',
  variant: 'gradient',
  to: undefined,
  disabled: false
});

const tag = computed(() => (props.to ? RouterLink : 'button'));

const variantClasses = computed(() => {
  switch (props.variant) {
    case 'solid':
      return 'bg-badge-gradient hover:opacity-90';
    case 'danger':
      return 'bg-red-600 hover:bg-red-700';
    case 'gradient':
    default:
      return 'bg-accent-gradient hover:opacity-90';
  }
});
</script>
