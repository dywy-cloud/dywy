<template>
  <section>
    <header class="mb-6 grid grid-cols-[1fr_auto_1fr] items-center">
      <span class="h-6 justify-self-start" aria-hidden="true"></span>

      <h2 class="page-title justify-self-center">Invitations</h2>

      <div class="flex items-center gap-3 justify-self-end">
        <button
          :aria-busy="isPrinting"
          :disabled="invitationPage.totalItems === 0 || isPrinting || isSelectionEmpty"
          aria-label="Print stickers"
          class="inline-flex h-10 w-10 items-center justify-center rounded-full bg-accent-gradient text-white shadow transition hover:scale-105 hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:scale-100"
          data-test="print-stickers-cta"
          title="Print stickers"
          type="button"
          @click="isPrintDialogOpen = true"
        >
          <img :src="printIcon" alt="" aria-hidden="true" class="h-6 w-6 brightness-0 invert" :class="{ 'animate-pulse': isPrinting }" />
        </button>

        <WriteOnly>
          <RouterLink
            :to="{ name: BACKOFFICE_ROUTE_NAMES.invitationAdd }"
            aria-label="Create invitation"
            class="inline-flex h-10 w-10 items-center justify-center rounded-full bg-accent-gradient text-white shadow transition hover:scale-105 hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
            data-test="create-invitation-cta"
            title="Create invitation"
          >
            <img :src="addInvitationIcon" alt="" aria-hidden="true" class="h-6 w-6 brightness-0 invert" />
          </RouterLink>
        </WriteOnly>
      </div>
    </header>

    <p v-if="printErrorMessage" class="mb-4 text-center text-sm text-red-700" data-test="print-stickers-error" role="alert">
      {{ printErrorMessage }}
    </p>

    <PrintStickersDialog
      v-if="isPrintDialogOpen"
      :count="hasSelection ? selectedCount : invitationPage.totalItems"
      :is-selection="hasSelection"
      @cancel="isPrintDialogOpen = false"
      @print="printStickers"
    />

    <div v-if="showFilter" class="mb-4">
      <label class="sr-only" for="invitation-label-filter">Filter invitations by label</label>
      <input
        id="invitation-label-filter"
        v-model="filterText"
        class="w-full rounded-md border border-secondary/40 bg-white px-3 py-2 text-sm text-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
        data-test="invitation-label-filter"
        placeholder="Filter by label"
        type="search"
      />
    </div>

    <p v-if="isLoading" class="py-8 text-center text-sm" aria-live="polite">Loading invitations...</p>

    <div v-else-if="errorMessage" class="space-y-3 py-8 text-center">
      <p class="text-sm text-red-700" role="alert">{{ errorMessage }}</p>
      <button
        class="rounded-md bg-accent-gradient px-4 py-2 text-white shadow transition hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        type="button"
        @click="loadData"
      >
        Try again
      </button>
    </div>

    <p
      v-else-if="invitationPage.items.length === 0 && trimmedFilter"
      class="py-8 text-center text-sm text-text/80"
      data-test="empty-no-match"
    >
      No invitations match "{{ trimmedFilter }}".
    </p>

    <div
      v-else-if="invitationPage.items.length === 0 && guestTotalItems === 0"
      class="space-y-4 py-8 text-center"
      data-test="empty-no-guests"
    >
      <p class="text-sm text-text/80">You need at least one guest before creating invitations.</p>
      <WriteOnly>
        <RouterLink
          :to="{ name: BACKOFFICE_ROUTE_NAMES.guestAdd }"
          class="inline-flex rounded-md bg-accent-gradient px-4 py-2 text-white shadow transition hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          data-test="create-first-guest-cta"
        >
          Create your first guest
        </RouterLink>
      </WriteOnly>
    </div>

    <p
      v-else-if="invitationPage.items.length === 0"
      class="py-8 text-center text-sm text-text/80"
      data-test="empty-no-invitations"
    >
      No invitations yet.
    </p>

    <template v-else>
      <div class="mb-4 flex flex-wrap items-center justify-between gap-2 text-sm text-text/80">
        <p data-test="invitation-list-summary">
          Showing {{ invitationPage.items.length }} of {{ invitationPage.totalItems }} invitations
          <template v-if="hasSelection">
            <span aria-hidden="true"> · </span>
            <strong data-test="invitation-selection-count">{{ selectionLabel }}</strong>
          </template>
        </p>
        <div class="flex items-center gap-3">
          <button
            v-if="selectedCount < invitationPage.totalItems"
            :disabled="isPrinting"
            class="rounded-md bg-accent-gradient px-3 py-1 text-white shadow transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
            data-test="select-all-invitations"
            type="button"
            @click="selectAll"
          >
            Select all ({{ invitationPage.totalItems }})
          </button>
          <button
            v-if="hasSelection"
            :disabled="isPrinting"
            class="rounded-md border border-secondary/40 bg-white px-3 py-1 text-text transition hover:bg-background/60 disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
            data-test="clear-invitation-selection"
            type="button"
            @click="clearSelection"
          >
            Clear
          </button>
        </div>
      </div>

      <div class="grid gap-4 md:grid-cols-2" data-test="invitation-card-list">
        <article
          v-for="invitation in invitationPage.items"
        :key="invitation.id"
        :class="isSelected(invitation.id) ? 'border-primary ring-2 ring-primary/40' : 'border-secondary/30'"
        class="flex h-full min-w-0 flex-col overflow-hidden rounded-xl border bg-white p-4 shadow-sm"
        data-test="invitation-card"
      >
        <div class="-mx-4 -mt-4 mb-3 flex items-center justify-between gap-2 rounded-t-xl border-b border-secondary/40 bg-background/60 px-4 py-2">
          <input
            :aria-label="`Select ${invitation.label}`"
            :checked="isSelected(invitation.id)"
            :disabled="isPrinting"
            class="h-4 w-4 shrink-0 cursor-pointer accent-primary disabled:cursor-not-allowed"
            data-test="invitation-select"
            type="checkbox"
            @change="toggleSelection(invitation.id)"
          />
          <h3 class="min-w-0 flex-1 truncate text-lg font-medium text-text" data-test="invitation-card-label">{{ invitation.label }}</h3>
          <RsvpStatusBadge :summary="invitation.rsvpSummary" class="shrink-0" />
        </div>

        <p class="mb-3 text-xs text-text/60">
          <span data-test="invitation-card-guest-count">{{ invitation.guestCount }} {{ invitation.guestCount === 1 ? 'guest' : 'guests' }}</span>
          <span aria-hidden="true"> · </span>
          <span data-test="invitation-card-delivery-method">{{ formatDeliveryMethod(invitation.deliveryMethod) }}</span>
        </p>


        <ul class="mb-3 space-y-1 text-xs text-text/80" data-test="invitation-card-guest-details">
          <li
            v-for="guest in invitation.guests"
            :key="guest.id"
            data-test="invitation-card-guest-item"
          >
            {{ guest.firstName }} {{ guest.lastName }}
          </li>
        </ul>

        <div class="mt-auto space-y-3 pt-2">
          <InvitationQrCodePanel
            :guest-access-url="buildGuestAccessUrl(invitation.accessToken)"
            :invitation-label="invitation.label"
            :show-downloads="false"
          />

          <div class="flex items-center justify-end gap-2">
            <RouterLink
              :to="{ name: BACKOFFICE_ROUTE_NAMES.invitationDetails, params: { id: invitation.id } }"
              aria-label="View invitation"
              class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-badge-gradient text-white shadow-sm transition hover:scale-110 hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
              title="View"
            >
              <img :src="viewInvitationIcon" alt="" aria-hidden="true" class="h-4 w-4 brightness-0 invert" />
            </RouterLink>
            <WriteOnly>
              <RouterLink
                :to="{ name: BACKOFFICE_ROUTE_NAMES.invitationEdit, params: { id: invitation.id } }"
                aria-label="Edit invitation"
                class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-accent-gradient text-white shadow-sm transition hover:scale-110 hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
                title="Edit"
              >
                <img :src="editInvitationIcon" alt="" aria-hidden="true" class="h-4 w-4 brightness-0 invert" />
              </RouterLink>
            </WriteOnly>
          </div>
        </div>
      </article>
      </div>

      <div v-if="hasMore" ref="sentinel" class="py-6 text-center" data-test="invitation-load-more-sentinel">
        <p v-if="isLoadingMore" class="text-sm" aria-live="polite">Loading more invitations...</p>
        <div v-else-if="loadMoreErrorMessage" class="space-y-2">
          <p class="text-sm text-red-700" role="alert">{{ loadMoreErrorMessage }}</p>
          <button
            class="rounded-md bg-accent-gradient px-4 py-2 text-white shadow transition hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
            data-test="invitation-load-more-retry"
            type="button"
            @click="loadMore"
          >
            Try again
          </button>
        </div>
        <button
          v-else
          class="rounded-md border border-secondary/40 bg-white px-4 py-2 text-sm text-text transition hover:bg-background/60 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          data-test="invitation-load-more"
          type="button"
          @click="loadMore"
        >
          Load more
        </button>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import InvitationQrCodePanel from '../components/InvitationQrCodePanel.vue';
