// @vitest-environment jsdom

import '@testing-library/jest-dom/vitest'

import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'

type JsonResponseInit = {
  status?: number
}

function jsonResponse(data: unknown, init: JsonResponseInit = {}) {
  return new Response(JSON.stringify(data), {
    status: init.status ?? 200,
    headers: {
      'Content-Type': 'application/json',
    },
  })
}

function createDeferredResponse<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason?: unknown) => void

  const promise = new Promise<T>((promiseResolve, promiseReject) => {
    resolve = promiseResolve
    reject = promiseReject
  })

  return {
    promise,
    resolve,
    reject,
  }
}

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

describe('Epic 5: UI pages', () => {
  it('TC-5.1: renders imported users and marks the selected user', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        jsonResponse([
          {
            id: 1,
            externalId: 101,
            name: 'Leanne Graham',
            username: 'Bret',
            email: 'leanne@example.com',
            companyName: 'Romaguera-Crona',
          },
          {
            id: 2,
            externalId: 102,
            name: 'Ervin Howell',
            username: 'Antonette',
            email: 'ervin@example.com',
            companyName: 'Deckow-Crist',
          },
        ]),
      )
      .mockResolvedValueOnce(
        jsonResponse({
          id: 1,
          externalId: 101,
          name: 'Leanne Graham',
          username: 'Bret',
          email: 'leanne@example.com',
          phone: '1-770-736-8031 x56442',
          website: 'hildegard.org',
          address: {
            street: 'Kulas Light',
            suite: 'Apt. 556',
            city: 'Gwenborough',
            zipcode: '92998-3874',
            geo: { lat: '-37.3159', lng: '81.1496' },
          },
          company: {
            name: 'Romaguera-Crona',
            catchPhrase: 'Multi-layered client-server neural-net',
            bs: 'harness real-time e-markets',
          },
        }),
      )
      .mockResolvedValueOnce(
        jsonResponse([
          {
            id: 11,
            externalId: 1001,
            title: 'Sunt aut facere',
            body: 'quia et suscipit',
          },
        ]),
      )

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    expect(screen.getByText(/loading users/i)).toBeTruthy()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /leanne graham/i })).toBeTruthy()
    })

    expect(screen.getByRole('button', { name: /leanne graham/i })).toHaveAttribute(
      'aria-current',
      'true',
    )
    await screen.findByRole('heading', { name: /leanne graham/i })
    expect(screen.getAllByText(/@bret/i).length).toBeGreaterThan(0)
  })

  it('TC-5.2: updates the detail page when a different user is selected', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        jsonResponse([
          {
            id: 1,
            externalId: 101,
            name: 'Leanne Graham',
            username: 'Bret',
            email: 'leanne@example.com',
            companyName: 'Romaguera-Crona',
          },
          {
            id: 2,
            externalId: 102,
            name: 'Ervin Howell',
            username: 'Antonette',
            email: 'ervin@example.com',
            companyName: 'Deckow-Crist',
          },
        ]),
      )
      .mockResolvedValueOnce(
        jsonResponse({
          id: 1,
          externalId: 101,
          name: 'Leanne Graham',
          username: 'Bret',
          email: 'leanne@example.com',
          phone: '1-770-736-8031 x56442',
          website: 'hildegard.org',
          address: {
            street: 'Kulas Light',
            suite: 'Apt. 556',
            city: 'Gwenborough',
            zipcode: '92998-3874',
            geo: { lat: '-37.3159', lng: '81.1496' },
          },
          company: {
            name: 'Romaguera-Crona',
            catchPhrase: 'Multi-layered client-server neural-net',
            bs: 'harness real-time e-markets',
          },
        }),
      )
      .mockResolvedValueOnce(
        jsonResponse([
          {
            id: 11,
            externalId: 1001,
            title: 'Sunt aut facere',
            body: 'quia et suscipit',
          },
        ]),
      )
      .mockResolvedValueOnce(
        jsonResponse({
          id: 2,
          externalId: 102,
          name: 'Ervin Howell',
          username: 'Antonette',
          email: 'ervin@example.com',
          phone: '010-692-6593 x09125',
          website: 'anastasia.net',
          address: {
            street: 'Victor Plains',
            suite: 'Suite 879',
            city: 'Wisokyburgh',
            zipcode: '90566-7771',
            geo: { lat: '-43.9509', lng: '-34.4618' },
          },
          company: {
            name: 'Deckow-Crist',
            catchPhrase: 'Proactive didactic contingency',
            bs: 'synergize scalable supply-chains',
          },
        }),
      )
      .mockResolvedValueOnce(
        jsonResponse([
          {
            id: 21,
            externalId: 2001,
            title: 'Updated post',
            body: 'new detail data',
          },
        ]),
      )

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await screen.findByRole('button', { name: /leanne graham/i })

    fireEvent.click(screen.getByRole('button', { name: /ervin howell/i }))

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /ervin howell/i })).toBeTruthy()
    })

    expect(screen.getByText(/010-692-6593 x09125/i)).toBeTruthy()
    expect(screen.getByText(/updated post/i)).toBeTruthy()
    expect(screen.getByRole('button', { name: /ervin howell/i })).toHaveAttribute(
      'aria-current',
      'true',
    )
    expect(fetchMock).toHaveBeenCalledWith('/api/users/2')
    expect(fetchMock).toHaveBeenCalledWith('/api/users/2/posts')
  })

  it('TC-5.3: shows loading states while list and detail data are being fetched', async () => {
    const usersDeferred = createDeferredResponse<Response>()
    const detailDeferred = createDeferredResponse<Response>()
    const postsDeferred = createDeferredResponse<Response>()

    const fetchMock = vi
      .fn()
      .mockReturnValueOnce(usersDeferred.promise)
      .mockReturnValueOnce(detailDeferred.promise)
      .mockReturnValueOnce(postsDeferred.promise)

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    expect(screen.getByText(/loading users/i)).toBeTruthy()

    usersDeferred.resolve(
      jsonResponse([
        {
          id: 1,
          externalId: 101,
          name: 'Leanne Graham',
          username: 'Bret',
          email: 'leanne@example.com',
          companyName: 'Romaguera-Crona',
        },
      ]),
    )

    await waitFor(() => {
      expect(screen.getByText(/loading selected user/i)).toBeTruthy()
    })
  })

  it('TC-5.4: shows empty states when no users exist', async () => {
    const fetchMock = vi.fn().mockResolvedValueOnce(jsonResponse([]))

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await screen.findByText(/no users imported yet/i)

    expect(screen.getByText(/select a user to inspect details/i)).toBeTruthy()
  })

  it('TC-5.5: shows backend 500 errors when the users request fails', async () => {
    const fetchMock = vi.fn().mockResolvedValueOnce(
      new Response('', {
        status: 500,
      }),
    )

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await waitFor(() => {
      expect(screen.getAllByText(/^backend returned 500$/i).length).toBeGreaterThan(1)
    })
  })

  it('TC-5.6: shows network errors when the users request cannot reach the backend', async () => {
    const fetchMock = vi.fn().mockRejectedValueOnce(new Error('Failed to fetch'))

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await waitFor(() => {
      expect(screen.getAllByText(/^network error$/i).length).toBeGreaterThan(1)
    })
  })

  it('TC-5.7: shows data fetch errors when the response cannot be parsed', async () => {
    const fetchMock = vi.fn().mockResolvedValueOnce(
      new Response('not-json', {
        status: 200,
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    )

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await waitFor(() => {
      expect(screen.getAllByText(/^data fetch error$/i).length).toBeGreaterThan(1)
    })
  })
})

