import React from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

export function EvasionTimeline({ evasionPoints }) {
  if (!evasionPoints || evasionPoints.length === 0) return (
    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', padding: '1rem' }}>Sem dados de evasão estruturada para este curso.</div>
  );

  const data = evasionPoints.map(ep => ({
    name: `Aula ${ep.lessonId}`,
    title: ep.lessonTitle,
    evasao: ep.evasionRate
  }));

  // Find max for highlight
  const maxRate = Math.max(...data.map(d => d.evasao));

  return (
    <div style={{ height: 250, width: '100%' }}>
      <h4 style={{ fontSize: '0.9rem', marginBottom: '1rem', color: 'var(--sebrae-blue)' }}>Gargalos de Evasão</h4>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} />
          <XAxis dataKey="name" fontSize={12} tickLine={false} axisLine={false} />
          <YAxis fontSize={12} tickLine={false} axisLine={false} unit="%" />
          <Tooltip 
            formatter={(val, name) => [`${val}%`, 'Evasão']} 
            labelFormatter={(label, payload) => payload?.[0]?.payload?.title || label}
          />
          <Line 
            type="monotone" 
            dataKey="evasao" 
            stroke="var(--risk-high-text)" 
            strokeWidth={3}
            dot={(props) => {
              const { cx, cy, payload } = props;
              const isMax = payload.evasao === maxRate;
              return (
                <circle 
                  key={payload.name}
                  cx={cx} cy={cy} r={isMax ? 6 : 4} 
                  fill={isMax ? "var(--risk-high-text)" : "white"} 
                  stroke="var(--risk-high-text)" strokeWidth={2} 
                />
              );
            }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
