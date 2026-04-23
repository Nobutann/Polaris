import React from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from 'recharts';

export function RetentionChart({ snapshots }) {
  if (!snapshots || snapshots.length === 0) return null;

  const total = snapshots.length;
  const r7 = snapshots.filter(s => s.retained7d).length;
  const r14 = snapshots.filter(s => s.retained14d).length;
  const r30 = snapshots.filter(s => s.retained30d).length;

  const data = [
    { period: '7 Dias', retidos: Math.round((r7/total)*100) },
    { period: '14 Dias', retidos: Math.round((r14/total)*100) },
    { period: '30 Dias', retidos: Math.round((r30/total)*100) }
  ];

  return (
    <div style={{ height: 250, width: '100%' }}>
      <h4 style={{ fontSize: '0.9rem', marginBottom: '1rem', color: 'var(--sebrae-blue)' }}>Retenção por Período</h4>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} />
          <XAxis dataKey="period" fontSize={12} tickLine={false} axisLine={false} />
          <YAxis fontSize={12} tickLine={false} axisLine={false} unit="%" />
          <Tooltip cursor={{ fill: 'rgba(0,0,0,0.05)' }} formatter={(v) => `${v}%`} />
          <Bar dataKey="retidos" fill="var(--sebrae-light-blue)" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
