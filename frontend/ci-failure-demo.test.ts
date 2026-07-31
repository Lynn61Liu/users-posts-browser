// frontend/src/ci-failure-demo.test.ts
import { describe, expect, it } from 'vitest';

describe('CI failure evidence', () => {
  it('intentionally fails for Task 2a screenshot evidence', () => {
    expect(true).toBe(false);
  });
});