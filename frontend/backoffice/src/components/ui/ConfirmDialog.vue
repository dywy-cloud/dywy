<template>
  <teleport to="body" v-if="dialogState">
    <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="handleCancel">
      <div class="rounded-lg border border-secondary/40 bg-white p-6 shadow-2xl max-w-md w-full mx-4 text-text">
        <h3 class="text-lg font-medium text-text">{{ dialogState.title }}</h3>
        <p class="mt-3 text-sm text-text/80">{{ dialogState.message }}</p>

        <div class="mt-6 flex justify-center gap-3">
          <BaseButton type="button" variant="solid" @click="handleCancel">
            {{ dialogState.cancelLabel }}
          </BaseButton>
          <BaseButton
            type="button"
            :variant="dialogState.isDangerous ? 'danger' : 'gradient'"
            @click="handleConfirm"
          >
            {{ dialogState.confirmLabel }}
          </BaseButton>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import BaseButton from './BaseButton.vue';
import { useConfirmDialog } from '../../composables/useConfirmDialog';

const { dialogState, confirm, cancel } = useConfirmDialog();

const handleConfirm = () => {
  confirm();
};

const handleCancel = () => {
  cancel();
};
</script>


