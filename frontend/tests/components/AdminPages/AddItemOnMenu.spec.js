import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import AddItemOnMenu from '@/components/AdminPages/AddItemOnMenu.vue';
import { useStore } from '@/stores/index.js';

// Mock localStorage
const setupLocalStorageMock = () => {
    let store = {};
    return {
        getItem: (key) => store[key] || null,
        setItem: (key, value) => (store[key] = value.toString()),
        removeItem: (key) => delete store[key],
        clear: () => (store = {}),
    };
};

describe('AddItemOnMenu.vue', () => {
    let wrapper;
    let store;

    const mountComponent = () => {
        const pinia = createPinia();
        setActivePinia(pinia);

        store = useStore();
        store.addItemOnMenu = vi.fn().mockResolvedValue(undefined);
        store.isItemAlreadyExist = vi.fn().mockResolvedValue(false);
        store.getAdultsMenuItems = vi.fn().mockResolvedValue([]);
        store.getChildrenMenuItems = vi.fn().mockResolvedValue([]);

        localStorage.setItem('token', 'mock-token');

        return mount(AddItemOnMenu, {
            global: { plugins: [pinia] },
        });
    };

    beforeEach(() => {
        Object.defineProperty(global, 'localStorage', {
            value: setupLocalStorageMock(),
        });
        wrapper = mountComponent();
    });

    afterEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    describe('Initial rendering', () => {
        it('renders the form with correct structure', () => {
            expect(wrapper.find('h1').text()).toBe('Voeg item toe aan menu');
            expect(wrapper.find('input[type="text"]').exists()).toBe(true);
            expect(wrapper.find('input[type="number"]').exists()).toBe(true);
            expect(wrapper.find('input[type="checkbox"]').exists()).toBe(true);
            expect(wrapper.find('button[type="submit"]').text()).toBe('Toevoegen');
        });

        it('has proper form accessibility attributes', () => {
            expect(wrapper.find('label[for="name"]').exists()).toBe(true);
            expect(wrapper.find('label[for="price"]').exists()).toBe(true);
            expect(wrapper.find('label[for="isChild"]').exists()).toBe(true);
            expect(wrapper.find('input[type="text"]').attributes('required')).toBeDefined();
            expect(wrapper.find('input[type="number"]').attributes('required')).toBeDefined();
        });
    });

    describe('Form interactions', () => {
        it('updates menuItem ref when inputs change', async () => {
            await wrapper.find('input[type="text"]').setValue('Pizza');
            await wrapper.find('input[type="number"]').setValue(15.35);
            await wrapper.find('input[type="checkbox"]').setValue(true);

            expect(wrapper.vm.menuItem).toEqual({
                name: 'Pizza',
                price: 15.35,
                childFood: true,
            });
        });

        it('trims whitespace from item name', async () => {
            await wrapper.find('input[type="text"]').setValue('  Pizza  ');
            await wrapper.find('input[type="number"]').setValue(10); // positieve prijs
            await wrapper.find('form').trigger('submit');
            await flushPromises();

            expect(store.addItemOnMenu).toHaveBeenCalledWith(expect.objectContaining({
                name: 'Pizza' // Geen whitespace
            }));
        });

        it('handles decimal prices correctly', async () => {
            await wrapper.find('input[type="number"]').setValue('15.999');
            await wrapper.find('form').trigger('submit');

            expect(store.addItemOnMenu).toHaveBeenCalledWith(expect.objectContaining({
                price: 15.999
            }));
        });
    });

    describe('Form submission', () => {
        const fillAndSubmitForm = async (values = {}) => {
            await wrapper.find('input[type="text"]').setValue(values.name || 'Pizza');
            await wrapper.find('input[type="number"]').setValue(values.price || 15.35);
            if (values.childFood !== undefined) {
                await wrapper.find('input[type="checkbox"]').setValue(values.childFood);
            }
            await wrapper.find('form').trigger('submit');
            await flushPromises();
        };

        it('calls addItemOnMenu with correct data on submit', async () => {
            await fillAndSubmitForm();

            expect(store.addItemOnMenu).toHaveBeenCalledTimes(1);
            expect(store.addItemOnMenu).toHaveBeenCalledWith({
                name: 'Pizza',
                price: 15.35,
                child: false, // Default checkbox value
            });
        });

        it('shows success message when submission succeeds', async () => {
            await fillAndSubmitForm();

            const statusMessage = wrapper.find('.status-message');
            expect(statusMessage.exists()).toBe(true);
            expect(statusMessage.text()).toBe('Item succesvol aan het menu toegevoegd!');
            expect(statusMessage.attributes('style')).toContain('color: green');
        });

        it('shows error message when submission fails', async () => {
            store.addItemOnMenu.mockRejectedValueOnce(new Error('API Error'));
            await fillAndSubmitForm();

            const statusMessage = wrapper.find('.status-message');
            expect(statusMessage.exists()).toBe(true);
            expect(statusMessage.text()).toBe('Er is iets mis gegaan met het toevoegen van dit item');
            expect(statusMessage.attributes('style')).toContain('color: red');
        });

        it('resets form after successful submission', async () => {
            await fillAndSubmitForm();

            expect(wrapper.vm.menuItem).toEqual({
                name: '',
                price: null,
                childFood: false,
            });
        });

        it('does not reset form on failed submission', async () => {
            store.addItemOnMenu.mockRejectedValueOnce(new Error('API Error'));
            await wrapper.find('input[type="text"]').setValue('Pizza');
            await wrapper.find('form').trigger('submit');
            await flushPromises();

            expect(wrapper.vm.menuItem.name).toBe('Pizza');
        });

        it('disables submit button during processing', async () => {
            let resolvePromise;
            const promise = new Promise(resolve => {
                resolvePromise = resolve;
            });
            store.addItemOnMenu.mockImplementationOnce(() => promise);

            const submitBtn = wrapper.find('button[type="submit"]');
            await wrapper.find('form').trigger('submit');

            expect(submitBtn.attributes('disabled')).toBeDefined();

            resolvePromise();
            await flushPromises();
            expect(submitBtn.attributes('disabled')).toBeUndefined();
        });
    });

    describe('Item existence check', () => {
        it('shows error when item already exists', async () => {
            store.isItemAlreadyExist.mockResolvedValue(true);
            store.getAdultsMenuItems.mockResolvedValue([{ name: 'Bestaand item', price: 10, childFood: false }]);
            store.getChildrenMenuItems.mockResolvedValue([]);

            await wrapper.find('input[type="text"]').setValue('Bestaand item');
            await wrapper.find('input[type="number"]').setValue(10); // positieve prijs
            await wrapper.find('form').trigger('submit');

            await flushPromises();

            const statusMessage = wrapper.find('.status-message');
            expect(statusMessage.exists()).toBe(true);
            expect(statusMessage.text()).toBe('Dit item staat al op het huidige menu!');
            expect(statusMessage.attributes('style')).toContain('color: red');

            expect(store.addItemOnMenu).not.toHaveBeenCalled();
        });

        it('checks both adult and children menus', async () => {
            await wrapper.find('form').trigger('submit');
            await flushPromises();

            expect(store.getAdultsMenuItems).toHaveBeenCalled();
            expect(store.getChildrenMenuItems).toHaveBeenCalled();
        });

        it('handles store errors during existence check', async () => {
            store.getAdultsMenuItems.mockRejectedValueOnce(new Error('Fetch error'));
            await wrapper.find('form').trigger('submit');
            await flushPromises();

            expect(wrapper.find('.status-message').text()).toContain('Er is iets mis gegaan');
        });
    });

    describe('Edge cases', () => {
        it('shows validation error for negative price', async () => {
            await wrapper.find('input[type="number"]').setValue(-5);
            await wrapper.find('form').trigger('submit');

            expect(store.addItemOnMenu).not.toHaveBeenCalled();
        });
    });
});