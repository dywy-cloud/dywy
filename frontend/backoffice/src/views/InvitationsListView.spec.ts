import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest';
import InvitationsListView from './InvitationsListView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { applyCapabilities, resetCapabilities } from '../composables/useCapabilities';
import { createInvitationResponse } from '../testFixtures/invitationFixtures';

const listInvitationsMock = vi.hoisted(() => vi.fn());
const listAllInvitationsMock = vi.hoisted(() => vi.fn());
const listGuestsMock = vi.hoisted(() => vi.fn());
const buildStickerPdfMock = vi.hoisted(() => vi.fn());

vi.mock('../services/invitationApi', () => ({
  listInvitations: listInvitationsMock,
  listAllInvitations: listAllInvitationsMock
}));

vi.mock('../services/stickerPdf', () => ({
  buildStickerPdf: buildStickerPdfMock
}));

vi.mock('../services/guestApi', async (importOriginal) => {
  const module = await importOriginal<typeof import('../services/guestApi')>();

  return {
    ...module,
    listGuests: listGuestsMock
  };
});

describe('InvitationsListView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    applyCapabilities({ canWrite: true });
  });

  afterEach(() => {
    resetCapabilities();
  });

  const mountView = async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/invitations',
          name: BACKOFFICE_ROUTE_NAMES.invitationList,
          component: InvitationsListView
        },
        {
          path: '/invitations/new',
          name: BACKOFFICE_ROUTE_NAMES.invitationAdd,
          component: defineComponent({
            template: '<div>create invitation view</div>'
          })
        },
        {
          path: '/invitations/:id',
          name: BACKOFFICE_ROUTE_NAMES.invitationDetails,
          component: defineComponent({
            template: '<div>invitation details view</div>'
          })
        },
        {
          path: '/invitations/:id/edit',
          name: BACKOFFICE_ROUTE_NAMES.invitationEdit,
          component: defineComponent({
            template: '<div>edit invitation view</div>'
          })
        },
        {
          path: '/guests/new',
          name: BACKOFFICE_ROUTE_NAMES.guestAdd,
          component: defineComponent({
            template: '<div>add guest view</div>'
          })
        }
      ]
    });

    await router.push('/invitations');
    await router.isReady();

    const wrapper = mount(defineComponent({
      template: '<RouterView />'
    }), {
      global: {
        plugins: [router],
        stubs: { teleport: true }
      }
    });

    await flushPromises();

    return { wrapper, router };
  };

  it('renders invitation cards with title, guest count, creation date and actions', async () => {
    listInvitationsMock.mockResolvedValue({
      items: [
        {
          id: 'inv-1',
          accessToken: 'token-invitation-1234567890',
          version: 1,
          creationDate: '2026-07-01T10:45:28Z',
          updateDate: '2026-07-01T10:45:28Z',
          label: 'Family table',
          description: 'ignored by card content',
          deliveryMethod: 'POSTED',
          guests: [
            {
              id: 'g-1',
              firstName: 'Alice',
              lastName: 'Martin',
              email: 'alice@example.com'
            },
            {
              id: 'g-2',
              firstName: 'Bob',
              lastName: 'Durand',
              email: 'bob@example.com'
            }
          ],
          guestCount: 2,
          rsvpSummary: { status: 'PENDING', attending: 0, declined: 0, pending: 2, total: 2 }
        }
      ],
      page: 0,
      size: 20,
      totalItems: 1,
      totalPages: 1
    });
    const { wrapper } = await mountView();

    const cards = wrapper.findAll('[data-test="invitation-card"]');
    const createInvitationCta = wrapper.get('[data-test="create-invitation-cta"]');

    expect(cards).toHaveLength(1);
    expect(createInvitationCta.attributes('href')).toBe('/invitations/new');
    expect(wrapper.find('[data-test="invitation-card-list"]').exists()).toBe(true);
    expect(wrapper.find('table').exists()).toBe(false);
    expect(wrapper.text()).toContain('Family table');
    expect(wrapper.text()).toContain('2 guests');
    expect(wrapper.get('[data-test="invitation-card-delivery-method"]').text()).toBe('Posted');
    expect(wrapper.find('[data-test="invitation-qr-panel"]').exists()).toBe(true);
    expect(wrapper.get('[data-test="invitation-qr-url"]').text()).toContain('token-invitation-1234567890');
    expect(wrapper.get('[data-test="invitation-qr-url"]').attributes('href')).toContain('/guest-access/token-invitation-1234567890');
    expect(wrapper.text()).toContain('Alice Martin');
    expect(wrapper.text()).toContain('Bob Durand');
    expect(wrapper.text()).not.toContain('alice@example.com');
    expect(wrapper.text()).not.toContain('bob@example.com');
    expect(cards[0].findAll('[data-test="invitation-card-guest-item"]')).toHaveLength(2);
    const viewLink = cards[0].get('a[aria-label="View invitation"]');
    const editLink = cards[0].get('a[aria-label="Edit invitation"]');
    expect(viewLink.attributes('href')).toBe('/invitations/inv-1');
    expect(editLink.attributes('href')).toBe('/invitations/inv-1/edit');
    expect(listGuestsMock).not.toHaveBeenCalled();
  });

  it('uses the singular form when an invitation has a single guest', async () => {
    listInvitationsMock.mockResolvedValue({
      items: [createInvitationResponse({ label: 'Solo', deliveryMethod: 'HAND_DELIVERED' })],
      page: 0,
      size: 20,
      totalItems: 1,
      totalPages: 1
    });

    const { wrapper } = await mountView();

    expect(wrapper.get('[data-test="invitation-card-guest-count"]').text()).toBe('1 guest');
    expect(wrapper.get('[data-test="invitation-card-delivery-method"]').text()).toBe('Hand delivered');
  });

  it('shows legacy invitations without delivery method as hand delivered', async () => {
    listInvitationsMock.mockResolvedValue({
      items: [createInvitationResponse({ deliveryMethod: null })],
      page: 0,
      size: 20,
      totalItems: 1,
      totalPages: 1
    });

    const { wrapper } = await mountView();

    expect(wrapper.get('[data-test="invitation-card-delivery-method"]').text()).toBe('Hand delivered');
  });

  it('shows create-first-guest CTA when there are no guests', async () => {
    listInvitationsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 20,
      totalItems: 0,
      totalPages: 0
    });
    listGuestsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 1,
      totalItems: 0,
      totalPages: 0
    });

    const { wrapper } = await mountView();

    expect(wrapper.find('[data-test="empty-no-guests"]').exists()).toBe(true);
    expect(wrapper.get('[data-test="create-first-guest-cta"]').attributes('href')).toBe('/guests/new');
    expect(wrapper.text()).toContain('Create your first guest');
  });

  it('shows invitations empty state when guests exist but invitations are empty', async () => {
    listInvitationsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 20,
      totalItems: 0,
      totalPages: 0
    });
    listGuestsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 1,
      totalItems: 2,
      totalPages: 2
    });

    const { wrapper } = await mountView();

    expect(wrapper.find('[data-test="empty-no-invitations"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="create-first-guest-cta"]').exists()).toBe(false);
    expect(wrapper.text()).toContain('No invitations yet.');
  });

  it('hides create and edit CTAs in read-only mode while keeping view link', async () => {
    resetCapabilities();
    listInvitationsMock.mockResolvedValue({
      items: [
        {
          id: 'inv-1',
          accessToken: 'token-invitation-1234567890',
          version: 1,
          creationDate: '2026-07-01T10:45:28Z',
          updateDate: '2026-07-01T10:45:28Z',
          label: 'Family table',
          description: 'ignored by card content',
          guests: [],
          guestCount: 0,
          rsvpSummary: { status: 'PENDING', attending: 0, declined: 0, pending: 0, total: 0 }
        }
      ],
      page: 0,
      size: 20,
      totalItems: 1,
      totalPages: 1
    });

    const { wrapper } = await mountView();
    const cards = wrapper.findAll('[data-test="invitation-card"]');

    expect(wrapper.find('[data-test="create-invitation-cta"]').exists()).toBe(false);
    expect(cards[0].find('a[aria-label="Edit invitation"]').exists()).toBe(false);
    expect(cards[0].find('a[aria-label="View invitation"]').exists()).toBe(true);
  });

  it('hides create-first-guest CTA in read-only mode', async () => {
    resetCapabilities();
    listInvitationsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 20,
      totalItems: 0,
      totalPages: 0
    });
    listGuestsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 1,
      totalItems: 0,
      totalPages: 0
    });

    const { wrapper } = await mountView();

    expect(wrapper.find('[data-test="empty-no-guests"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="create-first-guest-cta"]').exists()).toBe(false);
  });

  describe('print stickers', () => {
    const singleInvitationPage = {
      items: [createInvitationResponse()],
      page: 0,
      size: 20,
      totalItems: 1,
      totalPages: 1
    };

    const emptyInvitationPage = { items: [], page: 0, size: 20, totalItems: 0, totalPages: 0 };

    const confirmPrint = async (wrapper: Awaited<ReturnType<typeof mountView>>['wrapper']) => {
      await wrapper.get('[data-test="print-stickers-cta"]').trigger('click');
      await wrapper.get('[data-test="color-swatch-#166534"]').trigger('click');
      await wrapper.get('[data-test="print-stickers-confirm"]').trigger('click');
    };

    it('disables the button when there is no invitation', async () => {
      listInvitationsMock.mockResolvedValue(emptyInvitationPage);
      listGuestsMock.mockResolvedValue({ items: [], page: 0, size: 1, totalItems: 0, totalPages: 0 });

      const { wrapper } = await mountView();

      expect(wrapper.get('[data-test="print-stickers-cta"]').attributes('disabled')).toBeDefined();
    });

    it('is shown and enabled in read-only mode', async () => {
      resetCapabilities();
      listInvitationsMock.mockResolvedValue(singleInvitationPage);

      const { wrapper } = await mountView();

      expect(wrapper.get('[data-test="print-stickers-cta"]').attributes('disabled')).toBeUndefined();
    });

    it('removes the QR download buttons from the cards', async () => {
      listInvitationsMock.mockResolvedValue(singleInvitationPage);

      const { wrapper } = await mountView();

      expect(wrapper.find('[data-test="download-qr-png"]').exists()).toBe(false);
      expect(wrapper.find('[data-test="download-qr-svg"]').exists()).toBe(false);
    });

    it('opens the dialog and closes it on cancel', async () => {
      listInvitationsMock.mockResolvedValue(singleInvitationPage);

      const { wrapper } = await mountView();

      expect(wrapper.find('[data-test="print-stickers-confirm"]').exists()).toBe(false);

      await wrapper.get('[data-test="print-stickers-cta"]').trigger('click');

      expect(wrapper.find('[data-test="print-stickers-confirm"]').exists()).toBe(true);

      await wrapper.get('[data-test="print-stickers-cancel"]').trigger('click');

      expect(wrapper.find('[data-test="print-stickers-confirm"]').exists()).toBe(false);
    });

    it('shows a loading state then downloads the pdf with the chosen color', async () => {
      const save = vi.fn();
      let resolveInvitations: (value: unknown) => void = () => undefined;
      listInvitationsMock.mockResolvedValue(singleInvitationPage);
      listAllInvitationsMock.mockReturnValue(new Promise((resolve) => {
        resolveInvitations = resolve;
      }));
      buildStickerPdfMock.mockResolvedValue({ save });

      const { wrapper } = await mountView();

      await confirmPrint(wrapper);

      const button = wrapper.get('[data-test="print-stickers-cta"]');
      expect(button.attributes('aria-busy')).toBe('true');
      expect(button.attributes('disabled')).toBeDefined();

      resolveInvitations(singleInvitationPage.items);
      await flushPromises();

      expect(buildStickerPdfMock).toHaveBeenCalledWith(singleInvitationPage.items, '#166534');
      expect(save).toHaveBeenCalledWith('invitation-stickers.pdf');
      expect(wrapper.get('[data-test="print-stickers-cta"]').attributes('aria-busy')).toBe('false');
      expect(wrapper.find('[data-test="print-stickers-error"]').exists()).toBe(false);
    });

    it('shows an error message when the generation fails', async () => {
      listInvitationsMock.mockResolvedValue(singleInvitationPage);
      listAllInvitationsMock.mockRejectedValue(new Error('Unable to load invitations.'));

      const { wrapper } = await mountView();

      await confirmPrint(wrapper);
      await flushPromises();

      expect(wrapper.get('[data-test="print-stickers-error"]').text()).toBe('Unable to load invitations.');
      expect(wrapper.get('[data-test="print-stickers-cta"]').attributes('disabled')).toBeUndefined();
    });
  });
});

