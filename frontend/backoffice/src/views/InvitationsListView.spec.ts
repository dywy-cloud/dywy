import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest';
import InvitationsListView from './InvitationsListView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { applyCapabilities, resetCapabilities } from '../composables/useCapabilities';
import { createInvitationResponse, createRsvpSummary } from '../testFixtures/invitationFixtures';

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

  it('shows the RSVP badge of each invitation card', async () => {
    listInvitationsMock.mockResolvedValue({
      items: [
        createInvitationResponse({
          id: 'inv-1',
          rsvpSummary: createRsvpSummary({ status: 'ACCEPTED_ALL', attending: 3, pending: 0 })
        }),
        createInvitationResponse({
          id: 'inv-2',
          rsvpSummary: createRsvpSummary({ status: 'DECLINED_ALL', declined: 3, pending: 0 })
        }),
        createInvitationResponse({ id: 'inv-3' })
      ],
      page: 0,
      size: 20,
      totalItems: 3,
      totalPages: 1
    });

    const { wrapper } = await mountView();
    const badges = wrapper.findAll('[data-test="invitation-card"] [data-test="rsvp-status-badge"]');

    expect(badges.map((badge) => badge.attributes('data-status'))).toEqual(['ACCEPTED_ALL', 'DECLINED_ALL', 'PENDING']);
    expect(badges[0].text()).toContain('Accepted 3/3');
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

  describe('invitation selection', () => {
    const invitations = [
      createInvitationResponse({ id: 'inv-1', label: 'Table 1' }),
      createInvitationResponse({ id: 'inv-2', label: 'Table 2' })
    ];
    const everyInvitation = [...invitations, createInvitationResponse({ id: 'inv-3', label: 'Table 3' })];
    const listedPage = { items: invitations, page: 0, size: 20, totalItems: 3, totalPages: 1 };

    const printWithDefaultColor = async (wrapper: Awaited<ReturnType<typeof mountView>>['wrapper']) => {
      await wrapper.get('[data-test="print-stickers-cta"]').trigger('click');
      await wrapper.get('[data-test="print-stickers-confirm"]').trigger('click');
      await flushPromises();
    };

    beforeEach(() => {
      listInvitationsMock.mockResolvedValue(listedPage);
      listAllInvitationsMock.mockResolvedValue(everyInvitation);
      buildStickerPdfMock.mockResolvedValue({ save: vi.fn() });
    });

    it('shows an unchecked checkbox on each card and no selection count', async () => {
      const { wrapper } = await mountView();
      const checkboxes = wrapper.findAll('[data-test="invitation-select"]');

      expect(checkboxes).toHaveLength(2);
      expect(checkboxes.every((checkbox) => !(checkbox.element as HTMLInputElement).checked)).toBe(true);
      expect(wrapper.find('[data-test="invitation-selection-count"]').exists()).toBe(false);
      expect(wrapper.find('[data-test="clear-invitation-selection"]').exists()).toBe(false);
      expect(wrapper.get('[data-test="select-all-invitations"]').text()).toBe('Select all (3)');
    });

    it('keeps the list summary and adds the selection count', async () => {
      const { wrapper } = await mountView();

      await wrapper.findAll('[data-test="invitation-select"]')[0].setValue(true);

      expect(wrapper.get('[data-test="invitation-list-summary"]').text()).toContain('Showing 2 of 3 invitations');
      expect(wrapper.get('[data-test="invitation-selection-count"]').text()).toBe('1 selected');
    });

    it('is available in read-only mode', async () => {
      resetCapabilities();

      const { wrapper } = await mountView();

      expect(wrapper.findAll('[data-test="invitation-select"]')).toHaveLength(2);
    });

    it('selects every invitation, including the ones that are not listed', async () => {
      const { wrapper } = await mountView();

      await wrapper.get('[data-test="select-all-invitations"]').trigger('click');

      expect(wrapper.get('[data-test="invitation-selection-count"]').text()).toBe('All 3 selected');
      expect(wrapper.find('[data-test="select-all-invitations"]').exists()).toBe(false);
      expect(wrapper.findAll('[data-test="invitation-select"]').every((checkbox) => (checkbox.element as HTMLInputElement).checked)).toBe(true);
    });

    it('clears the selection', async () => {
      const { wrapper } = await mountView();

      await wrapper.get('[data-test="select-all-invitations"]').trigger('click');
      await wrapper.get('[data-test="clear-invitation-selection"]').trigger('click');

      expect(wrapper.find('[data-test="invitation-selection-count"]').exists()).toBe(false);
      expect(wrapper.findAll('[data-test="invitation-select"]').some((checkbox) => (checkbox.element as HTMLInputElement).checked)).toBe(false);
    });

    it('prints every invitation when nothing is selected', async () => {
      const { wrapper } = await mountView();

      await wrapper.get('[data-test="print-stickers-cta"]').trigger('click');

      expect(wrapper.get('[data-test="print-stickers-scope"]').text()).toBe('All 3 invitations will be printed.');

      await wrapper.get('[data-test="print-stickers-confirm"]').trigger('click');
      await flushPromises();

      expect(buildStickerPdfMock).toHaveBeenCalledWith(everyInvitation, expect.any(String));
    });

    it('prints only the selected invitations', async () => {
      const { wrapper } = await mountView();

      await wrapper.findAll('[data-test="invitation-select"]')[1].setValue(true);
      await wrapper.get('[data-test="print-stickers-cta"]').trigger('click');

      expect(wrapper.get('[data-test="print-stickers-confirm"]').text()).toBe('Print 1 sticker');

      await wrapper.get('[data-test="print-stickers-confirm"]').trigger('click');
      await flushPromises();

      expect(buildStickerPdfMock).toHaveBeenCalledWith([invitations[1]], expect.any(String));
    });

    it('prints the selected invitations in the order they were selected', async () => {
      const { wrapper } = await mountView();
      const checkboxes = wrapper.findAll('[data-test="invitation-select"]');

      await checkboxes[1].setValue(true);
      await checkboxes[0].setValue(true);
      await printWithDefaultColor(wrapper);

      expect(buildStickerPdfMock).toHaveBeenCalledWith([invitations[1], invitations[0]], expect.any(String));
    });

    it('prints a re-selected invitation last', async () => {
      const { wrapper } = await mountView();
      const checkboxes = wrapper.findAll('[data-test="invitation-select"]');

      await checkboxes[0].setValue(true);
      await checkboxes[1].setValue(true);
      await checkboxes[0].setValue(false);
      await checkboxes[0].setValue(true);
      await printWithDefaultColor(wrapper);

      expect(buildStickerPdfMock).toHaveBeenCalledWith([invitations[1], invitations[0]], expect.any(String));
    });

    it('does not fall back to printing everything when select all is followed by deselecting every invitation', async () => {
      const onePage = { ...listedPage, totalItems: 2 };
      listInvitationsMock.mockResolvedValue(onePage);

      const { wrapper } = await mountView();

      await wrapper.get('[data-test="select-all-invitations"]').trigger('click');
      for (const checkbox of wrapper.findAll('[data-test="invitation-select"]')) {
        await checkbox.setValue(false);
      }

      expect(wrapper.get('[data-test="invitation-selection-count"]').text()).toBe('0 selected');
      expect(wrapper.get('[data-test="print-stickers-cta"]').attributes('disabled')).toBeDefined();

      await wrapper.get('[data-test="print-stickers-cta"]').trigger('click');

      expect(wrapper.find('[data-test="print-stickers-confirm"]').exists()).toBe(false);
      expect(listAllInvitationsMock).not.toHaveBeenCalled();
      expect(buildStickerPdfMock).not.toHaveBeenCalled();

      await wrapper.get('[data-test="select-all-invitations"]').trigger('click');

      expect(wrapper.get('[data-test="print-stickers-cta"]').attributes('disabled')).toBeUndefined();
    });

    it('locks the selection controls while the stickers are generated', async () => {
      let resolveInvitations: (value: unknown) => void = () => undefined;
      listAllInvitationsMock.mockReturnValue(new Promise((resolve) => {
        resolveInvitations = resolve;
      }));

      const { wrapper } = await mountView();

      await wrapper.findAll('[data-test="invitation-select"]')[0].setValue(true);
      await printWithDefaultColor(wrapper);

      const isDisabled = (selector: string) => wrapper.get(selector).attributes('disabled') !== undefined;

      expect(wrapper.findAll('[data-test="invitation-select"]').every((checkbox) => checkbox.attributes('disabled') !== undefined)).toBe(true);
      expect(isDisabled('[data-test="select-all-invitations"]')).toBe(true);
      expect(isDisabled('[data-test="clear-invitation-selection"]')).toBe(true);

      resolveInvitations(everyInvitation);
      await flushPromises();

      expect(buildStickerPdfMock).toHaveBeenCalledWith([invitations[0]], expect.any(String));
      expect(wrapper.findAll('[data-test="invitation-select"]').some((checkbox) => checkbox.attributes('disabled') !== undefined)).toBe(false);
    });

    it('prints every invitation except the deselected ones after select all', async () => {
      const { wrapper } = await mountView();

      await wrapper.get('[data-test="select-all-invitations"]').trigger('click');
      await wrapper.findAll('[data-test="invitation-select"]')[0].setValue(false);

      expect(wrapper.get('[data-test="invitation-selection-count"]').text()).toBe('2 selected');

      await printWithDefaultColor(wrapper);

      expect(buildStickerPdfMock).toHaveBeenCalledWith([invitations[1], everyInvitation[2]], expect.any(String));
    });
  });

  describe('paging and filtering', () => {
    const page = (ids: string[], pageIndex: number, totalPages: number, totalItems: number) => ({
      items: ids.map((id) => createInvitationResponse({ id, label: `Label ${id}` })),
      page: pageIndex,
      size: 10,
      totalItems,
      totalPages
    });

    it('requests pages of 10 invitations and appends the next page on demand', async () => {
      listInvitationsMock
        .mockResolvedValueOnce(page(['inv-1', 'inv-2'], 0, 2, 3))
        .mockResolvedValueOnce(page(['inv-3'], 1, 2, 3));

      const { wrapper } = await mountView();

      expect(listInvitationsMock).toHaveBeenCalledWith({ page: 0, size: 10, label: '' });
      expect(wrapper.findAll('[data-test="invitation-card"]')).toHaveLength(2);

      await wrapper.get('[data-test="invitation-load-more"]').trigger('click');
      await flushPromises();

      expect(listInvitationsMock).toHaveBeenLastCalledWith({ page: 1, size: 10, label: '' });
      expect(wrapper.findAll('[data-test="invitation-card"]')).toHaveLength(3);
      expect(wrapper.find('[data-test="invitation-load-more-sentinel"]').exists()).toBe(false);
    });

    it('reloads the first page with the label filter after a debounce', async () => {
      vi.useFakeTimers();

      try {
        listInvitationsMock.mockResolvedValue(page(['inv-1'], 0, 1, 1));
        const { wrapper } = await mountView();

        await wrapper.get('[data-test="invitation-label-filter"]').setValue('  fam ');
        await vi.advanceTimersByTimeAsync(400);
        await flushPromises();

        expect(listInvitationsMock).toHaveBeenLastCalledWith({ page: 0, size: 10, label: 'fam' });
      } finally {
        vi.useRealTimers();
      }
    });

    it('shows a dedicated message when no invitation matches the filter', async () => {
      vi.useFakeTimers();

      try {
        listInvitationsMock.mockResolvedValueOnce(page(['inv-1'], 0, 1, 1));
        const { wrapper } = await mountView();

        listInvitationsMock.mockResolvedValueOnce(page([], 0, 0, 0));
        await wrapper.get('[data-test="invitation-label-filter"]').setValue('zzz');
        await vi.advanceTimersByTimeAsync(400);
        await flushPromises();

        expect(wrapper.get('[data-test="empty-no-match"]').text()).toContain('zzz');
        expect(wrapper.find('[data-test="invitation-label-filter"]').exists()).toBe(true);
      } finally {
        vi.useRealTimers();
      }
    });
  });
});
