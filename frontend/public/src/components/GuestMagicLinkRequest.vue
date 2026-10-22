<template>
  <button
    class="guest-button bg-badge-gradient disabled:cursor-not-allowed disabled:opacity-60"
    type="button"
    :disabled="isSending"
    :aria-label="`${t('invitation.magicLink.requestFor')} ${fullName}`"
    @click="request"
  >
    {{ firstName }}
  </button>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { requestMagicLink, type MagicLinkMode } from '../services/guestAccessMagicLinkApi';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

const props = defineProps<{ token: string; guestId: string; firstName: string; lastName: string; mode?: MagicLinkMode }>();

export type MagicLinkRequestStatus = 'sent' | 'rateLimited' | 'error';

export type MagicLinkRequested = { status: MagicLinkRequestStatus; firstName: string; mode: MagicLinkMode };

const emit = defineEmits<{
  requested: [MagicLinkRequested];
}>();

const isSending = ref(false);
const { t } = useGuestAccessI18n();

const fullName = computed(() => `${props.firstName} ${props.lastName}`);

const request = async (): Promise<void> => {
  if (isSending.value) {
    return;
  }

  isSending.value = true;
  const { firstName } = props;
  const mode = props.mode ?? 'INDIVIDUAL';

  try {
    const result = await requestMagicLink(props.token, props.guestId, mode);
    emit('requested', { status: result.status === 'rateLimited' ? 'rateLimited' : 'sent', firstName, mode });
  } catch {
    emit('requested', { status: 'error', firstName, mode });
  } finally {
    isSending.value = false;
  }
};
</script>


