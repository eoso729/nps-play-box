import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { SeatWarningBanner } from '../SeatWarningBanner';

describe('SeatWarningBanner Component', () => {
  it('renders nothing when seat utilization is below 80%', () => {
    const { container } = render(
      <SeatWarningBanner
        percentage={75}
        availableSeats={5}
        totalSeats={20}
      />
    );
    expect(container.firstChild).toBeNull();
  });

  it('renders warning banner when utilization is between 80% and 94.9%', () => {
    render(
      <SeatWarningBanner
        percentage={85}
        availableSeats={3}
        totalSeats={20}
      />
    );

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText(/Seat Quota Warning/i)).toBeInTheDocument();
    expect(screen.getByText(/85.0% of allocated seats/i)).toBeInTheDocument();
  });

  it('renders critical banner when utilization reaches 95% or higher', () => {
    render(
      <SeatWarningBanner
        percentage={95}
        availableSeats={1}
        totalSeats={20}
      />
    );

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText(/Seat Quota Critical - Invitations Restricted/i)).toBeInTheDocument();
    expect(screen.getByText(/cannot invite new members until seats are upgraded/i)).toBeInTheDocument();
  });

  it('renders critical banner when available seats reach 0', () => {
    render(
      <SeatWarningBanner
        percentage={100}
        availableSeats={0}
        totalSeats={20}
      />
    );

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText(/0 of 20 seat\(s\) remaining/i)).toBeInTheDocument();
  });

  it('calls onRequestSeats callback when Request Seats button is clicked', () => {
    const onRequestSeats = vi.fn();
    render(
      <SeatWarningBanner
        percentage={85}
        availableSeats={3}
        totalSeats={20}
        onRequestSeats={onRequestSeats}
      />
    );

    const button = screen.getByRole('button', { name: /Request Seats/i });
    fireEvent.click(button);
    expect(onRequestSeats).toHaveBeenCalledTimes(1);
  });
});
