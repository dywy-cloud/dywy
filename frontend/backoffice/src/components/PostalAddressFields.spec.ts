import { mount } from '@vue/test-utils';
import { afterEach, describe, expect, it, vi } from 'vitest';
import PostalAddressFields from './PostalAddressFields.vue';
import { createEmptyPostalAddressForm } from '../services/postalAddress';

const mountFields = () => mount(PostalAddressFields, {
  props: { modelValue: createEmptyPostalAddressForm() }
});

const optionValues = (selector: string, wrapper: ReturnType<typeof mountFields>) =>
  wrapper.findAll(`${selector} option`).map((option) => option.attributes('value'));

describe('PostalAddressFields', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('offers the detected system country in its own section and not in the full list', () => {
    vi.spyOn(navigator, 'languages', 'get').mockReturnValue(['fr-FR']);

    const wrapper = mountFields();
    const allOptions = wrapper.findAll('option').map((option) => option.attributes('value'));

    expect(optionValues('[data-test="postal-address-system-country-group"]', wrapper)).toEqual(['FR']);
    expect(allOptions.filter((value) => value === 'FR')).toHaveLength(1);
  });

  it('shows only the full country list when no system country is detected', () => {
    vi.spyOn(navigator, 'languages', 'get').mockReturnValue(['en']);

    const wrapper = mountFields();

    expect(wrapper.find('[data-test="postal-address-system-country-group"]').exists()).toBe(false);
    expect(wrapper.findAll('option').filter((option) => option.attributes('value') === 'FR')).toHaveLength(1);
  });
});