import PrintStickersDialog from '../components/PrintStickersDialog.vue';
import RsvpStatusBadge from '../components/RsvpStatusBadge.vue';
import WriteOnly from '../components/ui/WriteOnly.vue';
import addInvitationIcon from '../assets/icons/add-invitation.svg';
import editInvitationIcon from '../assets/icons/edit.svg';
import printIcon from '../assets/icons/print.svg';
import viewInvitationIcon from '../assets/icons/view.svg';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { buildGuestAccessUrl } from '../services/guestAccessUrl';
import { listGuests } from '../services/guestApi';
import { listAllInvitations, listInvitations, type InvitationPageResponse, type InvitationResponse } from '../services/invitationApi';
import { formatDeliveryMethod } from '../services/deliveryMethod';

const STICKERS_FILENAME = 'invitation-stickers.pdf';
const PAGE_SIZE = 10;
const FILTER_DEBOUNCE_MS = 300;

const invitationPage = ref<InvitationPageResponse>({
  items: [],
  page: 0,
  size: PAGE_SIZE,
  totalItems: 0,
  totalPages: 0
});
const guestTotalItems = ref(0);
const isLoading = ref(false);
const isLoadingMore = ref(false);
const errorMessage = ref('');
const loadMoreErrorMessage = ref('');
const filterText = ref('');
const sentinel = ref<HTMLElement | null>(null);
const isPrintDialogOpen = ref(false);
const isPrinting = ref(false);
const printErrorMessage = ref('');
const allSelected = ref(false);
const selectedIds = ref<Set<string>>(new Set());
const excludedIds = ref<Set<string>>(new Set());

