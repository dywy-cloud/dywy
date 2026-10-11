<template>
  <teleport to="body">
    <dialog
      ref="dialogRef"
      :aria-labelledby="titleId"
      class="m-auto w-full max-w-md rounded-lg border border-secondary/40 bg-white p-0 text-text shadow-2xl backdrop:bg-black/40"
      data-test="base-dialog"
      @cancel.prevent="emit('cancel')"
      @click.self="emit('cancel')"
    >
      <div class="p-6">
        <h3 :id="titleId" class="text-lg font-medium text-text" data-test="base-dialog-title">{{ title }}</h3>
        <slot />
        <div class="mt-6 flex justify-center gap-3" data-test="base-dialog-actions">
          <slot name="actions" />
        </div>
      </div>
    </dialog>
  </teleport>
</template>

<script setup lang="ts">
import { onMounted, ref, useId } from 'vue';

defineProps<{
  title: string;
}>();

const emit = defineEmits<{
  cancel: [];
}>();

const titleId = useId();
const dialogRef = ref<HTMLDialogElement>();

onMounted(() => {
  dialogRef.value?.showModal();
});
</script>

