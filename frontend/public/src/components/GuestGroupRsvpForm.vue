<template>
  <GuestRsvpFormLayout
    :load-state="loadState"
    :submit-state="submitState"
    :can-submit="canSubmit"
    :submit-label="t('groupRsvp.submit')"
    @retry="load"
    @submit="submit"
  >
    <nav class="flex gap-1 overflow-x-auto border-b border-secondary" :aria-label="t('groupRsvp.guests')">
      <button
        v-for="guest in guests"
        :key="guest.guestId"
        type="button"
        class="guest-tab flex shrink-0 items-center gap-2 whitespace-nowrap rounded-t-xl px-3 py-2 text-sm font-semibold transition"
        :class="guest.guestId === selectedGuestId ? 'bg-primary text-white' : 'text-primary'"
        :aria-current="guest.guestId === selectedGuestId ? 'true' : undefined"
        @click="selectedGuestId = guest.guestId"
      >
        <span>{{ guest.firstName }}</span>
        <span
          class="text-xs"
          :class="
            isDraftComplete(drafts[guest.guestId])
              ? guest.guestId === selectedGuestId
                ? 'text-white'
                : 'text-primary'
              : 'text-accent'
          "
          :aria-label="t(isDraftComplete(drafts[guest.guestId]) ? 'groupRsvp.complete' : 'groupRsvp.incomplete')"
        >{{ isDraftComplete(drafts[guest.guestId]) ? '✓' : '●' }}</span>
      </button>
    </nav>

    <p class="text-right text-xs text-primary" role="status">
      {{ completeCount }} {{ t('groupRsvp.of') }} {{ guests.length }} {{ t('groupRsvp.answered') }}
    </p>

    <div v-if="selectedGuest" class="rounded-2xl border border-secondary p-4">
      <h3 class="mb-3 text-base font-semibold text-text">{{ selectedGuest.firstName }}</h3>
      <GuestRsvpFields
        :key="selectedGuest.guestId"
        :model-value="drafts[selectedGuest.guestId]"
        :name-prefix="`${selectedGuest.guestId}-`"
        @update:model-value="edit(selectedGuest.guestId, $event)"
      />
    </div>
  </GuestRsvpFormLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import GuestRsvpFields from './GuestRsvpFields.vue';
import GuestRsvpFormLayout from './GuestRsvpFormLayout.vue';
import { fetchInvitationRsvps, submitInvitationRsvps, type InvitationGuestRsvp } from '../services/guestAccessSecuredApi';
import { draftFromRsvp, isDraftChanged, isDraftComplete, toRsvpAnswer, type RsvpDraft } from '../services/rsvpDraft';
import type { LoadState, SubmitState } from '../services/rsvpFormState';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

const { t } = useGuestAccessI18n();

const loadState = ref<LoadState>('loading');
const submitState = ref<SubmitState>('idle');
const guests = ref<InvitationGuestRsvp[]>([]);
const drafts = ref<Record<string, RsvpDraft>>({});
const selectedGuestId = ref<string | null>(null);

const selectedGuest = computed(() => guests.value.find((guest) => guest.guestId === selectedGuestId.value) ?? null);
const completeCount = computed(() => guests.value.filter((guest) => isDraftComplete(drafts.value[guest.guestId])).length);
const changed = computed(() =>
  guests.value.some((guest) => isDraftChanged(drafts.value[guest.guestId], draftFromRsvp(guest.rsvp))),
);
const canSubmit = computed(
  () =>
    guests.value.length > 0 &&
    completeCount.value === guests.value.length &&
    changed.value &&
    submitState.value !== 'submitting',
);

const applyGuests = (invitationGuests: InvitationGuestRsvp[]): void => {
  guests.value = invitationGuests;
  drafts.value = Object.fromEntries(invitationGuests.map((guest) => [guest.guestId, draftFromRsvp(guest.rsvp)]));
  selectedGuestId.value = invitationGuests.some((guest) => guest.guestId === selectedGuestId.value)
    ? selectedGuestId.value
    : (invitationGuests[0]?.guestId ?? null);
};

const load = async (): Promise<void> => {
  loadState.value = 'loading';

  try {
    applyGuests(await fetchInvitationRsvps());
    loadState.value = 'ready';
  } catch {
    loadState.value = 'error';
  }
};

const edit = (guestId: string, draft: RsvpDraft): void => {
  if (submitState.value === 'submitting') {
    return;
  }

  drafts.value = { ...drafts.value, [guestId]: draft };
  submitState.value = 'idle';
};

const submit = async (): Promise<void> => {
  if (!canSubmit.value) {
    return;
  }

  submitState.value = 'submitting';

  try {
    applyGuests(
      await submitInvitationRsvps(
        guests.value.map((guest) => ({ guestId: guest.guestId, answer: toRsvpAnswer(drafts.value[guest.guestId]) })),
      ),
    );
    submitState.value = 'success';
  } catch {
    submitState.value = 'error';
  }
};

onMounted(() => {
  void load();
});
</script>