let latestRequestId = 0;

const trimmedFilter = computed(() => filterText.value.trim());
const hasMore = computed(() => invitationPage.value.page + 1 < invitationPage.value.totalPages);
const showFilter = computed(() => trimmedFilter.value !== '' || invitationPage.value.totalItems > 0);

const selectedCount = computed(() => allSelected.value
  ? invitationPage.value.totalItems - excludedIds.value.size
  : selectedIds.value.size);
// "Select all" is an explicit selection even when every invitation is then deselected:
// it must not fall back to the default "nothing selected prints everything".
const hasSelection = computed(() => allSelected.value || selectedIds.value.size > 0);
const isSelectionEmpty = computed(() => hasSelection.value && selectedCount.value === 0);
const selectionLabel = computed(() => selectedCount.value === invitationPage.value.totalItems
  ? `All ${selectedCount.value} selected`
  : `${selectedCount.value} selected`);

const isSelected = (id: string) => allSelected.value ? !excludedIds.value.has(id) : selectedIds.value.has(id);

const toggleSelection = (id: string) => {
  const ids = allSelected.value ? excludedIds.value : selectedIds.value;

  if (ids.has(id)) {
    ids.delete(id);
  } else {
    ids.add(id);
  }
};

const selectAll = () => {
  allSelected.value = true;
  selectedIds.value = new Set();
  excludedIds.value = new Set();
};

const clearSelection = () => {
  allSelected.value = false;
  selectedIds.value = new Set();
  excludedIds.value = new Set();
};

