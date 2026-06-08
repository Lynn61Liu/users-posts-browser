// @vitest-environment jsdom

import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('TC-1.2: frontend project starts successfully', () => {
  it('renders the initial project shell', () => {
    render(<App />)

    expect(
      screen.getByRole('heading', { name: /users & posts browser/i }),
    ).toBeTruthy()
    expect(screen.getByText(/bootstrapped and runnable/i)).toBeTruthy()
    expect(screen.getByText(/docker compose up --build/i)).toBeTruthy()
  })
})
