<template>
  <fieldset class="rounded-xl border border-secondary/30 bg-white p-4 shadow-sm" data-test="delivery-section">
    <legend class="px-2 text-base font-medium text-text">Delivery</legend>

    <div class="flex items-center gap-3 text-sm text-text">
      <span :class="isPosted ? 'text-text/50' : 'font-medium'">Hand delivered</span>
      <button
        :aria-checked="isPosted"
        aria-label="Send by post"
        class="relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        :class="isPosted ? 'bg-accent-gradient' : 'bg-secondary/40'"
        data-test="delivery-method-switch"
        role="switch"
        type="button"
        @click="toggle"
      >
        <span
          class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition-transform"
          :class="isPosted ? 'translate-x-5' : 'translate-x-0.5'"
        />
      </button>
      <span :class="isPosted ? 'font-medium' : 'text-text/50'">Posted</span>
    </div>

    <PostalAddressFields v-if="isPosted" v-model="postalAddress" class="mt-4" />
  </fieldset>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { DeliveryMethod } from '../services/invitationApi';
import type { PostalAddressForm } from '../services/postalAddress';
import PostalAddressFields from './PostalAddressFields.vue';

const deliveryMethod = defineModel<DeliveryMethod>('deliveryMethod', { required: true });
const postalAddress = defineModel<PostalAddressForm>('postalAddress', { required: true });

const isPosted = computed(() => deliveryMethod.value === 'POSTED');

const toggle = () => {
  deliveryMethod.value = isPosted.value ? 'HAND_DELIVERED' : 'POSTED';
};
</script>