// Captures the selection when printing is confirmed, so that it cannot drift while the invitations are fetched.
const snapshotPrintSelection = () => {
  const isSelectionActive = hasSelection.value;
  const isEverySelected = allSelected.value;
  const orderedSelectedIds = [...selectedIds.value];
  const excluded = new Set(excludedIds.value);

  return (invitations: InvitationResponse[]) => {
    if (!isSelectionActive) {
      return invitations;
    }

    if (isEverySelected) {
      return invitations.filter((invitation) => !excluded.has(invitation.id));
    }

    const invitationsById = new Map(invitations.map((invitation) => [invitation.id, invitation]));

    return orderedSelectedIds.flatMap((id) => invitationsById.get(id) ?? []);
  };
};

const loadData = async () => {
  const requestId = ++latestRequestId;
  const label = trimmedFilter.value;

  isLoading.value = true;
  isLoadingMore.value = false;
  errorMessage.value = '';
  loadMoreErrorMessage.value = '';

  try {
    const invitations = await listInvitations({ page: 0, size: PAGE_SIZE, label });

    if (requestId !== latestRequestId) {
      return;
    }

    invitationPage.value = invitations;

    if (invitations.items.length === 0 && !label) {
      const guests = await listGuests({ page: 0, size: 1, status: 'active' });

      guestTotalItems.value = guests.totalItems;
    }
  } catch (error: unknown) {
    if (requestId !== latestRequestId) {
      return;
    }

    errorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while loading invitations.';
  } finally {
    if (requestId === latestRequestId) {
      isLoading.value = false;
    }
  }
};

const loadMore = async () => {
  if (isLoading.value || isLoadingMore.value || !hasMore.value) {
    return;
  }

  const requestId = latestRequestId;

  isLoadingMore.value = true;
  loadMoreErrorMessage.value = '';

  try {
    const nextPage = await listInvitations({
      page: invitationPage.value.page + 1,
      size: PAGE_SIZE,
      label: trimmedFilter.value
    });

    if (requestId !== latestRequestId) {
      return;
    }

    const knownIds = new Set(invitationPage.value.items.map((invitation) => invitation.id));

    invitationPage.value = {
      ...nextPage,
      items: [
        ...invitationPage.value.items,
        ...nextPage.items.filter((invitation) => !knownIds.has(invitation.id))
      ]
    };
  } catch (error: unknown) {
    if (requestId === latestRequestId) {
      loadMoreErrorMessage.value = error instanceof Error
        ? error.message
        : 'Unexpected error while loading more invitations.';
    }
  } finally {
    if (requestId === latestRequestId) {
      isLoadingMore.value = false;
      void nextTick(observeSentinel);
    }
  }
};

let observer: IntersectionObserver | null = null;

const observeSentinel = () => {
  observer?.disconnect();

  if (observer && sentinel.value && !loadMoreErrorMessage.value) {
    observer.observe(sentinel.value);
  }
};

watch(sentinel, () => {
  if (!observer && typeof IntersectionObserver !== 'undefined') {
    observer = new IntersectionObserver((entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        void loadMore();
      }
    }, { rootMargin: '200px' });
  }

  observeSentinel();
});

let filterTimer: ReturnType<typeof setTimeout> | undefined;

watch(trimmedFilter, () => {
  clearTimeout(filterTimer);
  filterTimer = setTimeout(() => {
    clearSelection();
    void loadData();
  }, FILTER_DEBOUNCE_MS);
});

onBeforeUnmount(() => {
  clearTimeout(filterTimer);
  observer?.disconnect();
});

const printStickers = async (color: string) => {
  isPrintDialogOpen.value = false;

  if (isSelectionEmpty.value) {
    return;
  }

  const selectInvitationsToPrint = snapshotPrintSelection();
  const printLabel = trimmedFilter.value || undefined;

  isPrinting.value = true;
  printErrorMessage.value = '';

  try {
    const invitations = await listAllInvitations({ label: printLabel });
    const invitationsToPrint = selectInvitationsToPrint(invitations);
    const { buildStickerPdf } = await import('../services/stickerPdf');
    const pdf = await buildStickerPdf(invitationsToPrint, color);

    pdf.save(STICKERS_FILENAME);
  } catch (error: unknown) {
    printErrorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while generating the stickers.';
  } finally {
    isPrinting.value = false;
  }
};

onMounted(() => {
  void loadData();
});
</script>