describe('Epic 6: Sync feedback', () => {
  it('TC-6.1: clicking sync calls the backend and keeps the UI responsive', async () => {
    const syncDeferred = createDeferredResponse<Response>()
    const fetchMock = vi.fn(async (input) => {
      if (input === '/api/users') {
        return jsonResponse([])
      }

      if (input === '/api/sync') {
        return syncDeferred.promise
      }

      return new Response('', { status: 404 })
    })

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await screen.findByText(/no users imported yet/i)

    const syncButton = screen.getByRole('button', { name: /sync data/i })
    fireEvent.click(syncButton)

    expect(fetchMock).toHaveBeenCalledWith('/api/sync', {
      method: 'POST',
    })
    expect(syncButton).toBeDisabled()

    syncDeferred.resolve(
      jsonResponse({
        status: 'success',
        message: 'Imported 1 user and 1 post.',
      }),
    )

    await waitFor(() => {
      expect(syncButton).not.toBeDisabled()
    })
  })

  it('TC-6.2: success feedback appears after a successful sync', async () => {
    const fetchMock = vi.fn(async (input) => {
      if (input === '/api/users') {
        return jsonResponse([])
      }

      if (input === '/api/sync') {
        return jsonResponse({
          status: 'success',
          message: 'Imported 1 user and 1 post.',
        })
      }

      return new Response('', { status: 404 })
    })

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await screen.findByText(/no users imported yet/i)

    fireEvent.click(screen.getByRole('button', { name: /sync data/i }))

    await waitFor(() => {
      expect(screen.getByText(/sync complete/i)).toBeTruthy()
    })
    expect(screen.getByText(/imported 1 user and 1 post/i)).toBeTruthy()
  })

  it('TC-6.3: no-change feedback appears when data is unchanged', async () => {
    const fetchMock = vi.fn(async (input) => {
      if (input === '/api/users') {
        return jsonResponse([])
      }

      if (input === '/api/sync') {
        return jsonResponse({
          status: 'no_change',
          message: 'No changes detected.',
        })
      }

      return new Response('', { status: 404 })
    })

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await screen.findByText(/no users imported yet/i)

    fireEvent.click(screen.getByRole('button', { name: /sync data/i }))

    await waitFor(() => {
      expect(screen.getByText(/^no changes$/i)).toBeTruthy()
    })
    expect(screen.getByText(/no changes detected/i)).toBeTruthy()
  })

  it('TC-6.4: update feedback appears when data changed', async () => {
    const fetchMock = vi.fn(async (input) => {
      if (input === '/api/users') {
        return jsonResponse([])
      }

      if (input === '/api/sync') {
        return jsonResponse({
          status: 'update',
          message: 'Updated 1 user and 1 post.',
        })
      }

      return new Response('', { status: 404 })
    })

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await screen.findByText(/no users imported yet/i)

    fireEvent.click(screen.getByRole('button', { name: /sync data/i }))

    await waitFor(() => {
      expect(screen.getByText(/^data updated$/i)).toBeTruthy()
    })
    expect(screen.getByText(/updated 1 user and 1 post/i)).toBeTruthy()
  })

  it('TC-6.5: error feedback appears when sync fails', async () => {
    const fetchMock = vi.fn(async (input) => {
      if (input === '/api/users') {
        return jsonResponse([])
      }

      if (input === '/api/sync') {
        return new Response('', { status: 500 })
      }

      return new Response('', { status: 404 })
    })

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await screen.findByText(/no users imported yet/i)

    fireEvent.click(screen.getByRole('button', { name: /sync data/i }))

    await waitFor(() => {
      expect(screen.getByText(/^sync failed$/i)).toBeTruthy()
    })
    expect(screen.getByText(/check the backend logs and try again/i)).toBeTruthy()
  })
})

