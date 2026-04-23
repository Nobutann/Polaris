import React from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

export function RiskDistribution({ courses }) {
  if (!courses || courses.length === 0) return null;

  const data = [
    { name: 'Crítico/Alto', value: 0, color: 'var(--risk-high-text)' },
    { name: 'Médio/Atenção', value: 0, color: 'var(--risk-med-text)' },
    { name: 'Baixo/Engajado', value: 0, color: 'var(--risk-low-text)' }
  ];

  courses.forEach(c => {
    data[0].value += c.highRiskCount;
    data[1].value += c.medRiskCount;
    data[2].value += c.lowRiskCount;
  });

  return (
    <div style={{ width: '100%', minHeight: 300 }}>
      <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem', color: 'var(--sebrae-blue)', textAlign: 'center' }}>Distribuição Global de Risco</h3>
      <ResponsiveContainer width="100%" height={240}>
        <PieChart>
          <Pie
            data={data}
            innerRadius={60}
            outerRadius={80}
            paddingAngle={5}
            dataKey="value"
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${entry.name}-${index}`} fill={entry.color} />
            ))}
          </Pie>
          <Tooltip formatter={(val) => [`${val} matriculados`, 'Risco']} />
          <Legend verticalAlign="bottom" height={36} iconType="circle" />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
