<template>
  <BaseDialog title="Print stickers" @cancel="emit('cancel')">
    <p class="mt-2 text-sm font-medium" data-test="print-stickers-scope">{{ scopeLabel }}</p>
    <p class="mt-2 text-sm text-text/80">Choose the color of the QR codes and labels.</p>

    <div class="mt-4 flex flex-wrap items-center gap-2" data-test="color-swatches">
      <button
        v-for="swatch in COLOR_SWATCHES"
        :key="swatch"
        :aria-label="`Use color ${swatch}`"
        :aria-pressed="color === swatch"
        :class="color === swatch ? 'ring-2 ring-offset-2 ring-text' : ''"
        :data-test="`color-swatch-${swatch}`"
        :style="{ backgroundColor: swatch }"
        class="h-8 w-8 rounded-full border border-secondary/40"
        type="button"
        @click="color = swatch"
      />
      <label class="ml-2 flex items-center gap-2 text-xs">
        Custom
        <input v-model="color" class="h-8 w-10 cursor-pointer" data-test="custom-color-input" type="color" />
      </label>
    </div>

    <div class="mt-4 flex flex-col items-center gap-2 rounded border border-secondary/20 bg-white p-4" data-test="sticker-preview">
      <QrcodeCanvas
        :value="PREVIEW_QR_VALUE"
        :size="PREVIEW_QR_SIZE"
        :margin="1"
        :foreground="color"
        level="M"
        data-test="sticker-preview-qr"
      />
      <span :style="{ color }" class="text-sm" data-test="sticker-preview-label">Family name</span>
    </div>

    <p v-if="!scannable" class="mt-3 text-sm text-red-700" data-test="low-contrast-warning" role="alert">
      This color is too light against white and the QR code may not scan. Choose a darker color.
    </p>

    <template #actions>
      <BaseButton data-test="print-stickers-cancel" type="button" variant="solid" @click="emit('cancel')">Cancel</BaseButton>
      <BaseButton :disabled="!scannable || count === 0" data-test="print-stickers-confirm" type="button" @click="emit('print', color)">
        {{ confirmLabel }}
      </BaseButton>
    </template>
  </BaseDialog>
</template>

<script setup lang="ts">
import { QrcodeCanvas } from 'qrcode.vue';
import { computed, ref } from 'vue';
import { DEFAULT_QR_COLOR } from '../composables/useQrPngDataUrl';
import { isScannableOnWhite } from '../services/colorContrast';
import BaseButton from './ui/BaseButton.vue';
import BaseDialog from './ui/BaseDialog.vue';

const COLOR_SWATCHES = [DEFAULT_QR_COLOR, '#000000', '#1e3a8a', '#166534', '#7f1d1d', '#581c87'];
const PREVIEW_QR_VALUE = 'https://dywy.cloud';
const PREVIEW_QR_SIZE = 120;

const props = defineProps<{
  count: number;
  isSelection: boolean;
}>();

const emit = defineEmits<{
  print: [color: string];
  cancel: [];
}>();

const color = ref(DEFAULT_QR_COLOR);
const scannable = computed(() => isScannableOnWhite(color.value));
const invitationNoun = computed(() => props.count === 1 ? 'invitation' : 'invitations');
const scopeLabel = computed(() => props.isSelection
  ? `${props.count} selected ${invitationNoun.value} will be printed.`
  : `All ${props.count} ${invitationNoun.value} will be printed.`);
const confirmLabel = computed(() => `Print ${props.count} ${props.count === 1 ? 'sticker' : 'stickers'}`);
</script>

