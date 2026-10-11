import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import BaseInput from './BaseInput.vue';

describe('BaseInput', () => {
  it('renders the label linked to the input', () => {
    const wrapper = mount(BaseInput, { props: { label: 'Name', inputId: 'name-input' } });

    expect(wrapper.get('label').text()).toBe('Name');
    expect(wrapper.get('label').attributes('for')).toBe('name-input');
    expect(wrapper.get('input').attributes('id')).toBe('name-input');
  });

  it('omits the label when none is given', () => {
    const wrapper = mount(BaseInput);

    expect(wrapper.find('label').exists()).toBe(false);
  });

  it('shows the model value and passes type, name and extra attributes', () => {
    const wrapper = mount(BaseInput, {
      props: { modelValue: 'Alice', type: 'email', name: 'email' },
      attrs: { placeholder: 'you@example.com' }
    });
    const input = wrapper.get('input');

    expect((input.element as HTMLInputElement).value).toBe('Alice');
    expect(input.attributes('type')).toBe('email');
    expect(input.attributes('name')).toBe('email');
    expect(input.attributes('placeholder')).toBe('you@example.com');
  });

  it('emits update:modelValue when typing', async () => {
    const wrapper = mount(BaseInput);

    await wrapper.get('input').setValue('Bob');

    expect(wrapper.emitted('update:modelValue')).toEqual([['Bob']]);
  });
});

