import { describe, expect, it } from 'vitest';
import { getCountryOptions, getSystemCountryCode, isIsoCountryCode } from './countries';
import {
  createEmptyPostalAddressForm,
  getPostalAddressValidationError
} from './postalAddress';

describe('countries', () => {
  it('accepts assigned ISO 3166-1 codes', () => {
    expect(isIsoCountryCode('FR')).toBe(true);
    expect(isIsoCountryCode('CA')).toBe(true);
  });

  it.each(['ZZ', 'AA', 'XX', 'fr', ''])('rejects %j', (code) => {
    expect(isIsoCountryCode(code)).toBe(false);
  });

  it.each([
    [['fr-FR', 'en-US'], 'FR'],
    [['en', 'fr-CA'], 'CA'],
    [['en-ZZ', 'de-DE'], 'DE'],
    [['en', 'fr'], null],
    [[], null]
  ])('detects the system country from %j', (languages, expected) => {
    expect(getSystemCountryCode(languages)).toBe(expected);
  });

  it('labels countries as "code - name" and sorts them by code', () => {
    const options = getCountryOptions('en');

    expect(options).toContainEqual({ code: 'FR', label: 'FR - France' });
    expect(options.map((option) => option.code)).toEqual(options.map((option) => option.code).sort());
  });
});

describe('getPostalAddressValidationError', () => {
  const validForm = {
    ...createEmptyPostalAddressForm(),
    line1: '1 rue de la Paix',
    locality: 'Paris',
    countryCode: 'FR'
  };

  it('accepts a complete address', () => {
    expect(getPostalAddressValidationError(validForm, true)).toBe('');
  });

  it.each(['ZZ', 'AA', 'XX'])('rejects the unassigned country code %s', (countryCode) => {
    expect(getPostalAddressValidationError({ ...validForm, countryCode }, true)).toBe('Select a country.');
  });

  it('ignores an empty address when not required', () => {
    expect(getPostalAddressValidationError(createEmptyPostalAddressForm())).toBe('');
  });
});


