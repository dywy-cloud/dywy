<template>
  <section class="mt-3">
    <p v-if="loadState === 'loading'" class="text-sm leading-6 text-text/80" role="status">
      {{ t('rsvp.loading') }}
    </p>

    <div v-else-if="loadState === 'error'" class="rounded-xl bg-secondary/25 p-4">
      <p class="text-sm leading-6 text-text/80" role="alert">{{ t('rsvp.loadError') }}</p>
      <button
        class="mt-3 w-full rounded-xl bg-badge-gradient px-4 py-2 text-sm font-semibold text-white transition hover:opacity-90"
        type="button"
        @click="emit('retry')"
      >
        {{ t('common.retry') }}
      </button>
    </div>

    <form v-else class="rsvp-form flex min-h-0 flex-1 flex-col gap-3" @submit.prevent="emit('submit')">
      <div class="flex flex-col gap-3" :inert="submitState === 'submitting'">
        <slot />
      </div>

      <div class="rsvp-footer sticky bottom-0 -mx-6 -mb-6 mt-auto bg-white px-6 pb-3 pt-2">
        <button
          class="rsvp-submit w-full rounded-xl bg-badge-gradient font-semibold text-white transition hover:opacity-90 disabled:opacity-50"
          type="submit"
          :disabled="!canSubmit"
        >
          {{ submitState === 'submitting' ? t('rsvp.submitting') : submitLabel }}
        </button>

        <div v-if="submitState === 'success'" class="mt-2 text-center text-sm text-text" role="status">
          <p class="font-semibold">{{ t('rsvp.saved') }}</p>
          <a
            class="mt-1 inline-block font-semibold underline hover:opacity-80"
            data-test="wedding-website-link"
            :href="WEDDING_WEBSITE_URL"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ t('rsvp.weddingWebsite') }}
          </a>
        </div>
        <p v-else-if="submitState === 'error'" class="mt-2 text-sm text-red-600" role="alert">
          {{ t('rsvp.submitError') }}
        </p>
      </div>
    </form>
  </section>
</template>

<script setup lang="ts">
import type { LoadState, SubmitState } from '../services/rsvpFormState';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

defineProps<{ loadState: LoadState; submitState: SubmitState; canSubmit: boolean; submitLabel: string }>();
const emit = defineEmits<{ retry: []; submit: [] }>();

const { t } = useGuestAccessI18n();

const WEDDING_WEBSITE_URL = 'https://thecla-gregory.dywy.cloud';
</script>

