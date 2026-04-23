import React from 'react';
import { AppHeader } from './AppHeader';
import { FiltersBar } from './FiltersBar';

export function PageLayout({ children, onClearFilters }) {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppHeader />
      <FiltersBar onClear={onClearFilters} />
      <main style={{ flex: 1 }}>
        {children}
      </main>
    </div>
  );
}
