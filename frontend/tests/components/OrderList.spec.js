import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import OrderList from '@/components/OrderList.vue'
import { useStore } from '@/stores/index.js'

describe('OrderList', () => {
    let wrapper
    let store
    const mockOrders = [
        { id: 1, familyName: 'Janssen' },
        { id: 2, familyName: 'Peters' }
    ]

    beforeEach(async () => {
        const pinia = createPinia()
        setActivePinia(pinia)

        store = useStore()
        store.getAllOrders = vi.fn().mockResolvedValue(mockOrders)

        wrapper = mount(OrderList, {
            global: {
                plugins: [pinia]
            }
        })

        await flushPromises()
    })

    it('loads orders on mount', () => {
        expect(store.getAllOrders).toHaveBeenCalled()
        expect(wrapper.vm.orders).toEqual(mockOrders)
    })

    it('filters orders by search query', async () => {
        await wrapper.find('.search-bar').setValue('jan')
        expect(wrapper.vm.filteredOrders).toEqual([mockOrders[0]])
    })
})