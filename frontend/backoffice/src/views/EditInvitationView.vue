<template>
  <section class="mx-auto max-w-3xl">
    <header class="mb-6 flex items-center justify-center">
      <h2 class="page-title text-center">Edit invitation</h2>
    </header>

    <p v-if="isLoadingInvitation" class="py-8 text-center text-sm" aria-live="polite">Loading invitation...</p>

    <div v-else-if="loadInvitationErrorMessage" class="space-y-3 py-8 text-center">
      <p class="text-sm text-red-700" data-test="invitation-load-error" role="alert">{{ loadInvitationErrorMessage }}</p>
      <button
        class="rounded-md bg-accent-gradient px-4 py-2 text-white shadow transition hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        type="button"
        @click="loadInvitation"
      >
        Try again
      </button>
    </div>

    <form v-else class="space-y-5" @submit.prevent="handleSubmit">
      <div>
        <label class="form-label" for="invitation-label">Label</label>
        <input
          id="invitation-label"
          v-model="label"
          class="form-input"
          data-test="invitation-label-input"
          required
          type="text"
        >
      </div>

      <div>
        <label class="form-label" for="invitation-description">Description</label>
        <textarea
          id="invitation-description"
          v-model="description"
          class="form-input min-h-24"
          data-test="invitation-description-input"
        />
      </div>

      <div class="rounded-xl border border-secondary/30 bg-white p-4 shadow-sm">
        <div class="mb-3 flex items-center justify-between gap-3">
          <h3 class="text-base font-medium text-text">Guests</h3>
          <input
            id="guest-search"
            v-model="searchQuery"
            aria-label="Search guests"
            class="form-input w-56"
            data-test="guest-search-input"
            placeholder="Search guest"
            type="search"
          >
        </div>

        <p v-if="isLoadingGuests" class="text-sm text-text/70">Loading guests...</p>
        <p v-else-if="guestLoadErrorMessage" class="text-sm text-red-700" data-test="guest-load-error">{{ guestLoadErrorMessage }}</p>

        <div
          v-else
          class="max-h-64 space-y-2 overflow-y-auto"
          data-test="guest-options-container"
          @scroll.passive="handleGuestListScroll"
        >
          <label
            v-for="guest in filteredGuests"
            :key="guest.id"
            class="flex cursor-pointer items-center gap-2 rounded-md border border-transparent px-2 py-1 hover:border-secondary/30"
            data-test="guest-checkbox-option"
          >
            <input
              v-model="selectedGuestIds"
              :value="guest.id"
              data-test="guest-checkbox"
              type="checkbox"
            >
            <span class="text-sm text-text">{{ guest.firstName }} {{ guest.lastName }} - {{ guest.email }}</span>
          </label>

          <div v-if="filteredGuests.length === 0 && !normalizedSearchQuery" class="space-y-2 text-sm text-text/70" data-test="empty-no-guests-available">
            <p>No guests available yet.</p>
            <RouterLink
              :to="{ name: BACKOFFICE_ROUTE_NAMES.guestAdd }"
              aria-label="Create guest"
              class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-accent-gradient text-white shadow-sm transition hover:scale-110 hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
              data-test="create-guest-link"
              title="Create guest"
            >
              <img :src="addGuestIcon" alt="" aria-hidden="true" class="h-4 w-4 brightness-0 invert" />
            </RouterLink>
          </div>
          <p
            v-else-if="filteredGuests.length === 0"
            class="text-sm text-text/70"
            data-test="empty-no-search-match"
          >
            No guests match your search.
          </p>

          <p v-if="isLoadingMoreGuests" class="text-sm text-text/70" data-test="guests-loading-more">
            Loading more guests...
          </p>
        </div>
      </div>

      <p v-if="displayedValidationError" class="text-sm text-red-700" data-test="invitation-validation-error">
        {{ displayedValidationError }}
      </p>
      <p v-if="submitErrorMessage" class="text-sm text-red-700" data-test="invitation-submit-error">
        {{ submitErrorMessage }}
      </p>

      <div class="flex justify-center gap-3 pt-2">
        <BaseButton
          variant="solid"
          data-test="cancel-edit-invitation"
          type="button"
          @click="navigateBack"
        >
          Cancel
        </BaseButton>
        <BaseButton
          :disabled="isSubmitDisabled"
          data-test="update-invitation-submit"
          type="submit"
        >
          {{ isSubmitting ? 'Updating invitation...' : 'Update invitation' }}
        </BaseButton>
      </div>
    </form>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import BaseButton from '../components/ui/BaseButton.vue';