describe('Dev reset tool', () => {
  it('keeps the reset button hidden when dev tools are disabled', async () => {
    const fetchMock = vi.fn().mockResolvedValueOnce(jsonResponse([]))

    vi.stubGlobal('fetch', fetchMock)

    render(<App />)

    await screen.findByText(/no users imported yet/i)

    expect(
      screen.queryByRole('button', { name: /reset database/i }),
    ).toBeNull()
  })

  it('shows the reset button when dev tools are enabled and calls the backend reset endpoint', async () => {
    const fetchMock = vi.fn(async (input) => {
      if (input === '/api/users') {
        return jsonResponse([])
      }

      if (input === '/api/dev/reset') {
        return jsonResponse({
          status: 'success',
          message: 'Development database reset successfully.',
        })
      }

      return new Response('', { status: 404 })
    })

    vi.stubGlobal('fetch', fetchMock)

    render(
      <App
        env={{
          VITE_ENABLE_DEV_TOOLS: 'true',
        }}
      />,
    )

    await waitFor(() => {
      expect(screen.getByText(/no users imported yet/i)).toBeTruthy()
    })

    fireEvent.click(screen.getByRole('button', { name: /reset database/i }))

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith('/api/dev/reset', {
        method: 'POST',
      })
    })

    expect(
      screen.getByText(/development database reset successfully/i),
    ).toBeTruthy()
  })
})
