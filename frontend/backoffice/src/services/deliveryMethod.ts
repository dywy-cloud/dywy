import type { DeliveryMethod } from './invitationApi';

const DELIVERY_METHOD_LABELS: Record<DeliveryMethod, string> = {
  HAND_DELIVERED: 'Hand delivered',
  POSTED: 'Posted'
};

export const DEFAULT_DELIVERY_METHOD: DeliveryMethod = 'HAND_DELIVERED';

export const resolveDeliveryMethod = (deliveryMethod?: DeliveryMethod | null): DeliveryMethod =>
  deliveryMethod ?? DEFAULT_DELIVERY_METHOD;

export const formatDeliveryMethod = (deliveryMethod?: DeliveryMethod | null): string =>
  DELIVERY_METHOD_LABELS[resolveDeliveryMethod(deliveryMethod)];

