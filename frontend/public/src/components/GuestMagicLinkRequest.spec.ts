import { flushPromises, mount } from '@vue/test-utils';
import { afterEach, describe, expect, it, vi } from 'vitest';
import GuestMagicLinkRequest from './GuestMagicLinkRequest.vue';

vi.mock('../services/guestAccessMagicLinkApi', () => ({
  requestMagicLink: vi.fn(),
}));

import { requestMagicLink } from '../services/guestAccessMagicLinkApi';

const requestMagicLinkMock = vi.mocked(requestMagicLink);

const mountComponent = () =>
  mount(GuestMagicLinkRequest, {
    props: { token: 'invite-token', guestId: 'guest-1', firstName: 'Alice', lastName: 'Martin' },
  });

describe('GuestMagicLinkRequest', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('labels the button with the guest first name', () => {
    const wrapper = mountComponent();

    expect(wrapper.find('button').text()).toBe('Alice');
  });

  it('emits a sent result when the request is accepted', async () => {
    requestMagicLinkMock.mockResolvedValue({ status: 'accepted' });
    const wrapper = mountComponent();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(requestMagicLinkMock).toHaveBeenCalledWith('invite-token', 'guest-1', 'INDIVIDUAL');
    expect(wrapper.emitted('requested')?.[0]).toEqual([{ status: 'sent', firstName: 'Alice', mode: 'INDIVIDUAL' }]);
  });

  it('requests the magic link with the given mode', async () => {
    requestMagicLinkMock.mockResolvedValue({ status: 'accepted' });
    const wrapper = mount(GuestMagicLinkRequest, {
      props: { token: 'invite-token', guestId: 'guest-1', firstName: 'Alice', lastName: 'Martin', mode: 'GROUP' },
    });

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(requestMagicLinkMock).toHaveBeenCalledWith('invite-token', 'guest-1', 'GROUP');
    expect(wrapper.emitted('requested')?.[0]).toEqual([{ status: 'sent', firstName: 'Alice', mode: 'GROUP' }]);
  });

  it('emits the mode the request was sent with even if the mode changes while it is pending', async () => {
    let resolveRequest: (value: { status: 'accepted' }) => void = () => undefined;
    requestMagicLinkMock.mockReturnValue(new Promise((resolve) => { resolveRequest = resolve; }));
    const wrapper = mount(GuestMagicLinkRequest, {
      props: { token: 'invite-token', guestId: 'guest-1', firstName: 'Alice', lastName: 'Martin', mode: 'GROUP' },
    });

    await wrapper.find('button').trigger('click');
    await wrapper.setProps({ mode: 'INDIVIDUAL' });
    resolveRequest({ status: 'accepted' });
    await flushPromises();

    expect(wrapper.emitted('requested')?.[0]).toEqual([{ status: 'sent', firstName: 'Alice', mode: 'GROUP' }]);
  });

  it('emits a rateLimited result when the request is rate limited', async () => {
    requestMagicLinkMock.mockResolvedValue({ status: 'rateLimited', retryAfterSeconds: 30 });
    const wrapper = mountComponent();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.emitted('requested')?.[0]).toEqual([{ status: 'rateLimited', firstName: 'Alice', mode: 'INDIVIDUAL' }]);
  });

  it('emits an error result when the request fails', async () => {
    requestMagicLinkMock.mockRejectedValue(new Error('network down'));
    const wrapper = mountComponent();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.emitted('requested')?.[0]).toEqual([{ status: 'error', firstName: 'Alice', mode: 'INDIVIDUAL' }]);
  });
});