import addGuestIcon from '../assets/icons/add-guest.svg';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { listGuests, type GuestResponse } from '../services/guestApi';
import { getInvitationById, updateInvitation, type InvitationGuestResponse } from '../services/invitationApi';

const router = useRouter();
const route = useRoute();

const invitationId = computed(() => String(route.params.id ?? ''));

const invitationVersion = ref<number>(0);
const initialLabel = ref('');
const initialDescription = ref('');
const initialGuestIds = ref<string[]>([]);
const guests = ref<GuestResponse[]>([]);
const invitationGuests = ref<InvitationGuestResponse[]>([]);
const selectedGuestIds = ref<string[]>([]);
const label = ref('');
const description = ref('');
const searchQuery = ref('');
const isLoadingInvitation = ref(false);
const isLoadingGuests = ref(false);
const isLoadingMoreGuests = ref(false);
const isSubmitting = ref(false);
const loadInvitationErrorMessage = ref('');
const guestLoadErrorMessage = ref('');
const validationErrorMessage = ref('');
const submitErrorMessage = ref('');
const guestPage = ref(0);
const totalGuestPages = ref(1);
let guestSearchDebounceTimer: ReturnType<typeof setTimeout> | undefined;
let latestGuestRequestId = 0;

const GUEST_PAGE_SIZE = 10;
const GUEST_LIST_SCROLL_THRESHOLD_PX = 24;
const GUEST_SEARCH_DEBOUNCE_MS = 300;

const hasMoreGuests = computed(() => guestPage.value + 1 < totalGuestPages.value);
const normalizedSearchQuery = computed(() => searchQuery.value.trim());
const normalizedLabel = computed(() => label.value.trim());
const normalizedDescription = computed(() => description.value.trim());

const normalizeGuestIds = (guestIds: string[]) => Array.from(new Set(guestIds)).sort();

const hasGuestSelectionChanged = computed(() => {
  const currentGuestIds = normalizeGuestIds(selectedGuestIds.value);
  const initialIds = normalizeGuestIds(initialGuestIds.value);

  if (currentGuestIds.length !== initialIds.length) {
    return true;
  }

  return currentGuestIds.some((guestId, index) => guestId !== initialIds[index]);
});

const hasFormChanged = computed(() => {
  return normalizedLabel.value !== initialLabel.value
    || normalizedDescription.value !== initialDescription.value
    || hasGuestSelectionChanged.value;
});

const currentValidationError = computed(() => {
  if (normalizedLabel.value.length === 0) {
    return 'Label is required.';
  }

  if (selectedGuestIds.value.length === 0) {
    return 'Select at least one guest.';
  }

  return '';
});

const isSubmitDisabled = computed(() => {
  return isSubmitting.value
    || !hasFormChanged.value
    || currentValidationError.value.length > 0;
});

const displayedValidationError = computed(() => currentValidationError.value);

const guestMatchesSearch = (guest: InvitationGuestResponse | GuestResponse, query: string) => {
  const normalizedGuest = `${guest.firstName} ${guest.lastName} ${guest.email}`.toLowerCase();
  return normalizedGuest.includes(query.toLowerCase());
};

const mergeDisplayedGuests = (loadedGuests: GuestResponse[], currentInvitationGuests: InvitationGuestResponse[]) => {
  const byId = new Map<string, GuestResponse | InvitationGuestResponse>();

  currentInvitationGuests.forEach((guest) => {
    byId.set(guest.id, guest);
  });

  loadedGuests.forEach((guest) => {
    byId.set(guest.id, guest);
  });

  return Array.from(byId.values());
};


const filteredGuests = computed(() => {
  const mergedGuests = mergeDisplayedGuests(guests.value, invitationGuests.value);

  if (!normalizedSearchQuery.value) {
    return mergedGuests;
  }

  return mergedGuests.filter((guest) => guestMatchesSearch(guest, normalizedSearchQuery.value));
});

const navigateBack = async () => {
  if (router.options.history.state.back != null) {
    router.back();
    return;
  }

  await router.push({ name: BACKOFFICE_ROUTE_NAMES.invitationDetails, params: { id: invitationId.value } });
};

