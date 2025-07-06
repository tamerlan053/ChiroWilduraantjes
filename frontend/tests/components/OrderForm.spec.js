import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import OrderForm from '@/components/OrderForm.vue';
import { useStore } from '@/stores/index.js';

describe('OrderForm.vue', () => {
    let wrapper;
    let store;

    const mockMenuItems = {
        adults: [
            { id: 3, name: 'Pizza', price: 12.5 },
            { id: 4, name: 'Pasta', price: 10.5 },
        ],
        children: [
            { id: 1, name: 'Kinderportie pizza', price: 8.5 },
            { id: 2, name: 'Kinderportie pasta', price: 7.5 },
        ],
    };

    const mountComponent = () => {
        const pinia = createPinia();
        setActivePinia(pinia);

        store = useStore();
        store.getChildrenMenuItems = vi.fn().mockResolvedValue(mockMenuItems.children);
        store.getAdultsMenuItems = vi.fn().mockResolvedValue(mockMenuItems.adults);
        store.postOrder = vi.fn().mockResolvedValue({});

        return mount(OrderForm, {
            global: { plugins: [pinia] },
        });
    };

    beforeEach(async () => {
        wrapper = mountComponent();
        await flushPromises();
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('Component Initialization', () => {
        it('fetches menu items on mount', () => {
            expect(store.getAdultsMenuItems).toHaveBeenCalled();
            expect(store.getChildrenMenuItems).toHaveBeenCalled();
        });

        it('initializes with empty form data', () => {
            expect(wrapper.vm.form).toEqual({
                familyName: "",
                email: "",
                drinkTokens: null,
                remarks: "",
                selectedItems: {},
                arrivalTime: "",
                submitted: false
            });
        });
    });

    describe('Form Rendering', () => {
        it('renders all form fields correctly', () => {
            expect(wrapper.find('h1').text()).toBe('Inschrijvingsformulier');

            const requiredFields = [
                'input[id="familienaam"]',
                'input[id="email"]',
                'select[id="time"]',
                'input[id="drankbonnetjes"]',
                'textarea[id="comments"]',
                'button[type="submit"]'
            ];

            requiredFields.forEach(selector => {
                expect(wrapper.find(selector).exists()).toBe(true);
            });
        });

        it('renders menu sections with correct items', () => {
            const menuSections = wrapper.findAll('.menu-items');
            expect(menuSections.length).toBe(2);

            expect(menuSections[0].find('h3').text()).toBe('Normale porties');
            expect(menuSections[0].findAll('.menu-item').length).toBe(2);

            expect(menuSections[1].find('h3').text()).toBe('Kinderporties');
            expect(menuSections[1].findAll('.menu-item').length).toBe(2);
        });
    });

    describe('Form Interactions', () => {
        const testCases = [
            { selector: 'input[id="familienaam"]', property: 'familyName', value: 'Janssen' },
            { selector: 'input[id="email"]', property: 'email', value: 'test@example.com' },
            { selector: 'select[id="time"]', property: 'arrivalTime', value: 'zaterdag-18' },
            { selector: 'input[id="drankbonnetjes"]', property: 'drinkTokens', value: 3 },
            { selector: 'textarea[id="comments"]', property: 'remarks', value: 'Geen opmerkingen' },
        ];

        testCases.forEach(({ selector, property, value }) => {
            it(`updates ${property} when input changes`, async () => {
                await wrapper.find(selector).setValue(value);
                expect(wrapper.vm.form[property]).toBe(value);
            });
        });

        it('updates selectedItems when menu quantities change', async () => {
            await wrapper.find('input[id="3"]').setValue(2);
            expect(wrapper.vm.form.selectedItems['Pizza']).toBe(2);
        });
    });

    describe('Form Validation', () => {
        it('requires family name and email', () => {
            const familyInput = wrapper.find('input[id="familienaam"]');
            const emailInput = wrapper.find('input[id="email"]');

            expect(familyInput.attributes('required')).toBeDefined();
            expect(emailInput.attributes('required')).toBeDefined();
        });

        it('requires arrival time selection', () => {
            expect(wrapper.find('select[id="time"]').attributes('required')).toBeDefined();
        });
    });

    describe('Form Submission', () => {
        const fillAndSubmitForm = async (overrides = {}) => {
            const defaults = {
                familyName: 'Janssen',
                email: 'test@example.com',
                arrivalTime: 'zaterdag-18',
                drinkTokens: 3,
                remarks: 'Geen opmerkingen',
                pizzaQuantity: 2,
                kidsPizzaQuantity: 1
            };

            const values = { ...defaults, ...overrides };

            await wrapper.find('input[id="familienaam"]').setValue(values.familyName);
            await wrapper.find('input[id="email"]').setValue(values.email);
            await wrapper.find('select[id="time"]').setValue(values.arrivalTime);
            await wrapper.find('input[id="drankbonnetjes"]').setValue(values.drinkTokens);
            await wrapper.find('textarea[id="comments"]').setValue(values.remarks);

            await wrapper.find('input[id="3"]').setValue(values.pizzaQuantity);
            await wrapper.find('input[id="1"]').setValue(values.kidsPizzaQuantity);

            await wrapper.find('form').trigger('submit.prevent');
            await flushPromises();
        };

        it('submits valid form data correctly', async () => {
            await fillAndSubmitForm();

            expect(store.postOrder).toHaveBeenCalledTimes(1);
            const submittedData = store.postOrder.mock.calls[0][0];

            expect(submittedData).toEqual({
                email: 'test@example.com',
                arrivalTime: expect.stringContaining('T18:00:00'),
                familyName: 'Janssen',
                remarks: 'Geen opmerkingen',
                drinkTokens: 3,
                menuItems: [
                    { name: 'Pizza', quantity: 2, price: 12.5 },
                    { name: 'Kinderportie pizza', quantity: 1, price: 8.5 }
                ]
            });
        });

        it('shows success modal after submission', async () => {
            await fillAndSubmitForm();

            expect(wrapper.vm.form.submitted).toBe(true);
            expect(wrapper.vm.showForm).toBe(false);
            expect(wrapper.find('.success-modal').exists()).toBe(true);
        });

        it('resets form when back button is clicked', async () => {
            await fillAndSubmitForm();
            await wrapper.find('.success-modal button').trigger('click');

            expect(wrapper.vm.showForm).toBe(true);
            expect(wrapper.vm.form).toEqual({
                familyName: '',
                email: '',
                drinkTokens: null,
                remarks: '',
                selectedItems: {},
                arrivalTime: '',
                submitted: false
            });
        });

        it('handles submission errors gracefully', async () => {
            const error = new Error('API Error');
            store.postOrder.mockRejectedValueOnce(error);
            console.error = vi.fn();

            await fillAndSubmitForm();

            expect(console.error).toHaveBeenCalledWith('Error submitting order:', error);
            expect(wrapper.vm.form.submitted).toBe(false);
            expect(wrapper.find('.success-modal').exists()).toBe(false);
        });

        it('trims whitespace from text inputs', async () => {
            await fillAndSubmitForm({
                familyName: '  Janssen  ',
                email: '  test@example.com  ',
                remarks: '  Geen opmerkingen  '
            });

            const submittedData = store.postOrder.mock.calls[0][0];
            expect(submittedData.familyName).toBe('Janssen');
            expect(submittedData.email).toBe('test@example.com');
            expect(submittedData.remarks).toBe('Geen opmerkingen');
        });
    });

    describe('Total Amount Calculation', () => {
        it('calculates total with menu items and drink tokens', async () => {
            await wrapper.find('input[id="3"]').setValue(2);
            await wrapper.find('input[id="1"]').setValue(1);
            await wrapper.find('input[id="drankbonnetjes"]').setValue(3);

            const expectedTotal = (2 * 12.5) + (1 * 8.5) + (3 * 2.0);
            expect(wrapper.vm.totalAmount).toBe(expectedTotal);
        });

        it('handles zero quantities correctly', async () => {
            await wrapper.find('input[id="3"]').setValue(0);
            await wrapper.find('input[id="drankbonnetjes"]').setValue(0);

            expect(wrapper.vm.totalAmount).toBe(0);
        });

        it('updates dynamically when quantities change', async () => {
            await wrapper.find('input[id="3"]').setValue(1);
            expect(wrapper.vm.totalAmount).toBe(12.5);

            await wrapper.find('input[id="3"]').setValue(2);
            expect(wrapper.vm.totalAmount).toBe(25.0);
        });
    });
});