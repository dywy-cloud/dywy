<template>
  <div>
    <label v-if="label" :for="inputId" class="form-label">{{ label }}</label>
    <input
      :id="inputId"
      :name="name"
      :type="type"
      :value="modelValue"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      class="form-input"
      v-bind="$attrs"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label?: string;
  modelValue?: string;
  type?: string;
  inputId?: string;
  name?: string;
}>();

defineEmits(['update:modelValue']);
</script>