const loadInvitation = async () => {
  if (!invitationId.value) {
    loadInvitationErrorMessage.value = 'Invitation not found.';
    return;
  }

  isLoadingInvitation.value = true;
  loadInvitationErrorMessage.value = '';

  try {
    const invitation = await getInvitationById(invitationId.value);
    invitationVersion.value = invitation.version;
    label.value = invitation.label;
    description.value = invitation.description;
    initialLabel.value = invitation.label.trim();
    initialDescription.value = invitation.description.trim();
    invitationGuests.value = invitation.guests;
    selectedGuestIds.value = invitation.guests.map((guest) => guest.id);
    initialGuestIds.value = invitation.guests.map((guest) => guest.id);
  } catch (error: unknown) {
    loadInvitationErrorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while loading invitation.';
  } finally {
    isLoadingInvitation.value = false;
  }
};

const loadGuests = async ({ reset = false }: { reset?: boolean } = {}) => {
  guestLoadErrorMessage.value = '';

  if (!reset && (isLoadingGuests.value || isLoadingMoreGuests.value)) {
    return;
  }

  if (reset) {
    guests.value = [];
    guestPage.value = 0;
    totalGuestPages.value = 1;
  }

  const pageToLoad = guestPage.value === 0 && guests.value.length === 0
    ? 0
    : guestPage.value + 1;

  const isFirstPage = pageToLoad === 0;

  if (!isFirstPage && !hasMoreGuests.value) {
    return;
  }

  if (isFirstPage) {
    isLoadingGuests.value = true;
    isLoadingMoreGuests.value = false;
  } else {
    isLoadingMoreGuests.value = true;
  }

  const requestId = ++latestGuestRequestId;
  const normalizedSearch = normalizedSearchQuery.value;

  try {
    const response = await listGuests({
      page: pageToLoad,
      size: GUEST_PAGE_SIZE,
      status: 'active',
      availability: 'unassigned',
      search: normalizedSearch || undefined
    });

    if (requestId !== latestGuestRequestId) {
      return;
    }

    if (isFirstPage) {
      guests.value = response.items;
    } else {
      guests.value = [...guests.value, ...response.items];
    }

    guestPage.value = response.page;
    totalGuestPages.value = response.totalPages;
  } catch (error: unknown) {
    if (requestId !== latestGuestRequestId) {
      return;
    }

    guestLoadErrorMessage.value = error instanceof Error
      ? error.message
      : 'Unable to retrieve guests at the moment.';
  } finally {
    if (requestId !== latestGuestRequestId) {
      return;
    }

    if (isFirstPage) {
      isLoadingGuests.value = false;
    } else {
      isLoadingMoreGuests.value = false;
    }
  }
};

const handleGuestListScroll = async (event: Event) => {
  if (isLoadingGuests.value || isLoadingMoreGuests.value || !hasMoreGuests.value) {
    return;
  }

  const target = event.target as HTMLElement | null;

  if (!target) {
    return;
  }

  const isNearBottom = target.scrollTop + target.clientHeight >= target.scrollHeight - GUEST_LIST_SCROLL_THRESHOLD_PX;

  if (isNearBottom) {
    await loadGuests();
  }
};

watch(searchQuery, () => {
  if (guestSearchDebounceTimer) {
    clearTimeout(guestSearchDebounceTimer);
  }

  guestSearchDebounceTimer = setTimeout(() => {
    void loadGuests({ reset: true });
  }, GUEST_SEARCH_DEBOUNCE_MS);
});

onBeforeUnmount(() => {
  if (guestSearchDebounceTimer) {
    clearTimeout(guestSearchDebounceTimer);
  }
});

const handleSubmit = async () => {
  validationErrorMessage.value = '';
  submitErrorMessage.value = '';

  if (currentValidationError.value) {
    validationErrorMessage.value = currentValidationError.value;
    return;
  }

  if (!hasFormChanged.value) {
    return;
  }

  isSubmitting.value = true;

  try {
    await updateInvitation(invitationId.value, {
      version: invitationVersion.value,
      label: normalizedLabel.value,
      description: normalizedDescription.value,
      guestIds: selectedGuestIds.value
    });

    await navigateBack();
  } catch (error: unknown) {
    submitErrorMessage.value = error instanceof Error
      ? error.message
      : 'Unable to update invitation at the moment.';
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  void loadInvitation();
  void loadGuests();
});
</script>


