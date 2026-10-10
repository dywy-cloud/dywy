import { isIsoCountryCode } from './countries';

export interface PostalAddress {
  line1: string;
  line2?: string | null;
  line3?: string | null;
  postalCode?: string | null;
  locality: string;
  region?: string | null;
  countryCode: string;
}

export interface PostalAddressForm {
  line1: string;
  line2: string;
  line3: string;
  postalCode: string;
  locality: string;
  region: string;
  countryCode: string;
}

export const createEmptyPostalAddressForm = (): PostalAddressForm => ({
  line1: '',
  line2: '',
  line3: '',
  postalCode: '',
  locality: '',
  region: '',
  countryCode: ''
});

export const toPostalAddressForm = (address?: PostalAddress | null): PostalAddressForm => ({
  line1: address?.line1 ?? '',
  line2: address?.line2 ?? '',
  line3: address?.line3 ?? '',
  postalCode: address?.postalCode ?? '',
  locality: address?.locality ?? '',
  region: address?.region ?? '',
  countryCode: address?.countryCode ?? ''
});

const normalizeForm = (form: PostalAddressForm): PostalAddressForm => ({
  line1: form.line1.trim(),
  line2: form.line2.trim(),
  line3: form.line3.trim(),
  postalCode: form.postalCode.trim(),
  locality: form.locality.trim(),
  region: form.region.trim(),
  countryCode: form.countryCode.trim().toUpperCase()
});

export const isPostalAddressFormEmpty = (form: PostalAddressForm): boolean =>
  Object.values(normalizeForm(form)).every((value) => value.length === 0);

export const getPostalAddressValidationError = (form: PostalAddressForm, required = false): string => {
  if (!required && isPostalAddressFormEmpty(form)) {
    return '';
  }

  const normalized = normalizeForm(form);

  if (!normalized.line1) {
    return 'Address line 1 is required.';
  }

  if (!normalized.locality) {
    return 'City is required.';
  }

  if (!isIsoCountryCode(normalized.countryCode)) {
    return 'Select a country.';
  }

  return '';
};

export const toPostalAddressPayload = (form: PostalAddressForm): PostalAddress | undefined => {
  if (isPostalAddressFormEmpty(form)) {
    return undefined;
  }

  const normalized = normalizeForm(form);

  return {
    line1: normalized.line1,
    line2: normalized.line2 || null,
    line3: normalized.line3 || null,
    postalCode: normalized.postalCode || null,
    locality: normalized.locality,
    region: normalized.region || null,
    countryCode: normalized.countryCode
  };
};

export const formatPostalAddress = (address: PostalAddress): string =>
  [
    address.line1,
    address.line2,
    address.line3,
    [address.postalCode, address.locality].filter(Boolean).join(' '),
    address.region,
    address.countryCode
  ]
    .filter((part): part is string => Boolean(part))
    .join(', ');

