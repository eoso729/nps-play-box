import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SeatUsageChart } from '../SeatUsageChart';

describe('SeatUsageChart Component', () => {
  it('renders usage count, total seats, and percentage', () => {
    render(
      <SeatUsageChart
        percentage={60}
        usedSeats={12}
        maxSeats={20}
      />
    );

    expect(screen.getByText('12')).toBeInTheDocument();
    expect(screen.getByText(/\/ 20 seats/i)).toBeInTheDocument();
    expect(screen.getByText('60.0%')).toBeInTheDocument();
  });

  it('renders progressbar element with appropriate aria attributes', () => {
    render(
      <SeatUsageChart
        percentage={85}
        usedSeats={17}
        maxSeats={20}
      />
    );

    const progressbar = screen.getByRole('progressbar');
    expect(progressbar).toBeInTheDocument();
    expect(progressbar).toHaveAttribute('aria-valuenow', '85');
    expect(progressbar).toHaveAttribute('aria-valuemin', '0');
    expect(progressbar).toHaveAttribute('aria-valuemax', '100');
  });

  it('clamps percentage to 100% when overflowed', () => {
    render(
      <SeatUsageChart
        percentage={110}
        usedSeats={22}
        maxSeats={20}
      />
    );

    const progressbar = screen.getByRole('progressbar');
    expect(progressbar).toHaveAttribute('aria-valuenow', '100');
  });
});
