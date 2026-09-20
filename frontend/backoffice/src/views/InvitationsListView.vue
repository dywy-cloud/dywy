<template>
  <section>
    <header class="mb-6 grid grid-cols-[1fr_auto_1fr] items-center">
      <span class="h-6 justify-self-start" aria-hidden="true"></span>

      <h2 class="page-title justify-self-center">Invitations</h2>

      <WriteOnly>
        <RouterLink
          :to="{ name: BACKOFFICE_ROUTE_NAMES.invitationAdd }"
          aria-label="Create invitation"
          class="justify-self-end inline-flex h-10 w-10 items-center justify-center rounded-full bg-accent-gradient text-white shadow transition hover:scale-105 hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          data-test="create-invitation-cta"
          title="Create invitation"
        >
          <img :src="addInvitationIcon" alt="" aria-hidden="true" class="h-6 w-6 brightness-0 invert" />
        </RouterLink>
      </WriteOnly>
    </header>

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
      <div class="mb-4 flex items-center justify-between text-sm text-text/80">
        <p>Showing {{ invitationPage.items.length }} of {{ invitationPage.totalItems }} invitations</p>
      </div>

      <div class="grid gap-4 md:grid-cols-2" data-test="invitation-card-list">
        <article
          v-for="invitation in invitationPage.items"
        :key="invitation.id"
        class="flex h-full min-w-0 flex-col overflow-hidden rounded-xl border border-secondary/30 bg-white p-4 shadow-sm"
        data-test="invitation-card"
      >
        <h3 class="-mx-4 -mt-4 mb-3 rounded-t-xl border-b border-secondary/40 bg-background/60 px-4 py-2 text-lg font-medium text-text" data-test="invitation-card-label">{{ invitation.label }}</h3>

        <p class="mb-1 text-xs text-text/60" data-test="invitation-card-guest-count">{{ invitation.guestCount }} guests</p>

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
    </template>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import InvitationQrCodePanel from '../components/InvitationQrCodePanel.vue';
import WriteOnly from '../components/ui/WriteOnly.vue';
import addInvitationIcon from '../assets/icons/add-invitation.svg';
import editInvitationIcon from '../assets/icons/edit.svg';
import viewInvitationIcon from '../assets/icons/view.svg';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { buildGuestAccessUrl } from '../services/guestAccessUrl';
import { listGuests } from '../services/guestApi';
import { listInvitations, type InvitationPageResponse } from '../services/invitationApi';

const invitationPage = ref<InvitationPageResponse>({
  items: [],
  page: 0,
  size: 20,
  totalItems: 0,
  totalPages: 0
});
const guestTotalItems = ref(0);
const isLoading = ref(false);
const errorMessage = ref('');

const loadData = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  try {
    const invitations = await listInvitations({ page: 0, size: 20 });

    invitationPage.value = invitations;

    if (invitations.items.length === 0) {
      const guests = await listGuests({ page: 0, size: 1, status: 'active' });

      guestTotalItems.value = guests.totalItems;
    }
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while loading invitations.';
  } finally {
    isLoading.value = false;
  }
};


onMounted(() => {
  void loadData();
});
</script>

