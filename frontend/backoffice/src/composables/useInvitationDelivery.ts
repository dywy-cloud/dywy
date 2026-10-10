import { computed, ref } from 'vue';
import type { DeliveryMethod, InvitationResponse } from '../services/invitationApi';
import { DEFAULT_DELIVERY_METHOD, resolveDeliveryMethod } from '../services/deliveryMethod';
import {
  createEmptyPostalAddressForm,
  getPostalAddressValidationError,
  toPostalAddressForm,
  toPostalAddressPayload
} from '../services/postalAddress';

export const useInvitationDelivery = () => {
  const deliveryMethod = ref<DeliveryMethod>(DEFAULT_DELIVERY_METHOD);
  const postalAddress = ref(createEmptyPostalAddressForm());

  const isPosted = computed(() => deliveryMethod.value === 'POSTED');

  const validationError = computed(() =>
    isPosted.value ? getPostalAddressValidationError(postalAddress.value, true) : ''
  );

  const payload = computed(() => ({
    deliveryMethod: deliveryMethod.value,
    postalAddress: isPosted.value ? toPostalAddressPayload(postalAddress.value) : undefined
  }));

  const loadFrom = (invitation: Pick<InvitationResponse, 'deliveryMethod' | 'postalAddress'>) => {
    deliveryMethod.value = resolveDeliveryMethod(invitation.deliveryMethod);
    postalAddress.value = toPostalAddressForm(invitation.postalAddress);
  };

  return { deliveryMethod, postalAddress, isPosted, validationError, payload, loadFrom };
};




