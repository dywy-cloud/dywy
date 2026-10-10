<template>
  <fieldset data-test="postal-address-fieldset">
    <legend class="mb-2 text-sm font-medium text-text/80">Postal address</legend>

    <div class="grid gap-3 sm:grid-cols-2">
      <div v-for="field in FIELDS" :key="field.key" :class="{ 'sm:col-span-2': field.wide }">
        <label class="form-label" :for="`postal-address-${field.id}`">{{ field.label }}</label>
        <input
          :id="`postal-address-${field.id}`"
          v-model="model[field.key]"
          class="form-input"
          :data-test="`postal-address-${field.id}-input`"
          type="text"
        >
      </div>
      <div class="min-w-0">
        <label class="form-label" for="postal-address-country-code">Country</label>
        <select
          id="postal-address-country-code"
          v-model="model.countryCode"
          class="form-input truncate"
          data-test="postal-address-country-code-input"
        >
          <option value="">Select a country</option>
          <optgroup v-if="systemCountry" label="Your region" data-test="postal-address-system-country-group">
            <option :value="systemCountry.code">{{ systemCountry.label }}</option>
          </optgroup>
          <optgroup label="All countries">
            <option v-for="country in otherCountries" :key="country.code" :value="country.code">
              {{ country.label }}
            </option>
          </optgroup>
        </select>
      </div>
    </div>
  </fieldset>
</template>

<script setup lang="ts">
import { getCountryOptions, getSystemCountryCode } from '../services/countries';
import type { PostalAddressForm } from '../services/postalAddress';

interface PostalAddressField {
  key: Exclude<keyof PostalAddressForm, 'countryCode'>;
  id: string;
  label: string;
  wide?: boolean;
}

const FIELDS: PostalAddressField[] = [
  { key: 'line1', id: 'line1', label: 'Address line 1', wide: true },
  { key: 'line2', id: 'line2', label: 'Address line 2', wide: true },
  { key: 'line3', id: 'line3', label: 'Address line 3', wide: true },
  { key: 'postalCode', id: 'postal-code', label: 'Postal code' },
  { key: 'locality', id: 'locality', label: 'City' },
  { key: 'region', id: 'region', label: 'Region' }
];

const COUNTRY_OPTIONS = getCountryOptions();
const systemCountryCode = getSystemCountryCode();
const systemCountry = COUNTRY_OPTIONS.find((country) => country.code === systemCountryCode);
const otherCountries = COUNTRY_OPTIONS.filter((country) => country.code !== systemCountryCode);

const model = defineModel<PostalAddressForm>({ required: true });
</script>
