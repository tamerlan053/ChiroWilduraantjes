import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Totals from '@/components/Totals.vue'
import { useStore } from '@/stores/index.js'

describe('Totals', () => {
    let wrapper
    let store
    const mockTotals = [
        { name: 'Pizza', quantity: 10 },
        { name: 'Pasta', quantity: 5 },
    ]

    beforeEach(async () => {
        const pinia = createPinia()
        setActivePinia(pinia)

        store = useStore()
        store.fetchTotals = vi.fn()
        store.totals = mockTotals

        wrapper = mount(Totals, {
            global: {
                plugins: [pinia]
            }
        })

        await flushPromises()
    })

    it('loads totals on mount', () => {
        expect(store.fetchTotals).toHaveBeenCalled()
        expect(wrapper.vm.totals).toEqual(mockTotals)
    })

    it('displays totals correctly', () => {
        const rows = wrapper.findAll('tbody tr')

        expect(rows.length).toBe(2)
        expect(rows[0].text()).toContain('Pizza')
        expect(rows[0].text()).toContain('10')
        expect(rows[1].text()).toContain('Pasta')
        expect(rows[1].text()).toContain('5')
    })
})