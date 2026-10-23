<template>
  <GuestRsvpFormLayout
    :load-state="loadState"
    :submit-state="submitState"
    :can-submit="canSubmit"
    :submit-label="t('rsvp.submit')"
    @retry="load"
    @submit="submit"
  >
    <GuestRsvpFields :key="fieldsKey" :model-value="draft" @update:model-value="edit" />
  </GuestRsvpFormLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import GuestRsvpFields from './GuestRsvpFields.vue';
import GuestRsvpFormLayout from './GuestRsvpFormLayout.vue';
import { fetchRsvp, submitRsvp, type GuestRsvpResponse } from '../services/guestAccessSecuredApi';
import { draftFromRsvp, emptyDraft, isDraftChanged, isDraftComplete, toRsvpAnswer, type RsvpDraft } from '../services/rsvpDraft';
import type { LoadState, SubmitState } from '../services/rsvpFormState';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

const { t } = useGuestAccessI18n();

const loadState = ref<LoadState>('loading');
const submitState = ref<SubmitState>('idle');
const saved = ref<RsvpDraft>(emptyDraft());
const draft = ref<RsvpDraft>(emptyDraft());
const fieldsKey = ref(0);

const canSubmit = computed(
  () => isDraftComplete(draft.value) && isDraftChanged(draft.value, saved.value) && submitState.value !== 'submitting',
);

const applySaved = (rsvp: GuestRsvpResponse | null): void => {
  saved.value = draftFromRsvp(rsvp);
  draft.value = { ...saved.value };
  fieldsKey.value++;
};

const load = async (): Promise<void> => {
  loadState.value = 'loading';

  try {
    applySaved(await fetchRsvp());
    loadState.value = 'ready';
  } catch {
    loadState.value = 'error';
  }
};

const edit = (changed: RsvpDraft): void => {
  if (submitState.value === 'submitting') {
    return;
  }

  draft.value = changed;
  submitState.value = 'idle';
};

const submit = async (): Promise<void> => {
  if (!canSubmit.value) {
    return;
  }

  submitState.value = 'submitting';

  try {
    const answer = toRsvpAnswer(draft.value);
    const result =
      answer.attendance === 'ATTENDING'
        ? await submitRsvp('ATTENDING', answer.meal, answer.song)
        : await submitRsvp('DECLINED');
    applySaved(result);
    submitState.value = 'success';
  } catch {
    submitState.value = 'error';
  }
};

onMounted(() => {
  void load();
});
</script>

