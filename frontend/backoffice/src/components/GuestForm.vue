<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <BaseInput
      v-model="form.firstName"
      input-id="firstName"
      name="given-name"
      autocomplete="given-name"
      label="First Name"
      placeholder="e.g. John"
      required
    />
    <BaseInput
      v-model="form.lastName"
      input-id="lastName"
      name="family-name"
      autocomplete="family-name"
      label="Last Name"
      placeholder="e.g. Doe"
      required
    />
    <BaseInput
      v-model="form.email"
      input-id="email"
      name="email"
      autocomplete="email"
      type="email"
      label="Email"
      placeholder="e.g. john.doe@email.com"
      required
    />
    <div>
      <label for="language" class="form-label">Language</label>
      <select
        id="language"
        v-model="form.language"
        name="language"
        class="form-input"
      >
        <option value="FR">Français</option>
        <option value="EN">English</option>
      </select>
    </div>
    <div v-if="props.showCancelButton" class="flex justify-center gap-3 pt-2">
      <BaseButton type="button" @click="handleCancel" variant="solid">
        Cancel
      </BaseButton>
      <BaseButton type="submit" :disabled="props.isSubmitting">
        {{ props.isSubmitting ? props.submittingLabel : props.submitLabel }}
      </BaseButton>
    </div>
    <div v-else class="flex justify-center pt-2">
      <BaseButton type="submit" :disabled="props.isSubmitting">
        {{ props.isSubmitting ? props.submittingLabel : props.submitLabel }}
      </BaseButton>
    </div>
  </form>
</template>

<script setup lang="ts">
import { toRef, watch } from 'vue';
import BaseInput from './ui/BaseInput.vue';
import BaseButton from './ui/BaseButton.vue';
import { useGuestForm, type GuestFormData } from '../composables/useGuestForm';

defineOptions({
  name: 'GuestForm'
});

const props = withDefaults(defineProps<{
  isSubmitting?: boolean;
  initialValues?: Partial<GuestFormData>;
  submitLabel?: string;
  submittingLabel?: string;
  resetOnSubmit?: boolean;
  showCancelButton?: boolean;
}>(), {
  isSubmitting: false,
  initialValues: () => ({
    firstName: '',
    lastName: '',
    email: '',
    language: 'FR'
  }),
  submitLabel: 'Add Guest',
  submittingLabel: 'Adding guest...',
  resetOnSubmit: true,
  showCancelButton: false
});

const emit = defineEmits<{
  (e: 'submit', payload: GuestFormData): void;
  (e: 'dirty-change', isDirty: boolean): void;
  (e: 'cancel'): void;
}>();

const { form, isDirty, reset } = useGuestForm(toRef(props, 'initialValues'));

watch(isDirty, (nextValue) => {
  emit('dirty-change', nextValue);
}, { immediate: true });

const handleSubmit = () => {
  if (props.isSubmitting) {
    return;
  }

  emit('submit', { ...form.value });

  if (props.resetOnSubmit) {
    reset();
  }
};

const handleCancel = () => {
  emit('cancel');
};
</script>
